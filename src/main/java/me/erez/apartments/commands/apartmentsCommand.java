package me.erez.apartments.commands;

import me.erez.apartments.Main;
import me.erez.apartments.Utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class apartmentsCommand implements CommandExecutor {
    private Main plugin;

    public apartmentsCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("apartments").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0)
            apartmentsMenu(player);


        return true;
    }

    public void apartmentsMenu(Player player){
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Apartments Menu");

        /*inventory.setItem(0, Utils.Items.createGuiItemSimple(Material.BARRIER,
            ChatColor.RED + "Close menu", ChatColor.DARK_RED + "click here to close the menu"));*/

        inventory.setItem(2, Utils.Items.createGuiItemSimple(Material.CRIMSON_DOOR,
                ChatColor.LIGHT_PURPLE + "Visit a friend",
                ChatColor.DARK_PURPLE + "Visit a friend's apartment"));

        inventory.setItem(4, Utils.Items.createGuiItemSimple(Material.OAK_DOOR,
                ChatColor.AQUA + "Your apartments", ChatColor.DARK_AQUA + "Check out your apartments"));

        inventory.setItem(6, Utils.Items.createGuiItemSimple(Material.EMERALD,
                ChatColor.GREEN + "Buy apartments", ChatColor.DARK_GREEN + "Buy a new apartment"));



        player.openInventory(inventory);
    }





}