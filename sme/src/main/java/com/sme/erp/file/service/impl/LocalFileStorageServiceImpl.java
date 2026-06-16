package com.sme.erp.file.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.file.dto.StoredFileDTO;
import com.sme.erp.file.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path productImageDirectory;
    private final long maxProductImageSize;

    public LocalFileStorageServiceImpl(
            @Value("${erp.storage.product-image-dir:uploads/products}") String productImageDirectory,
            @Value("${erp.upload.max-product-image-size:2097152}") long maxProductImageSize) {
        this.productImageDirectory = Paths.get(productImageDirectory).toAbsolutePath().normalize();
        this.maxProductImageSize = maxProductImageSize;
    }

    @Override
    public StoredFileDTO storeProductImage(MultipartFile file) {
        validateProductImage(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "product-image");
        String extension = extension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path target = productImageDirectory.resolve(storedFilename).normalize();

        if (!target.startsWith(productImageDirectory)) {
            throw new BadRequestException("Invalid file path");
        }

        try {
            Files.createDirectories(productImageDirectory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BadRequestException("Product image could not be stored");
        }

        return new StoredFileDTO(
                originalFilename,
                storedFilename,
                file.getContentType(),
                file.getSize(),
                target.toString(),
                "/api/v1/files/products/" + storedFilename);
    }

    @Override
    public Resource loadProductImage(String storedFilename) {
        String cleanFilename = StringUtils.cleanPath(storedFilename != null ? storedFilename : "");
        if (cleanFilename.contains("..") || cleanFilename.contains("/") || cleanFilename.contains("\\")) {
            throw new BadRequestException("Invalid file name");
        }

        Path filePath = productImageDirectory.resolve(cleanFilename).normalize();
        if (!filePath.startsWith(productImageDirectory)) {
            throw new BadRequestException("Invalid file path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Product image not found: " + cleanFilename);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BadRequestException("Product image path is invalid");
        }
    }

    private void validateProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Product image is empty");
        }
        if (file.getSize() > maxProductImageSize) {
            throw new BadRequestException("Product image exceeds maximum size");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = extension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Product image must be jpg, jpeg, png, or webp");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Product image content type is not allowed");
        }
    }

    private String extension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
