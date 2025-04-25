package com.inventory_api.controller;

import com.inventory_api.model.Item;
import com.inventory_api.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Optional<Item> opt = itemRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
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
}
