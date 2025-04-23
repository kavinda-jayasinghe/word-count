package com.example.word_count.config;


import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class NodeRegistry {
    private final Set<Integer> usedIds = ConcurrentHashMap.newKeySet();
    private final List<Node> nodes = new CopyOnWriteArrayList<>();

    public synchronized Node registerNode(int port) {
        Node node;
        do {
            node = new Node(port, new ArrayList<>(nodes));
        } while (!usedIds.add(node.getId()));

        nodes.add(node);
        reassignRoles();
        return node;
    }

    private synchronized void reassignRoles() {
        if (nodes.isEmpty()) return;

        // Sort nodes by ID
        List<Node> sortedNodes = new ArrayList<>(nodes);
        sortedNodes.sort(Comparator.comparingInt(Node::getId));

        // Assign roles
        for (int i = 0; i < sortedNodes.size(); i++) {
            Node node = sortedNodes.get(i);
            Node.ROLE newRole;

            if (i == 0) {
                newRole = Node.ROLE.LEARNER;  // Smallest ID
            } else if (i == sortedNodes.size() - 1) {
                newRole = Node.ROLE.COORDINATOR;  // Largest ID
            } else {
                newRole = (i % 2 == 1) ? Node.ROLE.ACCEPTOR : Node.ROLE.PROPOSER;
            }

            // Update role through reflection since role field is final
            try {
                java.lang.reflect.Field roleField = Node.class.getDeclaredField("role");
                roleField.setAccessible(true);
                roleField.set(node, newRole);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update node role", e);
            }
        }
    }

    public Optional<Node> getCoordinator() {
        return nodes.stream()
                .max(Comparator.comparingInt(Node::getId))
                .filter(n -> n.getRole() == Node.ROLE.COORDINATOR);
    }

    public Optional<Node> getLearner() {
        return nodes.stream()
                .min(Comparator.comparingInt(Node::getId))
                .filter(n -> n.getRole() == Node.ROLE.LEARNER);
    }

    public List<Node> getAcceptors() {
        return nodes.stream()
                .filter(n -> n.getRole() == Node.ROLE.ACCEPTOR)
                .collect(Collectors.toList());
    }

    public List<Node> getProposers() {
        return nodes.stream()
                .filter(n -> n.getRole() == Node.ROLE.PROPOSER)
                .collect(Collectors.toList());
    }

    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes);
    }

    public void updateNodeStatus(int port, boolean isActive) {
        nodes.stream()
                .filter(n -> n.getPort() == port)
                .findFirst()
                .ifPresent(node -> {
                    node.setActive(isActive);
                    System.out.printf("Node %04d (%s) status changed to %s%n",
                            node.getId(), node.getRole(),
                            isActive ? "ACTIVE" : "INACTIVE");
                });
    }

    public synchronized void removeNode(int port) {
        nodes.removeIf(n -> {
            if (n.getPort() == port) {
                usedIds.remove(n.getId());
                return true;
            }
            return false;
        });
        reassignRoles();
    }
}