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
    public void handle(HttpExchange exchange) throws IOException {
        ChatList chat = plugin.getChatList();
        int version = chat.getVersion();
        if (page == null || lastVersion < version) { //only rebuild if page changes
            StringBuilder builder = new StringBuilder();
            builder.append("<!DOCTYPE html>\n");
            //builder.append("<meta http-equiv=\"refresh\" content=\"1\" />\n");
            builder.append("<meta http-Equiv=\"Cache-Control\" Content=\"no-cache\" />");
            builder.append("<meta http-Equiv=\"Pragma\" Content=\"no-cache\" />");
            builder.append("<meta http-Equiv=\"Expires\" Content=\"0\" />");
            builder.append("<head>\n");
            builder.append("<style>\n");
            builder.append("p {\n");
            builder.append("margin: 0;\n");
            builder.append("padding: 0;\n");
            builder.append("font-size: 100%;\n");
            builder.append("</style>\n");
            builder.append("</head>\n");
            builder.append("<body>\n");
            builder.append("<p>\n");
            builder.append("<span style=\"font-style: italic;\">\n");
            builder.append("Current server time: $$$");
            builder.append("\n</span>\n");
            builder.append("<br>\n<br>\n");
            chat.forEach(line -> {
                builder.append(line);
                builder.append("\n<br>\n");
            });
            builder.append("</p>\n");
            builder.append("</body>\n");

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
