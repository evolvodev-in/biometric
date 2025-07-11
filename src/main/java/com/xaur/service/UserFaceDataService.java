package com.xaur.service;

import com.xaur.dto.UserFaceDataDto;
import com.xaur.dto.UserFaceDataResponse;
import com.xaur.model.UserFaceData;
import com.xaur.repository.UserFaceDataRepository;
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
public class UserFaceDataService {

    private final UserFaceDataRepository userFaceDataRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;

    @Transactional
    public UserFaceData saveUserFaceData(UserFaceData userFaceData) {
        return userFaceDataRepository.save(userFaceData);
    }
    public void deleteUserFaceData(String userId,String deviceSerialNumber){
        userFaceDataRepository.findByUserIdAndDeviceSerialNumber(userId,deviceSerialNumber).ifPresent(userFaceDataRepository::delete);
    }

    public Optional<UserFaceData> getUserFaceDataByUserIdAndDevice(String userId, String deviceSerialNumber) {
        return userFaceDataRepository.findByUserIdAndDeviceSerialNumber(userId, deviceSerialNumber);
    }

    @Transactional
    public UserFaceData updateUserFaceData(String userId, String deviceSerialNumber,
                                           String faceEnrolled, String faceData) {
        UserFaceData userFaceData = userFaceDataRepository
                .findByUserIdAndDeviceSerialNumber(userId, deviceSerialNumber)
                .orElse(new UserFaceData());

        userFaceData.setUserId(userId);
        userFaceData.setDeviceSerialNumber(deviceSerialNumber);
        userFaceData.setFaceEnrolled(faceEnrolled);
        userFaceData.setFaceData(faceData);

        return userFaceDataRepository.save(userFaceData);
    }

    
    public UserFaceDataResponse requestUserFaceData(String deviceSerialNumber, String userId) {
        
        Optional<UserFaceData> existingData = getUserFaceDataByUserIdAndDevice(userId, deviceSerialNumber);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingData.isPresent()) {
                UserFaceData faceData = existingData.get();
                return UserFaceDataResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(faceData))
                        .build();
            } else {
                return UserFaceDataResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetFaceDataRequest(userId);
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingData.isPresent()) {
                UserFaceData faceData = existingData.get();
                return UserFaceDataResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(faceData))
                        .build();
            } else {
                return UserFaceDataResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return UserFaceDataResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    private UserFaceDataDto convertToDto(UserFaceData faceData) {
        return UserFaceDataDto.builder()
                .userId(faceData.getUserId())
                .faceEnrolled(faceData.getFaceEnrolled())
                .faceData(faceData.getFaceData())
                .deviceSerialNumber(faceData.getDeviceSerialNumber())
                .build();
    }
}