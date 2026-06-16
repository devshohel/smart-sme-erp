package com.sme.erp.file.dto;

public class StoredFileDTO {
    private final String originalFilename;
    private final String storedFilename;
    private final String contentType;
    private final long fileSize;
    private final String storagePath;
    private final String publicUrl;

    public StoredFileDTO(
            String originalFilename,
            String storedFilename,
            String contentType,
            long fileSize,
            String storagePath,
            String publicUrl) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.publicUrl = publicUrl;
    }

    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public String getStoragePath() { return storagePath; }
    public String getPublicUrl() { return publicUrl; }
}
