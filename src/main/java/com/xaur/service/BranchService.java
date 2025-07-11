package com.xaur.service;

import com.xaur.model.Branch;
import com.xaur.model.Company;
import com.xaur.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Branch> getActiveBranches() {
        return branchRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public List<Branch> getBranchesByCompany(Company company) {
        return branchRepository.findByCompany(company);
    }

    @Transactional(readOnly = true)
    public List<Branch> getActiveBranchesByCompany(Company company) {
        return branchRepository.findByCompanyAndActive(company, true);
    }

    @Transactional(readOnly = true)
    public Optional<Branch> getBranchById(Long id) {
        return branchRepository.findById(id);
    }

    @Transactional
    public Branch createBranch(Branch branch) {
        log.info("Creating new branch: {} for company: {}", branch.getName(), branch.getCompany().getName());
        return branchRepository.save(branch);
    }

    @Transactional
    public Optional<Branch> updateBranch(Long id, Branch branchDetails) {
        log.info("Updating branch with id: {}", id);
        return branchRepository.findById(id)
                .map(branch -> {
                    branch.setName(branchDetails.getName());
                    branch.setCompany(branchDetails.getCompany());
                    branch.setActive(branchDetails.isActive());
                    branch.setExternalCode(branchDetails.getExternalCode());
                    branch.setExternalId(branchDetails.getExternalId());
                    return branchRepository.save(branch);
                });
    }
}