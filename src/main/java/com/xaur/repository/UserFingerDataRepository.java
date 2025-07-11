package com.xaur.repository;

import com.xaur.model.UserFingerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserFingerDataRepository extends JpaRepository<UserFingerData, Long> {

    Optional<UserFingerData> findByUserIdAndFingerNoAndDeviceSerialNumber(
            String userId, Integer fingerNo, String deviceSerialNumber);
    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "delete from user_finger_data where user_id=:userId and device_serial_number=:deviceSerialNumber")
    void deleteByUserIdAndDeviceSerialNumber(String userId, String deviceSerialNumber);
}