// AuthResponse.java
package edu.hm.cs.kreisel_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String message;
    private String token;
}
