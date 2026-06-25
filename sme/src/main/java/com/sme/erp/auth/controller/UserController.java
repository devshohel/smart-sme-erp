package com.sme.erp.auth.controller;

import com.sme.erp.auth.dto.RoleDTO;
import com.sme.erp.auth.dto.UserDTO;
import com.sme.erp.auth.service.UserService;
import com.sme.erp.enums.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_VIEW','USER_CREATE','USER_EDIT')")
    public ResponseEntity<List<UserDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(userService.getAll(keyword, status));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyAuthority('USER_VIEW','USER_CREATE','USER_EDIT')")
    public ResponseEntity<List<UserDTO>> getDeleted() {
        return ResponseEntity.ok(userService.getDeleted());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('USER_VIEW','USER_CREATE','USER_EDIT')")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.create(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @PutMapping("/{id:\\d+}/deactivate")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<UserDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('USER_RESTORE')")
    public ResponseEntity<UserDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(userService.restore(id));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('ROLE_VIEW','USER_VIEW','USER_CREATE','USER_EDIT')")
    public ResponseEntity<List<RoleDTO>> getRoles() {
        return ResponseEntity.ok(userService.getRoles());
    }
}
