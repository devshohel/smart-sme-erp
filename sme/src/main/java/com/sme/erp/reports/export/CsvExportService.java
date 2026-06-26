package com.sme.erp.reports.export;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvExportService {
    private final TabularExportSupport support;

    public CsvExportService(TabularExportSupport support) {
        this.support = support;
    }

    public byte[] export(String title, List<?> rows) {
        return export(title, rows, null);
    }

    public byte[] export(String title, List<?> rows, Class<?> rowType) {
        return support.delimited(title, rows, rowType, ",").getBytes(StandardCharsets.UTF_8);
    }
}
