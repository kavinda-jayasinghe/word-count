package com.example.word_count.config;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class NodeRegistry {
    private final Map<Integer, Boolean> nodeStatus = new ConcurrentHashMap<>();

    public void registerNode(int port) {
        nodeStatus.put(port, true);
    }

    public Map<Integer, Boolean> getAllNodes() {
        return new ConcurrentHashMap<>(nodeStatus);
    }

    public void updateNodeStatus(int port, boolean isActive) {
        nodeStatus.put(port, isActive);
    }
}