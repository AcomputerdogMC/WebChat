package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.acomputerdog.webchat.net.WebServer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Abstract parent class of all web API handlers
 */
public abstract class WebHandler implements HttpHandler {
    private final WebServer server;

    public WebHandler(WebServer server) {
        this.server = server;
    }

    public WebServer getServer() {
        return server;
    }

    public abstract void handleExchange(HttpExchange exchange) throws IOException;

    @Override
    public final void handle(HttpExchange exchange) {
        try {
            handleExchange(exchange);
        } catch (Exception e) {
            server.getLogger().log(Level.SEVERE, "Exception occurred handling HttpExchange", e);
        }

        // empty the stream
        try {
            exchange.getRequestBody().close();
        } catch (IOException ignored) {
            // ignored because the stream may already be closed
        }
        exchange.close();
    }

    protected void sendResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 200);
    }

    protected void sendErrorResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 500);
    }

    protected void sendEmptyResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    protected void sendMissingResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 404);
    }

    protected void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        if (exchange.getRequestMethod().equals("GET") || exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(code, response.getBytes().length);
            Writer writer = new OutputStreamWriter(exchange.getResponseBody());
            writer.write(response);
            writer.close();
        } else {
            exchange.close();
        }
    }

    protected String readBodyMessage(HttpExchange exchange) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                builder.append(reader.readLine());
            }
        }
        return builder.toString();
    }
}
