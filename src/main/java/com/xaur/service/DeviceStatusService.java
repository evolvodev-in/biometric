package com.xaur.service;

import com.xaur.model.DeviceStatus;
import com.xaur.repository.DeviceStatusRepository;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceStatusService {

    private final DeviceStatusRepository deviceStatusRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;

    
    private final ConcurrentHashMap<String, Boolean> queryingDevices = new ConcurrentHashMap<>();

    
    public List<DeviceStatus> getAllDeviceStatuses() {
        return deviceStatusRepository.findAll();
    }

    
    public Optional<DeviceStatus> getDeviceStatus(String deviceSerialNo) {
        return deviceStatusRepository.findById(deviceSerialNo);
    }

    
    public DeviceStatus updateDeviceStatus(DeviceStatus deviceStatus) {
        deviceStatus.setLastStatusUpdate(LocalDateTime.now());
        return deviceStatusRepository.save(deviceStatus);
    }

    public DeviceStatus saveDeviceStatus(DeviceStatus deviceStatus) {
        if (deviceStatus == null || StringUtils.isEmpty(deviceStatus.getDeviceSerialNo())) {
            throw new IllegalArgumentException("Device status or serial number cannot be null");
        }

        try {
            return deviceStatusRepository.save(deviceStatus);
        } catch (Exception e) {
            log.error("Error saving device status for device: {}", deviceStatus.getDeviceSerialNo(), e);
            throw new RuntimeException("Failed to save device status", e);
        }
    }
    
    public void updateDeviceOnlineStatus(String deviceSerialNo, boolean online) {
        deviceStatusRepository.findById(deviceSerialNo).ifPresent(status -> {
            status.setOnline(online);
            status.setLastOnline(online ? LocalDateTime.now() : status.getLastOnline());
            deviceStatusRepository.save(status);
        });
    }

    
    public DeviceStatus registerDevice(String deviceSerialNo, String terminalType, String terminalId,
                                       String productName, String deviceUid) {
        DeviceStatus deviceStatus = deviceStatusRepository.findById(deviceSerialNo)
                .orElse(DeviceStatus.builder()
                        .deviceSerialNo(deviceSerialNo)
                        .build());

        deviceStatus.setTerminalType(terminalType);
        deviceStatus.setTerminalId(terminalId);
        deviceStatus.setProductName(productName);
        deviceStatus.setDeviceUid(deviceUid);
        deviceStatus.setOnline(true);
        deviceStatus.setLastOnline(LocalDateTime.now());

        return deviceStatusRepository.save(deviceStatus);
    }

    
    public boolean queryDeviceStatus(String deviceSerialNo) {
        log.info("Device status query for: {}", deviceSerialNo);
        if (Boolean.TRUE.equals(queryingDevices.getOrDefault(deviceSerialNo, false))) {
            log.info("Device status query already in progress: {}", deviceSerialNo);
            return false;
        }

        WebSocketSession session = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNo);
        if (session != null && session.isOpen()) {
            try {
                
                queryingDevices.put(deviceSerialNo, true);

                
                String request = messageBuilder.buildGetDeviceStatusAllRequest();
                session.sendMessage(new TextMessage(request));

                
                String firmwareRequest = messageBuilder.buildGetFirmwareVersionRequest();
                session.sendMessage(new TextMessage(firmwareRequest));

                log.info("Sent device status query to device: {}", deviceSerialNo);
                return true;
            } catch (IOException e) {
                queryingDevices.put(deviceSerialNo, false);
                log.error("Failed to send device status query to device: {}", deviceSerialNo, e);
            }
        } else {
            log.warn("Device not connected: {}", deviceSerialNo);
        }
        return false;
    }

    
    public void completeDeviceStatusQuery(String deviceSerialNo) {
        queryingDevices.put(deviceSerialNo, false);
    }

    
    @Scheduled(fixedRate = 120000)
    public void scheduledDeviceStatusQuery() {
        log.info("Starting scheduled device status query");
        sessionManager.getAllSessions().values().forEach(session -> {
            String deviceSerialNo = sessionManager.getDeviceSerialNumber(session);
            if (deviceSerialNo != null) {
                queryDeviceStatus(deviceSerialNo);
            }
        });
    }
}