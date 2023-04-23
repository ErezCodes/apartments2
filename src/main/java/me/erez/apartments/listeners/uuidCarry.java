package me.erez.apartments.listeners;

import me.erez.apartments.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class uuidCarry implements Listener {
    private Main plugin;

    public uuidCarry(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void eventus(PlayerJoinEvent event) {
        plugin.carryingUUID.put(event.getPlayer().getName(), new String[]{"", ""});
    }

    @EventHandler
    public void leave(PlayerQuitEvent event){
        plugin.carryingUUID.remove(event.getPlayer().getName());
    }


}