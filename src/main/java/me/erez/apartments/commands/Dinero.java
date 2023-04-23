package me.erez.apartments.commands;

import me.erez.apartments.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Dinero implements CommandExecutor {
    private Main plugin;

    public Dinero(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("dinero").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (!player.isOp()){
            player.sendMessage(ChatColor.RED + "no dinero for you");
            return false;
        }
        plugin.getEconomy().depositPlayer(player, 1000000);
        player.sendMessage(ChatColor.GREEN + "Hoy cobre");

        return true;
    }



}