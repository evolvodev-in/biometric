package com.xaur.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ChunkedDataService {
    private final Map<String, ByteArrayOutputStream> dataBuffers = new ConcurrentHashMap<>();

    public void startChunkedTransfer(String deviceSerialNumber, String userId, String dataType) {
        String key = generateKey(deviceSerialNumber, userId, dataType);
        dataBuffers.put(key, new ByteArrayOutputStream());
    }

    public void addChunk(String deviceSerialNumber, String userId, String dataType, byte[] chunk) {
        String key = generateKey(deviceSerialNumber, userId, dataType);
        ByteArrayOutputStream buffer = dataBuffers.get(key);
        if (buffer != null) {
            try {
                buffer.write(chunk);
            } catch (IOException e) {
                log.error("Error writing chunk to buffer", e);
            }
        }
    }

    public byte[] completeTransfer(String deviceSerialNumber, String userId, String dataType) {
        String key = generateKey(deviceSerialNumber, userId, dataType);
        ByteArrayOutputStream buffer = dataBuffers.remove(key);
        return buffer != null ? buffer.toByteArray() : null;
    }

    private String generateKey(String deviceSerialNumber, String userId, String dataType) {
        return deviceSerialNumber + ":" + userId + ":" + dataType;
    }
}
