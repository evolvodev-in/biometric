package com.xaur.service;

import com.xaur.repository.UserRepository;
import com.xaur.websocket.WebSocketSessionManager;
import com.xaur.websocket.message.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSyncService {

    private final WebSocketSessionManager sessionManager;
    private final MessageBuilder messageBuilder;
    private final UserRepository userRepository;
    private final UserFingerDataService userFingerDataService;
    private final UserFaceDataService userFaceDataService;



    private final Map<String, Boolean> syncingDevices = new ConcurrentHashMap<>();


    public boolean syncUsersFromDevice(String deviceSerialNumber) {
        if (Boolean.TRUE.equals(syncingDevices.getOrDefault(deviceSerialNumber, false))) {
            log.info("Device already syncing: {}", deviceSerialNumber);
            return false;
        }

        WebSocketSession session = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber);
        if (session != null && session.isOpen()) {
            try {

                syncingDevices.put(deviceSerialNumber, true);


                String request = messageBuilder.buildGetFirstUserDataRequest();
                session.sendMessage(new TextMessage(request));
                log.info("Started user synchronization for device: {}", deviceSerialNumber);
                return true;
            } catch (IOException e) {
                syncingDevices.put(deviceSerialNumber, false);
                log.error("Failed to send GetFirstUserData request to device: {}", deviceSerialNumber, e);
            }
        } else {
            log.warn("Device not connected: {}", deviceSerialNumber);
        }
        return false;
    }


    public void continueUserSync(String deviceSerialNumber) {
        WebSocketSession session = sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber);
        if (session != null && session.isOpen()) {
            try {

                String request = messageBuilder.buildGetNextUserDataRequest();
                session.sendMessage(new TextMessage(request));
            } catch (IOException e) {
                syncingDevices.put(deviceSerialNumber, false);
                log.error("Failed to send GetNextUserData request to device: {}", deviceSerialNumber, e);
            }
        } else {
            syncingDevices.put(deviceSerialNumber, false);
            log.warn("Device disconnected during sync: {}", deviceSerialNumber);
        }
    }


    public void completeUserSync(String deviceSerialNumber) {
        syncingDevices.put(deviceSerialNumber, false);
        log.info("Completed user synchronization for device: {}", deviceSerialNumber);
    }



    @Scheduled(fixedRate = 120000)
    public void scheduledUserSync() {
        log.info("Starting scheduled user synchronization");
        sessionManager.getAllSessions().values().forEach(session -> {
            String deviceSerialNumber = sessionManager.getDeviceSerialNumber(session);
            if (deviceSerialNumber != null) {
                syncUsersFromDevice(deviceSerialNumber);
            }
        });
    }

    /*@Scheduled(fixedRate = 660000)
    public void scheduledUserFingerSync(){
        sessionManager.getAllSessions().values().forEach(session -> {
            String deviceSerialNumber = sessionManager.getDeviceSerialNumber(session);
            if (deviceSerialNumber != null) {


                List<User> list = userRepository.findAll();
                for (User user : list) {


                    log.info("userFingerData Synchronization started for userId:{},deviceSerialNumber:{}", user.getUserId(), user.getDeviceSerialNumber());

                    for (int i = 0; i < 10; i++) {
                        userFingerDataService.requestUserFingerData(user.getDeviceSerialNumber(), user.getUserId(), i);

                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            log.info("Unintrerrpted Exception");
                        }

                    }
                }
            }
        });
    }
    @Scheduled(fixedRate = 720000)
    public void scheduledUserFaceSync(){
        sessionManager.getAllSessions().values().forEach(session -> {
            String deviceSerialNumber = sessionManager.getDeviceSerialNumber(session);
            if (deviceSerialNumber != null) {


        List<  User> list=userRepository.findAll();
        for(User user:list){



            log.info("userFaceData Synchronization started for userId:{},deviceSerialNumber:{}",user.getUserId(),user.getDeviceSerialNumber());


                userFaceDataService.requestUserFaceData(user.getDeviceSerialNumber(), user.getUserId());

            }}});


    }
*/




















    public boolean isDeviceSyncing(String deviceSerialNumber) {
        return syncingDevices.getOrDefault(deviceSerialNumber, false);
    }
}