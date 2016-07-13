package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.acomputerdog.webchat.net.WebServer;

import java.io.*;

public abstract class WebHandler implements HttpHandler {
    private final WebServer server;

    public WebHandler(WebServer server) {
        this.server = server;
    }

    public WebServer getServer() {
        return server;
    }

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

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
        exchange.sendResponseHeaders(code, response.getBytes().length);
        Writer writer = new OutputStreamWriter(exchange.getResponseBody());
        writer.write(response);
        writer.close();
    }

    protected String readMessage(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (reader.ready()) {
            builder.append(reader.readLine());
            builder.append("\n");
        }
        return builder.toString();
    }
}
