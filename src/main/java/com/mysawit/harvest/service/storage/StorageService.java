package com.mysawit.harvest.service.storage;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface StorageService {
    String uploadFile(MultipartFile file);
    List<String> uploadFiles(List<MultipartFile> files);
}