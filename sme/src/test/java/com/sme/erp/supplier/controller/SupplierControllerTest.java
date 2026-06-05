package com.sme.erp.supplier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.erp.common.exception.GlobalExceptionHandler;
import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SupplierController(supplierService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAll_returnsSuppliersAndPassesFilters() throws Exception {
        SupplierDTO supplier = new SupplierDTO();
        supplier.setId(1L);
        supplier.setSupplierCode("SUP-0001");
        supplier.setName("Global Supplies");
        supplier.setStatus(Status.ACTIVE);

        when(supplierService.getAll("global", Status.ACTIVE)).thenReturn(List.of(supplier));

        mockMvc.perform(get("/api/v1/suppliers")
                        .param("keyword", "global")
                        .param("status", "ACTIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].supplierCode").value("SUP-0001"))
                .andExpect(jsonPath("$[0].name").value("Global Supplies"));

        verify(supplierService).getAll("global", Status.ACTIVE);
    }

    @Test
    void create_returnsCreatedSupplier() throws Exception {
        SupplierDTO request = new SupplierDTO();
        request.setName("Global Supplies");
        request.setStatus(Status.ACTIVE);

        SupplierDTO response = new SupplierDTO();
        response.setId(1L);
        response.setSupplierCode("SUP-0001");
        response.setName("Global Supplies");
        response.setStatus(Status.ACTIVE);

        when(supplierService.create(any(SupplierDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.supplierCode").value("SUP-0001"));
    }

    @Test
    void create_rejectsBlankName() throws Exception {
        SupplierDTO request = new SupplierDTO();
        request.setName("");

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Supplier name is required"));
    }

    @Test
    void update_passesSupplierIdAndBody() throws Exception {
        SupplierDTO request = new SupplierDTO();
        request.setName("Updated Supplier");

        SupplierDTO response = new SupplierDTO();
        response.setId(1L);
        response.setSupplierCode("SUP-0001");
        response.setName("Updated Supplier");

        when(supplierService.update(eq(1L), any(SupplierDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/suppliers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Supplier"));

        verify(supplierService).update(eq(1L), any(SupplierDTO.class));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/suppliers/1"))
                .andExpect(status().isNoContent());

        verify(supplierService).delete(1L);
    }
}
