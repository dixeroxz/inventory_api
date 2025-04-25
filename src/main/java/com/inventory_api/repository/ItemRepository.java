package com.inventory_api.repository;

import com.inventory_api.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // Aquí podrías añadir métodos de consulta personalizados,
    // por ejemplo: List<Item> findByNameContainingIgnoreCase(String name);
}
