package com.sme.erp.file.service;

import com.sme.erp.file.dto.StoredFileDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFileDTO storeProductImage(MultipartFile file);
    Resource loadProductImage(String storedFilename);
}
