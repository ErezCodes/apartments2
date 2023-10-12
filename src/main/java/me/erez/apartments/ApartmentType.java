package me.erez.apartments;

import me.erez.apartments.Utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

import static me.erez.apartments.Utilities.Utils.Strings.formatMoney;

public class ApartmentType {

    private String name;
    private double cost;
    private Material icon;
    private String schematicFileName;
    private String plotSize;
    private String type;

    public ApartmentType(String name, double cost, Material icon, String schematicFileName, String plotSize, String type) {
        this.name = name;
        this.cost = cost;
        this.icon = icon;
        this.schematicFileName = schematicFileName;
        this.plotSize = plotSize;
        this.type = type;
    }

    public ItemStack toItem(){
        ItemStack item = Utils.Items.createGuiItemSimple(icon, ChatColor.GREEN + name, ChatColor.DARK_GREEN + "Cost: $" + formatMoney(cost));
        return item;
    }
    public ItemStack toItemMyApartments(){
        ItemStack item = Utils.Items.createGuiItemSimple(icon, ChatColor.DARK_AQUA + name);
        return item;
    }
    public ItemStack toItemVisit(){
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "[Left Click] " + ChatColor.WHITE + "to warp to the apartment");
        lore.add(ChatColor.RED + "[Shift Right Click] " + ChatColor.WHITE + "to " + ChatColor.DARK_RED + "leave"
                + ChatColor.WHITE + " the apartment");
        ItemStack item = Utils.Items.createGuiItemComplex(icon, ChatColor.LIGHT_PURPLE + name, lore);
        return item;
    }



    @Override
    public String toString() {
        return "ApartmentType{" +
                "name='" + name + '\'' +
                ", cost=" + cost +
                ", icon=" + icon +
                ", schematicFileName='" + schematicFileName + '\'' +
                ", plotSize='" + plotSize + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public Material getIcon() {
        return icon;
    }
    public void setIcon(Material icon) {
        this.icon = icon;
    }
    public String getSchematicFileName() {
        return schematicFileName;
    }
    public void setSchematicFileName(String schematicFileName) {
        this.schematicFileName = schematicFileName;
    }
    public String getPlotSize() {return plotSize;}
    public void setPlotSize(String plotSize) {this.plotSize = plotSize;}
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
