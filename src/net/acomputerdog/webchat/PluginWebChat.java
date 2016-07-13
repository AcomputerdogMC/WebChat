package net.acomputerdog.webchat;

import net.acomputerdog.webchat.net.WebServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PluginWebChat extends JavaPlugin implements Listener {
    /*
    Delay in milliseconds between chat messages
     */
    public static int CHAT_DELAY = 750;
    public static int MAX_LINES = 50;

    private WebServer webServer;
    private ChatList chatList;
    private Date date;
    private Calendar calendar;

    @Override
    public void onEnable() {
        date = new Date();
        chatList = new ChatList();
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("EST"));

        getLogger().info("Starting web server...");
        try {
            webServer = new WebServer(this);
            webServer.start();
        } catch (IOException e) {
            getLogger().severe("Exception starting web server!");
            e.printStackTrace();
            stopServer();
        }
        getLogger().info("Server started.");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping server...");
        stopServer();
        getLogger().info("Server stopped.");
        webServer = null;
        chatList = null;
        date = null;
        calendar = null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        String line = "[" + getFormattedTime() + "][" +
                e.getPlayer().getName() +
                "] " +
                e.getMessage();

        chatList.addLine(line);
    }

    private void stopServer() {
        if (webServer != null) {
            try {
                webServer.stop();
            } catch (Throwable ignored) {
            }
        }
    }

    public ChatList getChatList() {
        return chatList;
    }

    public String getFormattedTime() {
        date.setTime(System.currentTimeMillis());
        calendar.setTime(date);
        return prefix(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + prefix(calendar.get(Calendar.MINUTE)) + ":" + prefix(calendar.get(Calendar.SECOND)) + " EST";
    }

    private String prefix(int val) {
        String str = String.valueOf(val);
        if (val >= 0 && val < 10) {
            str = "0" + str;
        }
        return str;
    }
}
