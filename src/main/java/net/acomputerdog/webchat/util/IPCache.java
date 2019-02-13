package net.acomputerdog.webchat.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Records IP addresses of logged in players so that they can use their name from the web chat interface
 *
 * TODO replace with some kind of login system
 */
public class IPCache implements Listener {
    private final Map<String, IPEntry> ipNameMap;
    private final Map<UUID, String> uuidIpMap;

    private final Plugin plugin;

    public IPCache(Plugin plugin) {
        ipNameMap = new HashMap<>();
        uuidIpMap = new HashMap<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent e) {
        String ip = e.getPlayer().getAddress().getAddress().getHostAddress();
        String oldIp = uuidIpMap.get(e.getPlayer().getUniqueId());
        if (oldIp != null && !oldIp.equals(ip)) {
            removeEntry(oldIp, e.getPlayer().getUniqueId());
        }

        IPEntry entry = new IPEntry(ip, e.getPlayer().getUniqueId());

        uuidIpMap.put(e.getPlayer().getUniqueId(), ip);
        ipNameMap.put(ip, entry);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent e) {
        ipNameMap.get(uuidIpMap.get(e.getPlayer().getUniqueId())).refreshEvent();
    }

    public void clear() {
        uuidIpMap.clear();
        for (IPEntry entry : ipNameMap.values()) {
            entry.cancelEvent();
        }
        ipNameMap.clear();
    }

    private void removeEntry(String ip, UUID uuid) {
        ipNameMap.remove(ip);
        uuidIpMap.remove(uuid);
    }

    private class IPEntry implements Runnable {
        private static final long ONE_DAY = 20L * 60L * 60L * 24L;

        private final String ip;
        private UUID uuid;
        private int eventID;

        private IPEntry(String ip, UUID uuid) {
            this.ip = ip;
            this.uuid = uuid;
            registerEvent();
        }

        private void cancelEvent() {
            plugin.getServer().getScheduler().cancelTask(eventID);
        }

        private void refreshEvent() {
            cancelEvent();
            registerEvent();
        }

        private void registerEvent() {
            this.eventID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, ONE_DAY);
        }

        /*
          Called to unregister this event
         */
        @Override
        public void run() {
            if (plugin.getServer().getPlayer(uuid) != null) {
                invalidate();
            } else {
                registerEvent();
            }
        }

        private void invalidate() {
            removeEntry(this.ip, this.uuid);
        }
    }
}
