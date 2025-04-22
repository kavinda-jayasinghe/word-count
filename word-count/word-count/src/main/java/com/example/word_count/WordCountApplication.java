package com.example.word_count;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class WordCountApplication {

	@Value("${node.min-port:8080}")
	private int minPort;

	@Value("${node.max-port:8090}")
	private int maxPort;

	@Value("${node.max-nodes:3}")
	private int maxNodes;

	private static final int MIN_PORT = 8080;
	private static final int MAX_PORT = 8090;
	private static final int MAX_NODES = 5;

	public static void main(String[] args) {
		SpringApplication.run(WordCountApplication.class, args);
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		List<Connector> additionalConnectors = new ArrayList<>();

		int nodesCreated = 0;
		for (int port = MIN_PORT + 1; port <= MAX_PORT && nodesCreated < MAX_NODES; port++) {
			if (isPortAvailable(port)) {
				additionalConnectors.add(createAdditionalConnector(port));
				nodesCreated++;
				System.out.println("Added node on port: " + port);
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


	@Bean
	public ServletWebServerFactory servletContainer(NodeRegistry registry) {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		List<Connector> additionalConnectors = new ArrayList<>();

		int nodesCreated = 0;
		for (int port = MIN_PORT + 1; port <= MAX_PORT && nodesCreated < MAX_NODES; port++) {
			if (isPortAvailable(port)) {
				Connector connector = createAdditionalConnector(port);
				additionalConnectors.add(connector);
				registry.registerNode(port);
				nodesCreated++;
				System.out.println("Added and registered node on port: " + port);
			}
		}

		factory.addAdditionalTomcatConnectors(additionalConnectors.toArray(new Connector[0]));
		return factory;
	}
}