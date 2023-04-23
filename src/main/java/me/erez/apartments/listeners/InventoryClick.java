package me.erez.apartments.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.erez.apartments.Apartment;
import me.erez.apartments.ApartmentType;
import me.erez.apartments.Files.DataManager;
import me.erez.apartments.Files.PlotsManager;
import me.erez.apartments.Main;
import me.erez.apartments.Utilities.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class InventoryClick implements Listener {
    private Main plugin;

    public InventoryClick(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void eventus(InventoryClickEvent event) {

        if (event.getView().getTitle().split("§").length != 1) return;

        String title = event.getView().getTitle();
        Player player = (Player) event.getView().getPlayer();
        String playerName = player.getName();
        int slot = event.getSlot();

        if (title.equals(ChatColor.GOLD + "Apartments Menu")){
            event.setCancelled(true);

            if (slot == 5){
                apartmentsForPurchaseMenu(player);
                return;
            }

            if (slot == 3){
                String uuid = player.getUniqueId().toString();
                String path = plugin.getDataFolder().getPath() + "/apartments/" + uuid + ".yml";
                File file = new File(path);
                if (!file.exists()) {
                    player.sendMessage(ChatColor.YELLOW + "You don't have any apartments");
                    player.closeInventory();
                    return;
                }

                HashMap<ApartmentType, String> apartmentTypes = new HashMap<>();
                DataManager dataManager = new DataManager(plugin, uuid);
                dataManager.reloadConfig();
                try{
                    for (String apartment : dataManager.getConfig().getConfigurationSection("owned").getKeys(false)) {
                    String apartmentType = dataManager.getConfig().getString("owned." + apartment + ".type");
                    apartmentTypes.put(plugin.apartmentTypes.get(apartmentType), apartment);
                    }
                } catch (Exception e){
                    player.sendMessage(ChatColor.YELLOW + "You don't have any apartments");
                    player.closeInventory();
                    return;
                }
                int size = apartmentTypes.size();
                int inventorySize = size / 9;
                if (size % 9 != 0)
                    inventorySize++;
                inventorySize *= 9;

                Inventory myApartments = Bukkit.createInventory(null, inventorySize, ChatColor.GOLD + "My apartments");
//                for (int i = 0; i < size; i++) {
//                    myApartments.setItem(i, apartmentTypes.get(i).toItemMyApartments());
//                }

                int index = 0;
                for (ApartmentType apartmentType : apartmentTypes.keySet()){
                    myApartments.setItem(index, apartmentType.toItemMyApartments(apartmentTypes.get(apartmentType)));
                    index++;
                }

                player.openInventory(myApartments);

            }

        }


        Inventory inventory = event.getInventory();

        //my house
        try {

            if (inventory.contains(Material.BARRIER) && inventory.contains(Material.PLAYER_HEAD)){
                event.setCancelled(true);
                String apartmentID = plugin.carryingUUID.get(event.getView().getPlayer().getName())[0];

                //guests section
                if (slot == 2){

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    World world = BukkitAdapter.adapt(player.getWorld());
                    RegionManager regions = container.get(world);
                    ProtectedRegion region = regions.getRegion(ChatColor.stripColor(apartmentID));
                    Set<UUID> members = region.getMembers().getUniqueIds();

                    //remove members
                    if (event.getClick().equals(ClickType.RIGHT)){
                        if (members.isEmpty()){
                            player.closeInventory();
                            player.sendMessage(ChatColor.YELLOW + "You don't have any guests to remove");
                            return;
                        }

                        int inventorySize = Utils.Inventories.figureInventorySize(members.size());

                        Inventory guests = Bukkit.createInventory(null,
                                inventorySize, ChatColor.GOLD + "Current guests");
                        int index = 0;
                        for (UUID uuid : members){
                            String name = Bukkit.getOfflinePlayer(uuid).getName();
                            guests.setItem(index, Utils.Items.createGuiSkullByNameSimple(name, ChatColor.RED + name,
                                    ChatColor.DARK_RED + "Click here to remove " + name));
                        }



                        player.openInventory(guests);


                        return;
                    }

                    ArrayList<ItemStack> availablePlayers = new ArrayList<>();

                    for (Player players : Bukkit.getOnlinePlayers()){
                        UUID uuid = players.getUniqueId();
                        boolean exists = false;

                        if (uuid.equals(player.getUniqueId())) continue;
                        for (UUID uuids : members){
                            if (players.getUniqueId().equals(uuids)) {
                                exists = true;
                                break;
                            }
                        }
                        if (exists) continue;

                        String name = players.getName();
                        availablePlayers.add(
                                Utils.Items.createGuiSkullByNameSimple(name, ChatColor.GREEN + name,
                                        ChatColor.DARK_GREEN + "Click here to invite " + name));

                    }
                    int size = Utils.Inventories.figureInventorySize(availablePlayers.size());
                    Inventory invite = Bukkit.createInventory(null, size, ChatColor.GOLD + "Invite guests");

                    int index = 0;
                    for (ItemStack skull : availablePlayers){
                        invite.setItem(index, skull);
                    }

                    player.openInventory(invite);
            }

                //warp
                if (slot == 4){}

                //sell
                if (slot == 6){

                    Inventory sell = Bukkit.createInventory(null, 9 , ChatColor.RED + "Sell apartment");

                    for (int i = 0; i < 9; i++) {
                        sell.setItem(i, Utils.Items.createGuiItemSimple(Material.GRAY_STAINED_GLASS_PANE, " "));
                    }

                    sell.setItem(3, Utils.Items.createGuiItemSimple(Material.REDSTONE_BLOCK, ChatColor.RED +
                            "Cancel", ChatColor.DARK_RED + "Cancel action"));

                    plugin.reloadConfig();
                    double percentage = plugin.getConfig().getDouble("defaultValues.returnOnSell") / 100.0;

                    ApartmentType apartmentType = plugin.apartmentTypes.get(plugin.carryingUUID.get(playerName)[1]);
                    double refund = apartmentType.getCost() * percentage;

                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.DARK_RED + "Sell your apartment for " + ChatColor.UNDERLINE + "$" + refund);
                    lore.add(ChatColor.BOLD + "" + ChatColor.DARK_RED +
                            "Make sure you haven't left any important items in your chests");

                    sell.setItem(5, Utils.Items.createGuiItemComplex(Material.EMERALD_BLOCK,
                            ChatColor.GREEN + "Sell apartment", lore));


                    player.openInventory(sell);


                }







            }

        } catch (Exception ignored){}

        if (title.equals(ChatColor.GOLD + "Apartments for sale")){
            event.setCancelled(true);
            ItemStack itemStack = event.getCurrentItem();
            Material mat = itemStack.getType();
            if (mat == null || mat.equals(Material.PURPLE_STAINED_GLASS_PANE)) return;
            Inventory confirmPurchase = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Confirm purchase");
            for (int i = 0; i < 9; i++) {
                confirmPurchase.setItem(i, Utils.Items.createGuiItemSimple(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
            confirmPurchase.setItem(4, itemStack);
            confirmPurchase.setItem(2, Utils.Items.createGuiItemSimple(Material.REDSTONE_BLOCK,
                    ChatColor.RED + "Cancel", ChatColor.DARK_RED + "Cancel purchase"));
            confirmPurchase.setItem(6, Utils.Items.createGuiItemSimple(Material.EMERALD_BLOCK,
                    ChatColor.GREEN + "Purchase", ChatColor.DARK_GREEN + "Complete the purchase"));
            player.openInventory(confirmPurchase);
        }
        if (title.equals(ChatColor.GOLD + "Confirm purchase")){
            event.setCancelled(true);
            Material mat = event.getCurrentItem().getType();
            if (mat.equals(Material.REDSTONE_BLOCK)){
                apartmentsForPurchaseMenu(player);
                return;
            }
            if (mat.equals(Material.EMERALD_BLOCK)){


                try {

                    ItemStack itemStack = event.getInventory().getItem(4);
                    ItemMeta meta = itemStack.getItemMeta();
                    double cost = Double.parseDouble(meta.getLore().get(0).split("\\$")[1]);
                    double money = plugin.getEconomy().getBalance(player);
                    if (cost > money) {
                        player.sendMessage(ChatColor.YELLOW + "You don't have enough money to complete that purchase");
                        player.closeInventory();
                        return;
                    }
                    plugin.getEconomy().withdrawPlayer(player, cost);
                    player.sendMessage(ChatColor.GREEN + "You have successfully purchased a new " + meta.getDisplayName());
                    String uuid = player.getUniqueId().toString();
                    String displayName = meta.getDisplayName();
                    displayName = ChatColor.stripColor(displayName);
                    ApartmentType apartmentType = plugin.apartmentTypes.get(displayName);
                    Apartment apartment = new Apartment(uuid, apartmentType);
                    if (plugin.apartments.containsKey(uuid)) {
                        plugin.apartments.get(uuid).add(apartment);
                    } else {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(apartment);
                        plugin.apartments.put(uuid, arrayList);
                    }

                    String size = apartmentType.getPlotSize();
                    String schematicFileName = apartmentType.getSchematicFileName();
                    UUID ownerUUID = player.getUniqueId();

                    configActions(schematicFileName, ownerUUID, size);

                    DataManager dataManager = new DataManager(plugin, ownerUUID.toString());
                    dataManager.reloadConfig();
                    String path = "owned." + uuid;
                    dataManager.getConfig().set(path + ".type", displayName);
                    dataManager.saveConfig();


                    player.closeInventory();

                } catch (Exception e){
                    player.sendMessage(ChatColor.DARK_PURPLE + e.getMessage());
                }

            }
        }
        if (title.equals(ChatColor.GOLD + "My apartments")){
            event.setCancelled(true);
            try {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem.getType().equals(Material.AIR)) return;
                ItemMeta meta = currentItem.getItemMeta();

                String name = ChatColor.stripColor(meta.getDisplayName());
                DataManager dataManager = new DataManager(plugin, event.getView().getPlayer().getUniqueId().toString());
                dataManager.reloadConfig();

                String uuid = null;

                for (String apartment : dataManager.getConfig().getConfigurationSection("owned").getKeys(false)){
                    if (dataManager.getConfig().getString("owned." + apartment + ".type").equals(name)){
                        uuid = apartment;
                        break;
                    }
                }

                if (uuid == null) return;


                String[] formed = {uuid, name};
                plugin.carryingUUID.replace(playerName, formed);

                myApartmentMenu(player);

            } catch (Exception ignored){
                return;
            }
        }
        if (title.equals(ChatColor.GOLD + "Invite guests")){
            event.setCancelled(true);
            ItemStack itemStack = event.getCurrentItem();
            if (!itemStack.getType().equals(Material.PLAYER_HEAD)) return;
            ItemMeta meta = itemStack.getItemMeta();
            String name = ChatColor.stripColor(meta.getDisplayName());
            String apartmentUUID = plugin.carryingUUID.get(event.getView().getPlayer().getName())[0];
            plugin.invites.put(new String[]{name, apartmentUUID}, System.currentTimeMillis() + 60000);
            inventory.setItem(event.getSlot(), null);
            Utils.Sounds.success((Player) event.getView().getPlayer());

            Player invited = Bukkit.getPlayer(name);
            Player inviter = (Player) event.getView().getPlayer();


            DataManager dataManager = new DataManager(plugin, inviter.getUniqueId().toString());
            dataManager.reloadConfig();
            String apartmentType = dataManager.getConfig().getString("owned." + apartmentUUID + ".type");

            invited.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + inviter.getName() +
                    " has invited you to their " + apartmentType + " apartment!"
                    /* + "\n" + "You have 60 seconds before the invitation expires" */);

            TextComponent accept = new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + "[Click Here]");
            TextComponent continuing = new TextComponent(ChatColor.BOLD + "" +  ChatColor.UNDERLINE + "" + ChatColor.GOLD + " to join their apartment");

            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invitation accept " + apartmentUUID));
            continuing.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invitation accept " + apartmentUUID));


            invited.spigot().sendMessage(accept, continuing);
        }

        if (title.equals(ChatColor.GOLD + "Current guests")){
            event.setCancelled(true);
            ItemStack itemStack = event.getCurrentItem();
            if (!itemStack.getType().equals(Material.PLAYER_HEAD)) return;
            ItemMeta meta = itemStack.getItemMeta();
            String name = ChatColor.stripColor(meta.getDisplayName());
            String apartmentUUID = plugin.carryingUUID.get(event.getView().getPlayer().getName())[0];
            inventory.setItem(event.getSlot(), null);
            Utils.Sounds.success((Player) event.getView().getPlayer());

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            World world = BukkitAdapter.adapt(player.getWorld());
            RegionManager regions = container.get(world);
            ProtectedRegion region = regions.getRegion(apartmentUUID);
            region.getMembers().removePlayer(Bukkit.getPlayer(name).getUniqueId());
        }
        if (title.equals(ChatColor.RED + "Sell apartment")){
            event.setCancelled(true);
            //cancel
            if (slot == 3){
                myApartmentMenu(player);
                return;
            }

            //sell apartment
            if (slot == 5){
                try {

                    String apartmentUUID = plugin.carryingUUID.get(playerName)[0];

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    World world = BukkitAdapter.adapt(player.getWorld());
                    RegionManager regions = container.get(world);
                    ProtectedRegion region = regions.getRegion(apartmentUUID);

                    try {

                        for (UUID uuid : region.getMembers().getUniqueIds()) {

                            DataManager dataManager = new DataManager(plugin, uuid.toString());
                            dataManager.reloadConfig();
                            dataManager.getConfig().set("guest." + apartmentUUID, null);
                            dataManager.saveConfig();
                        }

                    } catch (Exception ignored) {
                    }

                    DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
                    dataManager.reloadConfig();
                    dataManager.getConfig().set("owned." + apartmentUUID, null);

                    double percentage = plugin.getConfig().getDouble("defaultValues.returnOnSell") / 100.0;
                    ApartmentType apartmentType = plugin.apartmentTypes.get(plugin.carryingUUID.get(playerName)[1]);
                    double refund = apartmentType.getCost() * percentage;

                    plugin.getEconomy().depositPlayer(player, refund);

                    String size = apartmentType.getPlotSize();
                    PlotsManager plotsManager = new PlotsManager(plugin, size);
                    plotsManager.reloadConfig();
                    String config = plotsManager.getConfig().saveToString();
                    config = config.replace("plots:", "");

                    BlockVector3 minimum = region.getMinimumPoint();
                    BlockVector3 maximum = region.getMaximumPoint();


                    StringBuilder sb = new StringBuilder();
                    sb.append(UUID.randomUUID()).append("\n");

                    double x = (minimum.getX() + maximum.getX()) / 2.0;
                    double y = minimum.getY();
                    double z = (minimum.getZ() + maximum.getZ()) / 2.0;
                    String worldStr = world.getName();
                    sb.append("  x: ").append(x).append("\n");
                    sb.append("  y: ").append(y).append("\n");
                    sb.append("  z: ").append(z).append("\n");
                    sb.append("  world: ").append(worldStr).append("\n");
                    sb.append(config);
                    plotsManager.getConfig().set("plots", null);
                    plotsManager.getConfig().set("plots", sb.toString());
                    plotsManager.saveConfig();

                    if (size.equalsIgnoreCase("small"))
                        paste("45", world, x, y, z);

                    regions.removeRegion(apartmentUUID);


                    plugin.carryingUUID.replace(playerName, new String[]{"", ""});


                    player.sendMessage(ChatColor.YELLOW + "You have successfully sold your apartment for $" + refund);
                } catch (Exception e){
                    player.sendMessage(ChatColor.DARK_PURPLE + e.getMessage());
                }
                // adding the plot to the top of the available plots list (V?)

            }
        }



    }


    public void apartmentsForPurchaseMenu(Player player){
        int types = plugin.apartmentTypes.size();

        Inventory apartmentsForPurchase = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Apartments for sale");
        ItemStack emptyPane = Utils.Items.createGuiItemSimple(Material.PURPLE_STAINED_GLASS_PANE, " ");

        DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());

        dataManager.reloadConfig();
        ArrayList<String> blacklisted = new ArrayList<>();

        try {
            for (String uuid : dataManager.getConfig().getConfigurationSection("owned").getKeys(false))
                blacklisted.add(dataManager.getConfig().getString("owned." + uuid + ".type"));
        } catch (Exception ignored){}




        for (int i = 0; i < 9; i++)
            apartmentsForPurchase.setItem(i, emptyPane);
        for (int i = 45; i < 54; i++)
            apartmentsForPurchase.setItem(i, emptyPane);
        apartmentsForPurchase.setItem(9, emptyPane); apartmentsForPurchase.setItem(18, emptyPane);
        apartmentsForPurchase.setItem(27, emptyPane); apartmentsForPurchase.setItem(36, emptyPane);

        apartmentsForPurchase.setItem(17, emptyPane); apartmentsForPurchase.setItem(26, emptyPane);
        apartmentsForPurchase.setItem(35, emptyPane); apartmentsForPurchase.setItem(44, emptyPane);

        int i = 10;
        for (ApartmentType apartmentType : plugin.apartmentTypes.values()){

            boolean exists = false;

            for (String remove : blacklisted){
                if (remove.equals(apartmentType.getName())) {
                    exists = true;
                    break;
                }
            }

            if (exists) continue;
            apartmentsForPurchase.setItem(i, apartmentType.toItem());
            i++;
            if (i % 9 == 8)
                i += 2;
        }


        player.openInventory(apartmentsForPurchase);
    }

    public void myApartmentMenu(Player player){
        String name = plugin.carryingUUID.get(player.getName())[1];
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GOLD + ChatColor.stripColor(name));
        ItemStack sell = Utils.Items.createGuiItemSimple(Material.BARRIER, ChatColor.RED + "Sell apartment",
                ChatColor.BOLD + "" + ChatColor.DARK_RED + "Warning: this action is irreversible");
        inventory.setItem(6, sell);
        ItemStack home = Utils.Items.createGuiItemSimple(Material.OAK_DOOR,
                ChatColor.AQUA + "Go home", ChatColor.DARK_AQUA + "Warp to your apartment");
        inventory.setItem(4, home);

        //ItemStack partners = Utils.Items.createGuiItemSimple(Material.PLAYER_HEAD,
                //ChatColor.BLUE+ "Guests", ChatColor.DARK_BLUE + "Invite friends to your apartment");
        ArrayList<String> lore = new ArrayList<>();
        String left = ChatColor.GREEN + "[Left Click]";
        String right = ChatColor.RED + "[Right Click]";
        lore.add(left + ChatColor.WHITE + " to invite people");
        lore.add(right + ChatColor.WHITE + " to remove people");
        ItemStack partners = Utils.Items.createGuiItemComplex(Material.PLAYER_HEAD, ChatColor.BLUE + "Guests", lore);

        inventory.setItem(2, partners);

        player.openInventory(inventory);
    }

    public void configActions(String apartmentSchematicName, UUID ownerUUID, String size){
        PlotsManager plotsManager = new PlotsManager(plugin, size.toLowerCase());
        String firstUUID = plotsManager.getConfig().getConfigurationSection("plots").getKeys(false).iterator().next();
        String path = "plots." + firstUUID;
        double x = plotsManager.getConfig().getDouble(path + ".x");
        double y = plotsManager.getConfig().getDouble(path + ".y");
        double z = plotsManager.getConfig().getDouble(path + ".z");
        String worldName = plotsManager.getConfig().getString(path + ".world");
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        World world = BukkitAdapter.adapt(bukkitWorld);
        paste(apartmentSchematicName, world, x, y, z);
        plotsManager.getConfig().set(firstUUID, null);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(world);
        ProtectedCuboidRegion cuboidRegion = (ProtectedCuboidRegion) regions.getRegion(firstUUID);
        DefaultDomain defaultDomain = new DefaultDomain();
        defaultDomain.addPlayer(ownerUUID);
        cuboidRegion.setOwners(defaultDomain);
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

}