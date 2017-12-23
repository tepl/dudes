package com.tearulez.dudes.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tearulez.dudes.server.engine.GameModelConfig;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ConfigServer {
    private static final String CONFIG_PLACEHOLDER = "DEFAULT_CONFIG";
    private final Map<String, String> configData = new ConcurrentHashMap<>();

    void startServing(int port) {
        try (FileReader fileReader = new FileReader(new File("server.properties"))) {
            getConfigDataFromReader(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new ViewHandler());
            server.createContext("/save_config", new SubmissionHandler());
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ViewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response;
            try {
                InputStream inputStream = ConfigServer.class.getResource("/config.html").openStream();
                Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines();
                response = lines.collect(Collectors.joining("\n"));
            } catch (Exception e) {
                e.printStackTrace();
                // HttpServer swallows exception, so we return
                return;
            }
            response = response.replace(CONFIG_PLACEHOLDER, getConfigString());
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class SubmissionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            getConfigDataFromReader(new StringReader(t.getRequestURI().getQuery().substring("config=".length())));
            t.getResponseHeaders().add("Location", "/");
            t.sendResponseHeaders(303, 0);
        }
    }

    private String getConfigString() {
        return configData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    private void getConfigDataFromReader(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        for (String name : properties.stringPropertyNames()) {
            configData.put(name, properties.getProperty(name));
        }
    }

    GameModelConfig getGameModelConfig() {
        return new GameModelConfig(configData);
    }
}
