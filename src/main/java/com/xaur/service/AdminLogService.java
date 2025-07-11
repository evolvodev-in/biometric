package com.xaur.service;

import com.xaur.model.AdminLog;
import com.xaur.repository.AdminLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    @Transactional(readOnly = true)
    public List<AdminLog> getAllLogs() {
        return adminLogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByDeviceSerialNumber(String deviceSerialNumber) {
        return adminLogRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByAdminId(String adminId) {
        return adminLogRepository.findByAdminId(adminId);
    }

    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByUserId(String userId) {
        return adminLogRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return adminLogRepository.findByLogTimeBetween(start, end);
    }

    @Transactional
    public AdminLog saveLog(AdminLog adminLog) {
        log.info("Saving admin log for admin: {}, device: {}", adminLog.getAdminId(), adminLog.getDeviceSerialNumber());
        return adminLogRepository.save(adminLog);
    }
}