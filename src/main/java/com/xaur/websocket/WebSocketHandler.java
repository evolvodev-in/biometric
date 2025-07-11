package com.xaur.websocket;

import com.xaur.service.DeviceService;
import com.xaur.websocket.message.MessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final MessageParser messageParser;
    private final DeviceService deviceService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());
        sessionManager.addSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);

        try {
            String response = messageParser.parseAndProcessMessage(session, payload);
            if (response != null && !response.isEmpty()) {
                session.sendMessage(new TextMessage(response));
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            try {
                String errorResponse = "<?xml version=\"1.0\"?><Message><Response>Error</Response><Result>Fail</Result><Error>" + e.getMessage() + "</Error></Message>";
                session.sendMessage(new TextMessage(errorResponse));
            } catch (IOException ioe) {
                log.error("Failed to send error response", ioe);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
        String deviceSerialNumber = sessionManager.getDeviceSerialNumber(session);
        if (deviceSerialNumber != null) {
            deviceService.disconnectDevice(deviceSerialNumber);
        }
        sessionManager.removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: {}", exception.getMessage(), exception);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.error("Error closing session after transport error", e);
        }
    }
}