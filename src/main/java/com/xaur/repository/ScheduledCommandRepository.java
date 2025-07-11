package com.xaur.repository;

import com.xaur.model.ScheduledCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledCommandRepository extends JpaRepository<ScheduledCommand, Long> {

    List<ScheduledCommand> findByDeviceSerialNumber(String deviceSerialNumber);

    List<ScheduledCommand> findByStatusAndScheduledForBefore(String status, LocalDateTime dateTime);
    List<ScheduledCommand> findByStatus(String status);
}