package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.chat.ChatList;
import net.acomputerdog.webchat.net.WebServer;

import java.io.IOException;

/**
 * API endpoint that retrieves all chat messages after a specified point in time
 *
 * TODO use actual time instead of version
 */
public class ChatUpdateHandler extends WebHandler {
    private final PluginWebChat plugin;

    public ChatUpdateHandler(WebServer server, PluginWebChat plugin) {
        super(server);
        this.plugin = plugin;
    }

    @Override
    public void handleExchange(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, "405 Method not allowed: only GET is accepted.", 405);
                return;
            }
            String request = exchange.getRequestURI().getQuery();
            int version = getOldVersion(request);
            ChatList chat = plugin.getChatList();
            if (version != chat.getVersion()) {
                sendResponse(exchange, chat.toString());
            } else {
                sendEmptyResponse(exchange);
            }
        } catch (IllegalArgumentException e) {
            // TODO proper malformed request response
            sendErrorResponse(exchange, "Invalid query parameter");
        }
    }

    private int getOldVersion(String request) {
        if (request != null) {
            String[] params = request.split("&");
            for (String param : params) {
                String[] var = param.split("=");
                if (var.length == 2 && "version".equals(var[0])) {
                    try {
                        return Integer.parseInt(var[1]);
                    } catch (NumberFormatException ignored) {
                        throw new IllegalArgumentException("version is not an integer");
                    }
                }
            }

            throw new IllegalArgumentException("chat version argument is missing");
        } else {
            throw new IllegalArgumentException("request is null");
        }
    }
}
