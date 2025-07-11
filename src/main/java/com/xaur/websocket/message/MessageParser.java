package com.xaur.websocket.message;

import com.xaur.model.*;
import com.xaur.repository.BulkUserRepository;
import com.xaur.repository.TimeSheetRepository;

import com.xaur.service.*;
import com.xaur.util.TokenGenerator;
import com.xaur.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageParser {

    private final DeviceService deviceService;
    private final UserService userService;
    private final TimeLogService timeLogService;
    private final AdminLogService adminLogService;
    private final WebSocketSessionManager sessionManager;
    private final TokenGenerator tokenGenerator;
    private final MessageBuilder messageBuilder;
    private final UserSyncService userSyncService;
    private final DeviceStatusService deviceStatusService;
    private final ScheduledCommandService scheduledCommandService;
    private final DeviceLogStatusService deviceLogStatusService;
    private final DeviceAdditionalInfoService deviceAdditionalInfoService;
    private final DeviceWifiSettingsService deviceWifiSettingsService;
    private final DeviceEthernetSettingsService deviceEthernetSettingsService;
    private final UserPhotoService userPhotoService;
    private final UserFaceDataService userFaceDataService;
    private final UserFingerDataService userFingerDataService;
    private final DepartmentService departmentService;
    private final CompanyService companyService;
    private final TimeSheetRepository timeSheetRepository;
    private final BranchService branchService;
    private final BulkUserRepository bulkUserRepository;






    public String parseAndProcessMessage(WebSocketSession session, String message) throws DocumentException {
        Document document = DocumentHelper.parseText(message);
        Element root = document.getRootElement();

        Element requestElement = root.element("Request");
        Element eventElement = root.element("Event");
        Element responseElement = root.element("Response");

        if (requestElement != null) {
            String requestType = requestElement.getTextTrim();
            return handleRequest(session, root, requestType);
        } else if (eventElement != null) {
            String eventType = eventElement.getTextTrim();
            return handleEvent(session, root, eventType);
        } else if (responseElement != null) {
            String responseType = responseElement.getTextTrim();
            return handleResponse(session, root, responseType);
        } else {
            log.warn("Unknown message format: {}", message);
            return messageBuilder.buildErrorResponse("Unknown message format");
        }
    }
    private String handleResponse(WebSocketSession session, Element root, String responseType) {
        log.info("Processing response: {}", responseType);

        switch (responseType) {
            case "GetFirmwareVersion":
                return handleGetFirmwareVersionResponse(session, root);
            case "GetDeviceStatus":
                return handleGetDeviceStatusResponse(session, root);
            case "GetDeviceStatusAll":
                return handleGetDeviceStatusAllResponse(session, root);
            case "GetUserData":
                return handleGetUserDataResponse(session, root);
            case "GetFirstUserData":
                return handleGetFirstUserDataResponse(session, root);
            case "GetNextUserData":
                return handleGetNextUserDataResponse(session, root);
            case "SetUserData":
                return handleSetUserDataResponse(session, root);
            case "GetGlogPosInfo":
                return handleGetGlogPosInfoResponse(session, root);
            case "GetDeviceInfoExt":
                return handleGetDeviceInfoExtResponse(session, root);
            case "GetEthernetSetting":
                return handleGetEthernetSettingResponse(session, root);
            case "GetWiFiSetting":
                return handleGetWiFiSettingResponse(session, root);
            case "GetDepartment":
                return handleGetDepartmentResponse(session, root);
            case "GetFaceData":
                return handleGetFaceDataResponse(session, root);
            case "GetFingerData":
                return handleGetFingerDataResponse(session, root);
            case "GetUserPhoto":
                return handleGetUserPhotoResponse(session, root);
            case "SetTime":
                return handleSetTimeResponse(session, root);
            case "ClearLogData":
                return handleClearLogDataResponse(session, root);
            case "SetDepartment":
                return handleSetDepartmentResponse(session, root);
            case "SetWiFiSetting":
                return handleSetWiFiSettingResponse(session, root);
            case "SetEthernetSetting":
                return handleSetEthernetSettingResponse(session, root);
            case "GetTime":
                return handleGetTimeResponse(session, root);

            default:
                log.warn("Unsupported response type: {}", responseType);
                return null; 
        }
    }
    private String handleRequest(WebSocketSession session, Element root, String requestType) {
        log.info("Processing request: {}", requestType);

        switch (requestType) {
            case "Register":
                return handleRegisterRequest(session, root);
            case "Login":
                return handleLoginRequest(session, root);
            default:
                log.warn("Unsupported request type: {}", requestType);
                return messageBuilder.buildErrorResponse("Unsupported request type: " + requestType);
        }
    }

    private String handleEvent(WebSocketSession session, Element root, String eventType) {
        log.info("Processing event: {}", eventType);

        String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
        if (deviceSerialNo != null) {
            deviceService.updateDeviceActivity(deviceSerialNo);
        }

        switch (eventType) {
            case "TimeLog_v2":
                return handleTimeLogEvent(session, root);
            case "AdminLog_v2":
                return handleAdminLogEvent(session, root);
            case "KeepAlive":
                return handleKeepAliveEvent(session, root);
            default:
                log.warn("Unsupported event type: {}", eventType);
                return messageBuilder.buildErrorResponse("Unsupported event type: " + eventType);
        }
    }

    private String handleRegisterRequest(WebSocketSession session, Element root) {
        String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
        String terminalType = root.elementTextTrim("TerminalType");
        String cloudId = root.elementTextTrim("CloudId");

        if (deviceSerialNo == null || deviceSerialNo.isEmpty()) {
            return messageBuilder.buildErrorResponse("DeviceSerialNo is required");
        }

        Device device = deviceService.registerDevice(deviceSerialNo, terminalType, cloudId,null,null);
        sessionManager.registerDevice(session, deviceSerialNo);

        return messageBuilder.buildRegisterResponse(deviceSerialNo, device.getToken());
    }

    private String handleLoginRequest(WebSocketSession session, Element root) {
        String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
        String token = root.elementTextTrim("Token");

        if (StringUtils.isEmpty(deviceSerialNo) || StringUtils.isEmpty(token)) {
            log.warn("Login attempt with empty device serial or token");
            return messageBuilder.buildLoginResponse(deviceSerialNo, "Fail");
        }

        boolean loginSuccess = deviceService.loginDevice(deviceSerialNo, token);
        if (loginSuccess) {
            try {
                sessionManager.registerDevice(session, deviceSerialNo);

                Optional<Device> deviceOpt = deviceService.getDeviceBySerialNumber(deviceSerialNo);
                if (!deviceOpt.isPresent()) {
                    log.warn("Device not found in database: {}", deviceSerialNo);
                    return messageBuilder.buildLoginResponse(deviceSerialNo, "Fail");
                }

                Device device = deviceOpt.get();

                DeviceStatus deviceStatus = deviceStatusService.getDeviceStatus(deviceSerialNo)
                        .orElse(DeviceStatus.builder()
                                .deviceSerialNo(deviceSerialNo) // Ensure this is set first
                                .managerCount(0)
                                .userCount(0)
                                .faceCount(0)
                                .fpCount(0)
                                .cardCount(0)
                                .pwdCount(0)
                                .doorStatus(0)
                                .alarmStatus(0)
                                .branch(device.getBranch())
                                .company(device.getCompany())
                                .online(true)
                                .lastOnline(LocalDateTime.now())
                                .lastStatusUpdate(LocalDateTime.now())
                                .build());
                System.out.println(deviceStatus.getDeviceSerialNo());

                if (device.getBranch() != null) {
                    deviceStatus.setBranch(device.getBranch());
                }
                if (device.getCompany() != null) {
                    deviceStatus.setCompany(device.getCompany());
                }

                log.info("Saving device status for device: {}", deviceSerialNo);
                deviceStatusService.saveDeviceStatus(deviceStatus);

                deviceStatusService.updateDeviceOnlineStatus(deviceSerialNo, true);
                deviceStatusService.queryDeviceStatus(deviceSerialNo);

                return messageBuilder.buildLoginResponse(deviceSerialNo, "OK");

            } catch (Exception e) {
                log.error("Error during device login process", e);
                return messageBuilder.buildLoginResponse(deviceSerialNo, "Fail");
            }
        } else {
            log.error("Login failed for device: {} - Unknown token", deviceSerialNo);
            return messageBuilder.buildLoginResponse(deviceSerialNo, "FailUnknownToken");
        }
    }

    private String handleTimeLogEvent(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String logId = root.elementTextTrim("LogID");
            String timeStr = root.elementTextTrim("Time");
            String userId = root.elementTextTrim("UserID");
            String action = root.elementTextTrim("Action");
            String attendStat = root.elementTextTrim("AttendStat");
            String apStat = root.elementTextTrim("APStat");
            String jobCodeStr = root.elementTextTrim("JobCode");
            String photoStr = root.elementTextTrim("Photo");
            String logImage = root.elementTextTrim("LogImage");
            String transId = root.elementTextTrim("TransID");

            
            LocalDateTime logTime = parseDateTime(timeStr);

            
            Integer jobCode = jobCodeStr != null ? Integer.parseInt(jobCodeStr) : 0;

            
            boolean hasPhoto = "Yes".equalsIgnoreCase(photoStr);

            
            TimeLog timeLog = TimeLog.builder()
                    .logId(logId)
                    .deviceSerialNumber(deviceSerialNo)
                    .userId(userId)
                    .logTime(logTime)
                    .action(action)
                    .attendStat(attendStat)
                    .apStat(apStat)
                    .jobCode(jobCode)
                    .hasPhoto(hasPhoto)
                    .logImage(logImage)
                    .transId(transId)
                    .build();

            timeLogService.saveLog(timeLog);






            Optional<User> userOpt = userService.getUserByIdAndDeviceSerialNumber(
                    timeLog.getUserId(), timeLog.getDeviceSerialNumber());

            Device device = deviceService.getDeviceBySerialNumber(deviceSerialNo)
                    .orElse(new Device());  // Create an empty Device if not found



            Company company = (device.getCompany() != null)
                    ? companyService.getCompanyById(device.getCompany().getId()).orElse(new Company())  // Create an empty Company if not found
                    :  new Company();  // If device has no company set, create an empty Company

            Branch branch = (device.getBranch() != null)
                    ? branchService.getBranchById(device.getBranch().getId()).orElse(new Branch())  // Create an empty Branch if not found
                    : new Branch();




            String employeeName = null;
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getName() != null) {
                    employeeName = user.getName().replace("\u0000", "");
                }
            }

            if(device.getCompany()!=null&&device.getBranch()!=null) {


                HrTimeSheetLog timeSheetLog = new HrTimeSheetLog();
                timeSheetLog.setAttTime(logTime.toLocalTime());
                timeSheetLog.setAttDate(logTime.toLocalDate());
                timeSheetLog.setDateCode(logTime.toLocalDate());
                timeSheetLog.setCompanyCode(company.getExternalCode());
                timeSheetLog.setBranchCode(branch.getExternalCode());
                timeSheetLog.setDeviceLogId(Long.parseLong(transId));
                if (attendStat.equalsIgnoreCase("DutyOff")) {
                    timeSheetLog.setStatus("OUT");
                } else
                    timeSheetLog.setStatus("IN");
                timeSheetLog.setDeviceId(device.getSerialNumber());
                //  timeSheetLog.setEmployeeName(employeeName);
                timeSheetLog.setDeviceId(device.getSerialNumber());
                timeSheetLog.setEmployeeRefCode(userId);


                
                timeSheetLog.setLogDate(logTime);
                timeSheetLog.setCreatedBy(device.getSerialNumber());
                timeSheetLog.setCreatedDate(logTime);
                timeSheetLog.setIsProcessed(false);
                timeSheetLog.setIsModified(false);
                timeSheetLog.setIsTransfered(false);




                timeSheetRepository.save(timeSheetLog);

            }



            return messageBuilder.buildTimeLogResponse(transId, "OK");
         }catch (Exception e) {
            log.error("Error processing TimeLog_v2 event", e);
            return messageBuilder.buildTimeLogResponse(root.elementTextTrim("TransID"), "Fail");
        }
    }

    private String handleAdminLogEvent(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String logId = root.elementTextTrim("LogID");
            String timeStr = root.elementTextTrim("Time");
            String adminId = root.elementTextTrim("AdminID");
            String userId = root.elementTextTrim("UserID");
            String action = root.elementTextTrim("Action");
            String statStr = root.elementTextTrim("Stat");
            String transId = root.elementTextTrim("TransID");

            
            LocalDateTime logTime = parseDateTime(timeStr);

            
            Integer stat = statStr != null ? Integer.parseInt(statStr) : 0;

            
            AdminLog adminLog = AdminLog.builder()
                    .logId(logId)
                    .deviceSerialNumber(deviceSerialNo)
                    .adminId(adminId)
                    .userId(userId)
                    .logTime(logTime)
                    .action(action)
                    .stat(stat)
                    .transId(transId)
                    .build();

            adminLogService.saveLog(adminLog);

            return messageBuilder.buildAdminLogResponse(transId, "OK");
        } catch (Exception e) {
            log.error("Error processing AdminLog_v2 event", e);
            return messageBuilder.buildAdminLogResponse(root.elementTextTrim("TransID"), "Fail");
        }
    }

    private String handleKeepAliveEvent(WebSocketSession session, Element root) {
        String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
        String devTimeStr = root.elementTextTrim("DevTime");

        
        if (deviceSerialNo != null) {
            deviceService.updateDeviceActivity(deviceSerialNo);
        }

        
        String serverTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-'T'HH:mm:ss'Z'"));

        return messageBuilder.buildKeepAliveResponse(devTimeStr, serverTime);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-'T'HH:mm:ss'Z'");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            try {
                log.debug("Trying alternative date format for: {}", dateTimeStr);

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})-T([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})Z");
                java.util.regex.Matcher matcher = pattern.matcher(dateTimeStr);

                if (matcher.matches()) {
                    int year = Integer.parseInt(matcher.group(1));
                    int month = Integer.parseInt(matcher.group(2));
                    int day = Integer.parseInt(matcher.group(3));
                    int hour = Integer.parseInt(matcher.group(4));
                    int minute = Integer.parseInt(matcher.group(5));
                    int second = Integer.parseInt(matcher.group(6));

                    return LocalDateTime.of(year, month, day, hour, minute, second);
                } else {
                    
                    String cleaned = dateTimeStr.replace("-T", "T").replace("Z", "");
                    DateTimeFormatter lenientFormatter = DateTimeFormatter.ofPattern("yyyy-M-d'T'H:m:s");
                    return LocalDateTime.parse(cleaned, lenientFormatter);
                }
            } catch (Exception ex) {
                log.error("Error parsing date time with alternative formats: {}", dateTimeStr, ex);
            }

            log.warn("Falling back to current time for unparseable date: {}", dateTimeStr);
            return LocalDateTime.now();
        }
    }
    private String handleGetUserDataResponse(WebSocketSession session, Element root) {
        try {

            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalId = root.elementTextTrim("TerminalID");
            String terminalType = root.elementTextTrim("TerminalType");
            String userId = root.elementTextTrim("UserID");
            String encodedName = root.elementTextTrim("Name");
            String privilege = root.elementTextTrim("Privilege");
            String departmentStr = root.elementTextTrim("Depart");
            String enabledStr = root.elementTextTrim("Enabled");
            String timeSet1Str = root.elementTextTrim("TimeSet1");
            String timeSet2Str = root.elementTextTrim("TimeSet2");
            String timeSet3Str = root.elementTextTrim("TimeSet3");
            String timeSet4Str = root.elementTextTrim("TimeSet4");
            String timeSet5Str = root.elementTextTrim("TimeSet5");
            String userPeriodUsedStr = root.elementTextTrim("UserPeriod_Used");
            String userPeriodStartStr = root.elementTextTrim("UserPeriod_Start");
            String userPeriodEndStr = root.elementTextTrim("UserPeriod_End");
            String card = root.elementTextTrim("Card");
            String password = root.elementTextTrim("PWD");
            String fingers = root.elementTextTrim("Fingers");
            String faceEnrolledStr = root.elementTextTrim("FaceEnrolled");
            String faceData = root.elementTextTrim("FaceData");
            String result = root.elementTextTrim("Result");

            if ("OK".equals(result)) {
                
                String name = decodeBase64Utf16Name(encodedName);
                Integer department = departmentStr != null ? Integer.parseInt(departmentStr) : 0;
                boolean enabled = "Yes".equalsIgnoreCase(enabledStr);
                Integer timeSet1 = timeSet1Str != null ? Integer.parseInt(timeSet1Str) : 0;
                Integer timeSet2 = timeSet2Str != null ? Integer.parseInt(timeSet2Str) : 0;
                Integer timeSet3 = timeSet3Str != null ? Integer.parseInt(timeSet3Str) : 0;
                Integer timeSet4 = timeSet4Str != null ? Integer.parseInt(timeSet4Str) : 0;
                Integer timeSet5 = timeSet5Str != null ? Integer.parseInt(timeSet5Str) : 0;
                boolean userPeriodUsed = "Yes".equalsIgnoreCase(userPeriodUsedStr);
                Integer userPeriodStart = userPeriodStartStr != null ? Integer.parseInt(userPeriodStartStr) : 0;
                Integer userPeriodEnd = userPeriodEndStr != null ? Integer.parseInt(userPeriodEndStr) : 0;
                boolean faceEnrolled = "Yes".equalsIgnoreCase(faceEnrolledStr);

                
                User user = userService.getUserById(userId).orElse(new User());
                user.setDeviceSerialNumber(deviceSerialNo);
                user.setTerminalId(terminalId);
                user.setTerminalType(terminalType);
                user.setUserId(userId);
                user.setName(name);
                user.setPrivilege(privilege);
                user.setDepartment(department);
                user.setEnabled(enabled);
                user.setTimeSet1(timeSet1);
                user.setTimeSet2(timeSet2);
                user.setTimeSet3(timeSet3);
                user.setTimeSet4(timeSet4);
                user.setTimeSet5(timeSet5);
                user.setUserPeriodUsed(userPeriodUsed);
                user.setUserPeriodStart(userPeriodStart);
                user.setUserPeriodEnd(userPeriodEnd);
                user.setCard(card);
                user.setPassword(password);
                user.setFingers(fingers);
                user.setFaceEnrolled(faceEnrolled);
                user.setFaceData(faceData);

                userService.saveUser(user);
                log.info("Saved user data for user ID: {}", userId);
            } else {
                log.warn("Failed to get user data for user ID: {}, result: {}", userId, result);
            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetUserData response", e);
            return null;
        }
    }

    private String handleGetFirstUserDataResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String result = root.elementTextTrim("Result");
            String moreStr = root.elementTextTrim("More");

            if ("OK".equals(result)) {
                
                String userId = root.elementTextTrim("UserID");
                String encodedName = root.elementTextTrim("Name");
                String name = decodeBase64Utf16Name(encodedName);
                String privilege = root.elementTextTrim("Privilege");
                String departmentStr = root.elementTextTrim("Depart");
                String enabledStr = root.elementTextTrim("Enabled");
                String timeSet1Str = root.elementTextTrim("TimeSet1");
                String timeSet2Str = root.elementTextTrim("TimeSet2");
                String timeSet3Str = root.elementTextTrim("TimeSet3");
                String timeSet4Str = root.elementTextTrim("TimeSet4");
                String timeSet5Str = root.elementTextTrim("TimeSet5");
                String userPeriodUsedStr = root.elementTextTrim("UserPeriod_Used");
                String userPeriodStartStr = root.elementTextTrim("UserPeriod_Start");
                String userPeriodEndStr = root.elementTextTrim("UserPeriod_End");
                String card = root.elementTextTrim("Card");
                String password = root.elementTextTrim("PWD");
                String fingers = root.elementTextTrim("Fingers");
                String faceEnrolledStr = root.elementTextTrim("FaceEnrolled");

                
                Integer department = departmentStr != null ? Integer.parseInt(departmentStr) : 0;
                boolean enabled = "Yes".equalsIgnoreCase(enabledStr);
                Integer timeSet1 = timeSet1Str != null ? Integer.parseInt(timeSet1Str) : 0;
                Integer timeSet2 = timeSet2Str != null ? Integer.parseInt(timeSet2Str) : 0;
                Integer timeSet3 = timeSet3Str != null ? Integer.parseInt(timeSet3Str) : 0;
                Integer timeSet4 = timeSet4Str != null ? Integer.parseInt(timeSet4Str) : 0;
                Integer timeSet5 = timeSet5Str != null ? Integer.parseInt(timeSet5Str) : 0;
                boolean userPeriodUsed = "Yes".equalsIgnoreCase(userPeriodUsedStr);
                Integer userPeriodStart = userPeriodStartStr != null ? Integer.parseInt(userPeriodStartStr) : 0;
                Integer userPeriodEnd = userPeriodEndStr != null ? Integer.parseInt(userPeriodEndStr) : 0;
                boolean faceEnrolled = "Yes".equalsIgnoreCase(faceEnrolledStr);

                
                User user = userService.getUserByIdAndDeviceSerialNumber(userId,deviceSerialNo).orElse(new User());
                user.setUserId(userId);
                user.setName(name);
                user.setPrivilege(privilege);
                user.setDepartment(department);
                user.setEnabled(enabled);
                user.setTimeSet1(timeSet1);
                user.setTimeSet2(timeSet2);
                user.setTimeSet3(timeSet3);
                user.setTimeSet4(timeSet4);
                user.setTimeSet5(timeSet5);
                user.setUserPeriodUsed(userPeriodUsed);
                user.setUserPeriodStart(userPeriodStart);
                user.setUserPeriodEnd(userPeriodEnd);
                user.setCard(card);
                user.setPassword(password);
                user.setFingers(fingers);
                user.setFaceEnrolled(faceEnrolled);
                user.setDeviceSerialNumber(deviceSerialNo);
                userService.saveUser(user);
                log.info("Saved first user data for user ID: {}", userId);

                
                boolean more = "Yes".equalsIgnoreCase(moreStr);
                if (more) {
                    
                    userSyncService.continueUserSync(deviceSerialNo);
                } else {
                    
                    userSyncService.completeUserSync(deviceSerialNo);
                }
            } else {
                log.warn("Failed to get first user data, result: {}", result);
                userSyncService.completeUserSync(deviceSerialNo);
            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetFirstUserData response", e);
            return null;
        }
    }

    private String handleGetNextUserDataResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String result = root.elementTextTrim("Result");
            String moreStr = root.elementTextTrim("More");

            if ("OK".equals(result)) {
                
                String userId = root.elementTextTrim("UserID");
                String encodedName = root.elementTextTrim("Name");
                String name = decodeBase64Utf16Name(encodedName);
                String privilege = root.elementTextTrim("Privilege");
                String departmentStr = root.elementTextTrim("Depart");
                String enabledStr = root.elementTextTrim("Enabled");
                String timeSet1Str = root.elementTextTrim("TimeSet1");
                String timeSet2Str = root.elementTextTrim("TimeSet2");
                String timeSet3Str = root.elementTextTrim("TimeSet3");
                String timeSet4Str = root.elementTextTrim("TimeSet4");
                String timeSet5Str = root.elementTextTrim("TimeSet5");
                String userPeriodUsedStr = root.elementTextTrim("UserPeriod_Used");
                String userPeriodStartStr = root.elementTextTrim("UserPeriod_Start");
                String userPeriodEndStr = root.elementTextTrim("UserPeriod_End");
                String card = root.elementTextTrim("Card");
                String password = root.elementTextTrim("PWD");
                String fingers = root.elementTextTrim("Fingers");
                String faceEnrolledStr = root.elementTextTrim("FaceEnrolled");

                
                Integer department = departmentStr != null ? Integer.parseInt(departmentStr) : 0;
                boolean enabled = "Yes".equalsIgnoreCase(enabledStr);
                Integer timeSet1 = timeSet1Str != null ? Integer.parseInt(timeSet1Str) : 0;
                Integer timeSet2 = timeSet2Str != null ? Integer.parseInt(timeSet2Str) : 0;
                Integer timeSet3 = timeSet3Str != null ? Integer.parseInt(timeSet3Str) : 0;
                Integer timeSet4 = timeSet4Str != null ? Integer.parseInt(timeSet4Str) : 0;
                Integer timeSet5 = timeSet5Str != null ? Integer.parseInt(timeSet5Str) : 0;
                boolean userPeriodUsed = "Yes".equalsIgnoreCase(userPeriodUsedStr);
                Integer userPeriodStart = userPeriodStartStr != null ? Integer.parseInt(userPeriodStartStr) : 0;
                Integer userPeriodEnd = userPeriodEndStr != null ? Integer.parseInt(userPeriodEndStr) : 0;
                boolean faceEnrolled = "Yes".equalsIgnoreCase(faceEnrolledStr);

                
                User user = userService.getUserByIdAndDeviceSerialNumber(userId,deviceSerialNo).orElse(new User());
                user.setUserId(userId);
                user.setName(name);
                user.setPrivilege(privilege);
                user.setDepartment(department);
                user.setEnabled(enabled);
                user.setTimeSet1(timeSet1);
                user.setTimeSet2(timeSet2);
                user.setTimeSet3(timeSet3);
                user.setTimeSet4(timeSet4);
                user.setTimeSet5(timeSet5);
                user.setUserPeriodUsed(userPeriodUsed);
                user.setUserPeriodStart(userPeriodStart);
                user.setUserPeriodEnd(userPeriodEnd);
                user.setCard(card);
                user.setPassword(password);
                user.setFingers(fingers);
                user.setFaceEnrolled(faceEnrolled);
                user.setDeviceSerialNumber(deviceSerialNo);
              userService.saveUser(user);

                log.info("id: {},UserId:{}, deviceSerialNumber: {}", user.getId(),user.getUserId(), user.getDeviceSerialNumber());
                log.info("Saved next user data for user ID: {}", userId);

                
                boolean more = "Yes".equalsIgnoreCase(moreStr);
                if (more) {
                    
                    userSyncService.continueUserSync(deviceSerialNo);
                } else {
                    
                    userSyncService.completeUserSync(deviceSerialNo);
                }
            } else {
                log.warn("Failed to get next user data, result: {}", result);
                userSyncService.completeUserSync(deviceSerialNo);
            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetNextUserData response", e);
            return null;
        }
    }

    private String handleGetDeviceStatusResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String paramName = root.elementTextTrim("ParamName");
            String value = root.elementTextTrim("Value");

            if (deviceSerialNo == null || paramName == null || value == null) {
                log.warn("Missing required fields in GetDeviceStatus response");
                return null;
            }

            
            DeviceStatus deviceStatus = deviceStatusService.getDeviceStatus(deviceSerialNo)
                    .orElse(DeviceStatus.builder()
                            .deviceSerialNo(deviceSerialNo)
                            .build());

            Optional<Device> device = deviceService.getDeviceBySerialNumber(deviceSerialNo);
            if(device.isPresent()){
                Device _device = device.get();
                if(_device.getBranch()!=null)
                    deviceStatus.setBranch(_device.getBranch());
                if(_device.getCompany()!=null)
                    deviceStatus.setCompany(_device.getCompany());

            }

            switch (paramName) {
                case "ManagerCount":
                    deviceStatus.setManagerCount(Integer.parseInt(value));
                    break;
                case "UserCount":
                    deviceStatus.setUserCount(Integer.parseInt(value));
                    break;
                case "FaceCount":
                    deviceStatus.setFaceCount(Integer.parseInt(value));
                    break;
                case "FpCount":
                    deviceStatus.setFpCount(Integer.parseInt(value));
                    break;
                case "CardCount":
                    deviceStatus.setCardCount(Integer.parseInt(value));
                    break;
                case "PwdCount":
                    deviceStatus.setPwdCount(Integer.parseInt(value));
                    break;
                case "DoorStatus":
                    deviceStatus.setDoorStatus(Integer.parseInt(value));
                    break;
                case "AlarmStatus":
                    deviceStatus.setAlarmStatus(Integer.parseInt(value));
                    break;
                default:
                    log.debug("Unhandled device status parameter: {}", paramName);
            }

            
            deviceStatusService.updateDeviceStatus(deviceStatus);
            log.info("Updated device status parameter {} for device: {}", paramName, deviceSerialNo);

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetDeviceStatus response", e);
            return null;
        }
    }

    private String handleGetDeviceStatusAllResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");

            log.info("Received GetDeviceStatusAll response for device: {}", deviceSerialNo);
            DeviceStatus deviceStatus = deviceStatusService.getDeviceStatus(deviceSerialNo)
                    .orElse(DeviceStatus.builder()
                            .deviceSerialNo(deviceSerialNo)
                            .build());

            
            String managerCountStr = root.elementTextTrim("ManagerCount");
            String userCountStr = root.elementTextTrim("UserCount");
            String faceCountStr = root.elementTextTrim("FaceCount");
            String fpCountStr = root.elementTextTrim("FpCount");
            String cardCountStr = root.elementTextTrim("CardCount");
            String pwdCountStr = root.elementTextTrim("PwdCount");
            String doorStatusStr = root.elementTextTrim("DoorStatus");
            String alarmStatusStr = root.elementTextTrim("AlarmStatus");

            
            if (managerCountStr != null) deviceStatus.setManagerCount(Integer.parseInt(managerCountStr));
            if (userCountStr != null) deviceStatus.setUserCount(Integer.parseInt(userCountStr));
            if (faceCountStr != null) deviceStatus.setFaceCount(Integer.parseInt(faceCountStr));
            if (fpCountStr != null) deviceStatus.setFpCount(Integer.parseInt(fpCountStr));
            if (cardCountStr != null) deviceStatus.setCardCount(Integer.parseInt(cardCountStr));
            if (pwdCountStr != null) deviceStatus.setPwdCount(Integer.parseInt(pwdCountStr));
            if (doorStatusStr != null) deviceStatus.setDoorStatus(Integer.parseInt(doorStatusStr));
            if (alarmStatusStr != null) deviceStatus.setAlarmStatus(Integer.parseInt(alarmStatusStr));

            Optional<Device> device = deviceService.getDeviceBySerialNumber(deviceSerialNo);
            if (device.isPresent()) {
                Device _device = device.get();
                if (_device.getBranch() != null)
                    deviceStatus.setBranch(_device.getBranch());
                if (_device.getCompany() != null)
                    deviceStatus.setCompany(_device.getCompany());
            }





            deviceStatusService.updateDeviceStatus(deviceStatus);
            log.info("Updated device status for device: {}", deviceSerialNo);

            
            deviceStatusService.completeDeviceStatusQuery(deviceSerialNo);





            return null; 
        } catch (Exception e) {
            log.error("Error processing GetDeviceStatusAll response", e);
            return null;
        }
    }

    private String handleGetFirmwareVersionResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String version = root.elementTextTrim("Version");
            String buildNumber = root.elementTextTrim("BuildNumber");

            
            DeviceStatus deviceStatus = deviceStatusService.getDeviceStatus(deviceSerialNo)
                    .orElse(DeviceStatus.builder()
                            .deviceSerialNo(deviceSerialNo)
                            .build());

            
            deviceStatus.setFirmwareVersion(version);
            deviceStatus.setBuildNumber(buildNumber);

            
            deviceStatusService.updateDeviceStatus(deviceStatus);
            log.info("Updated firmware info for device: {}", deviceSerialNo);

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetFirmwareVersion response", e);
            return null;
        }
    }

    private String decodeBase64Utf16Name(String encodedString) {
        if (encodedString == null || encodedString.isEmpty()) {
            return "";
        }

        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedString);
            String result = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_16LE);

            if (result.contains("�")) {
                String utf8Result = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
                if (!utf8Result.contains("�")) {
                    log.info("Successfully decoded name using UTF-8 instead of UTF-16LE");
                    return utf8Result;
                }

                if (decodedBytes.length >= 2) {

                    if (decodedBytes[0] == (byte)0xFF && decodedBytes[1] == (byte)0xFE) {
                        return new String(decodedBytes, 2, decodedBytes.length - 2, java.nio.charset.StandardCharsets.UTF_16LE);
                    }

                    else if (decodedBytes[0] == (byte)0xFE && decodedBytes[1] == (byte)0xFF) {
                        return new String(decodedBytes, 2, decodedBytes.length - 2, java.nio.charset.StandardCharsets.UTF_16BE);
                    }
                }

                
                String asciiResult = new String(decodedBytes, java.nio.charset.StandardCharsets.US_ASCII);
                log.warn("Falling back to ASCII decoding for name: {}", asciiResult);
                return asciiResult;
            }

            return result;
        } catch (IllegalArgumentException e) {
            log.warn("The name doesn't appear to be base64 encoded, returning as-is: {}", encodedString);
            return encodedString;
        } catch (Exception e) {
            log.error("Error decoding base64 string: {}", encodedString, e);
            return encodedString;
        }
    }
    private String handleSetUserDataResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String userId = root.elementTextTrim("UserID");
            String type = root.elementTextTrim("Type");
            String result = root.elementTextTrim("Result");

            log.info("Received SetUserData response for user ID: {}, type: {}, result: {}", userId, type, result);
            if (result.equalsIgnoreCase("ok")){
                bulkUserRepository.updateStatus(userId,deviceSerialNo).ifPresent(e->{e.setStatus("COMPLETED");bulkUserRepository.save(e);});
            }

            
            List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                    .stream()
                    .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                            cmd.getUserId().equals(userId) &&
                            cmd.getCommandType().equalsIgnoreCase(type))
                    .collect(java.util.stream.Collectors.toList());

            if (!pendingCommands.isEmpty()) {
                
                ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                String status = "OK".equals(result) ? "COMPLETED" : "FAILED";
                scheduledCommandService.updateCommandStatus(command.getId(), status, root.asXML());
                if(status.equalsIgnoreCase("completed")&&type.equalsIgnoreCase("delete"))
                    userService.deleteUser(userId,deviceSerialNo);
                userFingerDataService.deleteUserFingerData(userId,deviceSerialNo);
                userFaceDataService.deleteUserFaceData(userId,deviceSerialNo);


            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing SetUserData response", e);
            return null;
        }
    }

    private String handleGetGlogPosInfoResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String logCountStr = root.elementTextTrim("LogCount");
            String maxCountStr = root.elementTextTrim("MaxCount");
            

            
                Integer logCount = logCountStr != null ? Integer.parseInt(logCountStr.trim()) : 0;
                Integer maxCount = maxCountStr != null ? Integer.parseInt(maxCountStr.trim()) : 0;

                
                deviceLogStatusService.updateDeviceLogStatus(
                        deviceSerialNo, terminalType, terminalId, logCount, maxCount);

                log.info("Updated log position info for device: {}, log count: {}, max count: {}",
                        deviceSerialNo, logCount, maxCount);




            return null; 
        } catch (Exception e) {
            log.error("Error processing GetGlogPosInfo response", e);
            return null;
        }
    }
    private String handleGetDeviceInfoExtResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String paramName = root.elementTextTrim("ParamName");
            String value1 = root.elementTextTrim("Value1");
            String value2 = root.elementTextTrim("Value2");
            String value3 = root.elementTextTrim("Value3");
            String value4 = root.elementTextTrim("Value4");
            String value5 = root.elementTextTrim("Value5");

            
            deviceAdditionalInfoService.updateDeviceAdditionalInfo(
                    deviceSerialNo, terminalType, terminalId, paramName,
                    value1, value2, value3, value4, value5);

            log.info("Updated device additional info for device: {}, param: {}", deviceSerialNo, paramName);

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetDeviceInfoExt response", e);
            return null;
        }
    }
    private String handleGetEthernetSettingResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String dhcp = root.elementTextTrim("DHCP");
            String ip = root.elementTextTrim("IP");
            String subnet = root.elementTextTrim("Subnet");
            String defaultGateway = root.elementTextTrim("DefaultGateway");
            String portStr = root.elementTextTrim("Port");
            String macAddress = root.elementTextTrim("MacAddress");
            String ipFromDhcp = root.elementTextTrim("IP_from_dhcp");
            String subnetFromDhcp = root.elementTextTrim("Subnet_from_dhcp");
            String defaultGatewayFromDhcp = root.elementTextTrim("DefaultGateway_from_dhcp");

            
            Integer port = portStr != null ? Integer.parseInt(portStr.trim()) : null;

            
            DeviceEthernetSettings settings = DeviceEthernetSettings.builder()
                    .deviceSerialNumber(deviceSerialNo)
                    .terminalType(terminalType)
                    .terminalId(terminalId)
                    .dhcp(dhcp)
                    .ip(ip)
                    .subnet(subnet)
                    .defaultGateway(defaultGateway)
                    .port(port)
                    .macAddress(macAddress)
                    .ipFromDhcp(ipFromDhcp)
                    .subnetFromDhcp(subnetFromDhcp)
                    .defaultGatewayFromDhcp(defaultGatewayFromDhcp)
                    .lastSyncTime(LocalDateTime.now())
                    .build();

            
            deviceEthernetSettingsService.updateEthernetSettings(settings);

            log.info("Updated ethernet settings for device: {}", deviceSerialNo);

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetEthernetSetting response", e);
            return null;
        }
    }

    private String handleGetWiFiSettingResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String use = root.elementTextTrim("Use");
            String ssid = root.elementTextTrim("SSID");
            String key = root.elementTextTrim("Key");
            String dhcp = root.elementTextTrim("DHCP");
            String ip = root.elementTextTrim("IP");
            String subnet = root.elementTextTrim("Subnet");
            String defaultGateway = root.elementTextTrim("DefaultGateway");
            String portStr = root.elementTextTrim("Port");
            String ipFromDhcp = root.elementTextTrim("IP_from_dhcp");
            String subnetFromDhcp = root.elementTextTrim("Subnet_from_dhcp");
            String defaultGatewayFromDhcp = root.elementTextTrim("DefaultGateway_from_dhcp");
            String result = root.elementTextTrim("Result");

            
            Integer port = portStr != null ? Integer.parseInt(portStr.trim()) : null;

            
            DeviceWifiSettings settings = DeviceWifiSettings.builder()
                    .deviceSerialNumber(deviceSerialNo)
                    .terminalType(terminalType)
                    .terminalId(terminalId)
                    .use(use)
                    .ssid(ssid)
                    .key(key)
                    .dhcp(dhcp)
                    .ip(ip)
                    .subnet(subnet)
                    .defaultGateway(defaultGateway)
                    .port(port)
                    .ipFromDhcp(ipFromDhcp)
                    .subnetFromDhcp(subnetFromDhcp)
                    .defaultGatewayFromDhcp(defaultGatewayFromDhcp)
                    .result(result)
                    .lastSyncTime(LocalDateTime.now())
                    .build();

            
            deviceWifiSettingsService.updateWifiSettings(settings);

            log.info("Updated WiFi settings for device: {}", deviceSerialNo);

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetWiFiSetting response", e);
            return null;
        }
    }
    private String handleGetDepartmentResponse(WebSocketSession session, Element root) {
        String deviceSerialNo = null;
        try {
            deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String deptNoStr = root.elementTextTrim("DeptNo");
            String encodedName = root.elementTextTrim("Name");
            String name = decodeBase64Utf16Name(encodedName);
            String error = root.elementTextTrim("Error");

            if (error != null && "Not exist".equals(error)) {
                log.warn("Department {} does not exist for device: {}", deptNoStr, deviceSerialNo);
                return null;
            }
            Integer deptNo = departmentService.getPendingDepartmentRequest(deviceSerialNo);

            if (deptNo == null) {
                log.warn("No pending department request found for device: {}", deviceSerialNo);
                return null;
            }

            departmentService.updateDepartment(deptNo, name, deviceSerialNo);

            log.info("Updated department {} for device: {}", deptNo, deviceSerialNo);

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetDepartment response", e);
            return null;
        }finally {
            if(deviceSerialNo!=null)
                departmentService.clearPendingDepartmentRequest(deviceSerialNo);
        }
    }

    private String handleGetFaceDataResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String userId = root.elementTextTrim("UserID");
            String faceEnrolled = root.elementTextTrim("FaceEnrolled");
            String faceData = root.elementTextTrim("FaceData");
            String result = root.elementTextTrim("Result");

            if ("OK".equals(result)) {
                
                userFaceDataService.updateUserFaceData(userId, deviceSerialNo, faceEnrolled, faceData);

                log.info("Updated face data for user: {}, device: {}", userId, deviceSerialNo);
            } else {
                log.warn("Failed to get face data for user: {}, device: {}, result: {}",
                        userId, deviceSerialNo, result);
            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetFaceData response", e);
            return null;
        }
    }

    private String handleGetFingerDataResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String userId = root.elementTextTrim("UserID");
            String fingerNoStr = root.elementTextTrim("FingerNo");
            String duress = root.elementTextTrim("Duress");
            String fingerData = root.elementTextTrim("FingerData");
            String result = root.elementTextTrim("Result");

            if (result == null) {
                Integer fingerNo = Integer.parseInt(fingerNoStr);


                userFingerDataService.updateUserFingerData(userId, fingerNo, deviceSerialNo, duress, fingerData);

                log.info("Updated finger data for user: {}, finger: {}, device: {}",
                        userId, fingerNo, deviceSerialNo);
            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetFingerData response", e);
            return null;
        }
    }

    private String handleGetUserPhotoResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String terminalType = root.elementTextTrim("TerminalType");
            String terminalId = root.elementTextTrim("TerminalID");
            String userId = root.elementTextTrim("UserID");
            String photoData = root.elementTextTrim("PhotoData");
            String result = root.elementTextTrim("Result");

            if ("OK".equals(result)) {
                
                userPhotoService.updateUserPhoto(userId, deviceSerialNo, photoData);

                log.info("Updated photo for user: {}, device: {}", userId, deviceSerialNo);
            } else {
                log.warn("Failed to get photo for user: {}, device: {}, result: {}",
                        userId, deviceSerialNo, result);
            }

            return null; 
        } catch (Exception e) {
            log.error("Error processing GetUserPhoto response", e);
            return null;
        }
    }
    private String handleSetTimeResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String result = root.elementTextTrim("Result");
            log.info("Received SetTime response for device: {}, result: {}", deviceSerialNo, result);

            List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                    .stream()
                    .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                            cmd.getCommandType().equalsIgnoreCase("SetTime"))
                    .collect(java.util.stream.Collectors.toList());

            if (!pendingCommands.isEmpty()) {
                ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                String status = "OK".equals(result) ? "COMPLETED" : "FAILED";
                scheduledCommandService.updateCommandStatus(command.getId(), status, root.asXML());
            }

            return null; // No response needed
        } catch (Exception e) {
            log.error("Error processing SetTime response", e);
            return null;
        }
    }

    private String handleClearLogDataResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String result = root.elementTextTrim("Result");

            log.info("Received ClearLogData response for device: {}, result: {}", deviceSerialNo, result);

            List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                    .stream()
                    .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                            cmd.getCommandType().equalsIgnoreCase("ClearLogData"))
                    .collect(java.util.stream.Collectors.toList());

            if (!pendingCommands.isEmpty()) {
                ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                String status = "OK".equals(result) ? "COMPLETED" : "FAILED";
                scheduledCommandService.updateCommandStatus(command.getId(), status, root.asXML());
            }

            if ("OK".equals(result)) {
               // timeLogService.clearLogsByDeviceSerialNumber(deviceSerialNo);
                log.info("Successfully cleared logs for device: {}", deviceSerialNo);
            }

            return null;
        } catch (Exception e) {
            log.error("Error processing ClearLogData response", e);
            return null;
        }
    }

    private String handleSetDepartmentResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String deptNoStr = root.elementTextTrim("DeptNo");
            String result = root.elementTextTrim("Result");

            Integer deptNo = deptNoStr != null ? Integer.parseInt(deptNoStr) : null;

            log.info("Received SetDepartment response for device: {}, department: {}, result: {}",
                    deviceSerialNo, deptNo, result);

            List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                    .stream()
                    .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                            cmd.getCommandType().equalsIgnoreCase("SetDepartment") &&
                            cmd.getCommandXml().contains("<DeptNo>" + deptNoStr + "</DeptNo>"))
                    .collect(java.util.stream.Collectors.toList());

            if (!pendingCommands.isEmpty()) {
                ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                String status = "OK".equals(result) ? "COMPLETED" : "FAILED";
                scheduledCommandService.updateCommandStatus(command.getId(), status, root.asXML());
            }
            return null;
        } catch (Exception e) {
            log.error("Error processing SetDepartment response", e);
            return null;
        }
    }

    private String handleSetWiFiSettingResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String result = root.elementTextTrim("Result");

            log.info("Received SetWiFiSetting response for device: {}, result: {}", deviceSerialNo, result);

            List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                    .stream()
                    .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                            cmd.getCommandType().equalsIgnoreCase("SetWiFiSetting"))
                    .collect(java.util.stream.Collectors.toList());

            if (!pendingCommands.isEmpty()) {
                ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                String status = "OK".equals(result) ? "COMPLETED" : "FAILED";
                scheduledCommandService.updateCommandStatus(command.getId(), status, root.asXML());
            }

            if ("OK".equals(result)) {
                String message = messageBuilder.buildGetWiFiSettingRequest();
                sessionManager.sendMessageToDevice(deviceSerialNo, message);
                log.info("Requested updated WiFi settings for device: {}", deviceSerialNo);
            }

            return null;
        } catch (Exception e) {
            log.error("Error processing SetWiFiSetting response", e);
            return null;
        }
    }

    private String handleSetEthernetSettingResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String result = root.elementTextTrim("Result");

            log.info("Received SetEthernetSetting response for device: {}, result: {}", deviceSerialNo, result);

            List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                    .stream()
                    .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                            cmd.getCommandType().equalsIgnoreCase("SetEthernetSetting"))
                    .collect(java.util.stream.Collectors.toList());

            if (!pendingCommands.isEmpty()) {
                ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                String status = "OK".equals(result) ? "COMPLETED" : "FAILED";
                scheduledCommandService.updateCommandStatus(command.getId(), status, root.asXML());
            }

            if ("OK".equals(result)) {
                String message = messageBuilder.buildGetEthernetSettingRequest();
                sessionManager.sendMessageToDevice(deviceSerialNo, message);
                log.info("Requested updated Ethernet settings for device: {}", deviceSerialNo);
            }
            return null;
        } catch (Exception e) {
            log.error("Error processing SetEthernetSetting response", e);
            return null;
        }
    }

    private String handleGetTimeResponse(WebSocketSession session, Element root) {
        try {
            String deviceSerialNo = root.elementTextTrim("DeviceSerialNo");
            String timeStr = root.elementTextTrim("Time");

                LocalDateTime deviceTime = parseDateTime(timeStr);
                log.info("Received device time for device: {}, time: {}", deviceSerialNo, deviceTime);

                DeviceStatus deviceStatus = deviceStatusService.getDeviceStatus(deviceSerialNo)
                        .orElse(DeviceStatus.builder()
                                .deviceSerialNo(deviceSerialNo)
                                .build());

                deviceStatus.setDeviceTime(deviceTime);
                deviceStatusService.updateDeviceStatus(deviceStatus);

                List<ScheduledCommand> pendingCommands = scheduledCommandService.getCommandsByDeviceSerialNumber(deviceSerialNo)
                        .stream()
                        .filter(cmd -> "SENT".equals(cmd.getStatus()) &&
                                cmd.getCommandType().equalsIgnoreCase("GetTime"))
                        .collect(java.util.stream.Collectors.toList());

                if (!pendingCommands.isEmpty()) {
                    ScheduledCommand command = pendingCommands.get(pendingCommands.size() - 1);
                    scheduledCommandService.updateCommandStatus(command.getId(), "COMPLETED", root.asXML());
                }

            return null;
        } catch (Exception e) {
            log.error("Error processing GetTime response", e);
            return null;
        }
    }
}