package me.erez.apartments;

import com.sk89q.worldedit.regions.Region;
import me.erez.apartments.Files.DataManager;
import me.erez.apartments.commands.Dinero;
import me.erez.apartments.commands.Invitation;
import me.erez.apartments.commands.apartmentsCommand;
import me.erez.apartments.commands.devTools;
import me.erez.apartments.listeners.InventoryClick;
import me.erez.apartments.listeners.uuidCarry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;


public final class Main extends JavaPlugin {

    //public values
    public HashMap<String, ApartmentType> apartmentTypes = new HashMap<>();
    public HashMap<String[], Long> invites = new HashMap<>();
    public HashMap<String, String[]> carryingUUID = new HashMap<>(); //[0] is uuid, [1] is apartmentType



    @Override
    public void onEnable() {

        //commands
        new Dinero(this);
        new apartmentsCommand(this);
        new devTools(this);
        new Invitation(this);

        //listeners
        new InventoryClick(this);
        new uuidCarry(this);

        for (Player player : Bukkit.getOnlinePlayers())
            carryingUUID.put(player.getName(), new String[]{"", ""});


        //setup
        setUpApartmentTypes();
        saveDefaultConfig();

        //economy
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        };
    }

    @Override
    public void onDisable() {

        //economy
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));

        carryingUUID.clear();
        //saveDefaultConfig();

    }

    //ApartmentType
    public void setUpApartmentTypes(){
        reloadConfig();
        for (String type : getConfig().getConfigurationSection("apartmentTypes").getKeys(false)){
            String path = "apartmentTypes." + type;
            String name = getConfig().getString(path + ".name");
            double cost = getConfig().getDouble(path + ".cost");
            Material icon = Material.matchMaterial(getConfig().getString(path + ".block-icon"));
            String schematicFileName = getConfig().getString(path + ".schematic-file-name");
            String plotSize = getConfig().getString(path + ".plotSize");
            apartmentTypes.put(name, new ApartmentType(name, cost, icon, schematicFileName, plotSize, type));
        }
    }

    //Economy
    private final Logger log = Logger.getLogger("Minecraft");
    private Economy econ = null;
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public Economy getEconomy() {
        return econ;
    }





}
