package com.xaur.service;

import com.xaur.dto.DeviceLogStatusDto;
import com.xaur.dto.DeviceLogStatusResponse;
import com.xaur.model.DeviceLogStatus;
import com.xaur.repository.DeviceLogStatusRepository;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceLogStatusService {

    private final DeviceLogStatusRepository deviceLogStatusRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;
    @Transactional
    public DeviceLogStatus saveDeviceLogStatus(DeviceLogStatus deviceLogStatus) {
        return deviceLogStatusRepository.save(deviceLogStatus);
    }

    public Optional<DeviceLogStatus> getDeviceLogStatusBySerialNumber(String deviceSerialNumber) {
        return deviceLogStatusRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    public List<DeviceLogStatus> getAllDeviceLogStatus() {
        return deviceLogStatusRepository.findAll();
    }

    @Transactional
    public DeviceLogStatus updateDeviceLogStatus(String deviceSerialNumber, String terminalType,
                                                 String terminalId, Integer logCount, Integer maxCount) {
        DeviceLogStatus status = deviceLogStatusRepository.findByDeviceSerialNumber(deviceSerialNumber)
                .orElse(new DeviceLogStatus());

        status.setDeviceSerialNumber(deviceSerialNumber);
        status.setTerminalType(terminalType);
        status.setTerminalId(terminalId);
        status.setLogCount(logCount);
        status.setMaxCount(maxCount);
        status.setLastSyncTime(LocalDateTime.now());

        return deviceLogStatusRepository.save(status);
    }

    public DeviceLogStatusResponse requestDeviceLogStatus(String deviceSerialNumber) {
        Optional<DeviceLogStatus> existingStatus = getDeviceLogStatusBySerialNumber(deviceSerialNumber);
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;
        if (!deviceConnected) {
            if (existingStatus.isPresent()) {
                DeviceLogStatus status = existingStatus.get();
                return DeviceLogStatusResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(status))
                        .build();
            } else {
                return DeviceLogStatusResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        String message = messageBuilder.buildGetGlogPosInfoRequest();
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (requestSent) {
            if (existingStatus.isPresent()) {
                DeviceLogStatus status = existingStatus.get();
                return DeviceLogStatusResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(status))
                        .build();
            } else {
                return DeviceLogStatusResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return DeviceLogStatusResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    public DeviceLogStatusDto convertToDto(DeviceLogStatus status) {
        return DeviceLogStatusDto.builder()
                .deviceSerialNumber(status.getDeviceSerialNumber())
                .terminalType(status.getTerminalType())
                .terminalId(status.getTerminalId())
                .logCount(status.getLogCount())
                .maxCount(status.getMaxCount())
                .lastSyncTime(status.getLastSyncTime() != null ?
                        status.getLastSyncTime().toString() : null)
                .build();
    }
}