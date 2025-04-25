// src/test/java/com/inventory_api/service/ItemServiceTest.java
package com.inventory_api.service;

import com.inventory_api.model.Item;
import com.inventory_api.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repo;

    @InjectMocks
    private ItemService service;

    @Test
    void create_savesAndReturnsItem() {
        Item in = new Item();
        in.setName("X");
        in.setQuantity(1);
        in.setPrice(new BigDecimal("9.99"));

        given(repo.save(in)).willReturn(in);

        Item out = service.create(in);

        assertThat(out).isSameAs(in);
        then(repo).should().save(in);
    }

    @Test
    void findById_existing_returnsItem() {
        Item saved = new Item();
        saved.setId(42L);
        given(repo.findById(42L)).willReturn(Optional.of(saved));

        Item out = service.findById(42L);

        assertThat(out.getId()).isEqualTo(42L);
    }

    @Test
    void findById_notFound_throwsException() {
        given(repo.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Item not found with id 99");
    }

    @Test
    void findAll_returnsPage() {
        Item one = new Item();
        one.setId(1L);
        Page<Item> page = new PageImpl<>(List.of(one));

        Pageable p = PageRequest.of(0, 10);
        given(repo.findAll(p)).willReturn(page);

        Page<Item> out = service.findAll(p);

        assertThat(out.getTotalElements()).isEqualTo(1);
        assertThat(out.getContent()).contains(one);
    }

    @Test
    void update_existing_updatesAndReturns() {
        Item existing = new Item();
        existing.setId(5L);
        existing.setName("Old");
        existing.setQuantity(2);
        existing.setPrice(new BigDecimal("5.00"));

        Item updated = new Item();
        updated.setName("New");
        updated.setQuantity(3);
        updated.setPrice(new BigDecimal("7.00"));

        given(repo.findById(5L)).willReturn(Optional.of(existing));
        given(repo.save(existing)).willReturn(existing);

        Item out = service.update(5L, updated);

        // tras el mapeo manual, existing lleva los valores de 'updated'
        assertThat(out.getName()).isEqualTo("New");
        assertThat(out.getQuantity()).isEqualTo(3);
        assertThat(out.getPrice()).isEqualByComparingTo("7.00");
    }

    @Test
    void delete_existing_invokesRepository() {
        Item existing = new Item();
        existing.setId(7L);
        given(repo.findById(7L)).willReturn(Optional.of(existing));

        service.delete(7L);

        then(repo).should().delete(existing);
    }
}
