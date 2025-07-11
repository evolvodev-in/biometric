package com.xaur.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToDeviceMap = new ConcurrentHashMap<>();
    private final Map<String, String> deviceToSessionMap = new ConcurrentHashMap<>();

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        String deviceSerialNumber = sessionToDeviceMap.get(sessionId);

        if (deviceSerialNumber != null) {
            deviceToSessionMap.remove(deviceSerialNumber);
            sessionToDeviceMap.remove(sessionId);
        }

        sessions.remove(sessionId);
    }

    public void registerDevice(WebSocketSession session, String deviceSerialNumber) {
        String sessionId = session.getId();
        sessionToDeviceMap.put(sessionId, deviceSerialNumber);
        deviceToSessionMap.put(deviceSerialNumber, sessionId);
    }

    public String getDeviceSerialNumber(WebSocketSession session) {
        return sessionToDeviceMap.get(session.getId());
    }

    public WebSocketSession getSessionByDeviceSerialNumber(String deviceSerialNumber) {
        String sessionId = deviceToSessionMap.get(deviceSerialNumber);
        return sessionId != null ? sessions.get(sessionId) : null;
    }

    public boolean sendMessageToDevice(String deviceSerialNumber, String message) {
        WebSocketSession session = getSessionByDeviceSerialNumber(deviceSerialNumber);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("Failed to send message to device: {}", deviceSerialNumber, e);
            }
        }
        return false;
    }

    public Map<String, WebSocketSession> getAllSessions() {
        return sessions;
    }
}