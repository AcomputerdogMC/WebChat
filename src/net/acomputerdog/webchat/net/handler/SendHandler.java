package net.acomputerdog.webchat.net.handler;

import com.sun.net.httpserver.HttpExchange;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.net.WebServer;
import org.bukkit.ChatColor;
import org.bukkit.Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SendHandler extends WebHandler {
    private final Server server;
    private final Logger logger;
    private final PluginWebChat plugin;

    private final Map<InetSocketAddress, Timeout> timeouts = new HashMap<>();

    public SendHandler(WebServer server, Server mc, Logger logger, PluginWebChat plugin) {
        super(server);
        this.server = mc;
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public void handleExchange(HttpExchange exchange) throws IOException {
        try {
            clearTimeouts();
            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, "<p>405 Method not allowed: only POST is accepted.</p>", 405);
                return;
            }
            if (!"application/x-www-form-urlencoded".equals(exchange.getRequestHeaders().get("Content-Type").get(0))) {
                sendResponse(exchange, "<p>406 Not acceptable: wrong Content-Type.</p>", 406);
                return;
            }
            if (!checkAndUpdateTimeout(exchange.getRemoteAddress())) {
                sendResponse(exchange, "<p>429 Too many requests: please wait to send multiple messages.</p>", 429);
                return;
            }
            InputStream body = exchange.getRequestBody();
            String message = readMessage(body);
            body.close();

            String[] parts = message.split("&");
            if (parts.length == 0) {
                sendResponse(exchange, "<p>422 Unprocessable Entity: Missing required field \"message\".</p>", 422);
                return;
            }
            if (parts.length > 1) {
                logger.warning("Too many parts in request: \"" + message + "\"");
            }
            String[] messageParts = parts[0].split("=");
            if (messageParts.length != 2) {
                sendResponse(exchange, "<p>400 Malformed request: malformed field.</p>", 400);
                return;
            }
            String chatEncoded = messageParts[1];
            String chatDecoded = URLDecoder.decode(chatEncoded, "UTF-8");
            addChat(exchange.getRemoteAddress(), chatDecoded);
            sendResponse(exchange, "<p>200 OK: message sent.</p>");
        } catch (Exception e) {
            logger.warning("Exception processing request!");
            e.printStackTrace();
            sendErrorResponse(exchange, "<p>500 Internal server error: An exception occurred processing the request.</p>");
        }
    }

    private void clearTimeouts() {
        //the glory of java 8
        timeouts.entrySet().removeIf(entry -> entry.getValue().finished);
    }

    private boolean checkAndUpdateTimeout(InetSocketAddress addr) {
        boolean canSend = true;

        Timeout timeout = timeouts.get(addr);
        if (timeout != null && !timeout.finished) {
            canSend = false;
        } else {
            timeout = new Timeout(plugin.chatDelay);
            timeouts.put(addr, timeout);
        }

        return canSend;
    }

    private void addChat(InetSocketAddress addr, String decoded) {
        String line = filterChat(decoded);
        String address = addr.getAddress().getHostAddress();
        String ip = hashIP(address);
        plugin.getLogger().info("[chat][" + address + "/" + ip + "] " + line);
        server.broadcastMessage("<" + ChatColor.GREEN + ip + ChatColor.WHITE + "> " + line); //send to players
        plugin.getChatList().addLine("[" + plugin.getFormattedTime() + "][" + ip + "] " + line); //add to chat list
    }

    private String filterChat(String decoded) {
        String line = decoded;
        if (line.length() > 256) {
            line = line.substring(0, 256);
        }
        line = line.replace('§', '&');
        return line;
    }

    private String hashIP(String addr) {
        return "WEB/" + Integer.toHexString(addr.hashCode()).toUpperCase();
    }

    private class Timeout {
        private boolean finished = false;

        private Timeout(long time) {
            Thread thread = new Thread(() -> {
                //time remaining as of now
                long remaining = time;
                //time that the current loop started
                long start = System.currentTimeMillis();

                //sleep for time milliseconds
                do {
                    try {
                        Thread.sleep(remaining);
                        remaining = 0;
                    } catch (InterruptedException ignored) {
                        long diff = System.currentTimeMillis() - start;
                        remaining -= diff;
                        start = System.currentTimeMillis();
                    }
                } while (remaining > 0);

                finished = true;
            });

            //daemon thread so that we don't stop JVM
            thread.setDaemon(true);
            thread.start();
        }
    }
}
