package com.xaur.service;

import com.xaur.dto.DeviceAdditionalInfoDto;
import com.xaur.dto.DeviceAdditionalInfoResponse;
import com.xaur.model.DeviceAdditionalInfo;
import com.xaur.repository.DeviceAdditionalInfoRepository;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceAdditionalInfoService {

    private final DeviceAdditionalInfoRepository deviceAdditionalInfoRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;

    private static final List<String> AVAILABLE_PARAM_NAMES = Arrays.asList(
            "MobileNetwork", "NTPServer", "VPNServer", "WebServerUrl",
            "SendLogUrl", "DeviceName", "GPS"
    );

    @Transactional
    public DeviceAdditionalInfo saveDeviceAdditionalInfo(DeviceAdditionalInfo info) {
        return deviceAdditionalInfoRepository.save(info);
    }

    public Optional<DeviceAdditionalInfo> getDeviceAdditionalInfoBySerialNumberAndParamName(
            String deviceSerialNumber, String paramName) {
        return deviceAdditionalInfoRepository.findByDeviceSerialNumberAndParamName(deviceSerialNumber, paramName);
    }

    public List<DeviceAdditionalInfo> getDeviceAdditionalInfoBySerialNumber(String deviceSerialNumber) {
        return deviceAdditionalInfoRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    public List<DeviceAdditionalInfo> getAllDeviceAdditionalInfo() {
        return deviceAdditionalInfoRepository.findAll();
    }

    @Transactional
    public DeviceAdditionalInfo updateDeviceAdditionalInfo(
            String deviceSerialNumber, String terminalType, String terminalId,
            String paramName, String value1, String value2, String value3,
            String value4, String value5) {

        DeviceAdditionalInfo info = deviceAdditionalInfoRepository
                .findByDeviceSerialNumberAndParamName(deviceSerialNumber, paramName)
                .orElse(new DeviceAdditionalInfo());

        info.setDeviceSerialNumber(deviceSerialNumber);
        info.setTerminalType(terminalType);
        info.setTerminalId(terminalId);
        info.setParamName(paramName);
        info.setValue1(value1);
        info.setValue2(value2);
        info.setValue3(value3);
        info.setValue4(value4);
        info.setValue5(value5);
        info.setLastSyncTime(LocalDateTime.now());

        return deviceAdditionalInfoRepository.save(info);
    }

    
    public DeviceAdditionalInfoResponse requestDeviceAdditionalInfo(String deviceSerialNumber, String paramName) {
        
        if (!AVAILABLE_PARAM_NAMES.contains(paramName)) {
            return DeviceAdditionalInfoResponse.builder()
                    .status("error")
                    .message("Invalid parameter name. Available parameters: " + String.join(", ", AVAILABLE_PARAM_NAMES))
                    .build();
        }

        
        Optional<DeviceAdditionalInfo> existingInfo = getDeviceAdditionalInfoBySerialNumberAndParamName(
                deviceSerialNumber, paramName);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingInfo.isPresent()) {
                DeviceAdditionalInfo info = existingInfo.get();
                return DeviceAdditionalInfoResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(info))
                        .build();
            } else {
                return DeviceAdditionalInfoResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetDeviceInfoExtRequest(paramName);
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingInfo.isPresent()) {
                DeviceAdditionalInfo info = existingInfo.get();
                return DeviceAdditionalInfoResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(info))
                        .build();
            } else {
                return DeviceAdditionalInfoResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return DeviceAdditionalInfoResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    
    public void requestAllDeviceAdditionalInfo(String deviceSerialNumber) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            log.warn("Device {} is not connected, cannot request additional info", deviceSerialNumber);
            return;
        }

        for (String paramName : AVAILABLE_PARAM_NAMES) {
            String message = messageBuilder.buildGetDeviceInfoExtRequest(paramName);
            boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

            if (sent) {
                log.info("Requested {} info for device {}", paramName, deviceSerialNumber);
            } else {
                log.error("Failed to request {} info for device {}", paramName, deviceSerialNumber);
            }
        }
    }

    public List<DeviceAdditionalInfoDto> getAllDeviceAdditionalInfoDtos() {
        return getAllDeviceAdditionalInfo().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DeviceAdditionalInfoDto convertToDto(DeviceAdditionalInfo info) {
        return DeviceAdditionalInfoDto.builder()
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
                .build();
    }
}