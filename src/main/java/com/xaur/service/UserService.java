package com.xaur.service;

import com.xaur.dto.BulkUserDTO;
import com.xaur.dto.UserData;
import com.xaur.model.*;
import com.xaur.repository.BulkUserRepository;
import com.xaur.repository.TimeSheetRepository;
import com.xaur.repository.UserRepository;

import com.xaur.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;
    private final BulkUserRepository bulkUserRepository;
    private final UserCommandService userCommandService;
    private final WebSocketSessionManager sessionManager;

    private final TimeSheetRepository timeSheetRepository;





    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    @Transactional
    public User saveUser(User user) {
        log.info("Saving user with ID: {}", user.getUserId());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId,String deviceSerialNumber) {
        log.info("Deleting user with ID: {}", userId);
        userRepository.findByUserIdAndDeviceSerialNumber(userId,deviceSerialNumber).ifPresent(userRepository::delete);
    }

    @Transactional
    public boolean existsById(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public List<User> getUsersByDeviceSerialNumber(String deviceSerialNumber) {
        return userRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    public Optional<User> getUserByIdAndDeviceSerialNumber(String userId, String deviceSerialNumber) {
        return userRepository.findByUserIdAndDeviceSerialNumber(userId, deviceSerialNumber);
    }

    @Scheduled(fixedRate = 120000
    )
    public void bulkUpload() throws InterruptedException {
        List<BulkUser> list=bulkUserRepository.find("PENDING");

        for(BulkUser bulkUser:list){
                UserData userData=new UserData();

                userData.setEnabled(true);
                userData.setName(bulkUser.getName());
                userData.setPrivilege(bulkUser.getPrivilege());
                userData.setUserPeriodUsed(false);
                userData.setAllowNoCertificate(true);

      String commandXml=          userCommandService.buildSetUserDataCommand(bulkUser.getUser_id(),userData);
            sessionManager.sendMessageToDevice(bulkUser.getDevice_serial_number(), commandXml);
Thread.sleep(5000);
        }



    }






public void userList(List<BulkUserDTO> list){

        for (BulkUserDTO bulkUser:list){
            BulkUser bulkUser1=new BulkUser();
            bulkUser1.setUser_id(bulkUser.getUser_id());
            bulkUser1.setDepartment(0);
            bulkUser1.setName(bulkUser.getName());
            bulkUser1.setEnabled(bulkUser.isEnabled());
            bulkUser1.setStatus(bulkUser.getStatus());
            bulkUser1.setDevice_serial_number(bulkUser.getDevice_serial_number());
            bulkUser1.setPrivilege(bulkUser.getPrivilege());
            bulkUser1.setUser_period_used(bulkUser.getUser_period_used());

            bulkUserRepository.save(bulkUser1);
        }

}



}