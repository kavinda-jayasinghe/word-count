package com.example.word_count;

import com.example.word_count.config.Node;
import com.example.word_count.config.NodeRegistry;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class WordCountApplication {

	@Autowired
	private NodeRegistry nodeRegistry;

	@Value("${node.min-port:8080}")
	private int minPort;

	@Value("${node.max-port:8090}")
	private int maxPort;

	@Value("${node.max-nodes:3}")
	private int maxNodes;

	public static void main(String[] args) {
		SpringApplication.run(WordCountApplication.class, args);
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		List<Connector> additionalConnectors = new ArrayList<>();

		int nodesCreated = 0;
		for (int port = minPort + 1; port <= maxPort && nodesCreated < maxNodes; port++) {
			if (isPortAvailable(port)) {
				additionalConnectors.add(createAdditionalConnector(port));
				Node node = nodeRegistry.registerNode(port);
				nodesCreated++;
				System.out.println(node); // Only print node details
			}
		}

		factory.addAdditionalTomcatConnectors(additionalConnectors.toArray(new Connector[0]));
		return factory;
	}

	private Connector createAdditionalConnector(int port) {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(port);
		return connector;
	}

	private boolean isPortAvailable(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			serverSocket.setReuseAddress(true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}