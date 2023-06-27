package me.erez.apartments.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.erez.apartments.ApartmentType;
import me.erez.apartments.Files.PlotsManager;
import me.erez.apartments.Main;
import me.erez.apartments.Utilities.Utils;
import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.ChatColor.*;


public class devTools implements CommandExecutor {
    private Main plugin;

    public devTools(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("devTools").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String argument = args[0];
        if (!player.isOp()) return true;

        if (argument.length() == 0){
            player.sendMessage(ChatColor.YELLOW + "Please include an argument (addPlot/pastePlot/findCeneter/apartmentTypes)");
            return true;
        }

//        else if (argument.equalsIgnoreCase("toString")){
//            for (String name : plugin.apartments.keySet()){
//                player.sendMessage(name);
//                for (Apartment apartment : plugin.apartments.get(name)){
//                    player.sendMessage(apartment.toString());
//                }
//
//            }
//
//        }
//
//        else if (argument.equalsIgnoreCase("saveall")){
//            plugin.saveAll();
//        }

        else if (argument.equalsIgnoreCase("papi")){
            boolean hasRole = Utils.GroupManager.hasRole(player, "Member");
            player.sendMessage(hasRole + "");
        }

        else if (argument.equalsIgnoreCase("addPlot")){
            String type;
            try {type = args[1];} catch (Exception e){
                player.sendMessage(ChatColor.RED + "please enter a type");
                player.sendMessage(ChatColor.YELLOW + "Valid types: small/medium/big");

                return true;
            }
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player); // WorldEdit's native Player class extends Actor
            SessionManager manager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = manager.get(actor);
            Region region; // declare the region variable
            // note: not necessarily the player's current world, see the concepts page
            World selectionWorld = localSession.getSelectionWorld();
            try {
                if (selectionWorld == null) throw new IncompleteRegionException();
                region = localSession.getSelection(selectionWorld);
            } catch (IncompleteRegionException ex) {
                actor.printError(TextComponent.of("Please make a region selection first."));
                return true;
            }

            String uuid = UUID.randomUUID().toString();

            int width = region.getWidth() - 1;
            int length = region.getLength() - 1;

            if (type.equalsIgnoreCase("small")){

                if (width != 45 || length != 45){
                    player.sendMessage(ChatColor.RED + "The selected region isn't 45x45");
                    incorrectMeasureMessage(player, width, length);
                    return true;
                }

                //Bukkit.broadcastMessage("HERE");
                fillConfig(region, type, uuid);


            }

            else if (type.equalsIgnoreCase("medium")){

                if (width != 63 || length != 93){
                    player.sendMessage(ChatColor.RED + "The selected region isn't 63x93");
                    incorrectMeasureMessage(player, width, length);
                    return true;
                }

                fillConfig(region, type, uuid);
            }

            else if (type.equalsIgnoreCase("big")){

                if (width != 73 || length != 115){
                    player.sendMessage(ChatColor.RED + "The selected region isn't 73x115");
                    incorrectMeasureMessage(player, width, length);
                    return true;
                }

                fillConfig(region, type, uuid);
            }

            else {
                player.sendMessage(ChatColor.RED + "Invalid type");
                player.sendMessage(ChatColor.YELLOW + "Valid types: small/medium/big");
                return true;
            }

            try {
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(selectionWorld);
                regions.addRegion(new ProtectedCuboidRegion(uuid, region.getMinimumPoint(), region.getMaximumPoint()));
                regions.save();
            } catch (StorageException e) {
                e.printStackTrace();
            }



            player.sendMessage("Successfully added a " + type + " region");
            /* you can now use the region object for edits, check for a specific shape, etc. */
        }

        else if (argument.equalsIgnoreCase("pastePlot")){
            player.sendMessage(args[1]);
            PlotsManager plotsManager = new PlotsManager(plugin, "small");
            String firstUUID = plotsManager.getConfig().getConfigurationSection("plots").getKeys(false).iterator().next();
            String path = "plots." + firstUUID;
            double x = plotsManager.getConfig().getDouble(path + ".x");
            double y = plotsManager.getConfig().getDouble(path + ".y");
            double z = plotsManager.getConfig().getDouble(path + ".z");
            String worldName = plotsManager.getConfig().getString(path + ".world");
            org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
            World world = BukkitAdapter.adapt(bukkitWorld);
            paste(args[1], world, x, y, z);
        }

