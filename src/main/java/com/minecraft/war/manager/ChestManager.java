package com.minecraft.war.manager;

import com.minecraft.war.WarPlugin;
import com.minecraft.war.model.Challenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ChestManager {
    
    private final WarPlugin plugin;
    
    public ChestManager(WarPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean isChestLocked(Player player) {
        Challenge challenge = plugin.getChallengeManager().getActiveChallenge(player.getUniqueId());
        if (challenge == null) {
            return false;
        }
        
        if (challenge.getState() == Challenge.State.CONFIRMING || 
            challenge.getState() == Challenge.State.BATTLING) {
            return true;
        }
        
        if (challenge.getState() == Challenge.State.LOOT_PLACING) {
            if (challenge.isChallenger(player.getUniqueId()) && challenge.isChallengerDone()) {
                return true;
            }
            if (challenge.isOpponent(player.getUniqueId()) && challenge.isOpponentDone()) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isWarChest(Location location) {
        Location chestLoc = plugin.getConfigManager().getChestLocation();
        if (chestLoc == null) {
            return false;
        }
        
        return location.getBlockX() == chestLoc.getBlockX() &&
               location.getBlockY() == chestLoc.getBlockY() &&
               location.getBlockZ() == chestLoc.getBlockZ() &&
               location.getWorld().equals(chestLoc.getWorld());
    }
    
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (event.getClickedInventory() == null) {
            return;
        }
        
        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            if (isWarChest(chest.getLocation())) {
                if (isChestLocked(player)) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.chest_locked"));
                }
            }
        }
    }
    
    public void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            if (isWarChest(chest.getLocation())) {
                if (isChestLocked(player)) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.chest_locked"));
                }
            }
        }
    }
    
    public boolean isPlayerStandingOnChest(Player player) {
        Block block = player.getLocation().clone().subtract(0, 1, 0).getBlock();
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST;
    }
    
    public Location getChestLocationFromPlayer(Player player) {
        Block block = player.getLocation().clone().subtract(0, 1, 0).getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            return block.getLocation();
        }
        return null;
    }
}
