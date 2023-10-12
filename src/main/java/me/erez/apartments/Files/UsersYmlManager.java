package me.erez.apartments.Files;

import me.erez.apartments.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class UsersYmlManager {

    private Main plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public UsersYmlManager(Main plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void createFile(){
        configFile = new File( "plugins/GroupManager/worlds/world/users.yml");
        try {
            configFile.createNewFile();
        }
        catch (Exception e){
            plugin.getLogger().log(Level.SEVERE, "Could not create config " + this.configFile, e);
        }
    }

    public void reloadConfig() {
        if (this.configFile == null)
            createFile();


        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = this.plugin.getResource("plugins/GroupManager/worlds/world/users.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }

    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null)
            reloadConfig();

        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null)
            createFile();

        if (!this.configFile.exists()) {
            this.plugin.saveResource("plugins/GroupManager/worlds/world/users.yml", false);
        }
    }

}
