package edu.hm.cs.kreisel_backend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileUploadConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void fileStorageProperties_ShouldReturnDefaultProperties() {
        // Given
        FileUploadConfig config = new FileUploadConfig();

        // When
        FileStorageProperties properties = config.fileStorageProperties();

        // Then
        assertNotNull(properties);
        assertEquals("uploads", properties.getDir());
    }

    @Test
    void fileStoragePath_ShouldCreateDirectorySuccessfully() {
        // Given
        FileUploadConfig config = new FileUploadConfig();
        FileStorageProperties properties = new FileStorageProperties();
        properties.setDir(tempDir.resolve("uploads").toString());

        // When
        Path result = config.fileStoragePath(properties);

        // Then
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertTrue(Files.isDirectory(result));
    }

    @Test
    void fileStoragePath_WhenDirectoryExists_ShouldReturnPath() {
        // Given
        FileUploadConfig config = new FileUploadConfig();
        FileStorageProperties properties = new FileStorageProperties();

        String uploadDirName = tempDir.resolve("existing-uploads").toString();
        properties.setDir(uploadDirName);

        // Create directory before test
        try {
            Files.createDirectories(Paths.get(uploadDirName));
        } catch (IOException e) {
            fail("Failed to create test directory: " + e.getMessage());
        }

        // When
        Path result = config.fileStoragePath(properties);

        // Then
        assertNotNull(result);
        assertTrue(Files.exists(result));
    }

    @Test
    void fileStoragePath_WhenDirectoryCreationFails_ShouldThrowException() {
        // Given
        FileUploadConfig config = new FileUploadConfig();
        FileStorageProperties properties = new FileStorageProperties();
        properties.setDir("test-uploads");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Simulated failure"));

            Path mockPath = mock(Path.class);
            when(mockPath.toAbsolutePath()).thenReturn(mockPath);
            when(mockPath.normalize()).thenReturn(mockPath);

            try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
                pathsMock.when(() -> Paths.get("test-uploads"))
                        .thenReturn(mockPath);

                // When/Then
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    config.fileStoragePath(properties);
                });

                assertEquals("Could not create upload directory", exception.getMessage());
                assertTrue(exception.getCause() instanceof IOException);
            }
        }
    }

    @Test
    @EnabledOnOs({OS.WINDOWS, OS.LINUX, OS.MAC})
    void fileStorageProperties_ShouldAllowCustomDirectory() {
        // Given
        FileUploadConfig config = new FileUploadConfig();
        FileStorageProperties properties = config.fileStorageProperties();

        // When
        properties.setDir("custom-uploads");

        // Then
        assertEquals("custom-uploads", properties.getDir());
    }
}