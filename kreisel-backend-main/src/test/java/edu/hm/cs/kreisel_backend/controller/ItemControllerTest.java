package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private SecurityUtils securityUtils;

    // Verwenden eines echten temporären Verzeichnisses für Dateitests
    @TempDir
    Path tempDir;

    @InjectMocks
    private ItemController itemController;

    private Item testItem;
    private User adminUser;
    private User regularUser;
    private List<Item> itemList;

    @BeforeEach
    void setUp() {
        // Setze das temporäre Verzeichnis als fileStoragePath im Controller
        ReflectionTestUtils.setField(itemController, "fileStoragePath", tempDir);

        // Initialize test item with correct enum values from the provided model
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setLocation(Item.Location.PASING);
        testItem.setGender(Item.Gender.UNISEX);
        testItem.setCategory(Item.Category.KLEIDUNG);
        testItem.setSubcategory(Item.Subcategory.JACKEN);
        testItem.setSize("M");
        testItem.setAvailable(true);
        testItem.setBrand("TestBrand");
        testItem.setZustand(Item.Zustand.NEU);
        testItem.setAverageRating(4.5);
        testItem.setReviewCount(10);

        // Create second test item
        Item testItem2 = new Item();
        testItem2.setId(2L);
        testItem2.setName("Another Item");
        testItem2.setDescription("Another Description");
        testItem2.setLocation(Item.Location.LOTHSTRASSE);
        testItem2.setGender(Item.Gender.HERREN);
        testItem2.setCategory(Item.Category.SCHUHE);
        testItem2.setSubcategory(Item.Subcategory.STIEFEL);
        testItem2.setSize("42");
        testItem2.setAvailable(true);
        testItem2.setBrand("AnotherBrand");
        testItem2.setZustand(Item.Zustand.GEBRAUCHT);

        itemList = Arrays.asList(testItem, testItem2);

        // Setup users for authorization tests
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@hm.edu");
        adminUser.setRole(User.Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setFullName("Regular User");
        regularUser.setEmail("user@hm.edu");
        regularUser.setRole(User.Role.USER);
    }

    @Test
    void getFilteredItems_ShouldReturnFilteredItems() {
        // Given
        when(itemService.filterItems(
                Item.Location.PASING,
                true,
                "jacke",
                Item.Gender.UNISEX,
                Item.Category.KLEIDUNG,
                Item.Subcategory.JACKEN,
                "M")).thenReturn(itemList);

        // When
        ResponseEntity<List<Item>> response = itemController.getFilteredItems(
                Item.Location.PASING,
                true,
                "jacke",
                Item.Gender.UNISEX,
                Item.Category.KLEIDUNG,
                Item.Subcategory.JACKEN,
                "M");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemList, response.getBody());
        verify(itemService).filterItems(
                Item.Location.PASING,
                true,
                "jacke",
                Item.Gender.UNISEX,
                Item.Category.KLEIDUNG,
                Item.Subcategory.JACKEN,
                "M");
    }

    @Test
    void getItemById_ShouldReturnItem() {
        // Given
        when(itemService.getItemById(1L)).thenReturn(testItem);

        // When
        ResponseEntity<Item> response = itemController.getItemById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem, response.getBody());
        verify(itemService).getItemById(1L);
    }

    @Test
    void createItem_WithAdminUser_ShouldCreateItem() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(itemService.createItem(testItem)).thenReturn(testItem);

        // When
        ResponseEntity<Item> response = itemController.createItem(testItem);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem, response.getBody());
        verify(itemService).createItem(testItem);
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void createItem_WithNonAdminUser_ShouldReturnForbidden() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);

        // When
        ResponseEntity<Item> response = itemController.createItem(testItem);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(itemService);
    }

    @Test
    void createItem_WithNoUser_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Item> response = itemController.createItem(testItem);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(itemService);
    }

    @Test
    void updateItem_WithAdminUser_ShouldUpdateItem() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(itemService.updateItem(1L, testItem)).thenReturn(testItem);

        // When
        ResponseEntity<Item> response = itemController.updateItem(1L, testItem);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem, response.getBody());
        verify(itemService).updateItem(1L, testItem);
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void updateItem_WithNonAdminUser_ShouldReturnForbidden() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);

        // When
        ResponseEntity<Item> response = itemController.updateItem(1L, testItem);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(itemService);
    }

    @Test
    void updateItem_WithNoUser_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Item> response = itemController.updateItem(1L, testItem);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(itemService);
    }

    @Test
    void deleteItem_WithAdminUser_ShouldDeleteItem() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        doNothing().when(itemService).deleteItem(1L);

        // When
        ResponseEntity<Void> response = itemController.deleteItem(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(itemService).deleteItem(1L);
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void deleteItem_WithNonAdminUser_ShouldReturnForbidden() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);

        // When
        ResponseEntity<Void> response = itemController.deleteItem(1L);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(itemService);
    }

    @Test
    void deleteItem_WithNoUser_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Void> response = itemController.deleteItem(1L);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(itemService);
    }

    @Test
    void uploadItemImage_WhenItemExists_ShouldUploadImageAndUpdateItem() throws IOException {
        // Given
        Long itemId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Bereite den ItemService-Mock vor
        when(itemService.getItemById(itemId)).thenReturn(testItem);
        when(itemService.updateItem(eq(itemId), any(Item.class))).thenReturn(testItem);

        // When
        ResponseEntity<?> response = itemController.uploadItemImage(itemId, file);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("imageUrl"));
        assertTrue(responseBody.get("imageUrl").startsWith("/api/items/images/"));

        // Verifiziere die Aufrufe
        verify(itemService).getItemById(itemId);
        verify(itemService).updateItem(eq(itemId), any(Item.class));

        // Überprüfe, ob die Datei tatsächlich erstellt wurde
        assertTrue(Files.list(tempDir).count() > 0);
    }

    @Test
    void uploadItemImage_WhenItemDoesNotExist_ShouldReturnNotFound() {
        // Given
        Long itemId = 99L;
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(itemService.getItemById(itemId)).thenReturn(null);

        // When
        ResponseEntity<?> response = itemController.uploadItemImage(itemId, file);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(itemService).getItemById(itemId);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void uploadItemImage_WhenExceptionOccurs_ShouldReturnInternalServerError() throws IOException {
        // Given
        Long itemId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Mock ItemService um item zurückzugeben
        when(itemService.getItemById(itemId)).thenReturn(testItem);

        // Überschreibe das tempDir mit einem nicht-existierenden Pfad, um eine Exception zu provozieren
        Path invalidPath = Paths.get("/non-existent-directory");
        ReflectionTestUtils.setField(itemController, "fileStoragePath", invalidPath);

        // When
        ResponseEntity<?> response = itemController.uploadItemImage(itemId, file);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Failed to upload image"));
        verify(itemService).getItemById(itemId);
    }

    @Test
    void getImage_WhenImageExists_ShouldReturnImage() throws Exception {
        // Given
        String filename = "test-image.jpg";
        Path imagePath = tempDir.resolve(filename);

        // Erstelle tatsächliche Testdatei
        Files.write(imagePath, "test image content".getBytes());

        // When
        ResponseEntity<Resource> response = itemController.getImage(filename);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UrlResource);
        assertTrue(((UrlResource) response.getBody()).exists());
    }

    @Test
    void getImage_WhenImageDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        String filename = "non-existent.jpg";
        // Stelle sicher, dass die Datei nicht existiert
        Path nonExistentPath = tempDir.resolve(filename);
        Files.deleteIfExists(nonExistentPath);

        // When
        ResponseEntity<Resource> response = itemController.getImage(filename);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}