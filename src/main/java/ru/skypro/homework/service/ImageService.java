package ru.skypro.homework.service;

import liquibase.pro.packaged.F;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${image.dir.path}")
    private String imageDir;

    public String saveImage(MultipartFile image) {

        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        Path filePath = Path.of(imageDir, filename);
        try {
            Files.write(filePath, image.getBytes());
        } catch (IOException e) {
            log.error("Error writing file: {}", e.getMessage());
            throw new RuntimeException("Error writing file", e);
        }
        return "/users/image/" + filename;
    }

    public byte[] getImage(String name) throws IOException {
        String fullPath = imageDir + "/" + name;
        File file = new File(fullPath);
        if (file.exists()) {
            return Files.readAllBytes(Path.of(fullPath));
        }
        return null;
    }
    public void deleteFileIfNotNull(String path) {
        String fileName = path.substring(path.lastIndexOf('/'));
        File fileToDelete = new File(imageDir + fileName);
        System.out.println(imageDir + fileName);
        System.out.println(fileToDelete.exists());
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                log.info("File successfully deleted");
            } else {
                log.info("Failed to delete file");
            }
        } else {
            log.info("File not found");
        }
    }
}
