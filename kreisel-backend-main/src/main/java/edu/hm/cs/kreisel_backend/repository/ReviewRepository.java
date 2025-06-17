package edu.hm.cs.kreisel_backend.repository;

import edu.hm.cs.kreisel_backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItemId(Long itemId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByRentalId(Long rentalId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
    Double getAverageRatingForItem(@Param("itemId") Long itemId);
}