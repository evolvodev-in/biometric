package com.xaur.repository;

import com.xaur.model.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, String> {

    Optional<DeviceStatus> findByDeviceSerialNo(String deviceSerialNo);
}