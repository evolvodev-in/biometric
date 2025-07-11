package com.xaur.controller;

import com.xaur.dto.DepartmentDto;
import com.xaur.dto.DepartmentResponse;
import com.xaur.dto.DeviceAdditionalInfoDto;
import com.xaur.dto.DeviceAdditionalInfoResponse;
import com.xaur.dto.DeviceEthernetSettingsDto;
import com.xaur.dto.DeviceEthernetSettingsResponse;
import com.xaur.dto.DeviceLogStatusResponse;
import com.xaur.dto.DeviceWifiSettingsDto;
import com.xaur.dto.DeviceWifiSettingsResponse;
import com.xaur.model.Branch;
import com.xaur.model.Company;
import com.xaur.model.Device;
import com.xaur.model.DeviceAdditionalInfo;
import com.xaur.model.DeviceStatus;
import com.xaur.repository.BranchRepository;
import com.xaur.repository.CompanyRepository;
import com.xaur.service.BranchService;
import com.xaur.service.CompanyService;
import com.xaur.service.DepartmentService;
import com.xaur.service.DeviceAdditionalInfoService;
import com.xaur.service.DeviceEthernetSettingsService;
import com.xaur.service.DeviceLogStatusService;
import com.xaur.service.DeviceService;
import com.xaur.service.DeviceStatusService;
import com.xaur.service.DeviceWifiSettingsService;
import com.xaur.service.TimeLogService;
import com.xaur.util.CustomMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Controller", description = "API for managing devices and their associated data")
@SecurityRequirement(name = "Bearer Authentication")
public class DeviceController {

    private final DeviceStatusService deviceStatusService;
    private final DeviceLogStatusService deviceLogStatusService;
    private final DeviceAdditionalInfoService deviceAdditionalInfoService;
    private final DeviceWifiSettingsService deviceWifiSettingsService;
    private final DeviceEthernetSettingsService deviceEthernetSettingsService;
    private final DepartmentService departmentService;
    private final TimeLogService timeLogService;
    private final CompanyService companyService;
    private final BranchService branchService;
    private final DeviceService deviceService;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;



