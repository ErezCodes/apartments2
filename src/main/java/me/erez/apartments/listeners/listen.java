package me.erez.apartments.listeners;

import me.erez.apartments.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class listen implements Listener {
    private Main plugin;

    public listen(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void eventus(PlayerInteractAtEntityEvent event) {







    }




}