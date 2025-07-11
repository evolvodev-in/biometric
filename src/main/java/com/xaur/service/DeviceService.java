package com.xaur.service;

import com.xaur.model.Branch;
import com.xaur.model.Company;
import com.xaur.model.Device;
import com.xaur.model.DeviceStatus;
import com.xaur.repository.DeviceRepository;
import com.xaur.repository.DeviceStatusRepository;

import com.xaur.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final TokenGenerator tokenGenerator;
    private final DeviceStatusRepository deviceStatusRepository;


    @Transactional(readOnly = true)
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Device> getDevicesByCompany(Company company) {
        return deviceRepository.findByCompany(company);
    }

    @Transactional(readOnly = true)
    public List<Device> getDevicesByBranch(Branch branch) {
        return deviceRepository.findByBranch(branch);
    }

    @Transactional(readOnly = true)
    public Optional<Device> getDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber);
    }

    @Transactional(readOnly = true)
    public Optional<Device> getDeviceBySerialNumberAndCompany(String serialNumber, Company company) {
        return deviceRepository.findBySerialNumberAndCompany(serialNumber, company);
    }

    @Transactional(readOnly = true)
    public Optional<Device> getDeviceBySerialNumberAndCompanyAndBranch(String serialNumber, Company company, Branch branch) {
        return deviceRepository.findBySerialNumberAndCompanyAndBranch(serialNumber, company, branch);
    }
    @Transactional(readOnly = true)
    public List<Device> getDeviceByCompanyAndBranch(Company company,Branch branch){
      return  deviceRepository.findByCompanyAndBranch(company,branch);
    }

    @Transactional
    public Device registerDevice(String serialNumber, String terminalType, String cloudId, Company company, Branch branch) {
        log.info("Registering device with serial number: {} for company: {} and branch: {}",
                serialNumber, company != null ? company.getName() : "N/A", branch != null ? branch.getName() : "N/A");

        Optional<Device> existingDevice = deviceRepository.findBySerialNumber(serialNumber);
        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            if(company == null){
                company = device.getCompany();
            }
            if(branch ==null){
                branch = device.getBranch();
            }
        }


        // device status lo serial
        Optional<DeviceStatus> deviceStatus=deviceStatusRepository.findByDeviceSerialNo(serialNumber);
        DeviceStatus deviceStatus1=new DeviceStatus();
        if(deviceStatus.isPresent()){
            deviceStatus1=deviceStatus.get();
            deviceStatus1.setBranch(branch);
            deviceStatus1.setCompany(company);
            deviceStatusRepository.save(deviceStatus1);
        }
        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();

            String token = tokenGenerator.generateToken();
            device.setToken(token);
            device.setTerminalType(terminalType);
            device.setCloudId(cloudId);
            device.setCompany(company);
            device.setBranch(branch);
            device.setRegistered(true);
            device.setLoggedIn(false);
            device.setLastConnectionTime(LocalDateTime.now());

            return deviceRepository.save(device);
        } else {

            String token = tokenGenerator.generateToken();
            Device device = Device.builder()
                    .serialNumber(serialNumber)
                    .terminalType(terminalType)
                    .cloudId(cloudId)
                    .company(company)
                    .branch(branch)
                    .token(token)
                    .registered(true)
                    .loggedIn(false)
                    .lastConnectionTime(LocalDateTime.now())
                    .build();




            return deviceRepository.save(device);
        }

    }

    @Transactional
    public boolean loginDevice(String serialNumber, String token) {
        log.info("Logging in device with serial number: {}", serialNumber);

        Optional<Device> deviceOpt = deviceRepository.findBySerialNumberAndToken(serialNumber, token);

        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setLoggedIn(true);
            device.setLastActivityTime(LocalDateTime.now());
            deviceRepository.save(device);
            return true;
        }

        return false;
    }

    @Transactional
    public void updateDeviceActivity(String serialNumber) {
        deviceRepository.findBySerialNumber(serialNumber).ifPresent(device -> {
            device.setLastActivityTime(LocalDateTime.now());
            deviceRepository.save(device);
        });
    }

    @Transactional
    public void disconnectDevice(String serialNumber) {
        log.info("Disconnecting device with serial number: {}", serialNumber);

        deviceRepository.findBySerialNumber(serialNumber).ifPresent(device -> {
            device.setLoggedIn(false);
            deviceRepository.save(device);
        });
        deviceStatusRepository.findByDeviceSerialNo(serialNumber).ifPresent(e->{e.setOnline(false);deviceStatusRepository.save(e);});
    }

    @Transactional
    public Optional<Device> updateDeviceCompanyAndBranch(Long deviceId, Company company, Branch branch) {
        log.info("Updating device {} with company {} and branch {}",
                deviceId, company.getName(), branch != null ? branch.getName() : "N/A");

        return deviceRepository.findById(deviceId)
                .map(device -> {
                    device.setCompany(company);
                    device.setBranch(branch);
                    return deviceRepository.save(device);
                });
    }


}