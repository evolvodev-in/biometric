package com.xaur.controller;

import com.xaur.model.TimeLog;
import com.xaur.model.User;
import com.xaur.repository.TimeLogRepository;
import com.xaur.repository.UserRepository;
import com.xaur.service.TimeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance Log Controller", description = "API for retrieving attendance logs")
@SecurityRequirement(name = "Bearer Authentication")
public class TimeLogController {

    private final TimeLogService timeLogService;
    private final UserRepository userRepository;
    private final TimeLogRepository timeLogRepository;

    @Operation(summary = "Get all attendance logs", description = "Retrieve all attendance logs from the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance logs",
            content = @Content(schema = @Schema(implementation = TimeLog.class)))
    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllAttendanceLogs() {
        log.info("Request received to get all attendance logs");
        List<TimeLog> list=timeLogService.getAllLogs();
        for(TimeLog timeLog:list){

            Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
            user.ifPresent(value -> timeLog.setUserName(value.getName()));



        }


        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get attendance logs by device", description = "Retrieve attendance logs for a specific device")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance logs",
            content = @Content(schema = @Schema(implementation = TimeLog.class)))
    @GetMapping("/device/{deviceSerialNumber}")
    public ResponseEntity<List<TimeLog>> getAttendanceLogsByDevice(
            @Parameter(description = "Serial number of the device", required = true)
            @PathVariable String deviceSerialNumber) {
        log.info("Request received to get attendance logs for device: {}", deviceSerialNumber);
        List<TimeLog> list=timeLogService.getLogsByDeviceSerialNumber(deviceSerialNumber);
        for(TimeLog timeLog:list){
            Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
            user.ifPresent(value -> timeLog.setUserName(value.getName()));
        }
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get attendance logs by user", description = "Retrieve attendance logs for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance logs",
            content = @Content(schema = @Schema(implementation = TimeLog.class)))
    @GetMapping("/user/{userId}/{deviceSerialNumber}")
    public ResponseEntity<List<TimeLog>> getAttendanceLogsByUser(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable String userId,@PathVariable String deviceSerialNumber) {
        log.info("Request received to get attendance logs for user: {}", userId);

        List<TimeLog> list=timeLogService.getLogsbyUserIdAndDeviceSerialNumber(userId,deviceSerialNumber);
        for(TimeLog timeLog:list){
            Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
            user.ifPresent(value -> timeLog.setUserName(value.getName()));
        }
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get attendance logs by date range", description = "Retrieve attendance logs within a specific date range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance logs",
            content = @Content(schema = @Schema(implementation = TimeLog.class)))
    @GetMapping("/date-range")
    public ResponseEntity<List<TimeLog>> getAttendanceLogsByDateRange(
            @Parameter(description = "Start date-time (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date-time (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Request received to get attendance logs between {} and {}", startDate, endDate);
        return ResponseEntity.ok(timeLogService.getLogsByDateRange(startDate, endDate));
    }

    @Operation(summary = "Get attendance logs by user and date range",
            description = "Retrieve attendance logs for a specific user within a date range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance logs",
            content = @Content(schema = @Schema(implementation = TimeLog.class)))
    @GetMapping("/user/{userId}/{deviceSerialNumber}/date-range")
    public ResponseEntity<List<TimeLog>> getAttendanceLogsByUserAndDateRange(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable String userId,
            @PathVariable String deviceSerialNumber,
            @Parameter(description = "Start date-time (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam /*@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)*/ LocalDate startDate,
            @Parameter(description = "End date-time (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam /*@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)*/ LocalDate endDate) {
        log.info("Request received to get attendance logs for user {} between {} and {}",
                userId, startDate, endDate);
        return ResponseEntity.ok(timeLogService.getLogsbyUserIdAndDeviceSerialNumberAndTime(userId,deviceSerialNumber,startDate,endDate));
    }


    @GetMapping("/logs")
    public ResponseEntity<?> logs(@RequestParam(name = "userId",required = false) String userId,
                                  @RequestParam(name = "deviceSerialNo" ,required = false) String deviceSerialNo,
                                  @RequestParam(name = "startDate",required = false) String startDate,
                                  @RequestParam(name = "endDate",required = false) String endDate){

        if(userId==null&&deviceSerialNo==null&&startDate==null&&endDate==null){
            return getAllAttendanceLogs();
        }
        else if (userId!=null&&deviceSerialNo!=null&&startDate!=null&&endDate!=null) {
            List<TimeLog> list=timeLogService.getLogsbyUserIdAndDeviceSerialNumberAndTime(userId,deviceSerialNo,
                    LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE),LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
            for(TimeLog timeLog:list){
                Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
                user.ifPresent(value -> timeLog.setUserName(value.getName()));
            }

            return ResponseEntity.ok(list);
        }

        else if(userId!=null&&deviceSerialNo!=null&&startDate==null&&endDate==null){
            return getAttendanceLogsByUser(userId,deviceSerialNo);


        }
        else if(deviceSerialNo!=null && startDate!=null&&endDate!=null){
            List<TimeLog> list=timeLogRepository.findByDeviceSerialNumberAndDate(deviceSerialNo,
                    LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE),LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
            for(TimeLog timeLog:list){
                Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
                user.ifPresent(value -> timeLog.setUserName(value.getName()));
            }
            return ResponseEntity.ok(list );}
        else if(userId!=null && startDate!=null&&endDate!=null){
            List<TimeLog> list=timeLogRepository.findByUserIdrAndDate(userId,
                    LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE),LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
            for(TimeLog timeLog:list){
                Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
                user.ifPresent(value -> timeLog.setUserName(value.getName()));
            }
            return ResponseEntity.ok(list );}












        else if (deviceSerialNo!=null&&userId==null&&startDate==null&&endDate==null) {
            return getAttendanceLogsByDevice(deviceSerialNo);

        } else if (userId==null&&deviceSerialNo==null&&startDate!=null&&endDate!=null) {
            List<TimeLog> list= timeLogRepository.findByDateRange(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE),LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE));
            for(TimeLog timeLog:list){
                Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
                user.ifPresent(value -> timeLog.setUserName(value.getName()));
            }


            return ResponseEntity.ok(list);
        }


        if(userId!=null&&startDate==null&&endDate==null&&deviceSerialNo==null

        ){
            List<TimeLog> list=timeLogRepository.findByUserId(userId);
            for(TimeLog timeLog:list){
                Optional<User> user=userRepository.findByUserIdAndDeviceSerialNumber(timeLog.getUserId(), timeLog.getDeviceSerialNumber());
                user.ifPresent(value -> timeLog.setUserName(value.getName()));
            }
            return ResponseEntity.ok(list );}



        return  ResponseEntity.ok("params req");
    }




}