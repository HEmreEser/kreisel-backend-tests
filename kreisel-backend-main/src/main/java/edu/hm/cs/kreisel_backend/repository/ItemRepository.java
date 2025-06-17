package edu.hm.cs.kreisel_backend.repository;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Item.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 🔹 Für erste Filterstufe: Location
    List<Item> findByLocation(Location location);
    List<Item> findByLocationAndAvailable(Location location, boolean available);

    // 🔹 Für optionale Textsuche (kombinierbar im Service)
    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(
            String name, String description, String brand
    );

    // 🔹 Für alle verfügbaren Items (falls Location nicht relevant)
    List<Item> findByAvailableTrue();
}
