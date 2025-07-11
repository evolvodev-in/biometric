package com.xaur.repository;

import com.xaur.model.DeviceEthernetSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceEthernetSettingsRepository extends JpaRepository<DeviceEthernetSettings, Long> {

    Optional<DeviceEthernetSettings> findByDeviceSerialNumber(String deviceSerialNumber);
}