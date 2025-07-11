package com.xaur.repository;

import com.xaur.model.Branch;
import com.xaur.model.Company;
import com.xaur.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findBySerialNumber(String serialNumber);
    Optional<Device> findBySerialNumberAndToken(String serialNumber, String token);
    Optional<Device> findBySerialNumberAndCompanyAndBranch(String serialNumber, Company company, Branch branch);
    Optional<Device> findBySerialNumberAndCompany(String serialNumber, Company company);
    List<Device> findByCompany(Company company);
    List<Device> findByBranch(Branch branch);
    boolean existsBySerialNumber(String serialNumber);
    List<Device> findByCompanyAndBranch(Company company,Branch branch);
}