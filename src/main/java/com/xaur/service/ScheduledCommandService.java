package com.xaur.service;

import com.xaur.model.ScheduledCommand;
import com.xaur.repository.ScheduledCommandRepository;
import com.xaur.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledCommandService {

    private final ScheduledCommandRepository commandRepository;
    private final WebSocketSessionManager sessionManager;

    @Transactional
    public ScheduledCommand saveCommand(ScheduledCommand command) {
        return commandRepository.save(command);
    }

    public Optional<ScheduledCommand> getCommandById(Long id) {
        return commandRepository.findById(id);
    }

    public List<ScheduledCommand> getAllCommands() {
        return commandRepository.findAll();
    }

    public List<ScheduledCommand> getCommandsByDeviceSerialNumber(String deviceSerialNumber) {
        return commandRepository.findByDeviceSerialNumber(deviceSerialNumber);
    }

    public List<ScheduledCommand> getPendingCommands() {
        return commandRepository.findByStatus("PENDING");
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void executeScheduledCommands() {
        List<ScheduledCommand> pendingCommands = getPendingCommands();

        for (ScheduledCommand command : pendingCommands) {
            try {
                String deviceSerialNumber = command.getDeviceSerialNumber();

                if (sessionManager.getSessionByDeviceSerialNumber(deviceSerialNumber) == null) {
                    log.warn("Device {} is not connected, skipping command execution", deviceSerialNumber);
                    continue;
                }

                boolean sent = sessionManager.sendMessageToDevice(deviceSerialNumber, command.getCommandXml());

                if (sent) {
                    command.setStatus("SENT");
                    command.setExecutedAt(LocalDateTime.now());
                    commandRepository.save(command);
                    log.info("Command {} sent to device {}", command.getId(), deviceSerialNumber);
                } else {
                    log.error("Failed to send command {} to device {}", command.getId(), deviceSerialNumber);
                }
            } catch (Exception e) {
                log.error("Error executing scheduled command {}", command.getId(), e);
            }
        }
    }

    @Transactional
    public void updateCommandStatus(Long commandId, String status, String responseXml) {
        Optional<ScheduledCommand> commandOpt = commandRepository.findById(commandId);

        if (commandOpt.isPresent()) {
            ScheduledCommand command = commandOpt.get();
            command.setStatus(status);
            command.setResponseXml(responseXml);

            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                command.setCompletedAt(LocalDateTime.now());
            }

            commandRepository.save(command);
        }
    }
}