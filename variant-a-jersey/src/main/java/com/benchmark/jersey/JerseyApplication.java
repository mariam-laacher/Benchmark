package com.benchmark.jersey;

import com.benchmark.jersey.config.JerseyBinder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class JerseyApplication {
    public static final String BASE_URI = "http://localhost:8081/";

    public static HttpServer startServer() {
        ResourceConfig config = new ResourceConfig();
        config.packages("com.benchmark.jersey");
        config.register(new JerseyBinder());
        MetricsServer.start();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) {
        HttpServer server = startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
            MetricsServer.stop();
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

