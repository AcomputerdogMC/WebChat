package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.ChatList;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.net.WebServer;

import java.io.IOException;
import java.util.regex.Pattern;

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
            StringBuilder builder = new StringBuilder();
            chat.forEach(line -> {
                builder.append(line);
                builder.append("\n");
            });

            page = builder.toString();
            lastVersion = version;
        }
        sendResponse(exchange, format(page, plugin.getFormattedTime()));
    }

    private String format(String response, String... replacements) throws IOException {
        int next = 0;
        while (response.contains("$$$")) {
            if (replacements.length <= next) {
                break;
            }
            response = response.replaceFirst(Pattern.quote("$$$"), replacements[next]);
            next++;
        }
        return response;
    }
}
