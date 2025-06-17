package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Item.*;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final SecurityUtils securityUtils;

    //nur der User soll diese Methode haben um nach seinen Wünschen zu filtern
    // Haupt-GET-Endpunkt mit allen Filtern
    @GetMapping
    public ResponseEntity<List<Item>> getFilteredItems(
            @RequestParam Location location,                    // Pflicht: Standort
            @RequestParam(required = false) Boolean available,   // Optional: Verfügbarkeit
            @RequestParam(required = false) String searchQuery,  // Optional: Textsuche
            @RequestParam(required = false) Gender gender,       // Optional: Gender
            @RequestParam(required = false) Category category,   // Optional: Kategorie
            @RequestParam(required = false) Subcategory subcategory, // Optional: Unterkategorie
            @RequestParam(required = false) String size          // Optional: Größe
    ) {
        return ResponseEntity.ok(itemService.filterItems(
                location,
                available,
                searchQuery,
                gender,
                category,
                subcategory,
                size
        ));
    }

    //hier was sinnvolles machen
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }
//nur der admin soll das machen dürfen
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(itemService.createItem(item));
    }
//nur der admin soll das machen dürfen
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(itemService.updateItem(id, item));
    }
// nur der Admin soll das machen dürfen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        itemService.deleteItem(id);
        return ResponseEntity.ok().build();
    }
    // Add these methods to your existing ItemController

    @Autowired
    private Path fileStoragePath;

    // Upload image for an item
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadItemImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {

        try {
            Item item = itemService.getItemById(id);
            if (item == null) {
                return ResponseEntity.notFound().build();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = "item_" + id + "_" + UUID.randomUUID().toString() + extension;

            // Save file
            Path targetLocation = fileStoragePath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Update item with image URL
            String imageUrl = "/api/items/images/" + filename;
            item.setImageUrl(imageUrl);
            itemService.updateItem(id, item);

            return ResponseEntity.ok().body(Map.of(
                    "imageUrl", imageUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload image: " + e.getMessage());
        }
    }

    // Serve images
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = fileStoragePath.resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Adjust content type as needed
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
