package edu.hm.cs.kreisel_backend.repository;

import edu.hm.cs.kreisel_backend.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserId(Long userId);

    List<Rental> findByUserIdAndReturnDateIsNull(Long userId);

    List<Rental> findByUserIdAndReturnDateIsNotNull(Long userId);

    Optional<Rental> findByItemIdAndReturnDateIsNull(Long itemId);

}