package com.xaur.repository;

import com.xaur.model.DeviceWifiSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceWifiSettingsRepository extends JpaRepository<DeviceWifiSettings, Long> {

    Optional<DeviceWifiSettings> findByDeviceSerialNumber(String deviceSerialNumber);
}