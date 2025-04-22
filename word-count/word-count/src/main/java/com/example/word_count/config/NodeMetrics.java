package com.example.word_count.config;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;


@Service
public class NodeMetrics {
    private final NodeRegistry nodeRegistry;
    private final MeterRegistry meterRegistry;

    public NodeMetrics(NodeRegistry nodeRegistry, MeterRegistry meterRegistry) {
        this.nodeRegistry = nodeRegistry;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        Gauge.builder("node.status", nodeRegistry, registry -> {
            return registry.getAllNodes().values().stream().filter(b -> b).count();
        }).register(meterRegistry);
    }
}
