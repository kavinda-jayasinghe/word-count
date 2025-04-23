package com.example.word_count.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Node {
    public enum ROLE {
        COORDINATOR,
        ACCEPTOR,
        PROPOSER,
        LEARNER
    }

    private final int id;
    private final int port;
    private final ROLE role;
    private volatile boolean active;

    public Node(int port, List<Node> existingNodes) {
        this.id = generateRandom4DigitId();
        this.port = port;
        this.active = true;
        this.role = determineRole(existingNodes);
    }

    private int generateRandom4DigitId() {
        return ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private ROLE determineRole(List<Node> existingNodes) {
        // Combine existing nodes with current node
        List<Node> allNodes = new ArrayList<>(existingNodes);
        allNodes.add(this);

        // Sort all nodes by ID
        Collections.sort(allNodes, (n1, n2) -> Integer.compare(n1.getId(), n2.getId()));

        // Find current node's position
        int position = allNodes.indexOf(this);
        int totalNodes = allNodes.size();

        // Only one coordinator (max ID) and one learner (min ID)
        if (position == totalNodes - 1) {
            // Check if this is truly the highest ID
            if (this.id == allNodes.get(totalNodes - 1).getId()) {
                return ROLE.COORDINATOR;
            }
            return ROLE.ACCEPTOR;
        } else if (position == 0) {
            // Check if this is truly the lowest ID
            if (this.id == allNodes.get(0).getId()) {
                return ROLE.LEARNER;
            }
            return ROLE.ACCEPTOR;
        } else {
            // Alternate between ACCEPTOR and PROPOSER for others
            return (position % 2 == 1) ? ROLE.ACCEPTOR : ROLE.PROPOSER;
        }
    }

    // Getters and other methods remain the same
    public int getId() { return id; }
    public int getPort() { return port; }
    public ROLE getRole() { return role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return String.format("Node[id=%04d, port=%d, role=%s, active=%b]",
                id, port, role, active);
    }
}