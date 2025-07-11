package com.xaur.service;

import com.xaur.dto.DepartmentDto;
import com.xaur.dto.DepartmentResponse;
import com.xaur.model.Department;
import com.xaur.repository.DepartmentRepository;
import com.xaur.util.CustomMap;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {
    private final Map<String, Integer> pendingDepartmentRequests = new ConcurrentHashMap<>();
    private final DepartmentRepository departmentRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;


    @Transactional
    public Department saveDepartment(Department department) {
      //  Optional<Department> _department = departmentRepository.findByDeptNo(department.getDeptNo());

       // _department.ifPresent(value -> department.setId(value.getId()));
        return departmentRepository.save(department);
    }

    public Optional<Department> getDepartmentByDeptNo(Integer deptNo) {
        return departmentRepository.findByDeptNo(deptNo);
    }

    public List<DepartmentDto> getDepartmentByDeviceSerialNumber(String deviceSerialNumber){
        return departmentRepository.findByDeviceSerialNumber(deviceSerialNumber).stream().
                map(this::convertToDto).collect(Collectors.toList());
    }



    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department updateDepartment(Integer deptNo, String name, String deviceSerialNo) {
        Department department = departmentRepository.findByDeptNoAndDeviceSerialNumber(deptNo,deviceSerialNo)
                .orElse(new Department());

        department.setDeptNo(deptNo);
        department.setName(name);
        department.setDeviceSerialNumber(deviceSerialNo);





        return departmentRepository.save(department);
    }

    
    public DepartmentResponse requestDepartment(String deviceSerialNumber, Integer deptNo) {
        if (deptNo < 0 || deptNo > 29) {
            return DepartmentResponse.builder()
                    .status("error")
                    .message("Department number must be between 0 and 29")
                    .build();
        }

        
        Optional<Department> existingDepartment = getDepartmentByDeptNo(deptNo);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingDepartment.isPresent()) {
                Department department = existingDepartment.get();
                return DepartmentResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(department))
                        .build();
            } else {
                return DepartmentResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetDepartmentRequest(deptNo);
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingDepartment.isPresent()) {
                Department department = existingDepartment.get();
                pendingDepartmentRequests.put(deviceSerialNumber, deptNo);
                return DepartmentResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(department))
                        .build();
            } else {
                pendingDepartmentRequests.put(deviceSerialNumber, deptNo);
                return DepartmentResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return DepartmentResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }


    public List<DepartmentDto> getAllDepartmentDtos() {
        return getAllDepartments().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DepartmentDto convertToDto(Department department) {
        return DepartmentDto.builder()
                .deptNo(department.getDeptNo())
                .name(department.getName())
                .build();
    }
    public void requestAllDepartmentsFromDevice(String deviceSerialNumber) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null) {
            for (int deptNo = 0; deptNo <= 29; deptNo++) {
                pendingDepartmentRequests.put(deviceSerialNumber, deptNo);
                String message = messageBuilder.buildGetDepartmentRequest(deptNo);
                sessionManager.sendMessageToDevice(deviceSerialNumber, message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Requested all departments from device: {}", deviceSerialNumber);
        } else {
            log.warn("Device not connected: {}", deviceSerialNumber);
        }
    }

    public Integer getPendingDepartmentRequest(String deviceSerialNo) {
        return pendingDepartmentRequests.get(deviceSerialNo);
    }

    public void clearPendingDepartmentRequest(String deviceSerialNo) {
        pendingDepartmentRequests.remove(deviceSerialNo);
    }
    public Map<String, String> setDepartment(String deviceSerialNumber, Integer deptNo, String name) {
        if (deptNo < 0 || deptNo > 29) {
            return CustomMap.of(
                    "status", "error",
                    "message", "Department number must be between 0 and 29"
            );
        }

        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            );
        }

        String message = messageBuilder.buildSetDepartmentCommand(deptNo, name);
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        System.out.println(deviceSerialNumber);

        if (sent) {
            // Save to database
            Department department = departmentRepository.findByDeptNoAndDeviceSerialNumber(deptNo,deviceSerialNumber).orElse(new Department());

            department.setDeptNo(deptNo);
            department.setName(name);
            department.setDeviceSerialNumber(deviceSerialNumber);
            saveDepartment(department);

            return CustomMap.of(
                    "status", "success",
                    "message", "Department set command sent to device"
            );
        } else {
            return CustomMap.of(
                    "status", "error",
                    "message", "Failed to send department set command to device"
            );
        }
    }
}