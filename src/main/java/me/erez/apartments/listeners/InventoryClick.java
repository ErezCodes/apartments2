package me.erez.apartments.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.erez.apartments.ApartmentType;
import me.erez.apartments.Files.DataManager;
import me.erez.apartments.Files.PlotsManager;
import me.erez.apartments.Files.UsersYmlManager;
import me.erez.apartments.Main;
import me.erez.apartments.Utilities.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static me.erez.apartments.Utilities.Utils.GroupManager.*;

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

            if (slot == 6){
                apartmentsForPurchaseMenu(player);
                return;
            }

            if (slot == 4){
                try {
                    String uuid = player.getUniqueId().toString();
                    String path = plugin.getDataFolder().getPath() + "/apartments/" + uuid + ".yml";
                    File file = new File(path);
                    if (!file.exists()) {
                        player.sendMessage(ChatColor.YELLOW + "You don't have any apartments");
                        player.closeInventory();
                        return;
                    }

                    ArrayList<ApartmentType> apartmentTypes = new ArrayList<>();
                    DataManager dataManager = new DataManager(plugin, uuid);
                    dataManager.reloadConfig();
                    try {
                        for (String apartment : dataManager.getConfig().getConfigurationSection("owned").getKeys(false)) {
                            String apartmentType = dataManager.getConfig().getString("owned." + apartment + ".type");
                            apartmentTypes.add(plugin.apartmentTypes.get(apartmentType));
                        }
                        if (dataManager.getConfig().getConfigurationSection("owned").getKeys(false).isEmpty()) {
                            player.sendMessage(ChatColor.YELLOW + "You don't have any apartments");
                            player.closeInventory();
                            return;
                        }

                    } catch (Exception e) {
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
                    for (ApartmentType apartmentType : apartmentTypes) {
                        myApartments.setItem(index, apartmentType.toItemMyApartments());
                        index++;
                    }

                    player.openInventory(myApartments);
                } catch (Exception ignored){}
            }

            if (slot == 2){
                DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
                ConfigurationSection section = dataManager.getConfig().getConfigurationSection("guest");
                if (section == null ||
                        dataManager.getConfig().getConfigurationSection("guest").getKeys(false).size() == 0) {
                    player.sendMessage(ChatColor.YELLOW + "You haven't been invited to any apartment yet");
                    player.closeInventory();
                    return;
                }
                ArrayList<ItemStack> content = new ArrayList<>();

                dataManager.reloadConfig();
                for (String owner : dataManager.getConfig().getConfigurationSection("guest").getKeys(false)){
                    String ownerName = Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName();
                    content.add(Utils.Items.createGuiSkullByNameSimple(ownerName, ChatColor.LIGHT_PURPLE
                            + "Visit " + ownerName));
                }
                Inventory visit = Bukkit.createInventory(null, Utils.Inventories.figureInventorySize(content.size())
                        , ChatColor.GOLD + "Visit a friend");

                for (ItemStack skull : content)
                    visit.addItem(skull);

                player.openInventory(visit);

            }

        }


        Inventory inventory = event.getInventory();

        //my house
        try {

            if (inventory.contains(Material.BARRIER) && inventory.contains(Material.PLAYER_HEAD)){
                event.setCancelled(true);
                String apartmentID = plugin.carryingUUID.get(player.getName())[0];
                //guests section
                if (slot == 2){

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(plugin.returnApartmentsWorld());
                    ProtectedRegion region = regions.getRegion(ChatColor.stripColor(apartmentID));
                    Set<UUID> members = region.getMembers().getUniqueIds();

                    //remove members
                    if (event.getClick().equals(ClickType.MIDDLE)){
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
                        index++;
                    }

                    player.openInventory(invite);
            }

                //warp
                if (slot == 4){

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(plugin.returnApartmentsWorld());
                    ProtectedRegion region = regions.getRegion(plugin.carryingUUID.get(playerName)[0]);
                    Location location = region.getFlag(Flags.TELE_LOC);
                    com.sk89q.worldedit.entity.Player sk89qPlayer = BukkitAdapter.adapt(player);
                    sk89qPlayer.setLocation(location);

                    player.closeInventory();
                    return;

                }

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
                    String costStr = meta.getLore().get(0).split("\\$")[1];

                    double cost = Utils.Strings.formatMoneyReverse(costStr);
                    double money = plugin.getEconomy().getBalance(player);
                    if (cost > money) {
                        player.sendMessage(ChatColor.YELLOW + "You don't have enough money to complete that purchase");
                        player.closeInventory();
                        return;
                    }
                    plugin.getEconomy().withdrawPlayer(player, cost);
                    player.sendMessage(ChatColor.GREEN + "You have successfully purchased a new " + meta.getDisplayName());
                    String displayName = meta.getDisplayName();
                    displayName = ChatColor.stripColor(displayName);
                    ApartmentType apartmentType = null;
                    for (ApartmentType apartmentTypes: plugin.apartmentTypes.values()){
                        if (displayName.equalsIgnoreCase(apartmentTypes.getName())) {
                            apartmentType = apartmentTypes;
                            break;
                        }
                    }

                    String size = apartmentType.getPlotSize();
                    String schematicFileName = apartmentType.getSchematicFileName();
                    UUID ownerUUID = player.getUniqueId();

                    String type = apartmentType.getType();

                    String apartmentID = configActions(schematicFileName, ownerUUID, size, BukkitAdapter.adapt(player), type, playerName);

                    DataManager dataManager = new DataManager(plugin, ownerUUID.toString());
                    dataManager.reloadConfig();
                    String path = "owned." + apartmentID;
                    dataManager.getConfig().set(path + ".type", apartmentType.getType());
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
                String type = "";

                for (ApartmentType apartmentType : plugin.apartmentTypes.values()){
                    if (ChatColor.stripColor(apartmentType.getName()).equalsIgnoreCase(name)){
                        type = apartmentType.getType();
                        break;
                    }
                }

                DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
                dataManager.reloadConfig();

                String uuid = null;

                for (String apartment : dataManager.getConfig().getConfigurationSection("owned").getKeys(false)){
                    if (dataManager.getConfig().getString("owned." + apartment + ".type").equals(type)){
                        uuid = apartment;
                        break;
                    }
                }

                if (uuid == null) return;


                String[] formed = {uuid, type};
                plugin.carryingUUID.replace(playerName, formed);

                myApartmentMenu(player);

            } catch (Exception e){
                player.sendMessage(ChatColor.RED + e.getMessage());
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
            String type = dataManager.getConfig().getString("owned." + apartmentUUID + ".type");
            String apartmentTypeName = "";
            for (ApartmentType apartmentType : plugin.apartmentTypes.values()){
                if (apartmentType.getType().equalsIgnoreCase(type)){
                    apartmentTypeName = apartmentType.getName();
                    break;
                }
            }

            invited.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + inviter.getName() +
                    " has invited you to their " + apartmentTypeName + " apartment!"
                    /* + "\n" + "You have 60 seconds before the invitation expires" */);

            TextComponent accept = new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + "[Click Here]");
            TextComponent continuing = new TextComponent(ChatColor.BOLD + "" +  ChatColor.UNDERLINE + "" + ChatColor.GOLD + " to join their apartment");

            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invitation acceptt " + apartmentUUID));
            continuing.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invitation acceptt " + apartmentUUID));


            invited.spigot().sendMessage(accept, continuing);
            invited.sendMessage(ChatColor.YELLOW + "(Or type /invitation accept)");
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
            RegionManager regions = container.get(plugin.returnApartmentsWorld());
            ProtectedRegion region = regions.getRegion(apartmentUUID);
            UUID guestUUID = Bukkit.getOfflinePlayer(name).getUniqueId();
            region.getMembers().removePlayer(guestUUID);

            DataManager dataManager = new DataManager(plugin, guestUUID.toString());
            dataManager.reloadConfig();
            String ownerID = player.getUniqueId().toString();
            dataManager.getConfig().set("guest." + ownerID + "." + apartmentUUID, null);
            ConfigurationSection section = dataManager.getConfig().getConfigurationSection("guest." + ownerID);
            if (section == null || section.getKeys(false).size() == 0)
                dataManager.getConfig().set("guest." + ownerID, null);
            dataManager.saveConfig();

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
                    RegionManager regions = container.get(plugin.returnApartmentsWorld());
                    ProtectedRegion region = regions.getRegion(apartmentUUID);
                    String ownerUUID = player.getUniqueId().toString();

                    try {

                        for (UUID uuid : region.getMembers().getUniqueIds()) {

                            DataManager dataManager = new DataManager(plugin, uuid.toString());
                            dataManager.reloadConfig();
                            dataManager.getConfig().set("guest." + ownerUUID + "." + apartmentUUID, null);
                            dataManager.saveConfig();
                        }

                    } catch (Exception ignored) {}

                    DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
                    dataManager.reloadConfig();
                    dataManager.getConfig().set("owned." + apartmentUUID, null);
                    dataManager.saveConfig();

                    double percentage = plugin.getConfig().getDouble("defaultValues.returnOnSell") / 100.0;
                    ApartmentType apartmentType = plugin.apartmentTypes.get(plugin.carryingUUID.get(playerName)[1]);
                    double refund = apartmentType.getCost() * percentage;

                    plugin.getEconomy().depositPlayer(player, refund);

                    String size = apartmentType.getPlotSize();
                    PlotsManager plotsManager = new PlotsManager(plugin, size);
                    plotsManager.reloadConfig();

                    BlockVector3 minimum = region.getMinimumPoint();
                    BlockVector3 maximum = region.getMaximumPoint();



                    double x = (minimum.getX() + maximum.getX()) / 2.0;
                    double y = 4;
                    double z = (minimum.getZ() + maximum.getZ()) / 2.0;

                    //castMessage("x: " + x + "\n" + "y: " + y + "\n" + "z:" + z + "world: " + world);

                    plugin.reloadConfig();
                    String worldStr = plugin.getConfig().getString("defaultValues.ApartmentsWorldName");
                    String newID = UUID.randomUUID().toString();
                    String path = "plots." + newID;

                    plotsManager.getConfig().set(path + ".x", x);
                    plotsManager.getConfig().set(path + ".y", y);
                    plotsManager.getConfig().set(path + ".z", z);
                    plotsManager.getConfig().set(path + ".world", worldStr);
                    plotsManager.saveConfig();

                    if (size.equalsIgnoreCase("small"))
                        paste("45", x, y, z);
                    if (size.equalsIgnoreCase("medium"))
                        paste("medium", x, y, z);
                    if (size.equalsIgnoreCase("big"))
                        paste("big", x, y, z);


                    regions.removeRegion(apartmentUUID);


                    plugin.carryingUUID.replace(playerName, new String[]{"", ""});


                    player.sendMessage(ChatColor.YELLOW + "You have successfully sold your apartment for $" + refund);
                } catch (Exception e){
                    player.sendMessage(ChatColor.DARK_PURPLE + e.getMessage());
                }
                // adding the plot to the top of the available plots list (V?)

            }
        }
        if (title.equals(ChatColor.GOLD + "Visit a friend")){
            event.setCancelled(true);
            ItemStack itemStack = event.getCurrentItem();
            if (!itemStack.getType().equals(Material.PLAYER_HEAD)) return;
            String itemName[] = itemStack.getItemMeta().getDisplayName().split(" ");
            String owner = ChatColor.stripColor(itemName[1]);
            String ownerUUID = Bukkit.getOfflinePlayer(owner).getUniqueId().toString();

            DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
            dataManager.reloadConfig();

            ArrayList<ItemStack> content = new ArrayList<>();

            for (String apartmentID : dataManager.getConfig().getConfigurationSection("guest." + ownerUUID).getKeys(false)){
                ApartmentType apartmentType = plugin.apartmentTypes.get(dataManager.getConfig().getString("guest."
                + ownerUUID + "." + apartmentID + ".type"));
                content.add(apartmentType.toItemVisit());
            }

            Inventory apartments = Bukkit.createInventory(null, Utils.Inventories.figureInventorySize(content.size())
            , ChatColor.GOLD + owner + "s' apartments");

            for (ItemStack item : content)
                apartments.addItem(item);

            player.openInventory(apartments);

        }
        if (title.contains("s'")){
            event.setCancelled(true);
            ClickType clickType = event.getClick();

            if (clickType.isLeftClick()){
                DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
                dataManager.reloadConfig();

                String owner = ChatColor.stripColor(title);
                owner = owner.split(" ")[0];
                owner = owner.substring(0, owner.length() - 2);
                String ownerUUID = Bukkit.getOfflinePlayer(owner).getUniqueId().toString();

                DataManager research = new DataManager(plugin, ownerUUID);
                research.reloadConfig();

                ItemStack itemStack = event.getCurrentItem();
                String type2 = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
                String type = "";
                for (ApartmentType apartmentType : plugin.apartmentTypes.values()){
                    if (type2.equalsIgnoreCase(apartmentType.getName())){
                        type = apartmentType.getType();
                        break;
                    }
                }
                String regionID = null;

                for (String apartmentID : research.getConfig().getConfigurationSection("owned").getKeys(false)){
                    if (research.getConfig().getString("owned." + apartmentID + ".type").equals(type)){
                        regionID = apartmentID;
                        break;
                    }
                }

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(plugin.returnApartmentsWorld());
                ProtectedRegion region = regions.getRegion(regionID);
                Location location = region.getFlag(Flags.TELE_LOC);
                com.sk89q.worldedit.entity.Player sk89qPlayer = BukkitAdapter.adapt(player);
                sk89qPlayer.setLocation(location);

            }

            if (clickType.isShiftClick() && clickType.isRightClick()){
                DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
                dataManager.reloadConfig();

                String owner = ChatColor.stripColor(title);
                owner = owner.split(" ")[0];
                owner = owner.substring(0, owner.length() - 2);
                String ownerUUID = Bukkit.getOfflinePlayer(owner).getUniqueId().toString();

                DataManager research = new DataManager(plugin, ownerUUID);
                research.reloadConfig();

                ItemStack itemStack = event.getCurrentItem();
                String type2 = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
                String type = "";
                for (ApartmentType apartmentType : plugin.apartmentTypes.values()){
                    if (type2.equalsIgnoreCase(apartmentType.getName())){
                        type = apartmentType.getType();
                        break;
                    }
                }
                String regionID = null;

                for (String apartmentID : research.getConfig().getConfigurationSection("owned").getKeys(false)){
                    if (research.getConfig().getString("owned." + apartmentID + ".type").equals(type)){
                        regionID = apartmentID;
                        break;
                    }
                }

                dataManager.getConfig().set("guest." + ownerUUID + "." + regionID, null);

                ConfigurationSection section = dataManager.getConfig().getConfigurationSection("guest." + ownerUUID);
                if (section == null || section.getKeys(false).size() == 0){
                    dataManager.getConfig().set("guest." + owner, null);
                }

                dataManager.saveConfig();

                itemStack.setAmount(0);
                itemStack.setType(Material.AIR);

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(plugin.returnApartmentsWorld());
                ProtectedRegion region = regions.getRegion(regionID);
                region.getMembers().removePlayer(player.getUniqueId());

                Utils.Sounds.success(player);
                return;
            }

        }



    }


    public void apartmentsForPurchaseMenu(Player player){

        int ownedHouses;
        String role = null;

        DataManager dataManager = new DataManager(plugin, player.getUniqueId().toString());
        dataManager.reloadConfig();
        try {
            ownedHouses = dataManager.getConfig().getConfigurationSection("owned").getKeys(false).size();
        } catch (Exception ignored){
            ownedHouses = 0;
        }
        plugin.reloadConfig();
        String tooManyHouses = ChatColor.RED + "You have reached your maximum capacity of owning houses";

//        if (hasRole(player, "Emperor")){
//            if (plugin.getConfig().getInt("defaultValues.EmperorMaxHouses") >= ownedHouses){
//                player.sendMessage(tooManyHouses);
//                return;
//            }
//        }
//
//        if (hasRole(player, "President")){
//            if (plugin.getConfig().getInt("defaultValues.PresidentMaxHouses") >= ownedHouses){
//                player.sendMessage(tooManyHouses);
//                return;
//            }
//        }
//
//        if (hasRole(player, "Governor")){
//            if (plugin.getConfig().getInt("defaultValues.GovernorMaxHouses") >= ownedHouses){
//                player.sendMessage(tooManyHouses);
//                return;
//            }
//        }
//
//        if (hasRole(player, "KingPin")){
//            if (plugin.getConfig().getInt("defaultValues.KingPinMaxHouses") >= ownedHouses){
//                player.sendMessage(tooManyHouses);
//                return;
//            }
//        }
//
//        if (hasRole(player, "Member")){
//            if (plugin.getConfig().getInt("defaultValues.MemberMaxHouses") >= ownedHouses){
//                player.sendMessage(tooManyHouses);
//                return;
//            }
//        }
        //ChatGPT suggestion, might not work lol
        Map<String, Integer> roleMaxHouses = new HashMap<>();
        roleMaxHouses.put("Emperor", plugin.getConfig().getInt("defaultValues.EmperorMaxHouses"));
        roleMaxHouses.put("President", plugin.getConfig().getInt("defaultValues.PresidentMaxHouses"));
        roleMaxHouses.put("Governor", plugin.getConfig().getInt("defaultValues.GovernorMaxHouses"));
        roleMaxHouses.put("Kingpin", plugin.getConfig().getInt("defaultValues.KingPinMaxHouses"));
        roleMaxHouses.put("Member", plugin.getConfig().getInt("defaultValues.MemberMaxHouses"));

        // Check if the player's role has exceeded the maximum house value
        for (Map.Entry<String, Integer> entry : roleMaxHouses.entrySet()) {
            role = entry.getKey();
            int maxHouses = entry.getValue();

            if (hasRole(player, role)){

                if (ownedHouses >= maxHouses) {
                    player.sendMessage(tooManyHouses);
                    return;
                }
                else break;
            }

        }

        Inventory apartmentsForPurchase = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Apartments for sale");
        ItemStack emptyPane = Utils.Items.createGuiItemSimple(Material.PURPLE_STAINED_GLASS_PANE, " ");

        dataManager.reloadConfig();
        ArrayList<String> blacklisted = new ArrayList<>();

        try {
            for (String uuid : dataManager.getConfig().getConfigurationSection("owned").getKeys(false))
                blacklisted.add(dataManager.getConfig().getString("owned." + uuid + ".type"));
        } catch (Exception ignored){}


        try {
            if (!check(player, "apartments.V1"))
                blacklisted.add("V1");

            if (!check(player, "apartments.V3"))
                blacklisted.add("V3");

            if (!check(player, "apartments.V5"))
                blacklisted.add("V5");
        } catch (Exception e){
            player.sendMessage(e.getMessage());
        }

        
        switch(role) {
            case "Emperor":
                break;
            case "President":
                blacklisted.add("Emp");
                break;
            case "Governor":
                blacklisted.add("Emp");
                blacklisted.add("pres");
                break;
            case "Kingpin":
                blacklisted.add("Emp");
                blacklisted.add("pres");
                blacklisted.add("gov");
                break;
            case "Member":
                blacklisted.add("Emp");
                blacklisted.add("pres");
                blacklisted.add("gov");
                blacklisted.add("kingpin");
                break;
        }
            


        for (int i = 0; i < 9; i++)
            apartmentsForPurchase.setItem(i, emptyPane);
        for (int i = 45; i < 54; i++)
            apartmentsForPurchase.setItem(i, emptyPane);
        apartmentsForPurchase.setItem(9, emptyPane); apartmentsForPurchase.setItem(18, emptyPane);
        apartmentsForPurchase.setItem(27, emptyPane); apartmentsForPurchase.setItem(36, emptyPane);

        apartmentsForPurchase.setItem(17, emptyPane); apartmentsForPurchase.setItem(26, emptyPane);
        apartmentsForPurchase.setItem(35, emptyPane); apartmentsForPurchase.setItem(44, emptyPane);

        int i = 10;
        HashMap<ApartmentType, Double> priceSorted = new HashMap<>();
        for (ApartmentType apartmentType : plugin.apartmentTypes.values()){

            boolean exists = false;

            for (String remove : blacklisted){
                if (remove.equalsIgnoreCase(apartmentType.getType())) {
                    exists = true;
                    break;
                }
            }

            if (exists) continue;
            priceSorted.put(apartmentType, apartmentType.getCost());
        }

        List<Map.Entry<ApartmentType, Double>> sorted = new ArrayList<>(priceSorted.entrySet());

        Collections.sort(sorted, new Comparator<Map.Entry<ApartmentType, Double>>() {
            @Override
            public int compare(Map.Entry<ApartmentType, Double> entry1, Map.Entry<ApartmentType, Double> entry2) {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });






        for (Map.Entry<ApartmentType, Double> entry : sorted){

            apartmentsForPurchase.setItem(i, entry.getKey().toItem());
            i++;
            if (i % 9 == 8)
                i += 2;

        }


        player.openInventory(apartmentsForPurchase);
    }

    public void myApartmentMenu(Player player){
        String type = plugin.carryingUUID.get(player.getName())[1];
        String name = "";
        for (String apartmentType : plugin.apartmentTypes.keySet()){
            if (type.equalsIgnoreCase(apartmentType)){
                name = plugin.apartmentTypes.get(apartmentType).getName();
            }
        }
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GOLD + ChatColor.stripColor(name));
        ArrayList<String> lorey = new ArrayList<>();
        lorey.add(ChatColor.BOLD + ""  + ChatColor.DARK_RED + "Warning: this action is irreversible");
        lorey.add(ChatColor.UNDERLINE + ""  + ChatColor.RED + "(This will delete all your items in this house as well)");
        ItemStack sell = Utils.Items.createGuiItemComplex(Material.BARRIER, ChatColor.RED + "Sell apartment", lorey);
        inventory.setItem(6, sell);
        ItemStack home = Utils.Items.createGuiItemSimple(Material.OAK_DOOR,
                ChatColor.AQUA + "Go home", ChatColor.DARK_AQUA + "Warp to your apartment");
        inventory.setItem(4, home);

        //ItemStack partners = Utils.Items.createGuiItemSimple(Material.PLAYER_HEAD,
                //ChatColor.BLUE+ "Guests", ChatColor.DARK_BLUE + "Invite friends to your apartment");
        ArrayList<String> lore = new ArrayList<>();
        String left = ChatColor.GREEN + "[Left Click]";
        String right = ChatColor.RED + "[Middle Click]";
        lore.add(left + ChatColor.WHITE + " to invite people");
        lore.add(right + ChatColor.WHITE + " to remove people");
        ItemStack partners = Utils.Items.createGuiItemComplex(Material.PLAYER_HEAD, ChatColor.BLUE + "Guests", lore);

        inventory.setItem(2, partners);

        player.openInventory(inventory);
    }

    public String configActions(String apartmentSchematicName, UUID ownerUUID, String size, com.sk89q.worldedit.entity.Player sk89qPlayer, String type, String ownerName){
        PlotsManager plotsManager = new PlotsManager(plugin, size.toLowerCase());
        plotsManager.reloadConfig();
        String firstUUID = plotsManager.getConfig().getConfigurationSection("plots").getKeys(false).iterator().next();
        String path = "plots." + firstUUID;
        double x = plotsManager.getConfig().getDouble(path + ".x");
        double y = plotsManager.getConfig().getDouble(path + ".y");
        double z = plotsManager.getConfig().getDouble(path + ".z");
        String worldName = plotsManager.getConfig().getString(path + ".world");
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        World world = BukkitAdapter.adapt(bukkitWorld);
        paste(apartmentSchematicName, x, y, z);
        plotsManager.getConfig().set(path, null);
        plotsManager.saveConfig();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(world);

        BlockVector3 minimum = null; BlockVector3 maximum = null;
        if (size.equalsIgnoreCase("small")){
            minimum = BlockVector3.at(x - 22.5, 0, z - 22.5);
            maximum = BlockVector3.at(x + 22.5, 255, z + 22.5);
        }

        else if (size.equalsIgnoreCase("medium")){
            minimum = BlockVector3.at(x - 31.5, 0, z - 46.5);
            maximum = BlockVector3.at(x + 31.5, 255, z + 46.5);
        }

        else if (size.equalsIgnoreCase("big")){
            minimum = BlockVector3.at(x - 36.5, 0, z - 57.5);
            maximum = BlockVector3.at(x + 36.5, 255, z + 57.5);
        }

        ProtectedCuboidRegion cuboidRegion = new ProtectedCuboidRegion(firstUUID, minimum, maximum);
        regions.addRegion(cuboidRegion);
        DefaultDomain defaultDomain = new DefaultDomain();
        defaultDomain.addPlayer(ownerUUID);
        cuboidRegion.setOwners(defaultDomain);

        cuboidRegion.setFlag(Flags.INTERACT, StateFlag.State.DENY);
        cuboidRegion.setFlag(Flags.INTERACT.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        cuboidRegion.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
        cuboidRegion.setFlag(Flags.CHEST_ACCESS.getRegionGroupFlag(), RegionGroup.MEMBERS);


        cuboidRegion.setFlag(Flags.GREET_TITLE, ChatColor.GOLD + ownerName + "'s apartment");


        double relativeX;
        double relativeY;
        double relativeZ;
        double pitch;
        double yaw;
        com.sk89q.worldedit.util.Location sk89qLocation;

        plugin.reloadConfig();
        try {
            for (String keys : plugin.apartmentTypes.keySet()) {
                if (keys.equalsIgnoreCase(type)) {
                    relativeX = x + plugin.getConfig().getDouble("apartmentTypes." + keys + ".spawnpoint.x");
                    relativeY = y + plugin.getConfig().getDouble("apartmentTypes." + keys + ".spawnpoint.y");
                    relativeZ = z + plugin.getConfig().getDouble("apartmentTypes." + keys + ".spawnpoint.z");
                    pitch = plugin.getConfig().getDouble("apartmentTypes." + keys + ".spawnpoint.pitch");
                    yaw = plugin.getConfig().getDouble("apartmentTypes." + keys + ".spawnpoint.yaw");
                    sk89qLocation = new com.sk89q.worldedit.util.Location(world, relativeX, relativeY, relativeZ);
                    sk89qLocation.setPitch((float) pitch);
                    sk89qLocation.setYaw((float) yaw);
                    cuboidRegion.setFlag(Flags.TELE_LOC, sk89qLocation);
                    sk89qPlayer.setLocation(sk89qLocation);
                    break;
                }
            }
        } catch (Exception ignored){}



        return firstUUID;
    }

    public void paste(String name, double x, double y, double z){
        try {
            Clipboard clipboard = null;
            File file = new File("plugins/WorldEdit/schematics/" + name + ".schem");
            if (name.equalsIgnoreCase("45") || name.equalsIgnoreCase("medium") || name.equalsIgnoreCase("big")){}
            else {y++;}
            if (name.equalsIgnoreCase("EMP")) y++;

            ClipboardFormat format = ClipboardFormats.findByFile(file);
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(plugin.returnApartmentsWorld())) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(x, y, z))
                        // configure here
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean hardcodedHasPermission(Player player, String permission, Main plugin) {
        UsersYmlManager usersYmlManager = new UsersYmlManager(plugin);
        usersYmlManager.reloadConfig();
        ConfigurationSection permissions = usersYmlManager.getConfig().getConfigurationSection("users." + player.getUniqueId().toString() + ".permissions");
        try {
            for (String perm : permissions.getKeys(false)) {
                if (perm.toLowerCase().contains(permission.toLowerCase()))
                    return true;
            }
        } catch (Exception ignored){return false;}
        return false;
    }

    public boolean check(Player player, String permission){
        for (String permissions : returnPermissions(player)){
            if (permissions.toLowerCase().contains(permission.toLowerCase()))
                return true;

        }
        return false;
    }

}