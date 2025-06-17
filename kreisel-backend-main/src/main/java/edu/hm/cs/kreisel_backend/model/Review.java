package edu.hm.cs.kreisel_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

    @Entity
    @Data
    public class Review {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "item_id", nullable = false)
        private Item item;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @OneToOne
        @JoinColumn(name = "rental_id", nullable = false, unique = true)
        private Rental rental;

        private int rating; // 1-5 stars
        private String comment;
        private LocalDateTime createdAt = LocalDateTime.now();
    }
