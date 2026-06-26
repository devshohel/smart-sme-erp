package com.sme.erp.reports.export;

import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
class TabularExportSupport {
    static final String EMPTY_MESSAGE = "No data available for selected filters";

    String delimited(String title, List<?> rows, String delimiter) {
        return delimited(title, rows, null, delimiter);
    }

    String delimited(String title, List<?> rows, Class<?> rowType, String delimiter) {
        List<Column> columns = columns(rows, rowType);
        StringBuilder out = new StringBuilder();
        out.append(cell(title, delimiter)).append('\n');
        if (!columns.isEmpty()) {
            out.append(join(columns.stream().map(Column::name).toList(), delimiter)).append('\n');
        }
        if (rows == null || rows.isEmpty()) {
            out.append(cell(EMPTY_MESSAGE, delimiter)).append('\n');
            return out.toString();
        }
        for (Object row : rows) {
            out.append(join(columns.stream().map(column -> text(value(row, column))).toList(), delimiter)).append('\n');
        }
        return out.toString();
    }

    String plainText(String title, List<?> rows) {
        return plainText(title, rows, null);
    }

    String plainText(String title, List<?> rows, Class<?> rowType) {
        List<Column> columns = columns(rows, rowType);
        StringBuilder out = new StringBuilder(title).append('\n');
        if (!columns.isEmpty()) {
            out.append(String.join(" | ", columns.stream().map(Column::name).toList())).append('\n');
        }
        if (rows == null || rows.isEmpty()) {
            out.append(EMPTY_MESSAGE).append('\n');
            return out.toString();
        }
        for (Object row : rows) {
            out.append(join(columns.stream().map(column -> text(value(row, column))).toList(), " | ")).append('\n');
        }
        return out.toString();
    }

    String htmlTable(String title, List<?> rows, Class<?> rowType) {
        TableData table = tableData(rows, rowType);
        List<ColumnData> columns = table.columns();
        StringBuilder out = new StringBuilder();
        out.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        out.append("<h3>").append(html(title)).append("</h3>");
        out.append("<table border=\"1\"><thead><tr>");
        for (ColumnData column : columns) {
            out.append("<th>").append(html(column.name())).append("</th>");
        }
        out.append("</tr></thead><tbody>");
        if (table.empty()) {
            int colspan = Math.max(columns.size(), 1);
            out.append("<tr><td colspan=\"").append(colspan).append("\">").append(EMPTY_MESSAGE).append("</td></tr>");
        } else {
            for (List<String> row : table.rows()) {
                out.append("<tr>");
                for (String cell : row) {
                    out.append("<td>").append(html(cell)).append("</td>");
                }
                out.append("</tr>");
            }
        }
        out.append("</tbody></table></body></html>");
        return out.toString();
    }

    TableData tableData(List<?> rows, Class<?> rowType) {
        List<Column> sourceColumns = columns(rows, rowType);
        List<ColumnData> exportColumns = sourceColumns.stream()
                .map(column -> new ColumnData(column.name()))
                .toList();
        if (rows == null || rows.isEmpty()) {
            return new TableData(exportColumns, List.of(), true);
        }
        List<List<String>> exportRows = rows.stream()
                .map(row -> sourceColumns.stream()
                        .map(column -> text(value(row, column)))
                        .toList())
                .toList();
        return new TableData(exportColumns, exportRows, false);
    }

    private List<Column> columns(List<?> rows, Class<?> rowType) {
        Class<?> type = rowType;
        if (type == null && rows != null && !rows.isEmpty()) {
            type = rows.get(0).getClass();
        }
        if (type == null) {
            return List.of();
        }
        if (type.isRecord()) {
            List<Column> columns = new ArrayList<>();
            for (RecordComponent component : type.getRecordComponents()) {
                columns.add(new Column(component.getName(), component.getAccessor()));
            }
            return columns;
        }
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> !method.getReturnType().equals(Void.TYPE))
                .filter(method -> method.getName().startsWith("get"))
                .filter(method -> !method.getName().equals("getClass"))
                .sorted(Comparator.comparing(Method::getName))
                .map(method -> new Column(Introspector.decapitalize(method.getName().substring(3)), method))
                .toList();
    }

    private Object value(Object row, Column column) {
        try {
            return column.accessor().invoke(row);
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }

    private String join(List<String> values, String delimiter) {
        return String.join(delimiter, values.stream().map(value -> cell(value, delimiter)).toList());
    }

    private String cell(String value, String delimiter) {
        if ("\t".equals(delimiter)) {
            return value.replace('\t', ' ').replace('\n', ' ');
        }
        return "\"" + value.replace("\"", "\"\"").replace('\n', ' ') + "\"";
    }

    private String text(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof TemporalAccessor) {
            return value.toString();
        }
        return String.valueOf(value);
    }

    private String html(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    record ColumnData(String name) {}

    record TableData(List<ColumnData> columns, List<List<String>> rows, boolean empty) {}

    private record Column(String name, Method accessor) {}
}
