package com.inventory_api.controller;

import com.inventory_api.dto.ItemDTO;
import com.inventory_api.model.Item;
import com.inventory_api.repository.ItemRepository;
import com.inventory_api.service.ItemService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;  // sólo para /search

    @Autowired
    private ModelMapper modelMapper;

    // Conversión entre entidad y DTO
    private ItemDTO toDto(Item item) {
        return modelMapper.map(item, ItemDTO.class);
    }

    private Item toEntity(ItemDTO dto) {
        return modelMapper.map(dto, Item.class);
    }

    // GET: list all items con paginación y ordenamiento
    @GetMapping
    public Page<ItemDTO> getAllItems(Pageable pageable) {
        return itemService.findAll(pageable)
                          .map(this::toDto);
    }

    // GET: get item by ID
    @GetMapping("/{id}")
    public ItemDTO getItemById(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return toDto(item);
    }

    // POST: crear nuevo item
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemDTO dto) {
        Item saved = itemService.create(toEntity(dto));
        return ResponseEntity.ok(toDto(saved));
    }

    // PUT: actualizar item existente
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDTO dto) {

        Item updated = itemService.update(id, toEntity(dto));
        return ResponseEntity.ok(toDto(updated));
    }

    // DELETE: eliminar item por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // SEARCH: filtrado básico por nombre y/o rango de precio
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

    // TEST: endpoint para ConstraintViolationException
    @GetMapping("/test/constraint")
    public ResponseEntity<String> testConstraint(
            @RequestParam("value") @Min(value = 1, message = "El parámetro 'value' debe ser >= 1") int value) {
        return ResponseEntity.ok("Recibido valor válido: " + value);
    }

    // TEST: endpoint para excepción genérica
    @GetMapping("/test/error")
    public void testError() {
        throw new RuntimeException("Fallo inesperado de prueba");
    }
}
