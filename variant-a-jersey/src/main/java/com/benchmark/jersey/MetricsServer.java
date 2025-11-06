package com.benchmark.jersey;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;

public class MetricsServer {
    private static HTTPServer server;

    public static void start() {
        try {
            DefaultExports.initialize();
            server = new HTTPServer(9091);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start metrics server", e);
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop();
        }
    }
}

