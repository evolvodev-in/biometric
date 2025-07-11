package com.xaur.controller;

import com.xaur.dto.*;
import com.xaur.model.ScheduledCommand;
import com.xaur.model.User;
import com.xaur.service.ScheduledCommandService;
import com.xaur.service.UserCommandService;
import com.xaur.service.UserCopyService;
import com.xaur.service.UserFaceDataService;
import com.xaur.service.UserFingerDataService;
import com.xaur.service.UserPhotoService;
import com.xaur.service.UserService;
import com.xaur.util.CustomMap;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Controller", description = "API for managing users and their associated data")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;
    private final ScheduledCommandService scheduledCommandService;
    private final UserCommandService userCommandService;
    private final UserPhotoService userPhotoService;
    private final UserFaceDataService userFaceDataService;
    private final UserFingerDataService userFingerDataService;
    private final UserCopyService userCopyService;

    @Operation(summary = "Get users by device", description = "Retrieve all users associated with a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/device/{deviceSerialNumber}")
    public ResponseEntity<List<User>> getUsersByDevice(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {
        List<User> users = userService.getUsersByDeviceSerialNumber(deviceSerialNumber);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID from device", description = "Retrieve a specific user by ID from a specific device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User or device not found")
    })
    @GetMapping("/device/{deviceSerialNumber}/user/{userId}")
    public ResponseEntity<User> getUserByIdFromDevice(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "ID of the user", required = true) @PathVariable String userId) {
        Optional<User> user = userService.getUserByIdAndDeviceSerialNumber(userId, deviceSerialNumber);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Request user data from device", description = "Send a request to a device to retrieve user data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request sent successfully", content = @Content(schema = @Schema(example = "{\"status\": \"success\", \"message\": \"Request sent to device\"}"))),
            @ApiResponse(responseCode = "400", description = "Device is not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send request to device")
    })
    @GetMapping("/device/{deviceSerialNumber}/request/{userId}")
    public ResponseEntity<Map<String, String>> requestUserFromDevice(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "ID of the user", required = true) @PathVariable String userId) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return ResponseEntity.badRequest().body(CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            ));
        }

        String message = messageBuilder.buildGetUserDataRequest(userId);
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (sent) {
            return ResponseEntity.ok(CustomMap.of(
                    "status", "success",
                    "message", "Request sent to device"
            ));
        } else {
            return ResponseEntity.internalServerError().body(CustomMap.of(
                    "status", "error",
                    "message", "Failed to send request to device"
            ));
        }
    }

    @Operation(summary = "Schedule user operation", description = "Schedule a user operation (SET or DELETE) on a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation scheduled successfully", content = @Content(schema = @Schema(implementation = ScheduledCommand.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping("/device/{deviceSerialNumber}/schedule")
    public ResponseEntity<ScheduledCommand> scheduleUserOperation(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @RequestBody UserOperationRequest request) {

        if (request.getUserId() == null || request.getOperationType() == null) {
            return ResponseEntity.badRequest().build();
        }

        String commandXml;
        if ("SET".equalsIgnoreCase(request.getOperationType())) {
            commandXml = userCommandService.buildSetUserDataCommand(request.getUserId(), request.getUserData());
        } else if ("DELETE".equalsIgnoreCase(request.getOperationType())) {
            commandXml = userCommandService.buildDeleteUserCommand(request.getUserId());
        } else {
            return ResponseEntity.badRequest().build();
        }

        ScheduledCommand command = ScheduledCommand.builder()
                .deviceSerialNumber(deviceSerialNumber)
                .commandType(request.getOperationType())
                .commandXml(commandXml)
                .status("PENDING")
                .userId(request.getUserId())
                .build();

        ScheduledCommand savedCommand = scheduledCommandService.saveCommand(command);
        return ResponseEntity.ok(savedCommand);
    }

    @Operation(summary = "Execute scheduled command", description = "Execute a previously scheduled command")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Command executed successfully", content = @Content(schema = @Schema(example = "{\"status\": \"success\", \"message\": \"Command sent to device\"}"))),
            @ApiResponse(responseCode = "404", description = "Command not found"),
            @ApiResponse(responseCode = "400", description = "Device is not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send command to device")
    })
    @PostMapping("/commands/{commandId}/execute")
    public ResponseEntity<Map<String, String>> executeCommand(
            @Parameter(description = "ID of the scheduled command", required = true) @PathVariable Long commandId) {
        Optional<ScheduledCommand> commandOpt = scheduledCommandService.getCommandById(commandId);

        if (!commandOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ScheduledCommand command = commandOpt.get();
        String deviceSerialNumber = command.getDeviceSerialNumber();

        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return ResponseEntity.badRequest().body(CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            ));
        }

        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, command.getCommandXml());

        if (sent) {
            command.setStatus("SENT");
            command.setExecutedAt(java.time.LocalDateTime.now());
            scheduledCommandService.saveCommand(command);

            return ResponseEntity.ok(CustomMap.of(
                    "status", "success",
                    "message", "Command sent to device"
            ));
        } else {
            return ResponseEntity.internalServerError().body(CustomMap.of(
                    "status", "error",
                    "message", "Failed to send command to device"
            ));
        }
    }

    @Operation(summary = "Get all scheduled commands", description = "Retrieve all scheduled commands")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved commands", content = @Content(schema = @Schema(implementation = ScheduledCommand.class)))
    @GetMapping("/commands")
    public ResponseEntity<List<ScheduledCommand>> getAllCommands() {
        return ResponseEntity.ok(scheduledCommandService.getAllCommands());
    }

    @Operation(summary = "Get commands by device", description = "Retrieve all scheduled commands for a specific device")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved commands", content = @Content(schema = @Schema(implementation = ScheduledCommand.class)))
    @GetMapping("/device/{deviceSerialNumber}/commands")
    public ResponseEntity<List<ScheduledCommand>> getCommandsByDevice(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber) {
        return ResponseEntity.ok(scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNumber));
    }

    @Operation(summary = "Get user face data", description = "Retrieve face data for a specific user from a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved face data", content = @Content(schema = @Schema(implementation = UserFaceDataResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve face data")
    })
    @GetMapping("/{deviceSerialNumber}/users/{userId}/face")
    public ResponseEntity<UserFaceDataResponse> getUserFaceData(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "ID of the user", required = true) @PathVariable String userId) {

        UserFaceDataResponse response = userFaceDataService.requestUserFaceData(deviceSerialNumber, userId);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Get user finger data", description = "Retrieve finger data for a specific user and finger number from a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved finger data", content = @Content(schema = @Schema(implementation = UserFingerDataResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve finger data")
    })
    @GetMapping("/{deviceSerialNumber}/users/{userId}/fingers/{fingerNo}")
    public ResponseEntity<UserFingerDataResponse> getUserFingerData(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "ID of the user", required = true) @PathVariable String userId,
            @Parameter(description = "Finger number (0-9)", required = true) @PathVariable Integer fingerNo) {

        UserFingerDataResponse response = userFingerDataService.requestUserFingerData(
                deviceSerialNumber, userId, fingerNo);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Get user photo", description = "Retrieve photo data for a specific user from a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved photo data", content = @Content(schema = @Schema(implementation = UserPhotoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or no cached data available"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve photo data")
    })
    @GetMapping("/{deviceSerialNumber}/users/{userId}/photo")
    public ResponseEntity<UserPhotoResponse> getUserPhoto(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "ID of the user", required = true) @PathVariable String userId) {

        UserPhotoResponse response = userPhotoService.requestUserPhoto(deviceSerialNumber, userId);

        if ("error".equals(response.getStatus()) && response.getData() == null) {
            return ResponseEntity.badRequest().body(response);
        } else if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Copy user between devices", description = "Copy a user with all credentials (face, finger, card, password) from one device to another")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User copied successfully", content = @Content(schema = @Schema(example = "{\"status\": \"success\", \"message\": \"User copied successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or devices not connected"),
            @ApiResponse(responseCode = "404", description = "User not found on source device"),
            @ApiResponse(responseCode = "500", description = "Failed to copy user")
    })
    @PostMapping("/copy")
    public ResponseEntity<Map<String, String>> copyUserBetweenDevices(
            @Parameter(description = "Source device serial number", required = true) @RequestParam String sourceDeviceSerialNumber,
            @Parameter(description = "Target device serial number", required = true) @RequestParam String targetDeviceSerialNumber,
            @Parameter(description = "User ID to copy", required = true) @RequestParam String userId) {

        boolean success = userCopyService.copyUserBetweenDevices(userId, sourceDeviceSerialNumber, targetDeviceSerialNumber);

        if (success) {
            return ResponseEntity.ok(CustomMap.of(
                    "status", "success",
                    "message", "User copied successfully to target device"
            ));
        } else {
            return ResponseEntity.internalServerError().body(CustomMap.of(
                    "status", "error",
                    "message", "Failed to copy user to target device"
            ));
        }
    }
    @Operation(summary = "Create or update user", description = "Create a new user or update an existing user on a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User data command sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or device not connected"),
            @ApiResponse(responseCode = "500", description = "Failed to send user data command")
    })
    @PostMapping("/device/{deviceSerialNumber}/users")
    public ResponseEntity<Map<String,Object>> createOrUpdateUser(
            @Parameter(description = "Serial number of the device", required = true) @PathVariable String deviceSerialNumber,
            @Parameter(description = "ID of the user", required = true) @RequestParam String userId,
            @RequestBody UserData userData) {

        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return ResponseEntity.badRequest().body(CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            ));
        }
