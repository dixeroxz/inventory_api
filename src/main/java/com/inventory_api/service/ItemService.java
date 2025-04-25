package com.inventory_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityNotFoundException;

import com.inventory_api.model.Item;
import com.inventory_api.repository.ItemRepository;

@Service
@Transactional  // Opcional a nivel de clase
public class ItemService {
  @Autowired private ItemRepository repo;

  public Item create(Item item) { return repo.save(item); }

  @Transactional(readOnly = true)
  public Page<Item> findAll(Pageable p) { return repo.findAll(p); }

  @Transactional(readOnly = true)
  public Item findById(Long id) {
    return repo.findById(id)
               .orElseThrow(() -> new EntityNotFoundException("Item not found with id " + id));
  }

  public Item update(Long id, Item updated) {
    Item existing = findById(id);
    // mapear campos, e.g. existing.setName(updated.getName());
    existing.setId(id);  // asegúrate de no cambiar el ID
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());
    existing.setQuantity(updated.getQuantity());
    existing.setPrice(updated.getPrice());
    return repo.save(existing);
  }

  public void delete(Long id) {
    repo.delete(findById(id));
  }
}
