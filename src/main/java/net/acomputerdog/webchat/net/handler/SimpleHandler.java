package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.net.WebServer;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * API endpoint that returns a hard-coded string reponse
 */
public class SimpleHandler extends WebHandler {
    private final String page;

    public SimpleHandler(WebServer server, URI uri) throws IOException {
        super(server);
        this.page = new String(Files.readAllBytes(Paths.get(uri)), StandardCharsets.UTF_8);
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
