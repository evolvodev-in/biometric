package com.xaur.service;

import com.xaur.dto.UserFingerDataDto;
import com.xaur.dto.UserFingerDataResponse;
import com.xaur.model.UserFingerData;
import com.xaur.repository.UserFingerDataRepository;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFingerDataService {

    private final UserFingerDataRepository userFingerDataRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;

    @Transactional
    public UserFingerData saveUserFingerData(UserFingerData userFingerData) {
        return userFingerDataRepository.save(userFingerData);
    }

    public Optional<UserFingerData> getUserFingerDataByUserIdFingerNoAndDevice(
            String userId, Integer fingerNo, String deviceSerialNumber) {
        return userFingerDataRepository.findByUserIdAndFingerNoAndDeviceSerialNumber(
                userId, fingerNo, deviceSerialNumber);
    }
    @Transactional
    public void deleteUserFingerData(String userId,String deviceSerialNumber){
        userFingerDataRepository.deleteByUserIdAndDeviceSerialNumber(userId,deviceSerialNumber);
    }

    @Transactional
    public UserFingerData updateUserFingerData(String userId, Integer fingerNo,
                                               String deviceSerialNumber, String duress,
                                               String fingerData) {
        UserFingerData userFingerData = userFingerDataRepository
                .findByUserIdAndFingerNoAndDeviceSerialNumber(userId, fingerNo, deviceSerialNumber)
                .orElse(new UserFingerData());

        userFingerData.setUserId(userId);
        userFingerData.setFingerNo(fingerNo);
        userFingerData.setDeviceSerialNumber(deviceSerialNumber);
        userFingerData.setDuress(duress);
        userFingerData.setFingerData(fingerData);

        return userFingerDataRepository.save(userFingerData);
    }

    
    public UserFingerDataResponse requestUserFingerData(String deviceSerialNumber,
                                                        String userId, Integer fingerNo) {
        if (fingerNo < 0 || fingerNo > 9) {
            return UserFingerDataResponse.builder()
                    .status("error")
                    .message("Finger number must be between 0 and 9")
                    .build();
        }

        
        Optional<UserFingerData> existingData = getUserFingerDataByUserIdFingerNoAndDevice(
                userId, fingerNo, deviceSerialNumber);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingData.isPresent()) {
                UserFingerData fingerData = existingData.get();
                return UserFingerDataResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(fingerData))
                        .build();
            } else {
                return UserFingerDataResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetFingerDataRequest(userId, fingerNo, false);
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingData.isPresent()) {
                UserFingerData fingerData = existingData.get();
                return UserFingerDataResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(fingerData))
                        .build();
            } else {
                return UserFingerDataResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return UserFingerDataResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    private UserFingerDataDto convertToDto(UserFingerData fingerData) {
        return UserFingerDataDto.builder()
                .userId(fingerData.getUserId())
                .fingerNo(fingerData.getFingerNo())
                .duress(fingerData.getDuress())
                .fingerData(fingerData.getFingerData())
                .deviceSerialNumber(fingerData.getDeviceSerialNumber())
                .build();
    }
}