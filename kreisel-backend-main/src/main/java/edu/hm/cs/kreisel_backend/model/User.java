package edu.hm.cs.kreisel_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Email
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@hm\\.edu$", message = "Nur HM-E-Mail-Adressen erlaubt")
    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-rentals")
    private List<Rental> rentals;

    public enum Role {
        USER,
        ADMIN
    }
}