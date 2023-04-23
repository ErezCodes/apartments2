package me.erez.apartments;

import me.erez.apartments.Files.DataManager;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class Apartment {

    private String owner;
    private Set<String> players = new HashSet<>();
    private ApartmentType apartmentType;

    private Location edge1;
    private Location edge2;

    public Apartment(String owner, ApartmentType apartmentType) {
        this.owner = owner;
        this.apartmentType = apartmentType;
    }

    public void addPlayer(String player){
        players.add(player);
    }

    public void removePLayer(String player){
        players.remove(player);
    }

    public boolean checkIfExists(String player){
        if (player.equalsIgnoreCase(owner)) return true;
        for (String name : players){
            if (name.equalsIgnoreCase(player)) return true;
        }
        return false;
    }

    public void invitePlayer(String player){

    }

    //data
    public void writeToYML(){

//        DataManager dataManager = Main.dataManager;
//        dataManager.reloadConfig();
//        String path = "spawners." + uuid;
//        FileConfiguration config = dataManager.getConfig();
//
//
//        dataManager.getConfig().set(path + ".world", location.getWorld().getName());
//        dataManager.getConfig().set(path + ".x", location.getX());
//        dataManager.getConfig().set(path + ".y", location.getY());
//        dataManager.getConfig().set(path + ".z", location.getZ());
//        dataManager.getConfig().set(path + ".type", type.toString());
//        dataManager.getConfig().set(path + ".stash1", stash1);
//        dataManager.getConfig().set(path + ".stash2", stash2);
//
//        dataManager.saveConfig();

    }


    @Override
    public String toString() {
        return "Apartment{" +
                "apartmentType=" + apartmentType.toString() +
                '}';
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public Set<String> getPlayers() {
        return players;
    }
    public void setPlayers(Set<String> players) {
        this.players = players;
    }
    public ApartmentType getApartmentType() {
        return apartmentType;
    }
    public void setApartmentType(ApartmentType apartmentType) {
        this.apartmentType = apartmentType;
    }
    public Location getEdge1() {
        return edge1;
    }
    public void setEdge1(Location edge1) {
        this.edge1 = edge1;
    }
    public Location getEdge2() {
        return edge2;
    }
    public void setEdge2(Location edge2) {
        this.edge2 = edge2;
    }

}