    @Operation(summary = "Get all devices", description = "Retrieve a list of all devices")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved devices", content = @Content(schema = @Schema(implementation = DeviceStatus.class)))
    @GetMapping
    public ResponseEntity<?> getAllDevices() {



        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @Operation(summary = "Get devices by company", description = "Retrieve a list of devices for a specific company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved devices"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Device>> getDevicesByCompany(
            @Parameter(description = "ID of the company", required = true) @PathVariable Long companyId) {
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        return companyOpt.map(company -> ResponseEntity.ok(deviceService.getDevicesByCompany(company))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get devices by branch", description = "Retrieve a list of devices for a specific branch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved devices"),
            @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Device>> getDevicesByBranch(
            @Parameter(description = "ID of the branch", required = true) @PathVariable Long branchId) {
        Optional<Branch> branchOpt = branchService.getBranchById(branchId);
        if (!branchOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deviceService.getDevicesByBranch(branchOpt.get()));
    }

    @Operation(summary = "Get device by serial number, company and branch", description = "Retrieve a device by its serial number, company and branch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved device"),
            @ApiResponse(responseCode = "404", description = "Device, company or branch not found")
    })
    @GetMapping("/company/{companyId}/branch/{branchId}")
    public ResponseEntity<?> getDeviceBySerialNumberCompanyAndBranch(
           // @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNo,
            @Parameter(description = "ID of the company", required = true) @PathVariable Long companyId,
            @Parameter(description = "ID of the branch", required = true) @PathVariable Long branchId) {
        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (!companyOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Branch> branchOpt = branchService.getBranchById(branchId);
        if (!branchOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(
                deviceService.getDeviceByCompanyAndBranch(companyOpt.get(),branchOpt.get()));

    }

    @Operation(summary = "Get device status", description = "Retrieve the status of a specific device by its serial number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved device status", content = @Content(schema = @Schema(implementation = DeviceStatus.class))),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{deviceSerialNo}")
    public ResponseEntity<DeviceStatus> getDeviceStatus(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNo) {
        return deviceStatusService.getDeviceStatus(deviceSerialNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Register device with company and branch", description = "Register a new device with company and branch association")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device registered successfully"),
            @ApiResponse(responseCode = "404", description = "Company or branch not found")
    })
    @PostMapping("/register")
    public ResponseEntity<Device> registerDevice(
            @RequestParam String serialNumber,
            @RequestParam(required = false) String terminalType,
            @RequestParam(required = false) String cloudId,
            @RequestParam Long companyId,
            @RequestParam(required = false) Long branchId, HttpServletRequest request) {

        System.out.println(serialNumber);

        Optional<Company> companyOpt = companyService.getCompanyById(companyId);
        if (!companyOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Branch branch = null;
        if (branchId != null) {
            Optional<Branch> branchOpt = branchService.getBranchById(branchId);
            if (!branchOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            branch = branchOpt.get();
        }



        Device device = deviceService.registerDevice(serialNumber, terminalType, cloudId, companyOpt.get(), branch);


        Map<String,Object> map=new HashMap<>();
        map.put("data",device);
        map.put("message","Device Registered with"+companyOpt.get().getName());
        return new ResponseEntity<>(device, HttpStatus.CREATED);
    }

    @Operation(summary = "Query device status", description = "Initiate a query to retrieve the status of a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device status query initiated successfully", content = @Content(schema = @Schema(example = "Device status query initiated"))),
            @ApiResponse(responseCode = "400", description = "Failed to query device status")
    })
    @PostMapping("/{deviceSerialNo}/query-status")
    public ResponseEntity<String> queryDeviceStatus(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNo) {
        boolean queried = deviceStatusService.queryDeviceStatus(deviceSerialNo);
        if (queried) {
            return ResponseEntity.ok("Device status query initiated");
        } else {
            return ResponseEntity.badRequest().body("Failed to query device status");
        }
    }

    @Operation(summary = "Get device log status", description = "Retrieve the log status of a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved device log status", content = @Content(schema = @Schema(implementation = DeviceLogStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve device log status")
    })
    @GetMapping("/{deviceSerialNumber}/logs/status")
    public ResponseEntity<DeviceLogStatusResponse> getDeviceLogStatus(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {
        DeviceLogStatusResponse response = deviceLogStatusService.requestDeviceLogStatus(deviceSerialNumber);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

//    @Operation(summary = "Get all device log statuses", description = "Retrieve the log statuses of all devices")
//    @ApiResponse(responseCode = "200", description = "Successfully retrieved device log statuses", content = @Content(schema = @Schema(implementation = DeviceLogStatusDto.class)))
//    @GetMapping("/logs/status")
//    public ResponseEntity<List<DeviceLogStatusDto>> getAllDeviceLogStatus() {
//        List<DeviceLogStatus> statusList = deviceLogStatusService.getAllDeviceLogStatus();
//        List<DeviceLogStatusDto> dtoList = statusList.stream()
//                .map(deviceLogStatusService::convertToDto)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(dtoList);
//    }

    @Operation(summary = "Get device additional info", description = "Retrieve additional information for a specific device and parameter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved device additional info", content = @Content(schema = @Schema(implementation = DeviceAdditionalInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve device additional info")
    })
    @GetMapping("/{deviceSerialNumber}/info/{paramName}")
    public ResponseEntity<DeviceAdditionalInfoResponse> getDeviceAdditionalInfo(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "Name of the parameter to retrieve", required = true) @PathVariable String paramName) {

        DeviceAdditionalInfoResponse response = deviceAdditionalInfoService.requestDeviceAdditionalInfo(
                deviceSerialNumber, paramName);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Get all device additional info", description = "Retrieve all additional information for a specific device")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved device additional info", content = @Content(schema = @Schema(implementation = DeviceAdditionalInfoDto.class)))
    @GetMapping("/{deviceSerialNumber}/info")
    public ResponseEntity<List<DeviceAdditionalInfoDto>> getDeviceAllAdditionalInfo(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {

        List<DeviceAdditionalInfo> infoList = deviceAdditionalInfoService.getDeviceAdditionalInfoBySerialNumber(deviceSerialNumber);

        if (infoList.isEmpty()) {
            deviceAdditionalInfoService.requestAllDeviceAdditionalInfo(deviceSerialNumber);
        }

        List<DeviceAdditionalInfoDto> dtoList = infoList.stream()
                .map(info -> DeviceAdditionalInfoDto.builder()
                        .deviceSerialNumber(info.getDeviceSerialNumber())
                        .terminalType(info.getTerminalType())
                        .terminalId(info.getTerminalId())
                        .paramName(info.getParamName())
                        .value1(info.getValue1())
                        .value2(info.getValue2())
                        .value3(info.getValue3())
                        .value4(info.getValue4())
                        .value5(info.getValue5())
                        .lastSyncTime(info.getLastSyncTime() != null ?
                                info.getLastSyncTime().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

//    @Operation(summary = "Get all devices' additional info", description = "Retrieve all additional information for all devices")
//    @ApiResponse(responseCode = "200", description = "Successfully retrieved all devices' additional info", content = @Content(schema = @Schema(implementation = DeviceAdditionalInfoDto.class)))
//    @GetMapping("/info")
//    public ResponseEntity<List<DeviceAdditionalInfoDto>> getAllDeviceAdditionalInfo() {
//        return ResponseEntity.ok(deviceAdditionalInfoService.getAllDeviceAdditionalInfoDtos());
//    }

    @Operation(summary = "Get device ethernet settings", description = "Retrieve the ethernet settings for a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved device ethernet settings", content = @Content(schema = @Schema(implementation = DeviceEthernetSettingsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve device ethernet settings")
    })
    @GetMapping("/{deviceSerialNumber}/ethernet")
    public ResponseEntity<DeviceEthernetSettingsResponse> getDeviceEthernetSettings(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {

        DeviceEthernetSettingsResponse response = deviceEthernetSettingsService.requestEthernetSettings(deviceSerialNumber);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

//    @Operation(summary = "Get all devices' ethernet settings", description = "Retrieve the ethernet settings for all devices")
//    @ApiResponse(responseCode = "200", description = "Successfully retrieved all devices' ethernet settings", content = @Content(schema = @Schema(implementation = DeviceEthernetSettingsDto.class)))
//    @GetMapping("/ethernet")
//    public ResponseEntity<List<DeviceEthernetSettingsDto>> getAllDeviceEthernetSettings() {
//        return ResponseEntity.ok(deviceEthernetSettingsService.getAllEthernetSettingsDtos());
//    }

    @Operation(summary = "Get device wifi settings", description = "Retrieve the wifi settings for a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved device wifi settings", content = @Content(schema = @Schema(implementation = DeviceWifiSettingsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve device wifi settings")
    })
    @GetMapping("/{deviceSerialNumber}/wifi")
    public ResponseEntity<DeviceWifiSettingsResponse> getDeviceWifiSettings(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {

        DeviceWifiSettingsResponse response = deviceWifiSettingsService.requestWifiSettings(deviceSerialNumber);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    /*@Operation(summary = "Get all devices' wifi settings", description = "Retrieve the wifi settings for all devices")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all devices' wifi settings", content = @Content(schema = @Schema(implementation = DeviceWifiSettingsDto.class)))
    @GetMapping("/wifi")
    public ResponseEntity<List<DeviceWifiSettingsDto>> getAllDeviceWifiSettings() {
        return ResponseEntity.ok(deviceWifiSettingsService.getAllWifiSettingsDtos());
    }*/

    @Operation(summary = "Get department info", description = "Retrieve department information for a specific device and department number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved department info", content = @Content(schema = @Schema(implementation = DepartmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve department info")
    })
    @GetMapping("/{deviceSerialNumber}/departments/{deptNo}")
    public ResponseEntity<DepartmentResponse> getDepartment(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "Department number to retrieve", required = true) @PathVariable Integer deptNo) {

        DepartmentResponse response = departmentService.requestDepartment(deviceSerialNumber, deptNo);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Get all departments from a device", description = "Retrieve all department information from a specific device")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved departments", content = @Content(schema = @Schema(implementation = DepartmentDto.class)))
    @GetMapping("/{deviceSerialNumber}/departments")
    public ResponseEntity<List<DepartmentDto>> getAllDepartmentsFromDevice(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {

        departmentService.requestAllDepartmentsFromDevice(deviceSerialNumber);
        //List<DepartmentDto> departments = departmentService.getAllDepartmentDtos();
        List<DepartmentDto> departments=departmentService.getDepartmentByDeviceSerialNumber(deviceSerialNumber);
        return ResponseEntity.ok(departments);
    }
    @Operation(summary = "Set device time", description = "Set the time on a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Time set command sent successfully"),
            @ApiResponse(responseCode = "400", description = "Device is not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send time set command")
    })
    @PostMapping("/{deviceSerialNumber}/time")
    public ResponseEntity<Map<String, String>> setDeviceTime(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "Time string in format yyyy-MM-dd-'T'HH:mm:ss'Z'") @RequestParam(required = false) String timeString) {

        Map<String, String> response = timeLogService.setDeviceTime(deviceSerialNumber, timeString);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Get device time", description = "Get the current time from a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Time request sent successfully"),
            @ApiResponse(responseCode = "400", description = "Device is not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send time request")
    })
    @GetMapping("/{deviceSerialNumber}/time")
    public ResponseEntity<Map<String, String>> getDeviceTime(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {

        Map<String, String> response = timeLogService.getDeviceTime(deviceSerialNumber);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Empty time logs", description = "Clear all time logs from a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empty time logs command sent successfully"),
            @ApiResponse(responseCode = "400", description = "Device is not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send empty time logs command")
    })
    @PostMapping("/{deviceSerialNumber}/logs/empty")
    public ResponseEntity<Map<String, String>> emptyTimeLogs(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {

        boolean success = timeLogService.emptyTimeLogs(deviceSerialNumber);

        if (success) {
            return ResponseEntity.ok(CustomMap.of(
                    "status", "success",
                    "message", "Empty time logs command sent to device"
            ));
        } else {
            return ResponseEntity.badRequest().body(CustomMap.of(
                    "status", "error",
                    "message", "Failed to send empty time logs command to device"
            ));
        }
    }

    @Operation(summary = "Set department", description = "Set department information on a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department set command sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or device not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send department set command")
    })
    @PostMapping("/{deviceSerialNumber}/departments")
    public ResponseEntity<Map<String, String>> setDepartment(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "Department number (0-29)", required = true) @RequestParam Integer deptNo,
            @Parameter(description = "Department name", required = true) @RequestParam String name) {

        Map<String, String> response = departmentService.setDepartment(deviceSerialNumber, deptNo, name);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Set WiFi settings", description = "Configure WiFi settings on a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "WiFi settings command sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or device not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send WiFi settings command")
    })
    @PostMapping("/{deviceSerialNumber}/wifi")
    public ResponseEntity<Map<String, String>> setWifiSettings(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @RequestBody DeviceWifiSettingsDto settings) {

        Map<String, String> response = deviceWifiSettingsService.setWifiSettings(deviceSerialNumber, settings);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Set Ethernet settings", description = "Configure Ethernet settings on a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ethernet settings command sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or device not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send Ethernet settings command")
    })
    @PostMapping("/{deviceSerialNumber}/ethernet")
    public ResponseEntity<Map<String, String>> setEthernetSettings(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @RequestBody DeviceEthernetSettingsDto settings) {

        Map<String, String> response = deviceEthernetSettingsService.setEthernetSettings(deviceSerialNumber, settings);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
}