package com.xaur.repository;

import com.xaur.model.DeviceLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceLogStatusRepository extends JpaRepository<DeviceLogStatus, Long> {

    Optional<DeviceLogStatus> findByDeviceSerialNumber(String deviceSerialNumber);
}