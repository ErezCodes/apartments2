package me.erez.apartments.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.erez.apartments.Files.DataManager;
import me.erez.apartments.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;


public class Invitation implements CommandExecutor {
    private Main plugin;

    public Invitation(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("invitation").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        String answer = args[0];
        String apartmentID = args[1];

        String[] form = {player.getName(), apartmentID};
        long expiration = 0L;

        if (answer.equalsIgnoreCase("accept")){

            for (String[] invitation : plugin.invites.keySet()){
                if (Arrays.toString(invitation).equals(Arrays.toString(form))){
                    expiration = plugin.invites.get(invitation);
                    form = invitation;
                    break;
                }
            }

            if (expiration == 0L || System.currentTimeMillis() > expiration) {
                player.sendMessage(ChatColor.YELLOW + "This invitation has expired");
                plugin.invites.remove(form);
                return true;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            World world = BukkitAdapter.adapt(player.getWorld());
            RegionManager regions = container.get(world);
            ProtectedRegion region = regions.getRegion(apartmentID);
            region.getMembers().addPlayer(player.getUniqueId());
            String owner = region.getOwners().getUniqueIds().iterator().next().toString();

            DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
            dataManager.reloadConfig();
            DataManager research = new DataManager(plugin, owner);
            research.reloadConfig();
            String apartmentType = research.getConfig().getString("owned." + apartmentID + ".type");

            dataManager.getConfig().set("guest." + owner + "." + apartmentID + ".type", apartmentType);

            dataManager.saveConfig();



            player.sendMessage(ChatColor.GREEN + "success");
            plugin.invites.remove(form);

            return true;

        }

        if (answer.equalsIgnoreCase("decline")){

        }

        player.sendMessage("Invalid choice");
        return true;
    }







}