if(userService.getUserByIdAndDeviceSerialNumber(userId,deviceSerialNumber).isPresent())
    return ResponseEntity.badRequest().body(CustomMap.of("status","400","message","userId :"+userId+" existed with deviceSerialNo :"+deviceSerialNumber));


        String commandXml = userCommandService.buildSetUserDataCommand(userId, userData);
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, commandXml);

        if (sent) {
            // Save to database if needed
            if (!userService.getUserByIdAndDeviceSerialNumber(userId, deviceSerialNumber).isPresent()) {
                User user = new User();
                user.setUserId(userId);
                user.setDeviceSerialNumber(deviceSerialNumber);
                user.setName(userData.getName());
                user.setDepartment(userData.getDepartment());
                user.setPrivilege(userData.getPrivilege());
                user.setEnabled(userData.getEnabled());
                userService.saveUser(user);
            }

            return ResponseEntity.ok(CustomMap.of(
                    "status", "success",
                    "message", "User data command sent to device"
            ));
        } else {
            return ResponseEntity.internalServerError().body(CustomMap.of(
                    "status", "error",
                    "message", "Failed to send user data command to device"
            ));
        }
    }


    @PostMapping("/bulk")

    public void bulkUserUpload(@RequestBody List<BulkUserDTO> bulkUsers){

        userService.userList(bulkUsers);
    }




}