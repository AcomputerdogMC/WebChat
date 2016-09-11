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

    public abstract void handleExchange(HttpExchange exchange) throws IOException;

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            handleExchange(exchange);
        } catch (Exception e) {
            server.getLogger().severe("Exception occurred handling HttpExchange!");
            e.printStackTrace();
        }
        try {
            //read any left over data
            readMessage(exchange.getRequestBody());
        } catch (IOException ignored) {
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
            exchange.sendResponseHeaders(code, 0);
        }
    }

    protected String readMessage(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        Reader reader = new InputStreamReader(in, "utf-8");
        while (reader.ready()) {
            builder.append((char) reader.read());
        }
        return builder.toString();
    }
}
