package com.xaur.service;

import com.xaur.dto.UserPhotoDto;
import com.xaur.dto.UserPhotoResponse;
import com.xaur.model.UserPhoto;
import com.xaur.repository.UserPhotoRepository;
import com.xaur.repository.UserRepository;
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
public class UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;
    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;
    private final UserRepository userRepository;

    @Transactional
    public UserPhoto saveUserPhoto(UserPhoto userPhoto) {
        return userPhotoRepository.save(userPhoto);
    }

    public Optional<UserPhoto> getUserPhotoByUserIdAndDevice(String userId, String deviceSerialNumber) {
        return userPhotoRepository.findByUserIdAndDeviceSerialNumber(userId, deviceSerialNumber);
    }

    @Transactional
    public UserPhoto updateUserPhoto(String userId, String deviceSerialNumber, String photoData) {
        UserPhoto userPhoto = userPhotoRepository
                .findByUserIdAndDeviceSerialNumber(userId, deviceSerialNumber)
                .orElse(new UserPhoto());

        userPhoto.setUserId(userId);
        userPhoto.setDeviceSerialNumber(deviceSerialNumber);
        userPhoto.setPhotoData(photoData);

        return userPhotoRepository.save(userPhoto);
    }

    
    public UserPhotoResponse requestUserPhoto(String deviceSerialNumber, String userId) {
        
        Optional<UserPhoto> existingData = getUserPhotoByUserIdAndDevice(userId, deviceSerialNumber);

        
        boolean deviceConnected = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) != null;

        
        if (!deviceConnected) {
            if (existingData.isPresent()) {
                UserPhoto photo = existingData.get();
                return UserPhotoResponse.builder()
                        .status("cached")
                        .message("Device not connected, showing cached data")
                        .data(convertToDto(photo))
                        .build();
            } else {
                return UserPhotoResponse.builder()
                        .status("error")
                        .message("Device is not connected and no cached data available")
                        .build();
            }
        }

        
        String message = messageBuilder.buildGetUserPhotoRequest(userId);
        boolean requestSent = sessionManager.sendMessageToDevice(deviceSerialNumber, message);

        
        if (requestSent) {
            if (existingData.isPresent()) {
                UserPhoto photo = existingData.get();
                return UserPhotoResponse.builder()
                        .status("refreshing")
                        .message("Request sent to device, showing cached data")
                        .data(convertToDto(photo))
                        .build();
            } else {
                return UserPhotoResponse.builder()
                        .status("pending")
                        .message("Request sent to device, no cached data available")
                        .build();
            }
        } else {
            return UserPhotoResponse.builder()
                    .status("error")
                    .message("Failed to send request to device")
                    .build();
        }
    }

    private UserPhotoDto convertToDto(UserPhoto photo) {
        return UserPhotoDto.builder()
                .userId(photo.getUserId())
                .photoData(photo.getPhotoData())
                .deviceSerialNumber(photo.getDeviceSerialNumber())
                .build();
    }
}