package com.example.word_count.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.Map;

@Service
public class NodeHealthMonitor {
    private final NodeRegistry nodeRegistry;
    private final RestTemplate restTemplate;

    public NodeHealthMonitor(NodeRegistry nodeRegistry, RestTemplate restTemplate) {
        this.nodeRegistry = nodeRegistry;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 2000) // Check every 2 seconds
    public void monitorNodes() {
        Map<Integer, Boolean> nodes = nodeRegistry.getAllNodes();

        for (Map.Entry<Integer, Boolean> entry : nodes.entrySet()) {
            int port = entry.getKey();
            String healthUrl = "http://localhost:" + port + "/health";

            try {
                String response = restTemplate.getForObject(healthUrl, String.class);
                nodeRegistry.updateNodeStatus(port, true);
            } catch (ResourceAccessException e) {
                nodeRegistry.updateNodeStatus(port, false);
                System.err.println("Node on port " + port + " is DOWN!");
                // You could add automatic restart logic here
            }
        }
    }
}