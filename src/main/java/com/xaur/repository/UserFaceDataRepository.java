package com.xaur.repository;

import com.xaur.model.UserFaceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFaceDataRepository extends JpaRepository<UserFaceData, Long> {

    Optional<UserFaceData> findByUserIdAndDeviceSerialNumber(String userId, String deviceSerialNumber);
}