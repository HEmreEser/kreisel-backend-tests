package edu.hm.cs.kreisel_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "app_item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String size; // z. B. L, XL, 120cm, 1.5L etc.
    private boolean available = true;
    private String description;
    private String brand;
    // Add these fields to your existing Item class

    // Simple image storage
    private String imageUrl; // Store path or URL to image

    // Review statistics
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    private Location location;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Subcategory subcategory;

    @Enumerated(EnumType.STRING)
    private Zustand zustand;

    public enum Location {
        PASING, LOTHSTRASSE, KARLSTRASSE
    }

    public enum Gender {
        DAMEN, HERREN, UNISEX
    }

    public enum Category {
        KLEIDUNG, SCHUHE, ACCESSOIRES, TASCHEN, EQUIPMENT
    }

    public enum Subcategory {
        HOSEN, JACKEN, STIEFEL, WANDERSCHUHE,
        MUETZEN, HANDSCHUHE, SCHALS, BRILLEN, FLASCHEN, SKI, SNOWBOARDS, HELME
    }

    public void markAsUnavailable() {
        this.available = false;
    }

    public void markAsAvailable() {
        this.available = true;
    }

    public enum Zustand {
        NEU, GEBRAUCHT
    }

}