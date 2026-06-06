package com.sme.erp.audit.service.impl;

import com.sme.erp.audit.dto.LoginHistoryDTO;
import com.sme.erp.audit.entity.LoginHistory;
import com.sme.erp.audit.repository.LoginHistoryRepository;
import com.sme.erp.audit.service.AuditRequestContext;
import com.sme.erp.audit.service.LoginHistoryService;
import com.sme.erp.auth.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginHistoryServiceImpl implements LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;
    private final AuditRequestContext requestContext;

    public LoginHistoryServiceImpl(LoginHistoryRepository loginHistoryRepository, AuditRequestContext requestContext) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.requestContext = requestContext;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(User user) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setUsername(user.getUsername());
        history.setStatus("SUCCESS");
        history.setIpAddress(requestContext.ipAddress());
        history.setUserAgent(requestContext.userAgent());
        loginHistoryRepository.save(history);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(String username, String failureReason) {
        LoginHistory history = new LoginHistory();
        history.setUsername(username != null && !username.isBlank() ? username : "(blank)");
        history.setStatus("FAILED");
        history.setIpAddress(requestContext.ipAddress());
        history.setUserAgent(requestContext.userAgent());
        history.setFailureReason(failureReason);
        loginHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginHistoryDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action) {
        return loginHistoryRepository.search(fromDate, toDate, normalize(username), normalize(action))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private LoginHistoryDTO toDto(LoginHistory history) {
        User user = history.getUser();
        return new LoginHistoryDTO(
                history.getId(),
                user != null ? user.getId() : null,
                history.getUsername(),
                history.getStatus(),
                history.getIpAddress(),
                history.getUserAgent(),
                history.getFailureReason(),
                history.getCreatedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
