package com.inventory_api.controller;

import com.inventory_api.dto.ItemDTO;
import com.inventory_api.model.Item;
import com.inventory_api.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Conversion methods
    private ItemDTO toDto(Item item) {
        return modelMapper.map(item, ItemDTO.class);
    }

    private Item toEntity(ItemDTO dto) {
        return modelMapper.map(dto, Item.class);
    }

    // GET: list all items
    @GetMapping
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    // GET: get item by ID
    @GetMapping("/{id}")
    public ItemDTO getItemById(@PathVariable Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item not found with id " + id));
        return toDto(item);
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemDTO dto) {
        Item saved = itemRepository.save(toEntity(dto));
        return ResponseEntity.ok(toDto(saved));
    }

    // PUT: update existing item
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDTO dto) {

        Item existing = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item not found with id " + id));

        modelMapper.map(dto, existing);
        existing.setId(id);

        Item updated = itemRepository.save(existing);
        return ResponseEntity.ok(toDto(updated));
    }

    // DELETE: remove item by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Item existing = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item not found with id " + id));
        itemRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    // SEARCH: filter by name and/or price range
    @GetMapping("/search")
    public List<ItemDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<Item> items = (name != null)
            ? itemRepository.findByNameContainingIgnoreCase(name)
            : itemRepository.findAll();

        if (minPrice != null && maxPrice != null) {
            items = items.stream()
                .filter(i -> i.getPrice().compareTo(minPrice) >= 0
                          && i.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
        }

        return items.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    // TEST: constraint violation endpoint
    @GetMapping("/test/constraint")
    public ResponseEntity<String> testConstraint(
            @RequestParam("value") @Min(value = 1, message = "El parámetro 'value' debe ser >= 1") int value) {
        return ResponseEntity.ok("Recibido valor válido: " + value);
    }

    // TEST: generic exception endpoint
    @GetMapping("/test/error")
    public void testError() {
        throw new RuntimeException("Fallo inesperado de prueba");
    }
}
