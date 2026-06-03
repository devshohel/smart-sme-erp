package com.sme.erp.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.erp.common.exception.GlobalExceptionHandler;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.service.CustomerService;
import com.sme.erp.enums.Status;
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
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerController(customerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAll_returnsCustomersAndPassesFilters() throws Exception {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setCustomerCode("CUS-0001");
        customer.setName("Acme Trading");
        customer.setStatus(Status.ACTIVE);

        when(customerService.getAll("acme", Status.ACTIVE)).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/v1/customers")
                        .param("keyword", "acme")
                        .param("status", "ACTIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerCode").value("CUS-0001"))
                .andExpect(jsonPath("$[0].name").value("Acme Trading"));

        verify(customerService).getAll("acme", Status.ACTIVE);
    }

    @Test
    void create_returnsCreatedCustomer() throws Exception {
        CustomerDTO request = new CustomerDTO();
        request.setName("Acme Trading");
        request.setStatus(Status.ACTIVE);

        CustomerDTO response = new CustomerDTO();
        response.setId(1L);
        response.setCustomerCode("CUS-0001");
        response.setName("Acme Trading");
        response.setStatus(Status.ACTIVE);

        when(customerService.create(any(CustomerDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerCode").value("CUS-0001"));
    }

    @Test
    void create_rejectsBlankName() throws Exception {
        CustomerDTO request = new CustomerDTO();
        request.setName("");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Customer name is required"));
    }

    @Test
    void update_passesCustomerIdAndBody() throws Exception {
        CustomerDTO request = new CustomerDTO();
        request.setName("Updated Customer");

        CustomerDTO response = new CustomerDTO();
        response.setId(1L);
        response.setCustomerCode("CUS-0001");
        response.setName("Updated Customer");

        when(customerService.update(eq(1L), any(CustomerDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Customer"));

        verify(customerService).update(eq(1L), any(CustomerDTO.class));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(1L);
    }
}
