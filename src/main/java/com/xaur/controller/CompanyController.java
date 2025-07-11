package com.xaur.controller;

import com.xaur.dto.CompanyDto;
import com.xaur.dto.CompanyResponse;
import com.xaur.model.Company;
import com.xaur.model.Device;
import com.xaur.repository.DeviceRepository;
import com.xaur.repository.TimeLogRepository;
import com.xaur.repository.UserRepository;
import com.xaur.service.CompanyService;
import com.xaur.util.CustomMap;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CompanyController {

    private final CompanyService companyService;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private  final TimeLogRepository timeLogRepository;

    @GetMapping
    public ResponseEntity<?> getAllCompanies() {
        List<CompanyResponse> companies = companyService.getAllCompanies().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        Map<String,Object> map=new HashMap<>();
        map.put("data",companies);
        map.put("message","success");
        return ResponseEntity.ok(map);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CompanyResponse>> getActiveCompanies() {
        List<CompanyResponse> companies = companyService.getActiveCompanies().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id)
                .map(company -> ResponseEntity.ok(convertToResponse(company)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDto companyDto, BindingResult bindingResult) {
if(bindingResult.hasErrors())
{
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed"+bindingResult.getAllErrors());
}

        return companyService.createCompany(companyDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @RequestBody CompanyDto companyDto) {
        Company company = convertToEntity(companyDto);
        return companyService.updateCompany(id, company)

                .map(updatedCompany -> ResponseEntity.ok(CustomMap.of("message","Company Updated Successfully","data",convertToResponse(updatedCompany))))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/details/{id}")
    public ResponseEntity<?> details(@PathVariable("id") long id){
        if(id==0){
            List< Device> list=deviceRepository.findAll();
            int a=0;
            for(Device device:list){
                a=a+userRepository.findByDeviceSerialNumber(device.getSerialNumber()).size();

            }
            List<String> deviceNumbers=list.stream().map(Device::getSerialNumber).collect(Collectors.toList());
            Integer count=     timeLogRepository.countOfLogTime(deviceNumbers, LocalDate.now());

            return ResponseEntity.ok().body(CustomMap.of("devices",list.size(),"users",a,"logs",count));

        }


       Optional< Company> company=companyService.getCompanyById(Long.valueOf(id));
       Company company1=new Company();
       if(company.isPresent()){
           company1=company.get();
       }
       else{





           return ResponseEntity.badRequest().body("company not exist");
       }
       List< Device> list=deviceRepository.findByCompany(company1);
       int a=0;
       for (Device device:list){
       a=a+    userRepository.findByDeviceSerialNumber(device.getSerialNumber()).size();

       }
       List<String> deviceNumbers=list.stream().map(Device::getSerialNumber).collect(Collectors.toList());
  Integer count=     timeLogRepository.countOfLogTime(deviceNumbers, LocalDate.now());

       return ResponseEntity.ok().body(CustomMap.of("devices",list.size(),"users",a,"logs",count));


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


}