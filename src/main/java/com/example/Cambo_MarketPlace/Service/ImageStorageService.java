package com.example.Cambo_MarketPlace.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

    private final String UPLOAD_DIR = "uploads/products/";

    public String uploadImage(MultipartFile file) {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            Files.write(filePath, file.getBytes());

            return "/images/products/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed");
        }
    }
}
