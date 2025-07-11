package com.xaur.service;

import com.xaur.dto.DeviceWifiSettingsDto;
import com.xaur.dto.DeviceWifiSettingsResponse;
import com.xaur.model.DeviceWifiSettings;
import com.xaur.repository.DeviceWifiSettingsRepository;
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
public class DeviceWifiSettingsService {

    private final DeviceWifiSettingsRepository wifiSettingsRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;

    @Transactional
    public DeviceWifiSettings saveWifiSettings(DeviceWifiSettings settings) {
        return wifiSettingsRepository.save(settings);
    }

    public Optional<DeviceWifiSettings> getWifiSettingsBySerialNumber(String deviceSerialNumber) {
        return wifiSettingsRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    public List<DeviceWifiSettings> getAllWifiSettings() {
        return wifiSettingsRepository.findAll();
    }

    @Transactional
    public DeviceWifiSettings updateWifiSettings(DeviceWifiSettings updatedSettings) {
        DeviceWifiSettings settings = wifiSettingsRepository
                .findByDeviceSerialNumber(updatedSettings.getDeviceSerialNumber())
                .orElse(new DeviceWifiSettings());

        
        settings.setDeviceSerialNumber(updatedSettings.getDeviceSerialNumber());
        settings.setTerminalType(updatedSettings.getTerminalType());
        settings.setTerminalId(updatedSettings.getTerminalId());
        settings.setUse(updatedSettings.getUse());
        settings.setSsid(updatedSettings.getSsid());
        settings.setKey(updatedSettings.getKey());
        settings.setDhcp(updatedSettings.getDhcp());
        settings.setIp(updatedSettings.getIp());
        settings.setSubnet(updatedSettings.getSubnet());
        settings.setDefaultGateway(updatedSettings.getDefaultGateway());
        settings.setPort(updatedSettings.getPort());
        settings.setIpFromDhcp(updatedSettings.getIpFromDhcp());
        settings.setSubnetFromDhcp(updatedSettings.getSubnetFromDhcp());
        settings.setDefaultGatewayFromDhcp(updatedSettings.getDefaultGatewayFromDhcp());
        settings.setResult(updatedSettings.getResult());
        settings.setLastSyncTime(LocalDateTime.now());

        return wifiSettingsRepository.save(settings);
    }

    
    public DeviceWifiSettingsResponse requestWifiSettings(String deviceSerialNumber) {
        
        Optional<DeviceWifiSettings> existingSettings = getWifiSettingsBySerialNumber(deviceSerialNumber);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingSettings.isPresent()) {
                DeviceWifiSettings settings = existingSettings.get();
                return DeviceWifiSettingsResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(settings))
                        .build();
            } else {
                return DeviceWifiSettingsResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetWiFiSettingRequest();
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingSettings.isPresent()) {
                DeviceWifiSettings settings = existingSettings.get();
                return DeviceWifiSettingsResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(settings))
                        .build();
            } else {
                return DeviceWifiSettingsResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return DeviceWifiSettingsResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    public List<DeviceWifiSettingsDto> getAllWifiSettingsDtos() {
        return getAllWifiSettings().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DeviceWifiSettingsDto convertToDto(DeviceWifiSettings settings) {
        return DeviceWifiSettingsDto.builder()
                .deviceSerialNumber(settings.getDeviceSerialNumber())
                .terminalType(settings.getTerminalType())
                .terminalId(settings.getTerminalId())
                .use(settings.getUse())
                .ssid(settings.getSsid())
                .key(settings.getKey())
                .dhcp(settings.getDhcp())
                .ip(settings.getIp())
                .subnet(settings.getSubnet())
                .defaultGateway(settings.getDefaultGateway())
                .port(settings.getPort())
                .ipFromDhcp(settings.getIpFromDhcp())
                .subnetFromDhcp(settings.getSubnetFromDhcp())
                .defaultGatewayFromDhcp(settings.getDefaultGatewayFromDhcp())
                .result(settings.getResult())
                .lastSyncTime(settings.getLastSyncTime() != null ?
                        settings.getLastSyncTime().toString() : null)
                .build();
    }
    public Map<String, String> setWifiSettings(String deviceSerialNumber, DeviceWifiSettingsDto settings) {
        if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
            return CustomMap.of(
                    "status", "error",
                    "message", "Device is not connected"
            );
        }

        settings.setDeviceSerialNumber(deviceSerialNumber);
        String message = messageBuilder.buildSetWifiSettingsCommand(settings);
        boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        if (sent) {
            // Save to database
            DeviceWifiSettings entity = new DeviceWifiSettings();
            entity.setDeviceSerialNumber(deviceSerialNumber);
            entity.setUse(settings.getUse());
            entity.setSsid(settings.getSsid());
            entity.setKey(settings.getKey());
            entity.setDhcp(settings.getDhcp());
            entity.setIp(settings.getIp());
            entity.setSubnet(settings.getSubnet());
            entity.setDefaultGateway(settings.getDefaultGateway());
            entity.setPort(settings.getPort());
            entity.setLastSyncTime(LocalDateTime.now());

            saveWifiSettings(entity);

            return CustomMap.of(
                    "status", "success",
                    "message", "WiFi settings command sent to device"
            );
        } else {
            return CustomMap.of(
                    "status", "error",
                    "message", "Failed to send WiFi settings command to device"
            );
        }
    }
}