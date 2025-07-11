package com.xaur.service;

import com.xaur.dto.UserData;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserCommandService {

    
    public String buildSetUserDataCommand(String userId, UserData userData) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("SetUserData");
        root.addElement("UserID").setText(userId);
        root.addElement("Type").setText("Set");
        root.addElement("Depart").setText(String.valueOf(userData.getDepartment()));


        if (userData.getName() != null) {
            
            byte[] nameBytes = userData.getName().getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
            String encodedName = java.util.Base64.getEncoder().encodeToString(nameBytes);
            root.addElement("Name").setText(encodedName);
        }

        if (userData.getPrivilege() != null) {
            root.addElement("Privilege").setText(userData.getPrivilege());
        }

        if (userData.getEnabled() != null) {
            root.addElement("Enabled").setText(userData.getEnabled() ? "Yes" : "No");
        }

        addOptionalIntElement(root, "TimeSet1", userData.getTimeSet1());
        addOptionalIntElement(root, "TimeSet2", userData.getTimeSet2());
        addOptionalIntElement(root, "TimeSet3", userData.getTimeSet3());
        addOptionalIntElement(root, "TimeSet4", userData.getTimeSet4());
        addOptionalIntElement(root, "TimeSet5", userData.getTimeSet5());

        if (userData.getUserPeriodUsed() != null) {
            root.addElement("UserPeriod_Used").setText(userData.getUserPeriodUsed() ? "Yes" : "No");
        }

        addOptionalIntElement(root, "UserPeriod_Start", userData.getUserPeriodStart());
        addOptionalIntElement(root, "UserPeriod_End", userData.getUserPeriodEnd());

        if (userData.getCard() != null) {
            root.addElement("Card").setText(userData.getCard());
        }

        if (userData.getPassword() != null) {
            root.addElement("PWD").setText(userData.getPassword());
        }

        if (userData.getFaceData() != null) {
            root.addElement("FaceData").setText(userData.getFaceData());
        }

        if (userData.getAllowNoCertificate() != null) {
            root.addElement("AllowNoCertificate").setText(userData.getAllowNoCertificate() ? "Yes" : "No");
        }

        return document.asXML();
    }

    
    public String buildDeleteUserCommand(String userId) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("SetUserData");
        root.addElement("UserID").setText(userId);
        root.addElement("Type").setText("Delete");

        return document.asXML();
    }

    private void addOptionalIntElement(Element parent, String elementName, Integer value) {
        if (value != null) {
            parent.addElement(elementName).setText(value.toString());
        }
    }

    public String buildSetFingerDataCommand(String userId, Integer fingerNo, String duress, String fingerData) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");

        root.addElement("Request").setText("SetFingerData");
        root.addElement("UserID").setText(userId);
        root.addElement("FingerNo").setText(fingerNo.toString());

        if (duress != null) {
            root.addElement("Duress").setText(duress);
        }

        if (fingerData != null) {
            root.addElement("FingerData").setText(fingerData);
        }

        return document.asXML();
    }


}