package com.xaur.service;

import com.xaur.dto.DeviceEthernetSettingsDto;
import com.xaur.dto.DeviceEthernetSettingsResponse;
import com.xaur.model.DeviceEthernetSettings;
import com.xaur.repository.DeviceEthernetSettingsRepository;
import com.xaur.util.CustomMap;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceEthernetSettingsService {

    private final DeviceEthernetSettingsRepository ethernetSettingsRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;

    @Transactional
    public DeviceEthernetSettings saveEthernetSettings(DeviceEthernetSettings settings) {
        return ethernetSettingsRepository.save(settings);
    }

    public Optional<DeviceEthernetSettings> getEthernetSettingsBySerialNumber(String deviceSerialNumber) {
        return ethernetSettingsRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    public List<DeviceEthernetSettings> getAllEthernetSettings() {
        return ethernetSettingsRepository.findAll();
    }

    @Transactional
    public DeviceEthernetSettings updateEthernetSettings(DeviceEthernetSettings updatedSettings) {
        DeviceEthernetSettings settings = ethernetSettingsRepository
                .findByDeviceSerialNumber(updatedSettings.getDeviceSerialNumber())
                .orElse(new DeviceEthernetSettings());

        
        settings.setDeviceSerialNumber(updatedSettings.getDeviceSerialNumber());
        settings.setTerminalType(updatedSettings.getTerminalType());
        settings.setTerminalId(updatedSettings.getTerminalId());
        settings.setDhcp(updatedSettings.getDhcp());
        settings.setIp(updatedSettings.getIp());
        settings.setSubnet(updatedSettings.getSubnet());
        settings.setDefaultGateway(updatedSettings.getDefaultGateway());
        settings.setPort(updatedSettings.getPort());
        settings.setMacAddress(updatedSettings.getMacAddress());
        settings.setIpFromDhcp(updatedSettings.getIpFromDhcp());
        settings.setSubnetFromDhcp(updatedSettings.getSubnetFromDhcp());
        settings.setDefaultGatewayFromDhcp(updatedSettings.getDefaultGatewayFromDhcp());
        settings.setLastSyncTime(LocalDateTime.now());

        return ethernetSettingsRepository.save(settings);
    }

    
    public DeviceEthernetSettingsResponse requestEthernetSettings(String deviceSerialNumber) {
        
        Optional<DeviceEthernetSettings> existingSettings = getEthernetSettingsBySerialNumber(deviceSerialNumber);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingSettings.isPresent()) {
                DeviceEthernetSettings settings = existingSettings.get();
                return DeviceEthernetSettingsResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(settings))
                        .build();
            } else {
                return DeviceEthernetSettingsResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetEthernetSettingRequest();
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingSettings.isPresent()) {
                DeviceEthernetSettings settings = existingSettings.get();
                return DeviceEthernetSettingsResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(settings))
                        .build();
            } else {
                return DeviceEthernetSettingsResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return DeviceEthernetSettingsResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    public List<DeviceEthernetSettingsDto> getAllEthernetSettingsDtos() {
        return getAllEthernetSettings().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DeviceEthernetSettingsDto convertToDto(DeviceEthernetSettings settings) {
        return DeviceEthernetSettingsDto.builder()
                .deviceSerialNumber(settings.getDeviceSerialNumber())
                .terminalType(settings.getTerminalType())
                .terminalId(settings.getTerminalId())
                .dhcp(settings.getDhcp())
                .ip(settings.getIp())
                .subnet(settings.getSubnet())
                .defaultGateway(settings.getDefaultGateway())
                .port(settings.getPort())
                .macAddress(settings.getMacAddress())
                .ipFromDhcp(settings.getIpFromDhcp())
                .subnetFromDhcp(settings.getSubnetFromDhcp())
                .defaultGatewayFromDhcp(settings.getDefaultGatewayFromDhcp())
                .lastSyncTime(settings.getLastSyncTime() != null ?
                        settings.getLastSyncTime().toString() : null)
                .build();
    }
    public Map<String, String> setEthernetSettings(String deviceSerialNumber, DeviceEthernetSettingsDto settings) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            );
        }

        settings.setDeviceSerialNumber(deviceSerialNumber);
        String message = messageBuilder.buildSetEthernetSettingsCommand(settings);
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (sent) {
            // Save to database
            DeviceEthernetSettings entity = new DeviceEthernetSettings();
            entity.setDeviceSerialNumber(deviceSerialNumber);
            entity.setDhcp(settings.getDhcp());
            entity.setIp(settings.getIp());
            entity.setSubnet(settings.getSubnet());
            entity.setDefaultGateway(settings.getDefaultGateway());
            entity.setPort(settings.getPort());
            entity.setLastSyncTime(LocalDateTime.now());

            saveEthernetSettings(entity);

            return CustomMap.of(
                    "status", "success",
                    "message", "Ethernet settings command sent to device"
            );
        } else {
            return CustomMap.of(
                    "status", "error",
                    "message", "Failed to send Ethernet settings command to device"
            );
        }
    }
}