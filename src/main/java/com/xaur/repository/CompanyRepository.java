package com.xaur.repository;

import com.xaur.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByShortCode(String shortCode);
    List<Company> findByActive(boolean active);
    Optional<Company> findByExternalId(String externalId);
}