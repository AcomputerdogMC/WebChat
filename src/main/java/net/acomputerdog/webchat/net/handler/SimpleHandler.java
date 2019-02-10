package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.net.WebServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SimpleHandler extends WebHandler {
    private final String page;

    public SimpleHandler(WebServer server, InputStream in) {
        super(server);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            while (reader.ready()) {
                builder.append(reader.readLine());
                builder.append('\n');
            }
            this.page = builder.toString();
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load main page!");
        }
    }

    @Override
    public void handleExchange(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, "405 Method not allowed: only GET is accepted.", 405);
            return;
        }
        sendResponse(exchange, page);
    }
}