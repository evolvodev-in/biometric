package com.xaur.repository;

import com.xaur.model.DeviceAdditionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceAdditionalInfoRepository extends JpaRepository<DeviceAdditionalInfo, Long> {

    Optional<DeviceAdditionalInfo> findByDeviceSerialNumberAndParamName(String deviceSerialNumber, String paramName);

    List<DeviceAdditionalInfo> findByDeviceSerialNumber(String deviceSerialNumber);
}