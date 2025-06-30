package edu.hm.cs.kreisel_backend.security;

import java.util.Date;

/**
 * A test version of JwtUtil that allows overriding the expiration date
 * for testing purposes.
 */
public class TestJwtUtil extends JwtUtil {

    private Date customExpirationDate;

    public void setCustomExpirationDate(Date expirationDate) {
        this.customExpirationDate = expirationDate;
    }

    @Override
    protected Date getExpirationDate() {
        return customExpirationDate != null ?
                customExpirationDate :
                super.getExpirationDate();
    }
}