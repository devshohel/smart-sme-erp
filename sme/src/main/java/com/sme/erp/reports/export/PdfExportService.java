package com.sme.erp.reports.export;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PdfExportService {
    private static final double PAGE_WIDTH = 842;
    private static final double PAGE_HEIGHT = 595;
    private static final double MARGIN = 28;
    private static final double TABLE_TOP = 455;
    private static final double TABLE_BOTTOM = 58;
    private static final double TABLE_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final double HEADER_HEIGHT = 20;
    private static final double CELL_PADDING = 3;
    private static final double BODY_FONT = 7;
    private static final double HEADER_FONT = 7;
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TabularExportSupport support;

    public PdfExportService(TabularExportSupport support) {
        this.support = support;
    }

    public byte[] export(String title, List<?> rows) {
        return export(title, rows, null, "All available records");
    }

    public byte[] export(String title, List<?> rows, Class<?> rowType) {
        return export(title, rows, rowType, "All available records");
    }

    public byte[] export(String title, List<?> rows, Class<?> rowType, String selectedPeriod) {
        TabularExportSupport.TableData table = support.tableData(rows, rowType);
        List<ColumnLayout> columns = columns(table);
        List<RowLayout> rowLayouts = rows(table, columns);
        List<List<RowLayout>> pages = paginate(rowLayouts);
        if (pages.isEmpty()) {
            pages.add(List.of());
        }
        List<String> totals = totals(table);

        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
        StringBuilder kids = new StringBuilder();
        int firstPageObj = 3;
        int fontRegularObj = firstPageObj + (pages.size() * 2);
        int fontBoldObj = fontRegularObj + 1;
        for (int i = 0; i < pages.size(); i++) {
            int pageObj = firstPageObj + (i * 2);
            kids.append(pageObj).append(" 0 R ");
        }
        objects.add("2 0 obj << /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >> endobj\n");

        for (int i = 0; i < pages.size(); i++) {
            int pageObj = firstPageObj + (i * 2);
            int contentObj = pageObj + 1;
            String content = renderPage(title, selectedPeriod, table, columns, pages.get(i), totals, i + 1, pages.size());
            byte[] contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
            objects.add(pageObj + " 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT
                    + "] /Resources << /Font << /F1 " + fontRegularObj + " 0 R /F2 " + fontBoldObj
                    + " 0 R >> >> /Contents " + contentObj + " 0 R >> endobj\n");
            objects.add(contentObj + " 0 obj << /Length " + contentBytes.length + " >> stream\n" + content + "\nendstream endobj\n");
        }
        objects.add(fontRegularObj + " 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
        objects.add(fontBoldObj + " 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> endobj\n");

        return buildPdf(objects);
    }

    private String renderPage(String title, String selectedPeriod, TabularExportSupport.TableData table,
                              List<ColumnLayout> columns, List<RowLayout> rows, List<String> totals,
                              int pageNumber, int totalPages) {
        StringBuilder out = new StringBuilder();
        text(out, "F2", 15, MARGIN, 558, "Smart SME ERP");
        text(out, "F2", 12, MARGIN, 538, safe(title));
        text(out, "F1", 8, MARGIN, 522, "Generated: " + LocalDateTime.now().format(GENERATED_FORMAT));
        text(out, "F1", 8, MARGIN, 509, "Selected period: " + safe(selectedPeriod));
        line(out, MARGIN, 498, PAGE_WIDTH - MARGIN, 498);

        if (table.empty()) {
            text(out, "F2", 11, MARGIN, TABLE_TOP - 35, TabularExportSupport.EMPTY_MESSAGE);
        } else {
            renderTableHeader(out, columns, TABLE_TOP);
            double y = TABLE_TOP - HEADER_HEIGHT;
            for (RowLayout row : rows) {
                renderRow(out, columns, row, y);
                y -= row.height();
            }
        }

        line(out, MARGIN, 46, PAGE_WIDTH - MARGIN, 46);
        text(out, "F1", 8, MARGIN, 32, "Total records: " + table.rows().size());
        if (!totals.isEmpty()) {
            text(out, "F1", 7, MARGIN, 20, "Totals: " + String.join("   ", totals));
        }
        textRight(out, "F1", 8, PAGE_WIDTH - MARGIN, 32, "Page " + pageNumber + " of " + totalPages);
        return out.toString();
    }

    private void renderTableHeader(StringBuilder out, List<ColumnLayout> columns, double topY) {
        rectFill(out, MARGIN, topY - HEADER_HEIGHT, TABLE_WIDTH, HEADER_HEIGHT, 0.90);
        rectStroke(out, MARGIN, topY - HEADER_HEIGHT, TABLE_WIDTH, HEADER_HEIGHT);
        double x = MARGIN;
        for (ColumnLayout column : columns) {
            rectStroke(out, x, topY - HEADER_HEIGHT, column.width(), HEADER_HEIGHT);
            text(out, "F2", HEADER_FONT, x + CELL_PADDING, topY - 13, column.header());
            x += column.width();
        }
    }

    private void renderRow(StringBuilder out, List<ColumnLayout> columns, RowLayout row, double topY) {
        rectStroke(out, MARGIN, topY - row.height(), TABLE_WIDTH, row.height());
        double x = MARGIN;
        for (int i = 0; i < columns.size(); i++) {
            ColumnLayout column = columns.get(i);
            rectStroke(out, x, topY - row.height(), column.width(), row.height());
            List<String> lines = row.cells().get(i);
            double textY = topY - 10;
            for (String line : lines) {
                text(out, "F1", BODY_FONT, x + CELL_PADDING, textY, line);
                textY -= 9;
            }
            x += column.width();
        }
    }

    private List<ColumnLayout> columns(TabularExportSupport.TableData table) {
        List<TabularExportSupport.ColumnData> source = table.columns();
        if (source.isEmpty()) {
            return List.of();
        }
        double[] weights = new double[source.size()];
        double totalWeight = 0;
        for (int i = 0; i < source.size(); i++) {
            int maxLength = pretty(source.get(i).name()).length();
            for (List<String> row : table.rows()) {
                if (i < row.size()) {
                    maxLength = Math.max(maxLength, Math.min(row.get(i).length(), 40));
                }
            }
            weights[i] = Math.max(6, Math.min(maxLength, 28));
            totalWeight += weights[i];
        }
        List<ColumnLayout> columns = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            double width = TABLE_WIDTH * (weights[i] / totalWeight);
            columns.add(new ColumnLayout(pretty(source.get(i).name()), width));
        }
        return columns;
    }

    private List<RowLayout> rows(TabularExportSupport.TableData table, List<ColumnLayout> columns) {
        List<RowLayout> result = new ArrayList<>();
        for (List<String> row : table.rows()) {
            List<List<String>> cells = new ArrayList<>();
            int maxLines = 1;
            for (int i = 0; i < columns.size(); i++) {
                String value = i < row.size() ? row.get(i) : "";
                List<String> lines = wrap(value, columns.get(i).width() - (CELL_PADDING * 2), BODY_FONT);
                cells.add(lines);
                maxLines = Math.max(maxLines, lines.size());
            }
            double height = Math.max(18, 8 + (maxLines * 9));
            result.add(new RowLayout(cells, height));
        }
        return result;
    }

    private List<List<RowLayout>> paginate(List<RowLayout> rows) {
        List<List<RowLayout>> pages = new ArrayList<>();
        List<RowLayout> page = new ArrayList<>();
        double remaining = TABLE_TOP - TABLE_BOTTOM - HEADER_HEIGHT;
        for (RowLayout row : rows) {
            if (!page.isEmpty() && row.height() > remaining) {
                pages.add(page);
                page = new ArrayList<>();
                remaining = TABLE_TOP - TABLE_BOTTOM - HEADER_HEIGHT;
            }
            page.add(row);
            remaining -= row.height();
        }
        if (!page.isEmpty()) {
            pages.add(page);
        }
        return pages;
    }

    private List<String> totals(TabularExportSupport.TableData table) {
        if (table.empty() || table.columns().isEmpty()) {
            return List.of();
        }
        List<String> totals = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < table.columns().size(); columnIndex++) {
            BigDecimal total = BigDecimal.ZERO;
            boolean numeric = false;
            for (List<String> row : table.rows()) {
                if (columnIndex >= row.size()) {
                    continue;
                }
                BigDecimal value = parseDecimal(row.get(columnIndex));
                if (value != null) {
                    total = total.add(value);
                    numeric = true;
                }
            }
            if (numeric) {
                totals.add(pretty(table.columns().get(columnIndex).name()) + "=" + total.stripTrailingZeros().toPlainString());
            }
        }
        return totals.stream().limit(5).toList();
    }

    private List<String> wrap(String value, double width, double fontSize) {
        int maxChars = Math.max(4, (int) Math.floor(width / (fontSize * 0.52)));
        String normalized = safe(value).replace('\n', ' ').replace('\r', ' ').trim();
        if (normalized.isEmpty()) {
            return List.of("");
        }
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : normalized.split("\\s+")) {
            if (word.length() > maxChars) {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
                for (int i = 0; i < word.length(); i += maxChars) {
                    lines.add(word.substring(i, Math.min(i + maxChars, word.length())));
                }
                continue;
            }
            if (current.length() + word.length() + 1 > maxChars) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (!current.isEmpty()) {
                    current.append(' ');
                }
                current.append(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private byte[] buildPdf(List<String> objects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.size() + 1];
        for (int i = 0; i < objects.size(); i++) {
            offsets[i + 1] = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
            pdf.append(objects.get(i));
        }
        int xref = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append('\n');
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i < offsets.length; i++) {
            pdf.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        pdf.append("trailer << /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private void text(StringBuilder out, String font, double size, double x, double y, String value) {
        out.append("BT /").append(font).append(' ').append(number(size)).append(" Tf ")
                .append(number(x)).append(' ').append(number(y)).append(" Td (")
                .append(pdfText(value)).append(") Tj ET\n");
    }

    private void textRight(StringBuilder out, String font, double size, double rightX, double y, String value) {
        double x = rightX - (safe(value).length() * size * 0.5);
        text(out, font, size, Math.max(MARGIN, x), y, value);
    }

    private void line(StringBuilder out, double x1, double y1, double x2, double y2) {
        out.append(number(x1)).append(' ').append(number(y1)).append(" m ")
                .append(number(x2)).append(' ').append(number(y2)).append(" l S\n");
    }

    private void rectStroke(StringBuilder out, double x, double y, double width, double height) {
        out.append(number(x)).append(' ').append(number(y)).append(' ')
                .append(number(width)).append(' ').append(number(height)).append(" re S\n");
    }

    private void rectFill(StringBuilder out, double x, double y, double width, double height, double gray) {
        out.append(number(gray)).append(" g ")
                .append(number(x)).append(' ').append(number(y)).append(' ')
                .append(number(width)).append(' ').append(number(height)).append(" re f 0 g\n");
    }

    private String pdfText(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String pretty(String value) {
        StringBuilder out = new StringBuilder();
        String text = safe(value);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) {
                out.append(' ');
            }
            out.append(ch);
        }
        return out.toString().replace('_', ' ').trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String number(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private record ColumnLayout(String header, double width) {}

    private record RowLayout(List<List<String>> cells, double height) {}
}
