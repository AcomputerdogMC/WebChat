package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.ChatList;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.net.WebServer;

import java.io.IOException;

public class ChatVersionHandler extends WebHandler {
    private final PluginWebChat plugin;

    public ChatVersionHandler(WebServer server, PluginWebChat plugin) {
        super(server);
        this.plugin = plugin;
    }

    @Override
    public void handleExchange(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, "405 Method not allowed: only GET is accepted.", 405);
            return;
        }

        ChatList chat = plugin.getChatList();
        sendResponse(exchange, String.valueOf(chat.getVersion()));
    }
}
