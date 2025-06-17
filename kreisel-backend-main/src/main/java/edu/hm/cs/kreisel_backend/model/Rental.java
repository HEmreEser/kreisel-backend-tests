package edu.hm.cs.kreisel_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@Entity
@Table(name = "app_rental")
public class Rental {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        @JsonBackReference("user-rentals")
        private User user;

        @ManyToOne
        @JoinColumn(name = "item_id")
        private Item item;

        @Column(name = "rental_date")
        private LocalDate rentalDate; // Wann ausgeliehen

        @Column(name = "end_date")
        private LocalDate endDate; // Geplantes Rückgabedatum (beim Ausleihen festgelegt)

        @Column(name = "return_date")
        private LocalDate returnDate; // Tatsächliches Rückgabedatum (null = noch nicht zurückgegeben)

        @Column(name = "extended")
        private boolean extended = false; // Max. 1 Verlängerung erlaubt

        // Dynamisches Statusfeld für JSON
        @JsonProperty("status")
        public String getStatus() {
                if (returnDate != null) {
                        return "RETURNED";
                }
                if (endDate.isBefore(LocalDate.now())) {
                        return "OVERDUE";
                }
                return "ACTIVE";
        }
}
