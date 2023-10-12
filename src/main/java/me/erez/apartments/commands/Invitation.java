package me.erez.apartments.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.erez.apartments.Files.DataManager;
import me.erez.apartments.Main;
import org.bukkit.Bukkit;
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
        String apartmentID;
        try {
            apartmentID = args[1];
        } catch (Exception e){
            apartmentID = "empty";
        }

        String[] form = {player.getName(), apartmentID};
        long expiration = 0L;

        if (answer.equalsIgnoreCase("accept")){

            try {
                for (String[] invitation : plugin.invites.keySet()) {
                    if (invitation[0].equalsIgnoreCase(player.getName())) {
                        expiration = plugin.invites.get(invitation);
                        apartmentID = invitation[1];
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
                RegionManager regions = container.get(plugin.returnApartmentsWorld());
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
                try {
                    Set<String> owners = region.getOwners().getPlayers();
                    Bukkit.getPlayer(owners.iterator().next()).sendMessage(ChatColor.GREEN + player.getName() + " has " +
                            "accepted your invitation!");
                } catch (Exception ignored) {
                }
                plugin.invites.remove(form);

                return true;
            } catch (Exception e){
                player.sendMessage(ChatColor.RED + e.getMessage());
            }
        }

        if (answer.equalsIgnoreCase("acceptt")){

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
            RegionManager regions = container.get(plugin.returnApartmentsWorld());
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
            try {
                Set<String> owners = region.getOwners().getPlayers();
                Bukkit.getPlayer(owners.iterator().next()).sendMessage(ChatColor.GREEN + player.getName() + " has " +
                        "accepted your invitation!");
            } catch (Exception ignored){}
            plugin.invites.remove(form);

            return true;

        }

        if (answer.equalsIgnoreCase("decline")){

        }

        player.sendMessage("Invalid choice");
        return true;
    }







}