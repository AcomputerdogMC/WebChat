package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.chat.ChatList;
import net.acomputerdog.webchat.net.WebServer;

import java.io.IOException;

/**
 * Outdated endpoint to retrieve entire chat log
 *
 * TODO why is this still here?
 *
 * @deprecated Inefficient, use ChatUpdateHandler instead
 */
@Deprecated
public class ChatHandler extends WebHandler {
    private final PluginWebChat plugin;
    private String page = null;
    private int lastVersion = -1;

    public ChatHandler(WebServer server, PluginWebChat plugin) {
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
        int version = chat.getVersion();
        if (page == null || lastVersion != version) { //only rebuild if page changes
            lastVersion = chat.getVersion();
            page = chat.toString();
        }
        sendResponse(exchange, page);
    }
}
