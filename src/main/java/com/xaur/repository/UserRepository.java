package com.xaur.repository;

import com.xaur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
    List<User> findByDeviceSerialNumber(String deviceSerialNumber);


    Optional<User> findByUserIdAndDeviceSerialNumber(String userId, String deviceSerialNumber);

}