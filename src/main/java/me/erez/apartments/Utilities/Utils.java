package me.erez.apartments.Utilities;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import me.erez.apartments.Files.UsersYmlManager;
import me.erez.apartments.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    public static class Sounds{

        public static void success(Player player){
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 2);

        }

        public static void victory(Player player, Color color) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 0);

            Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();

            meta.setPower(2);
            meta.addEffect(FireworkEffect.builder().withColor(color).flicker(true).build());
            fw.setFireworkMeta(meta);
            fw.detonate();
        }

        public static void joined(Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 1);

        }

        public static void left(Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 0);

        }

        public static void hmph(Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);

        }

        public static void start(Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_BELL_RESONATE, 10, (float) 1.7);

        }

        public static void nofitication(Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 10, 1);

        }

        public static void eliminated(Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 3, 1);

        }

        public static void deathmatch(Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 3, 1);

        }

        public static void toggle(Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 3, 1);

        }

    }

    public static class Items{

        public static ItemStack createGuiItemEmpty(final Material material){
            final ItemStack item = new ItemStack(material, 1);
            final ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(" ");

            item.setItemMeta(meta);

            return item;
        }

        public static ItemStack createGuiItemSimple(final Material material, final String name, final String... lore) {
            final ItemStack item = new ItemStack(material, 1);
            final ItemMeta meta = item.getItemMeta();

            // Set the name of the item
            meta.setDisplayName(name);

            // Set the lore of the item
            meta.setLore(Arrays.asList(lore));

            item.setItemMeta(meta);

            return item;
        }

        public static ItemStack createGuiItemComplex(final Material material, final String name, final ArrayList<String> lore) {
            final ItemStack item = new ItemStack(material, 1);
            final ItemMeta meta = item.getItemMeta();

            // Set the name of the item
            meta.setDisplayName(name);

            // Set the lore of the item
            meta.setLore(lore);

            item.setItemMeta(meta);

            return item;
        }

        public static void addEmptyEnchantment(ItemStack item) {
            item.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }

        @SuppressWarnings("deprecation")
        public static ItemStack createSkullByName(String playerName){
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setOwner(playerName);

            head.setItemMeta(meta);
            return head;
        }


        @SuppressWarnings("deprecation")
        public static ItemStack createGuiSkullByNameSimple(String playerName, String name, String... lore){
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setDisplayName(name);

            meta.setLore(Arrays.asList(lore));

            meta.setOwner(playerName);

            head.setItemMeta(meta);

            return head;
        }

        @SuppressWarnings("deprecation")
        public static ItemStack createGuiSkullByNameComplex(String playerName, String name, ArrayList<String> lore){
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setDisplayName(name);

            meta.setLore(lore);

            meta.setOwner(playerName);

            head.setItemMeta(meta);

            return head;
        }

    }

    public static class Locations{

        public static boolean locationsEqual(Location location1, Location location2){

            if (location1.getX() != location2.getX()) return false;

            if (location1.getY() != location2.getY()) return false;

            return location1.getZ() == location2.getZ();

        }

        public static Location deconstructStringToLocation(String string){

            String newString = string.split("name=")[1];
            String world = newString.split("}")[0];

            String xStr = newString.split("x=")[1];
            xStr = xStr.split(",")[0];
            double x = Double.parseDouble(xStr);

            String yStr = newString.split("y=")[1];
            yStr = yStr.split(",")[0];
            double y = Double.parseDouble(yStr);

            String zStr = newString.split("z=")[1];
            zStr = zStr.split(",")[0];
            double z = Double.parseDouble(zStr);


            return new Location(Bukkit.getWorld(world), x, y, z);
        }

        public static Location findCenter(Location location1, Location location2){
            if (!location1.getWorld().getName().equalsIgnoreCase(location2.getWorld().getName())) return null;

            Location center = location1.clone();
            center.setX( (location1.getX() + location2.getX()) / 2.0);
            center.setY( (location1.getY() + location2.getY()) / 2.0);
            center.setX( (location1.getZ() + location2.getZ()) / 2.0);

            return center;
        }

    }

    public static class Strings{

        public static String findDifferenceInDates(Date start_date, Date end_date) {

            // SimpleDateFormat converts the
            // string format to date object

            // Try Block
            try {

                // parse method is used to parse
                // the text from a string to
                // produce the date


                // Calucalte time difference
                // in milliseconds
                long difference_In_Time
                        = start_date.getTime() - end_date.getTime();

                // Calucalte time difference in
                // seconds, minutes, hours, years,
                // and days
                long difference_In_Seconds
                        = (difference_In_Time
                        / 1000)
                        % 60;

                long difference_In_Minutes
                        = (difference_In_Time
                        / (1000 * 60))
                        % 60;

                long difference_In_Hours
                        = (difference_In_Time
                        / (1000 * 60 * 60))
                        % 24;

                long difference_In_Years
                        = (difference_In_Time
                        / (1000L * 60 * 60 * 24 * 365));

                long difference_In_Days
                        = (difference_In_Time
                        / (1000 * 60 * 60 * 24))
                        % 365;

                // Print the date difference in
                // years, in days, in hours, in
                // minutes, and in seconds


                String answer = "";
                if(difference_In_Years != 0)
                    answer += difference_In_Years + "y ";
                if(difference_In_Days != 0)
                    answer += difference_In_Days + "d ";
                if(difference_In_Hours != 0)
                    answer += difference_In_Hours + "h ";
                if(difference_In_Minutes != 0)
                    answer += difference_In_Minutes + "m ";
                if(difference_In_Seconds != 0)
                    answer += difference_In_Seconds + "s ";

                return answer;
            }

            // Catch the Exception
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static String capitalizeFirstLetter(String string){
            string = string.substring(0, 1).toUpperCase() + string.substring(1);
            return string;
        }

        public static String normalizeSpigotText(String string){
            string = string.toLowerCase();
            string = string.replaceAll("_", " ");
            string = capitalizeFirstLetter(string);
            return string;
        }

        private final static Pattern UUID_REGEX_PATTERN =
                Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

        public static boolean isValidUUID(String str) {
            if (str == null) {
                return false;
            }
            str = ChatColor.stripColor(str);
            return UUID_REGEX_PATTERN.matcher(str).matches();
        }

        public static String formatMoney(double amount) {
            String[] suffixes = new String[]{"", "K", "M", "B", "T"};
            int suffixIndex = 0;

            while (amount >= 1000 && suffixIndex < suffixes.length - 1) {
                amount /= 1000;
                suffixIndex++;
            }

            // Format the amount with two decimal places
            String formattedAmount = String.format("%.2f", amount);

            // Add the suffix
            formattedAmount += suffixes[suffixIndex];

            return formattedAmount;
        }

        public static double formatMoneyReverse(String formattedString) {
            // Remove any non-numeric characters from the input string
            String numericPart = formattedString.replaceAll("[^\\d.]", "");

            // Check if the numeric part has a valid value
            if (numericPart.isEmpty()) {
                throw new IllegalArgumentException("Invalid input format");
            }

            // Parse the numeric part to a double
            double value = Double.parseDouble(numericPart);

            // Get the multiplier suffix (e.g., "K" for thousand, "M" for million, etc.)
            String suffix = formattedString.replaceAll("[\\d.]", "").toUpperCase();

            // Apply the appropriate multiplier based on the suffix
            switch (suffix) {
                case "K":
                    value *= Math.pow(10, 3);
                    break;
                case "M":
                    value *= Math.pow(10, 6);
                    break;
                case "B":
                    value *= Math.pow(10, 9);
                    break;
                case "T":
                    value *= Math.pow(10, 12);
                    break;
                // Add more cases for other multipliers if needed
                default:
                    return value;
            }

            return value;
        }

    }

    public static class Messages{

        public static void actionBarMessage(Player player, String message){

            TextComponent textComponent = new TextComponent(message);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);

        }

    }

    public static class MerchantRecipes{

        public static MerchantRecipe createRecipe(ItemStack ingredient1, @Nullable ItemStack ingredient2, ItemStack result){

        MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);
        recipe.addIngredient(ingredient1);
        recipe.addIngredient(ingredient2);
        return recipe;


    }

        public static void openRecipe(Player player, String title, MerchantRecipe[] recipes){

            Merchant merchant = Bukkit.createMerchant(title);

            merchant.setRecipes(Arrays.asList(recipes));

            player.openMerchant(merchant, true);


        }


    }

    public static class Recipes{

        public static void addCustomRecipe(String string1, @Nullable String string2, @Nullable String string3,
           ItemStack outcome, ArrayList<Material> ingridients, int diversityAmount, Plugin plugin){
            NamespacedKey namespacedKey = new NamespacedKey(plugin, outcome.getType().name());
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, outcome);
        }

    }

    public static class Spawners{

        public static EntityType getSpawnerEntityType(ItemStack mobSpawner){
            BlockStateMeta bsm = (BlockStateMeta) mobSpawner.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
            return cs.getSpawnedType();
        }

        public static void setSpawnerEntityType(ItemStack mobSpawner, EntityType type){
            BlockStateMeta bsm = (BlockStateMeta) mobSpawner.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();

            cs.setSpawnedType(type);
            bsm.setBlockState(cs);
            mobSpawner.setItemMeta(bsm);
        }

    }

    public static class WorldEdit{




    }

    public static class Inventories{

        public static int figureInventorySize(int size){
            int result;
            if (size < 9)
                return 9;
            if (size > 54)
                return 54;

            if (size % 9 == 0){
                return size;
            }



            return ((size / 9) * 9) + 9;
        }

    }

    public static class GroupManager{

        public static String getGroup(Player player){
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin groupManagerPlugin = pluginManager.getPlugin("GroupManager");

            if (groupManagerPlugin != null && groupManagerPlugin instanceof org.anjocaido.groupmanager.GroupManager) {
                org.anjocaido.groupmanager.GroupManager groupManager = (org.anjocaido.groupmanager.GroupManager) groupManagerPlugin;
                return groupManager.getWorldsHolder().getWorldPermissions(player).getGroup(player.getName());
            }
            return "error";
        }

        public static boolean hasRole(Player player, String roleName) {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin groupManagerPlugin = pluginManager.getPlugin("GroupManager");

            if (groupManagerPlugin != null && groupManagerPlugin instanceof org.anjocaido.groupmanager.GroupManager) {
                org.anjocaido.groupmanager.GroupManager groupManager = (org.anjocaido.groupmanager.GroupManager) groupManagerPlugin;
                return groupManager.getWorldsHolder().getWorldPermissions(player).getGroup(player.getName()).equalsIgnoreCase(roleName);
            }

            return false;
        }

        public static boolean hasPermission(Player player, String permission) {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin groupManagerPlugin = pluginManager.getPlugin("GroupManager");

            if (groupManagerPlugin != null && groupManagerPlugin instanceof org.anjocaido.groupmanager.GroupManager) {
                org.anjocaido.groupmanager.GroupManager groupManager = (org.anjocaido.groupmanager.GroupManager) groupManagerPlugin;
                return groupManager.getWorldsHolder().getWorldPermissions(player).getPermissionBoolean(player.getName(), permission);
                //return groupManager.getWorldsHolder().getWorldPermissions(player).getAllPlayersPermissions();
            }

            return false;
        }

        public static List<String> returnPermissions(Player player){
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin groupManagerPlugin = pluginManager.getPlugin("GroupManager");

            if (groupManagerPlugin != null && groupManagerPlugin instanceof org.anjocaido.groupmanager.GroupManager) {
                org.anjocaido.groupmanager.GroupManager groupManager = (org.anjocaido.groupmanager.GroupManager) groupManagerPlugin;
                return groupManager.getWorldsHolder().getWorldPermissions(player).getAllPlayersPermissions(player.getName());
                //return groupManager.getWorldsHolder().getWorldPermissions(player).getAllPlayersPermissions();
            }
            return null;
        }






    }


}
