package com.inventory_api.controller;

import jakarta.persistence.EntityNotFoundException;
import com.inventory_api.model.Item;
import com.inventory_api.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    // GET /api/items
    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public Item getItemById(@PathVariable Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Item not found with id " + id)
            );
    }


    // POST /api/items
    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    // PUT /api/items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Long id,
            @RequestBody Item details) {

        return itemRepository.findById(id)
            .map(item -> {
                item.setName(details.getName());
                item.setDescription(details.getDescription());
                item.setQuantity(details.getQuantity());
                item.setPrice(details.getPrice());
                return ResponseEntity.ok(itemRepository.save(item));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        return itemRepository.findById(id)
            .map(item -> {
                itemRepository.delete(item);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // Nuevo endpoint para probar ConstraintViolationException
    @GetMapping("/test/constraint")
    public ResponseEntity<String> testConstraint(
            @RequestParam("value") @Min(value = 1, message = "El parámetro 'value' debe ser >= 1") int value) {
        return ResponseEntity.ok("Recibido valor válido: " + value);
    }

    // GET /api/items/test/error
    @GetMapping("/test/error")
    public void testError() {
        throw new RuntimeException("Fallo inesperado de prueba");
}
}
