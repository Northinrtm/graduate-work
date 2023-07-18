package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${image.dir.path}")
    private String imageDir;

    public String saveImage(MultipartFile image, String name) {

        String filename = name + "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
        Path filePath = Paths.get(imageDir).resolve(filename);

        try {
            Files.write(filePath, image.getBytes());
        } catch (IOException e) {
            log.error("Error writing file: {}", e.getMessage());
            throw new RuntimeException("Error writing file", e);
        }

        return filePath.toString();
    }

    public byte[] getImage(String path) throws IOException {
        String fullPath = imageDir + path;
        System.out.println(fullPath);
        return Files.readAllBytes(Path.of(fullPath));
    }
}
