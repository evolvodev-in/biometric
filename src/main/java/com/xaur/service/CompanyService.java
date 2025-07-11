package com.xaur.service;

import com.xaur.dto.CompanyDto;
import com.xaur.dto.CompanyResponse;
import com.xaur.model.Company;
import com.xaur.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Company> getActiveCompanies() {
        return companyRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    @Transactional
    public ResponseEntity<?> createCompany(CompanyDto companyDto) {
        log.info("Creating new company: {}", companyDto.getName());
     Company company=   convertToEntity(companyDto);
        try{
            if(company.getShortCode().equals(null)|| company.getName().equals(null))
                throw new Exception("required fields are empty");
            else

            companyRepository.save(company);

        }
        catch (Exception e){
            System.out.println("error created");
            Map<String,Object> map=new HashMap<>();
            map.put("message","Error Creating Company");
            map.put("error_code",HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("data",convertToResponse(company));
        map.put("message","success");
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @Transactional
    public Optional<Company> updateCompany(Long id, Company companyDetails) {
        log.info("Updating company with id: {}", id);
        return companyRepository.findById(id)
                .map(company -> {
                    company.setName(companyDetails.getName());
                    company.setShortCode(companyDetails.getShortCode());
                    company.setActive(companyDetails.isActive());
                    company.setExternalCode(companyDetails.getExternalCode());
                    company.setExternalId(companyDetails.getExternalId());
                    return companyRepository.save(company);
                });
    }



    private CompanyResponse convertToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .shortCode(company.getShortCode())
                .active(company.isActive())
                .externalCode(company.getExternalCode())
                .externalId(company.getExternalId())
                .build();
    }
    private Company convertToEntity(CompanyDto dto) {
        return Company.builder()
                .id(dto.getId())
                .name(dto.getName())
                .shortCode(dto.getShortCode())
                .active(dto.isActive())
                .externalCode(dto.getExternalCode())
                .externalId(dto.getExternalId())
                .build();
    }
}