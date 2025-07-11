package com.xaur.repository;

import com.xaur.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByDeptNo(Integer deptNo);
    List<Department> findByDeviceSerialNumber(String deviceSreialNumber);
    Optional<Department> findByDeptNoAndDeviceSerialNumber(Integer deptNo,String deviceSerialNumber);


}