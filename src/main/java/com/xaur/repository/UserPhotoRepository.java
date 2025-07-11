package com.xaur.repository;

import com.xaur.model.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {

    Optional<UserPhoto> findByUserIdAndDeviceSerialNumber(String userId, String deviceSerialNumber);

}