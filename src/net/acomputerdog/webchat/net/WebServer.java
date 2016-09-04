package net.acomputerdog.webchat.net;

import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.net.handler.ChatHandler;
import net.acomputerdog.webchat.net.handler.SendHandler;
import net.acomputerdog.webchat.net.handler.SimpleHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

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
        System.getProperties().setProperty("sun.net.httpserver.maxReqTime", "10");
        System.getProperties().setProperty("sun.net.httpserver.maxRspTime", "10");
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        this.serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Throwable t) {
                logger.severe("Uncaught exception in server thread!  Server stopping!");
                t.printStackTrace();
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        });
        serverThread.setName("web_server");
        SimpleHandler main = new SimpleHandler(this, getClass().getResourceAsStream("/main.html"));
        server.createContext("/", main);
        server.createContext("/main.html", main);
        server.createContext("/chat", new ChatHandler(this, plugin));
        server.createContext("/send", new SendHandler(this, plugin.getServer(), logger, plugin));
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
