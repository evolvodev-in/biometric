package com.xaur.service;

import com.xaur.dto.UserData;
import com.xaur.model.User;
import com.xaur.model.UserFaceData;
import com.xaur.model.UserFingerData;
import com.xaur.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCopyService {

    private final UserService userService;
    private final UserFaceDataService userFaceDataService;
    private final UserFingerDataService userFingerDataService;
    private final UserCommandService userCommandService;
    private final WebSocketSessionManager sessionManager;

    public boolean copyUserBetweenDevices(String userId, String sourceDeviceSerialNumber, String targetDeviceSerialNumber) {
        log.info("Copying user {} from device {} to device {}", userId, sourceDeviceSerialNumber, targetDeviceSerialNumber);
        if (sessionManager.getSessionByDeviceSerialNumber(sourceDeviceSerialNumber) == null ||
                sessionManager.getSessionByDeviceSerialNumber(targetDeviceSerialNumber) == null) {
            log.error("One or both devices are not connected");
            return false;
        }

        Optional<User> userOpt = userService.getUserByIdAndDeviceSerialNumber(userId, sourceDeviceSerialNumber);
        if (!userOpt.isPresent()) {
            log.error("User {} not found on source device {}", userId, sourceDeviceSerialNumber);
            return false;
        }
        System.out.println(userOpt.get().getUserId());
        User user = userOpt.get();
        UserData userData = UserData.builder()
                .name(user.getName())
                .privilege(user.getPrivilege())
                .enabled(Boolean.valueOf(user.isEnabled()))
                .timeSet1(user.getTimeSet1())
                .timeSet2(user.getTimeSet2())
                .timeSet3(user.getTimeSet3())
                .timeSet4(user.getTimeSet4())
                .timeSet5(user.getTimeSet5())
                .userPeriodUsed(Boolean.valueOf(user.isUserPeriodUsed()))
                .userPeriodStart(user.getUserPeriodStart())
                .userPeriodEnd(user.getUserPeriodEnd())
                .card(user.getCard())
                .password(user.getPassword())
                .allowNoCertificate(Boolean.valueOf(true))
                .build();
        Optional<UserFaceData> faceDataOpt = userFaceDataService.getUserFaceDataByUserIdAndDevice(userId, sourceDeviceSerialNumber);
        faceDataOpt.ifPresent(userFaceData -> userData.setFaceData(userFaceData.getFaceData()));

        String commandXml = userCommandService.buildSetUserDataCommand(userId, userData);
        boolean sent = sessionManager.sendMessageToDevice(targetDeviceSerialNumber, commandXml);

        if (!sent) {
            log.error("Failed to send user data to target device");
            return false;
        }
        for (int fingerNo = 0; fingerNo <= 9; fingerNo++) {
            Optional<UserFingerData> fingerDataOpt = userFingerDataService.getUserFingerDataByUserIdFingerNoAndDevice(
                    userId, Integer.valueOf(fingerNo), sourceDeviceSerialNumber);

            if (fingerDataOpt.isPresent()) {
                UserFingerData fingerData = fingerDataOpt.get();

                String fingerCommand = userCommandService.buildSetFingerDataCommand(userId, Integer.valueOf(fingerNo), fingerData.getDuress(), fingerData.getFingerData());

                boolean fingerSent = sessionManager.sendMessageToDevice(targetDeviceSerialNumber, fingerCommand);

                if (!fingerSent) {
                    log.warn("Failed to send finger data {} for user {} to target device", Optional.of(fingerNo), userId);
                }
            }

        }
        System.out.println(sent);

        log.info("Successfully copied user {} from device {} to device {}", userId, sourceDeviceSerialNumber, targetDeviceSerialNumber);
        return true;
    }
}