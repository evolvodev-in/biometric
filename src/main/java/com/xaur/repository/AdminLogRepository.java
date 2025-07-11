package com.xaur.repository;

import com.xaur.model.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    List<AdminLog> findByDeviceSerialNumber(String deviceSerialNumber);
    List<AdminLog> findByAdminId(String adminId);
    List<AdminLog> findByUserId(String userId);
    List<AdminLog> findByLogTimeBetween(LocalDateTime start, LocalDateTime end);
}