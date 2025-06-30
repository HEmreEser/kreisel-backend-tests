package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item1;
    private Item item2;
    private Item item3;
    private List<Item> allItems;

    @BeforeEach
    void setUp() {
        // Setup test items with different properties for filtering tests
        item1 = new Item();
        item1.setId(1L);
        item1.setName("Blue Jacket");
        item1.setDescription("Waterproof winter jacket");
        item1.setBrand("NorthFace");
        item1.setSize("M");
        item1.setAvailable(true);
        item1.setLocation(Item.Location.PASING);
        item1.setGender(Item.Gender.UNISEX);
        item1.setCategory(Item.Category.KLEIDUNG);
        item1.setSubcategory(Item.Subcategory.JACKEN);
        item1.setZustand(Item.Zustand.NEU);

        item2 = new Item();
        item2.setId(2L);
        item2.setName("Hiking Boots");
        item2.setDescription("Durable mountain boots");
        item2.setBrand("Salomon");
        item2.setSize("42");
        item2.setAvailable(false);
        item2.setLocation(Item.Location.PASING);
        item2.setGender(Item.Gender.HERREN);
        item2.setCategory(Item.Category.SCHUHE);
        item2.setSubcategory(Item.Subcategory.WANDERSCHUHE);
        item2.setZustand(Item.Zustand.GEBRAUCHT);

        item3 = new Item();
        item3.setId(3L);
        item3.setName("Ski Goggles");
        item3.setDescription("UV protection goggles");
        item3.setBrand("Oakley");
        item3.setSize("ONE SIZE");
        item3.setAvailable(true);
        item3.setLocation(Item.Location.LOTHSTRASSE);
        item3.setGender(Item.Gender.UNISEX);
        item3.setCategory(Item.Category.ACCESSOIRES);
        item3.setSubcategory(Item.Subcategory.BRILLEN);
        item3.setZustand(Item.Zustand.GEBRAUCHT);

        allItems = Arrays.asList(item1, item2, item3);
    }

    @Test
    void filterItems_ByLocationOnly_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<Item> result = itemService.filterItems(Item.Location.PASING, null, null, null, null, null, null);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_ByLocationAndAvailability_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<Item> result = itemService.filterItems(Item.Location.PASING, true, null, null, null, null, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(item1, result.get(0));
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_BySearchQuery_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When - Search in name
        List<Item> result1 = itemService.filterItems(Item.Location.PASING, null, "jacket", null, null, null, null);

        // Then
        assertEquals(1, result1.size());
        assertEquals(item1, result1.get(0));

        // When - Search in description
        List<Item> result2 = itemService.filterItems(Item.Location.PASING, null, "waterproof", null, null, null, null);

        // Then
        assertEquals(1, result2.size());
        assertEquals(item1, result2.get(0));

        // When - Search in brand
        List<Item> result3 = itemService.filterItems(Item.Location.PASING, null, "north", null, null, null, null);

        // Then
        assertEquals(1, result3.size());
        assertEquals(item1, result3.get(0));

        verify(itemRepository, times(3)).findAll();
    }

    @Test
    void filterItems_ByGender_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<Item> result = itemService.filterItems(Item.Location.PASING, null, null, Item.Gender.HERREN, null, null, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(item2, result.get(0));
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_ByCategoryAndSubcategory_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<Item> result = itemService.filterItems(
                Item.Location.PASING,
                null,
                null,
                null,
                Item.Category.KLEIDUNG,
                Item.Subcategory.JACKEN,
                null);

        // Then
        assertEquals(1, result.size());
        assertEquals(item1, result.get(0));
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_BySize_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When - Case insensitive size matching
        List<Item> result = itemService.filterItems(Item.Location.PASING, null, null, null, null, null, "m");

        // Then
        assertEquals(1, result.size());
        assertEquals(item1, result.get(0));
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_WithMultipleFilters_ShouldReturnMatchingItems() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When
        List<Item> result = itemService.filterItems(
                Item.Location.PASING,
                true,
                "jacket",
                Item.Gender.UNISEX,
                Item.Category.KLEIDUNG,
                Item.Subcategory.JACKEN,
                "M");

        // Then
        assertEquals(1, result.size());
        assertEquals(item1, result.get(0));
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_WithNoMatches_ShouldReturnEmptyList() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When - No items at KARLSTRASSE
        List<Item> result = itemService.filterItems(Item.Location.KARLSTRASSE, null, null, null, null, null, null);

        // Then
        assertTrue(result.isEmpty());
        verify(itemRepository).findAll();
    }

    @Test
    void filterItems_WithNullSizeButItemHasSize_ShouldIncludeItem() {
        // Given
        when(itemRepository.findAll()).thenReturn(allItems);

        // When - Filter only by location, but item has size
        List<Item> result = itemService.filterItems(Item.Location.PASING, null, null, null, null, null, null);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(item1)); // item1 has size "M"
        verify(itemRepository).findAll();
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));

        // When
        Item result = itemService.getItemById(1L);

        // Then
        assertEquals(item1, result);
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ShouldThrowException() {
        // Given
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.getItemById(99L);
        });
        assertEquals("Item not found", exception.getMessage());
        verify(itemRepository).findById(99L);
    }

    @Test
    void createItem_ShouldSaveAndReturnItem() {
        // Given
        Item newItem = new Item();
        newItem.setName("New Test Item");

        when(itemRepository.save(any(Item.class))).thenReturn(newItem);

        // When
        Item result = itemService.createItem(newItem);

        // Then
        assertEquals(newItem, result);
        verify(itemRepository).save(newItem);
    }

    @Test
    void updateItem_WhenItemExists_ShouldUpdateAllFields() {
        // Given
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");

        Item updatedItem = new Item();
        updatedItem.setName("Updated Name");
        updatedItem.setDescription("Updated Description");
        updatedItem.setBrand("Updated Brand");
        updatedItem.setSize("L");
        updatedItem.setAvailable(false);
        updatedItem.setLocation(Item.Location.LOTHSTRASSE);
        updatedItem.setGender(Item.Gender.DAMEN);
        updatedItem.setCategory(Item.Category.ACCESSOIRES);
        updatedItem.setSubcategory(Item.Subcategory.BRILLEN);
        updatedItem.setZustand(Item.Zustand.GEBRAUCHT);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Item result = itemService.updateItem(1L, updatedItem);

        // Then
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Brand", result.getBrand());
        assertEquals("L", result.getSize());
        assertFalse(result.isAvailable());
        assertEquals(Item.Location.LOTHSTRASSE, result.getLocation());
        assertEquals(Item.Gender.DAMEN, result.getGender());
        assertEquals(Item.Category.ACCESSOIRES, result.getCategory());
        assertEquals(Item.Subcategory.BRILLEN, result.getSubcategory());
        assertEquals(Item.Zustand.GEBRAUCHT, result.getZustand());

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item savedItem = itemCaptor.getValue();

        // Verify all fields were updated correctly
        assertEquals(1L, savedItem.getId()); // ID should remain the same
        assertEquals("Updated Name", savedItem.getName());
        assertEquals("Updated Description", savedItem.getDescription());
        assertEquals("Updated Brand", savedItem.getBrand());
        assertEquals("L", savedItem.getSize());
        assertFalse(savedItem.isAvailable());
        assertEquals(Item.Location.LOTHSTRASSE, savedItem.getLocation());
        assertEquals(Item.Gender.DAMEN, savedItem.getGender());
        assertEquals(Item.Category.ACCESSOIRES, savedItem.getCategory());
        assertEquals(Item.Subcategory.BRILLEN, savedItem.getSubcategory());
        assertEquals(Item.Zustand.GEBRAUCHT, savedItem.getZustand());
    }

    @Test
    void updateItem_WhenItemDoesNotExist_ShouldThrowException() {
        // Given
        Item updatedItem = new Item();
        updatedItem.setName("Updated Name");

        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.updateItem(99L, updatedItem);
        });
        assertEquals("Item not found", exception.getMessage());
        verify(itemRepository).findById(99L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_ShouldDeleteItem() {
        // Given
        Long itemId = 1L;
        doNothing().when(itemRepository).deleteById(itemId);

        // When
        itemService.deleteItem(itemId);

        // Then
        verify(itemRepository).deleteById(itemId);
    }
}