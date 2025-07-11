package com.xaur.websocket.message;

import com.xaur.dto.DeviceEthernetSettingsDto;
import com.xaur.dto.DeviceWifiSettingsDto;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class MessageBuilder {

    public String buildRegisterResponse(String deviceSerialNo, String token) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Response").setText("Register");
        root.addElement("DeviceSerialNo").setText(deviceSerialNo);
        root.addElement("Token").setText(token);
        root.addElement("Result").setText("OK");

        return document.asXML();
    }

    public String buildLoginResponse(String deviceSerialNo, String result) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Response").setText("Login");
        root.addElement("DeviceSerialNo").setText(deviceSerialNo);
        root.addElement("Result").setText(result);

        return document.asXML();
    }

    public String buildTimeLogResponse(String transId, String result) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Response").setText("TimeLog_v2");
        if (transId != null) {
            root.addElement("TransID").setText(transId);
        }
        root.addElement("Result").setText(result);

        return document.asXML();
    }

    public String buildAdminLogResponse(String transId, String result) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Response").setText("AdminLog_v2");
        if (transId != null) {
            root.addElement("TransID").setText(transId);
        }
        root.addElement("Result").setText(result);

        return document.asXML();
    }

    public String buildKeepAliveResponse(String devTime, String serverTime) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Response").setText("KeepAlive");
        root.addElement("Result").setText("OK");
        root.addElement("DevTime").setText(devTime);
        root.addElement("ServerTime").setText(serverTime);

        return document.asXML();
    }

    public String buildErrorResponse(String errorMessage) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Response").setText("Error");
        root.addElement("Result").setText("Fail");
        root.addElement("Error").setText(errorMessage);

        return document.asXML();
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
    public String buildGetFirstUserDataRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("GetFirstUserData");

        return document.asXML();
    }

    public String buildGetNextUserDataRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("GetNextUserData");

        return document.asXML();
    }

    public String buildGetUserDataRequest(String userId) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("GetUserData");
        root.addElement("UserID").setText(userId);

        return document.asXML();
    }
    public String buildGetDeviceStatusAllRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("GetDeviceStatusAll");

        return document.asXML();
    }

    public String buildGetFirmwareVersionRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("GetFirmwareVersion");

        return document.asXML();
    }

    public String buildGetDeviceStatusRequest(String paramName) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("GetDeviceStatus");
        root.addElement("ParamName").setText(paramName);

        return document.asXML();
    }
    public String buildGetGlogPosInfoRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetGlogPosInfo");
        return document.asXML();
    }
    public String buildGetDeviceInfoExtRequest(String paramName) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetDeviceInfoExt");
        root.addElement("ParamName").setText(paramName);
        return document.asXML();
    }
    public String buildGetEthernetSettingRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetEthernetSetting");
        return document.asXML();
    }
    public String buildGetWiFiSettingRequest() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetWiFiSetting");
        return document.asXML();
    }
    public String buildGetDepartmentRequest(Integer deptNo) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetDepartment");
        root.addElement("DeptNo").setText(String.valueOf(deptNo));
        return document.asXML();
    }
    public String buildGetFaceDataRequest(String userId) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetFaceData");
        root.addElement("UserID").setText(userId);
        return document.asXML();
    }
    public String buildGetFingerDataRequest(String userId, Integer fingerNo, boolean fingerOnly) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetFingerData");
        root.addElement("UserID").setText(userId);
        root.addElement("FingerNo").setText(String.valueOf(fingerNo));
        root.addElement("FingerOnly").setText(fingerOnly ? "1" : "0");
        return document.asXML();
    }
    public String buildGetUserPhotoRequest(String userId) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetUserPhoto");
        root.addElement("UserID").setText(userId);
        return document.asXML();
    }

    public String buildSetTimeCommand(String timeString) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("SetTime");
        root.addElement("Time").setText(timeString);
        return document.asXML();
    }

    public String buildEmptyTimeLogCommand() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("ClearLogData");
        return document.asXML();
    }

    public String buildSetDepartmentCommand(Integer deptNo, String name) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("SetDepartment");
        root.addElement("DeptNo").setText(String.valueOf(deptNo));

        if (name != null) {
            byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
            String encodedName = java.util.Base64.getEncoder().encodeToString(nameBytes);
           // root.addElement("Name").setText(encodedName);
            root.addElement("Data").setText(encodedName);
        }

        return document.asXML();
    }

    public String buildSetWifiSettingsCommand(DeviceWifiSettingsDto settings) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("SetWiFiSetting");

        if (settings.getUse() != null) {
            root.addElement("Use").setText(settings.getUse());
        }

        if (settings.getSsid() != null) {
            root.addElement("SSID").setText(settings.getSsid());
        }

        if (settings.getKey() != null) {
            root.addElement("Key").setText(settings.getKey());
        }

        if (settings.getDhcp() != null) {
            root.addElement("DHCP").setText(settings.getDhcp());
        }

        if (settings.getIp() != null) {
            root.addElement("IP").setText(settings.getIp());
        }

        if (settings.getSubnet() != null) {
            root.addElement("Subnet").setText(settings.getSubnet());
        }

        if (settings.getDefaultGateway() != null) {
            root.addElement("DefaultGateway").setText(settings.getDefaultGateway());
        }

        if (settings.getPort() != null) {
            root.addElement("Port").setText(settings.getPort().toString());
        }

        return document.asXML();
    }

    public String buildSetEthernetSettingsCommand(DeviceEthernetSettingsDto settings) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("SetEthernetSetting");

        if (settings.getDhcp() != null) {
            root.addElement("DHCP").setText(settings.getDhcp());
        }

        if (settings.getIp() != null) {
            root.addElement("IP").setText(settings.getIp());
        }

        if (settings.getSubnet() != null) {
            root.addElement("Subnet").setText(settings.getSubnet());
        }

        if (settings.getDefaultGateway() != null) {
            root.addElement("DefaultGateway").setText(settings.getDefaultGateway());
        }

        if (settings.getPort() != null) {
            root.addElement("Port").setText(settings.getPort().toString());
        }

        return document.asXML();
    }

    public String buildGetTimeCommand() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        root.addElement("Request").setText("GetTime");
        return document.asXML();
    }

}