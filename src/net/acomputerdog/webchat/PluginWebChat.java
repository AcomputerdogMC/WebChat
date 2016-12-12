package net.acomputerdog.webchat;

import net.acomputerdog.webchat.net.WebServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PluginWebChat extends JavaPlugin implements Listener {
    /*
    Delay in milliseconds between chat messages
     */
    public int chatDelay = 750;
    public int maxLines = 50;
    public int webPort = 8080;

    private WebServer webServer;
    private ChatList chatList;
    private Date date;
    private Calendar calendar;

    @Override
    public void onEnable() {
        loadConfig();

        date = new Date();
        chatList = new ChatList(this);
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

    private void loadConfig() {
        saveDefaultConfig(); //only saves if it doesn't exist

        chatDelay = getConfig().getInt("chat_delay", chatDelay);
        maxLines = getConfig().getInt("max_lines", maxLines);
        webPort = getConfig().getInt("web_port", webPort);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        addMessage(e.getPlayer().getName(), e.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent e) {
        addMessage(null, e.getPlayer().getName() + " joined.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        addMessage(null, e.getPlayer().getName() + " quit.");
    }

    /*
    Handles console commands
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent e) {
        String command = e.getCommand();
        String cmdName = getCommandName(command).toLowerCase();
        if (cmdName.equals("say")) {
            handleSay("SERVER", command);
        } else if (cmdName.equals("me")) {
            handleMe("SERVER", command);
        }
    }

    /*
    Handles player commands
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage();//getCommand();
        String cmdName = getCommandName(command).toLowerCase();
        String playerName = e.getPlayer().getName();
        if (cmdName.equals("/say")) {
            handleSay(playerName, command);
        } else if (cmdName.equals("/me")) {
            handleMe(playerName, command);
        }
    }

    /*
    Parses the text of a /me command
     */
    private void handleMe(String name, String command) {
        String args = getCommandArgs(command);
        if (args != null && !args.isEmpty()) {
            addMessage(name, "* " + name + " " + args);
        }
    }

    /*
    Parses the text of a /say command
     */
    private void handleSay(String name, String command) {
        String args = getCommandArgs(command);
        if (args != null && !args.isEmpty()) {
            addMessage(name, args);
        }
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

    private void addMessage(String name, String message) {
        StringBuilder builder = new StringBuilder(6);
        builder.append('[');
        builder.append(getFormattedTime());
        if (name != null) {
            builder.append("][");
            builder.append(name);
        }
        builder.append("] ");
        builder.append(message);
        chatList.addLine(builder.toString());
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

    private String getCommandName(String command) {
        if (command == null) {
            return null;
        }
        if (command.isEmpty()) {
            return "";
        }
        int idx = command.indexOf(' ');
        if (idx > 0) {
            return command.substring(0, idx);
        } else {
            return command;
        }
    }

    private String getCommandArgs(String command) {
        if (command == null) {
            return null;
        }
        if (command.isEmpty()) {
            return "";
        }
        int idx = command.indexOf(' ');
        if (idx > 0 && idx < command.length() - 1) {
            return command.substring(idx + 1, command.length());
        } else {
            return null;
        }
    }
}
