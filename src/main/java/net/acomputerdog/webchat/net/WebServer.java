package net.acomputerdog.webchat.net;

import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.net.handler.ChatUpdateHandler;
import net.acomputerdog.webchat.net.handler.ChatVersionHandler;
import net.acomputerdog.webchat.net.handler.SendHandler;
import net.acomputerdog.webchat.net.handler.SimpleHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP web server
 */
public class WebServer {
    private final HttpServer server;
    private final Thread serverThread;
    private final Logger logger;
    private final PluginWebChat plugin;

    public WebServer(PluginWebChat plugin) throws IOException {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        //set timeouts.  Is either 10 or 100 seconds (unsure do to closed source/undocumented code)
        //should still set in JVM arguments
        System.setProperty("sun.net.httpserver.maxReqTime", "10");
        System.setProperty("sun.net.httpserver.maxRspTime", "10");
        this.server = HttpServer.create(new InetSocketAddress(plugin.getWebPort()), 0);
        this.serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Uncaught exception in server thread, server stopping.", e);
                plugin.stop();
            }
        });
        serverThread.setName("web_server");
        SimpleHandler main = new SimpleHandler(this, "/web/main.html");
        server.createContext("/", main);
        server.createContext("/main.html", main);
        server.createContext("/js/chatClient.js", new SimpleHandler(this, "/web/js/chatClient.js"));
        server.createContext("/css/main", new SimpleHandler(this, "/web/css/main.css"));
        server.createContext("/updatechat", new ChatUpdateHandler(this, plugin));
        server.createContext("/chatversion", new ChatVersionHandler(this, plugin));
        server.createContext("/send", new SendHandler(this, logger, plugin));
    }

    public void stop() {
        server.stop(0);
    }

    public void start() {
        if (serverThread.isAlive()) {
            throw new IllegalStateException("Server thread is already running!");
        }
        serverThread.start();
    }

    public HttpServer getServer() {
        return server;
    }

    public Thread getServerThread() {
        return serverThread;
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }
}
