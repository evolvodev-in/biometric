package com.xaur.controller;

import com.xaur.dto.BranchDto;
import com.xaur.dto.BranchResponse;
import com.xaur.dto.CompanyResponse;
import com.xaur.model.Branch;
import com.xaur.model.Company;
import com.xaur.service.BranchService;
import com.xaur.service.CompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class BranchController {

    private final BranchService branchService;
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches() {
        List<BranchResponse> branches = branchService.getAllBranches().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BranchResponse>> getActiveBranches() {
        List<BranchResponse> branches = branchService.getActiveBranches().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<BranchResponse>> getBranchesByCompany(@PathVariable Long companyId) {
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (!companyOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<BranchResponse> branches = branchService.getBranchesByCompany(companyOpt.get()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/company/{companyId}/active")
    public ResponseEntity<List<BranchResponse>> getActiveBranchesByCompany(@PathVariable Long companyId) {
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (!companyOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<BranchResponse> branches = branchService.getActiveBranchesByCompany(companyOpt.get()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable Long id) {
        return branchService.getBranchById(id)
                .map(branch -> ResponseEntity.ok(convertToResponse(branch)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody BranchDto branchDto) {
        Optional<Company> companyOpt = companyService.getCompanyById(branchDto.getCompanyId());
        if (!companyOpt.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Branch branch = convertToEntity(branchDto, companyOpt.get());
        Branch savedBranch = branchService.createBranch(branch);
        Map<String,Object> map=new HashMap<>();
        map.put("data",convertToResponse(savedBranch));
        map.put("message","branch Created succesfully");

        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, @RequestBody BranchDto branchDto) {
        Optional<Company> companyOpt = companyService.getCompanyById(branchDto.getCompanyId());
        if (!companyOpt.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Branch branch = convertToEntity(branchDto, companyOpt.get());
        Optional<Branch> branch1=branchService.updateBranch(id, branch);
        Map<String,Object> map=new HashMap<>();
         if(!branch1.isPresent()){
             map.put("message","Branch not found");
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
         }
         else {
             map.put("data",branch1.get());
             map.put("message","Branch Updated Successfully");
             return ResponseEntity.status(HttpStatus.CREATED).body(map);
         }

    }

    private Branch convertToEntity(BranchDto dto, Company company) {
        return Branch.builder()
                .id(dto.getId())
                .name(dto.getName())
                .company(company)
                .active(dto.isActive())
                .externalCode(dto.getExternalCode())
                .externalId(dto.getExternalId())
                .build();
    }

    private BranchResponse convertToResponse(Branch branch) {
        CompanyResponse companyResponse = CompanyResponse.builder()
                .id(branch.getCompany().getId())
                .name(branch.getCompany().getName())
                .shortCode(branch.getCompany().getShortCode())
                .active(branch.getCompany().isActive())
                .externalCode(branch.getCompany().getExternalCode())
                .externalId(branch.getCompany().getExternalId())
                .build();

        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .company(companyResponse)
                .active(branch.isActive())
                .externalCode(branch.getExternalCode())
                .externalId(branch.getExternalId())
                .build();
    }
}