package com.xaur.repository;

import com.xaur.model.Branch;
import com.xaur.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByCompany(Company company);
    List<Branch> findByCompanyAndActive(Company company, boolean active);
    Optional<Branch> findByExternalId(String externalId);
    List<Branch> findByActive(boolean active);
}