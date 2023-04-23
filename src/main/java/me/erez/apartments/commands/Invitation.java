package me.erez.apartments.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
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
        String uuid = args[1];

        String[] form = {player.getName(), uuid};
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
            ProtectedRegion region = regions.getRegion(uuid);
            region.getMembers().addPlayer(player.getUniqueId());

            player.sendMessage(ChatColor.GREEN + "success");

            return true;

        }

        if (answer.equalsIgnoreCase("decline")){

        }

        player.sendMessage("Invalid choice");
        return true;
    }







}