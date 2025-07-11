package com.xaur.service;

import com.xaur.model.TimeLog;
import com.xaur.repository.TimeLogRepository;
import com.xaur.util.CustomMap;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final MessageBuilder messageBuilder;
    private final WebSocketSessionManager sessionManager;

    @Transactional(readOnly = true)
    public List<TimeLog> getAllLogs() {
        return timeLogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TimeLog> getLogsByDeviceSerialNumber(String deviceSerialNumber) {
        return timeLogRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    @Transactional(readOnly = true)
    public List<TimeLog> getLogsByUserId(String userId) {
        return timeLogRepository.findByUserId(userId);
    }

    public List<TimeLog> getLogsbyUserIdAndDeviceSerialNumber(String userId,String deviceSerialNumber){
        return timeLogRepository.findByUserIdAndDeviceSerialNumber(userId,deviceSerialNumber);
    }
    public List<TimeLog> getLogsbyUserIdAndDeviceSerialNumberAndTime(String userId, String deviceSerialNumber, LocalDate start, LocalDate end){
        return timeLogRepository.findByUserAndDateRange(userId,deviceSerialNumber,start,end);
    }


    @Transactional(readOnly = true)
    public List<TimeLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return timeLogRepository.findByLogTimeBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<TimeLog> getLogsByUserIdAndDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return timeLogRepository.findByUserIdAndLogTimeBetween(userId, start, end);
    }

    @Transactional
    public TimeLog saveLog(TimeLog timeLog) {
        log.info("Saving time log for user: {}, device: {}", timeLog.getUserId(), timeLog.getDeviceSerialNumber());
        return timeLogRepository.save(timeLog);
    }

    @Transactional
    public boolean emptyTimeLogs(String deviceSerialNumber) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            log.warn("Device {} is not connected, cannot empty time logs", deviceSerialNumber);
            return false;
        }

        String message = messageBuilder.buildEmptyTimeLogCommand();
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (sent) {
            log.info("Empty time logs command sent to device: {}", deviceSerialNumber);
            return true;
        } else {
            log.error("Failed to send empty time logs command to device: {}", deviceSerialNumber);
            return false;
        }
    }

    public Map<String, String> getDeviceTime(String deviceSerialNumber) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            Map<String,String> respMap = new HashMap<>();
            respMap.put("status","error");
            respMap.put("message","Device is not connected");
            return respMap;
        }

        String message = messageBuilder.buildGetTimeCommand();
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (sent) {
            return CustomMap.of(
                    "status", "success",
                    "message", "Time request sent to device"
            );
        } else {
            return CustomMap.of(
                    "status", "error",
                    "message", "Failed to send time request to device"
            );
        }
    }

    public Map<String, String> setDeviceTime(String deviceSerialNumber, String timeString) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            );
        }

        if (timeString == null || timeString.isEmpty()) {
            timeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-'T'HH:mm:ss'Z'"));
        }

        String message = messageBuilder.buildSetTimeCommand(timeString);
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (sent) {
            return CustomMap.of(
                    "status", "success",
                    "message", "Time set command sent to device"
            );
        } else {
            return CustomMap.of(
                    "status", "error",
                    "message", "Failed to send time set command to device"
            );
        }
    }
}