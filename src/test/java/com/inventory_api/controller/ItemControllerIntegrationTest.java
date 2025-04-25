// src/test/java/com/inventory_api/ItemControllerIntegrationTest.java
package com.inventory_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory_api.dto.ItemDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long createdId;

    @Test
    @Order(1)
    void createItem_valid_returns200AndBody() throws Exception {
        ItemDTO dto = new ItemDTO(null, "TestItem", "Desc", 5, new BigDecimal("19.99"));
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("TestItem"))
            .andDo(result -> {
                ItemDTO resp = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ItemDTO.class
                );
                createdId = resp.getId();
            });
    }

    @Test
    @Order(2)
    void getItemById_existing_returns200AndItem() throws Exception {
        mockMvc.perform(get("/api/items/{id}", createdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdId))
            .andExpect(jsonPath("$.name").value("TestItem"));
    }

    @Test
    @Order(3)
    void getAllItems_returnsListContainingCreated() throws Exception {
        mockMvc.perform(get("/api/items"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(createdId));
    }

    @Test
    @Order(4)
    void updateItem_existing_returnsUpdated() throws Exception {
        ItemDTO update = new ItemDTO(null, "UpdatedName", "NewDesc", 10, new BigDecimal("29.99"));
        mockMvc.perform(put("/api/items/{id}", createdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("UpdatedName"))
            .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    @Order(5)
    void searchItems_byName_returnsFiltered() throws Exception {
        mockMvc.perform(get("/api/items/search")
                .param("name", "Updated"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("UpdatedName"));
    }

    @Test
    @Order(6)
    void deleteItem_existing_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", createdId))
            .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    void getItemById_deleted_returns404() throws Exception {
        mockMvc.perform(get("/api/items/{id}", createdId))
            .andExpect(status().isNotFound());
    }

    // Adicional: validación, JSON malformado, constraint, error genérico...
    // Por ejemplo:
    @Test
    @Order(8)
    void createItem_invalid_validationError() throws Exception {
        ItemDTO bad = new ItemDTO(null, "", "", -1, new BigDecimal("-5"));
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
@Order(9)
void getAllItems_withPagination_returnsPagedResults() throws Exception {
    // Crea 15 items de prueba
    for (int i = 1; i <= 15; i++) {
        ItemDTO dto = new ItemDTO(null, "Item" + i, "Desc " + i, i, new BigDecimal("1.00"));
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    // Página 0, tamaño 5
    mockMvc.perform(get("/api/items")
            .param("page", "0")
            .param("size", "5")
            .param("sort", "name,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.totalElements").value(15))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.number").value(0));

    // Página 2, tamaño 5 (la última)
    mockMvc.perform(get("/api/items")
            .param("page", "2")
            .param("size", "5")
            .param("sort", "name,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.number").value(2));
}
}
