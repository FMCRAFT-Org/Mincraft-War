package com.minecraft.war.manager;

import com.minecraft.war.WarPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final WarPlugin plugin;
    private Location chestLocation;
    private Location fieldLocation;
    
    public ConfigManager(WarPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        if (config.contains("chest.world")) {
            World world = Bukkit.getWorld(config.getString("chest.world"));
            if (world != null) {
                double x = config.getDouble("chest.x");
                double y = config.getDouble("chest.y");
                double z = config.getDouble("chest.z");
                float yaw = (float) config.getDouble("chest.yaw");
                float pitch = (float) config.getDouble("chest.pitch");
                chestLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
        
        if (config.contains("field.world")) {
            World world = Bukkit.getWorld(config.getString("field.world"));
            if (world != null) {
                double x = config.getDouble("field.x");
                double y = config.getDouble("field.y");
                double z = config.getDouble("field.z");
                float yaw = (float) config.getDouble("field.yaw");
                float pitch = (float) config.getDouble("field.pitch");
                fieldLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }
    
    public void setChestLocation(Location location) {
        this.chestLocation = location.clone();
        saveLocation("chest", location);
    }
    
    public void setFieldLocation(Location location) {
        this.fieldLocation = location.clone();
        saveLocation("field", location);
    }
    
    private void saveLocation(String prefix, Location location) {
        FileConfiguration config = plugin.getConfig();
        config.set(prefix + ".world", location.getWorld().getName());
        config.set(prefix + ".x", location.getX());
        config.set(prefix + ".y", location.getY());
        config.set(prefix + ".z", location.getZ());
        config.set(prefix + ".yaw", location.getYaw());
        config.set(prefix + ".pitch", location.getPitch());
        plugin.saveConfig();
    }
    
    public Location getChestLocation() {
        return chestLocation;
    }
    
    public Location getFieldLocation() {
        return fieldLocation;
    }
    
    public boolean hasChestLocation() {
        return chestLocation != null;
    }
    
    public boolean hasFieldLocation() {
        return fieldLocation != null;
    }
    
    public void reload() {
        loadConfig();
    }
}
