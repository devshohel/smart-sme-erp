package com.sme.erp.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returns404ForResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("missing resource"));
    }

    @Test
    void returns409ForDuplicateResourceException() throws Exception {
        mockMvc.perform(get("/test/duplicate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("duplicate resource"));
    }

    @Test
    void returns400ForBadRequestException() throws Exception {
        mockMvc.perform(get("/test/bad-request").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad request"));
    }

    @Test
    void returns409ForDataIntegrityViolationException() throws Exception {
        mockMvc.perform(get("/test/data-integrity").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Duplicate entry 'CUS-0001' for key 'customers.customer_code'"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestExceptionController {

        @GetMapping("/not-found")
        String notFound() {
            throw new ResourceNotFoundException("missing resource");
        }

        @GetMapping("/duplicate")
        String duplicate() {
            throw new DuplicateResourceException("duplicate resource");
        }

        @GetMapping("/bad-request")
        String badRequest() {
            throw new BadRequestException("bad request");
        }

        @GetMapping("/data-integrity")
        String dataIntegrity() {
            throw new DataIntegrityViolationException(
                    "could not execute statement",
                    new RuntimeException("Duplicate entry 'CUS-0001' for key 'customers.customer_code'"));
        }
    }
}
