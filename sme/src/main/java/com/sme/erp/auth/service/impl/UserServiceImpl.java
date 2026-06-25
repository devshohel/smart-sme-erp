package com.sme.erp.auth.service.impl;

import com.sme.erp.auth.dto.RoleDTO;
import com.sme.erp.auth.dto.UserDTO;
import com.sme.erp.auth.entity.Role;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RoleRepository;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.auth.service.RefreshTokenService;
import com.sme.erp.auth.service.UserService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            ActivityLogService activityLogService,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAll(String keyword, Status status) {
        return userRepository.search(RequestValueUtils.normalize(keyword), status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getDeleted() {
        return userRepository.findDeletedUsers()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        return toDto(findUser(id));
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        normalize(dto);
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        validateUnique(dto, null);

        User user = new User();
        apply(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDeleted(false);
        UserDTO saved = toDto(userRepository.save(user));
        activityLogService.log("USER_CREATE", "USER", "users", saved.getId(), "Created user " + saved.getUsername());
        auditLogService.log("users", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        normalize(dto);
        User user = findUser(id);
        UserDTO oldData = toDto(user);
        validateUnique(dto, id);
        apply(dto, user);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            invalidateUserSessions(user);
        }
        UserDTO saved = toDto(userRepository.save(user));
        activityLogService.log("USER_UPDATE", "USER", "users", saved.getId(), "Updated user " + saved.getUsername());
        auditLogService.log("users", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public UserDTO deactivate(Long id) {
        User user = findUser(id);
        UserDTO oldData = toDto(user);
        user.setStatus(Status.INACTIVE);
        invalidateUserSessions(user);
        UserDTO saved = toDto(userRepository.save(user));
        activityLogService.log("USER_UPDATE", "USER", "users", saved.getId(), "Deactivated user " + saved.getUsername());
        auditLogService.log("users", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findUser(id);
        UserDTO oldData = toDto(user);
        invalidateUserSessions(user);
        userRepository.delete(user);
        activityLogService.log("USER_DELETE", "USER", "users", id, "Deleted user " + oldData.getUsername());
        auditLogService.log("users", id, auditLogService.toJson(oldData), null, "DELETE");
    }

    @Override
    @Transactional
    public UserDTO restore(Long id) {
        int updated = userRepository.restoreById(id);
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        UserDTO restored = toDto(findUser(id));
        activityLogService.log("USER_RESTORE", "USER", "users", id, "Restored user " + restored.getUsername());
        auditLogService.log("users", id, null, auditLogService.toJson(restored), "RESTORE");
        return restored;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new RoleDTO(role.getId(), role.getRoleName(), role.getDescription()))
                .collect(Collectors.toList());
    }

    private void normalize(UserDTO dto) {
        dto.setName(RequestValueUtils.normalizeRequired(dto.getName(), "Name"));
        dto.setUsername(RequestValueUtils.normalizeRequired(dto.getUsername(), "Username"));
        dto.setEmail(RequestValueUtils.normalizeRequired(dto.getEmail(), "Email"));
        dto.setPhone(RequestValueUtils.normalize(dto.getPhone()));
    }

    private void apply(UserDTO dto, User user) {
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(findRole(dto.getRoleId()));
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
    }

    private void validateUnique(UserDTO dto, Long currentId) {
        boolean usernameExists = currentId == null
                ? userRepository.existsByUsername(dto.getUsername())
                : userRepository.existsByUsernameAndIdNot(dto.getUsername(), currentId);
        if (usernameExists) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }

        boolean emailExists = currentId == null
                ? userRepository.existsByEmail(dto.getEmail())
                : userRepository.existsByEmailAndIdNot(dto.getEmail(), currentId);
        if (emailExists) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
    }

    private Role findRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void invalidateUserSessions(User user) {
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        if (user.getId() != null) {
            refreshTokenService.revokeAllForUser(user.getId());
        }
    }

    private UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRoleId(user.getRole().getId());
        dto.setRoleName(user.getRole().getRoleName());
        dto.setStatus(user.getStatus());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
