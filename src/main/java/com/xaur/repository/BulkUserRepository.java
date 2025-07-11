package com.xaur.repository;

import com.xaur.model.BulkUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BulkUserRepository extends JpaRepository<BulkUser,Long> {
@Query(nativeQuery = true,value = "select * from user where status=:status limit 50")
List<BulkUser> find(String status);

@Query(nativeQuery = true,value = "select * from user where user_id=:userId and device_serial_number=:deviceSerialNumber")
Optional<BulkUser> updateStatus(String userId,String deviceSerialNumber);
}
