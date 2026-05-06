package com.minecraft.war.listener;

import com.minecraft.war.WarPlugin;
import com.minecraft.war.model.Challenge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WarListener implements Listener {
    
    private final WarPlugin plugin;
    
    public WarListener(WarPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Challenge challenge = plugin.getChallengeManager().getActiveChallenge(player.getUniqueId());
        
        if (challenge != null && challenge.getState() == Challenge.State.BATTLING) {
            plugin.getChallengeManager().handlePlayerDeath(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getChallengeManager().handlePlayerQuit(player);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getChallengeManager().handlePlayerJoin(player);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getChestManager().handleInventoryClick(event);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        plugin.getChestManager().handleInventoryDrag(event);
    }
}
