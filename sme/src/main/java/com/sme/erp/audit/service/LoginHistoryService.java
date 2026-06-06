package com.sme.erp.audit.service;

import com.sme.erp.audit.dto.LoginHistoryDTO;
import com.sme.erp.auth.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryService {
    void success(User user);

    void failed(String username, String failureReason);

    List<LoginHistoryDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action);
}
