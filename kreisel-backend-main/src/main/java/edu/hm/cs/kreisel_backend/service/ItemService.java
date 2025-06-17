package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Item.*;
import edu.hm.cs.kreisel_backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    // Haupt-Filtermethode
    public List<Item> filterItems(Location location, Boolean available, String searchQuery,
                                  Gender gender, Category category, Subcategory subcategory, String size) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getLocation() == location)
                .filter(item -> available == null || item.isAvailable() == available)
                .filter(item -> matchesSearch(item, searchQuery))
                .filter(item -> gender == null || item.getGender() == gender)
                .filter(item -> category == null || item.getCategory() == category)
                .filter(item -> subcategory == null || item.getSubcategory() == subcategory)
                .filter(item -> size == null || (item.getSize() != null && item.getSize().equalsIgnoreCase(size)))
                .toList();
    }

    private boolean matchesSearch(Item item, String searchQuery) {
        if (searchQuery == null) return true;
        String query = searchQuery.toLowerCase();
        return item.getName().toLowerCase().contains(query) ||
                (item.getBrand() != null && item.getBrand().toLowerCase().contains(query)) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(query));
    }


    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    public Item updateItem(Long id, Item updated) {
        Item existing = getItemById(id);
        existing.setName(updated.getName());
        existing.setSize(updated.getSize());
        existing.setDescription(updated.getDescription());
        existing.setBrand(updated.getBrand());
        existing.setAvailable(updated.isAvailable());
        existing.setLocation(updated.getLocation());
        existing.setGender(updated.getGender());
        existing.setCategory(updated.getCategory());
        existing.setSubcategory(updated.getSubcategory());
        existing.setZustand(updated.getZustand());
        return itemRepository.save(existing);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}
