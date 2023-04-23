package me.erez.apartments;

import me.erez.apartments.Utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ApartmentType {

    private String name;
    private double cost;
    private Material icon;
    private String schematicFileName;
    private String plotSize;

    public ApartmentType(String name, double cost, Material icon, String schematicFileName, String plotSize) {
        this.name = name;
        this.cost = cost;
        this.icon = icon;
        this.schematicFileName = schematicFileName;
        this.plotSize = plotSize;
    }

    public ItemStack toItem(){
        ItemStack item = Utils.Items.createGuiItemSimple(icon, ChatColor.GREEN + name, ChatColor.DARK_GREEN + "Cost: $" + cost);
        return item;
    }
    public ItemStack toItemMyApartments(String uuid){
        ItemStack item = Utils.Items.createGuiItemSimple(icon, ChatColor.DARK_AQUA + name);
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
}