        else if (argument.equalsIgnoreCase("findCenter")){
            com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player); // WorldEdit's native Player class extends Actor
            SessionManager manager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = manager.get(actor);
            Region region; // declare the region variable
            // note: not necessarily the player's current world, see the concepts page
            World selectionWorld = localSession.getSelectionWorld();
            try {
                if (selectionWorld == null) throw new IncompleteRegionException();
                region = localSession.getSelection(selectionWorld);
            } catch (IncompleteRegionException ex) {
                actor.printError(TextComponent.of("Please make a region selection first."));
                return true;
            }

            player.sendMessage(region.getCenter().toString());
        }

        else if (argument.equalsIgnoreCase("apartmentTypes")){
            for (ApartmentType apartmentType : plugin.apartmentTypes.values()){
                player.sendMessage(apartmentType.toString());
            }
            for (String name : plugin.apartmentTypes.keySet()){
                player.sendMessage(name);
            }
        }

        else if (argument.equalsIgnoreCase("test")){
            String test = COLOR_CHAR + "6asdf";
            player.sendMessage(test);
            player.sendMessage(getLastColors(test));
        }

        else if (argument.equalsIgnoreCase("test2")){
            try {
                Inventory inventory = Bukkit.createInventory(null, 9, "test");
                ItemStack testy = Utils.Items.createGuiItemSimple(Material.AIR, "test", "lorey");
                inventory.setItem(0, testy);
                player.openInventory(inventory);
            } catch (Exception e){
                player.sendMessage(e.getMessage());
            }
//            ItemStack testt = inventory.getItem(0);
//            ItemMeta meta = testt.getItemMeta();
//            player.sendMessage(meta.getLore().get(0));
        }

        else if (argument.equalsIgnoreCase("printConfig")){
            PlotsManager plotsManager = new PlotsManager(plugin, "small");
            player.sendMessage(plotsManager.getConfig().saveToString());
        }

        else if (argument.equalsIgnoreCase("carry")){
            player.sendMessage("[0]: " + plugin.carryingUUID.get(player.getName())[0]);
            player.sendMessage("[1]: " + plugin.carryingUUID.get(player.getName())[1]);
        }

        else if (argument.equalsIgnoreCase("config")){
            PlotsManager plotsManager = new PlotsManager(plugin, "small");
            plotsManager.reloadConfig();
            String config = plotsManager.getConfig().saveToString();
            config = config.replace("plots:", "");

            BlockVector3 minimum = BlockVector3.at(0, 0, 0);
            BlockVector3 maximum = BlockVector3.at(45, 255, 45);


            StringBuilder sb = new StringBuilder();
            sb.append(UUID.randomUUID()).append("\n");

            double x = (minimum.getX() + maximum.getX()) / 2.0;
            double y = minimum.getY();
            double z = (minimum.getZ() + maximum.getZ()) / 2.0;
            String worldStr = "world";
            sb.append("  x: ").append(x).append("\n");
            sb.append("  y: ").append(y).append("\n");
            sb.append("  z: ").append(z).append("\n");
            sb.append("  world: ").append(worldStr).append("\n");
            sb.append(config);
            plotsManager.getConfig().set("plots", null);
            String result = sb.toString();
            result = result.replaceAll("\\|", "");
            plotsManager.getConfig().set("plots", result);
            plotsManager.saveConfig();
        }

        else if (argument.equalsIgnoreCase("config2")){

            PlotsManager plotsManager = new PlotsManager(plugin, "small");
            plotsManager.reloadConfig();

            BlockVector3 minimum = BlockVector3.at(0, 0, 0);
            BlockVector3 maximum = BlockVector3.at(45, 255, 45);

            double x = (minimum.getX() + maximum.getX()) / 2.0;
            double y = minimum.getY();
            double z = (minimum.getZ() + maximum.getZ()) / 2.0;
            String worldStr = "world";
            String uuid = UUID.randomUUID().toString();

            String path = "plots." + uuid;
            plotsManager.getConfig().set(path + ".x", x);
            plotsManager.getConfig().set(path + ".y", y);
            plotsManager.getConfig().set(path + ".z", z);
            plotsManager.getConfig().set(path + ".world", worldStr);
            plotsManager.saveConfig();



            try {
                File file = new File(plugin.getDataFolder() + "/apartments/smallPlots.yml");
                String yamlString = null;
                try {
                    byte[] fileBytes = Files.readAllBytes(Paths.get(file.getPath()));
                    yamlString = new String(fileBytes);
                } catch (IOException e) {
                    player.sendMessage("wrong file");
                    return true;
                }

                // Parse the data from the YAML string
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(yamlString);

                String lastKey = null;
                Object lastValue = null;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    lastKey = entry.getKey();
                    lastValue = entry.getValue();
                }
                data.remove(lastKey);
                data.put(lastKey, lastValue);

                // Serialize the modified data to YAML
                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
                String newYamlString = yaml.dump(data);

                // Write the YAML string to the file
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(newYamlString);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e){
                player.sendMessage(ChatColor.DARK_PURPLE + e.getMessage());
            }
        }

        else if (argument.equalsIgnoreCase("set")){

            try {
                org.bukkit.World bukkitWorld = Bukkit.getWorld("world");
                World world = BukkitAdapter.adapt(bukkitWorld);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(world);
                ProtectedRegion cuboidRegion = regions.getRegion("f18d7be1-74f0-4fe2-bf93-284f4275179d");

                World sk8qWorld = BukkitAdapter.adapt(Bukkit.getWorld("world"));
                com.sk89q.worldedit.util.Location location1 = new com.sk89q.worldedit.util.Location(sk8qWorld, 25, 5, 25);
//                LocationFlag locationFlag = new LocationFlag("flag");
//                Vector3 vector3 = Vector3.at(1, 1, 1);
//                BlockVector3 blockVector3 = BlockVector3.at(1, 1, 1);
//                BlockVector2 blockVector2 = BlockVector2.at(1, 1);
//                Vector2 vector2 = Vector2.at(1 , 1);
                cuboidRegion.setFlag(Flags.TELE_LOC, location1);
            } catch (Exception e){
                player.sendMessage(e.getMessage() + "\n" + e.getCause());
            }
        }


        return true;
    }

    public void apartmentsForPurchaseMenu(Player player){
        int types = plugin.apartmentTypes.size();
        int size;
        if (types < 9)
            size = 1;
        else if (types % 9 == 0)
            size = types/9;
        else {
            size = types/9 + 1;
        }

        Inventory apartmentsForPurchase = Bukkit.createInventory(null, size, ChatColor.GOLD + "Apartments for sale");
        for (int i = 0; i < types; i++)
            apartmentsForPurchase.setItem(i, plugin.apartmentTypes.get(i).toItem());

        player.openInventory(apartmentsForPurchase);
    }

    public void fillConfig(Region region, String type, String uuid){

        String path = "plots." + uuid;
        Vector3 vector3 = region.getCenter();
        PlotsManager plotsManager = new PlotsManager(plugin, type.toLowerCase());

        plotsManager.getConfig().set(path + ".x", vector3.getX());
        plotsManager.getConfig().set(path + ".y", vector3.getY());
        plotsManager.getConfig().set(path + ".z", vector3.getZ());
        plotsManager.getConfig().set(path + ".world", region.getWorld().getName());
        plotsManager.saveConfig();

    }

    public void paste(String name, World world, double x, double y, double z){
        Clipboard clipboard = null;
        File file = new File( "plugins/WorldEdit/schematics/" + name + ".schem");
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(x, y, z))
                    // configure here
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    public void incorrectMeasureMessage(Player player, int width, int length){
        player.sendMessage(ChatColor.YELLOW + "Current width: " + width + " | Current length: " + length);
        player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Don't include the borders when selecting a plot");
    }


    public static String getLastColors(String input) {
        String result = "";
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = getByChar(c);

                if (color != null) {
                    result = color + result;

                    // Once we find a color or reset we can stop searching
                    if (color.isColor() || color.equals(RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    class generic<V>{
        V obj;
        generic(V obj) {this.obj = obj;}
        public V getObj() { return this.obj; };
    }



}