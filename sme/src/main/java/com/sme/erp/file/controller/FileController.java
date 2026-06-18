package com.sme.erp.file.controller;

import com.sme.erp.file.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/products/{storedFilename:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String storedFilename) {
        Resource resource = fileStorageService.loadProductImage(storedFilename);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .contentType(resolveContentType(storedFilename))
                .body(resource);
    }

    @GetMapping("/expenses/{storedFilename:.+}")
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    public ResponseEntity<Resource> getExpenseReceipt(@PathVariable String storedFilename) {
        Resource resource = fileStorageService.loadExpenseReceipt(storedFilename);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(resolveContentType(storedFilename))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + storedFilename + "\"")
                .body(resource);
    }

    private MediaType resolveContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        return MediaType.IMAGE_JPEG;
    }
}
