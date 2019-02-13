package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.net.WebServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * API endpoint that returns a hard-coded string reponse
 */
public class SimpleHandler extends WebHandler {
    private final String page;

    public SimpleHandler(WebServer server, String resource) throws IOException {
        super(server);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resource), StandardCharsets.UTF_8))) {
            this.page = in.lines().collect(Collectors.joining());
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
