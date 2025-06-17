package edu.hm.cs.kreisel_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.upload")
    public FileStorageProperties fileStorageProperties() {
        return new FileStorageProperties();
    }

    @Bean
    public Path fileStoragePath(FileStorageProperties properties) {
        Path path = Paths.get(properties.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
        return path;
    }
}