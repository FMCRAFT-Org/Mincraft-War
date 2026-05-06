package com.minecraft.war.manager;

import com.minecraft.war.WarPlugin;
import com.minecraft.war.model.Challenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class ChallengeManager {
    
    private final WarPlugin plugin;
    private final Map<UUID, Challenge> pendingChallenges;
    private final Map<UUID, Challenge> activeChallenges;
    private final Map<UUID, Location> savedLocations;
    private final Set<UUID> offlinePlayers;
    private BukkitRunnable timeoutTask;
    private static final long TIMEOUT_SECONDS = 60;
    
    public ChallengeManager(WarPlugin plugin) {
        this.plugin = plugin;
        this.pendingChallenges = new HashMap<>();
        this.activeChallenges = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.offlinePlayers = new HashSet<>();
        startTimeoutTask();
    }
    
    private void startTimeoutTask() {
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTimeouts();
            }
        };
        timeoutTask.runTaskTimer(plugin, 20L * 10, 20L * 10);
    }
    
    private void checkTimeouts() {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = TIMEOUT_SECONDS * 1000;
        
        List<Challenge> pendingToRemove = new ArrayList<>();
        for (Challenge challenge : pendingChallenges.values()) {
            if (challenge.getState() == Challenge.State.WAITING) {
                if (currentTime - challenge.getStateStartTime() > timeoutMillis) {
                    pendingToRemove.add(challenge);
                }
            }
        }
        
        for (Challenge challenge : pendingToRemove) {
            Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
            if (challenger != null) {
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("timeout.challenge_expired"));
            }
            pendingChallenges.remove(challenge.getChallengerId());
        }
        
        List<Challenge> activeToRemove = new ArrayList<>();
        for (Challenge challenge : activeChallenges.values().stream().distinct().collect(Collectors.toList())) {
            if (challenge.getState() == Challenge.State.LOOT_PLACING || 
                challenge.getState() == Challenge.State.CONFIRMING) {
                if (currentTime - challenge.getStateStartTime() > timeoutMillis) {
                    activeToRemove.add(challenge);
                }
            }
        }
        
        for (Challenge challenge : activeToRemove) {
            endChallengeWithTimeout(challenge);
        }
    }
    
    private void endChallengeWithTimeout(Challenge challenge) {
        Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
        Player opponent = Bukkit.getPlayer(challenge.getOpponentId());
        
        if (challenger != null) {
            challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("timeout.challenge_timeout"));
        }
        if (opponent != null) {
            opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("timeout.challenge_timeout"));
        }
        
        activeChallenges.remove(challenge.getChallengerId());
        activeChallenges.remove(challenge.getOpponentId());
        pendingChallenges.remove(challenge.getChallengerId());
        challenge.setState(Challenge.State.ENDED);
    }
    
    public boolean createPublicChallenge(Player challenger) {
        if (isInChallenge(challenger.getUniqueId())) {
            return false;
        }
        
        Challenge challenge = new Challenge(challenger, true);
        pendingChallenges.put(challenger.getUniqueId(), challenge);
        return true;
    }
    
    public boolean createPrivateChallenge(Player challenger, Player target) {
        if (isInChallenge(challenger.getUniqueId()) || isInChallenge(target.getUniqueId())) {
            return false;
        }
        
        Challenge challenge = new Challenge(challenger, target);
        pendingChallenges.put(challenger.getUniqueId(), challenge);
        return true;
    }
    
    public boolean acceptChallenge(Player player) {
        Challenge publicChallenge = null;
        for (Challenge challenge : pendingChallenges.values()) {
            if (challenge.isPublic()) {
                publicChallenge = challenge;
                break;
            }
        }
        
        if (publicChallenge != null) {
            if (isInChallenge(player.getUniqueId())) {
                return false;
            }
            
            publicChallenge.setOpponent(player);
            pendingChallenges.remove(publicChallenge.getChallengerId());
            startChallenge(publicChallenge);
            return true;
        }
        
        for (Challenge challenge : pendingChallenges.values()) {
            if (!challenge.isPublic() && player.getUniqueId().equals(challenge.getOpponentId())) {
                pendingChallenges.remove(challenge.getChallengerId());
                startChallenge(challenge);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean declineChallenge(Player player) {
        for (Challenge challenge : pendingChallenges.values()) {
            if (!challenge.isPublic() && player.getUniqueId().equals(challenge.getOpponentId())) {
                pendingChallenges.remove(challenge.getChallengerId());
                return true;
            }
        }
        return false;
    }
    
    private void startChallenge(Challenge challenge) {
        Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
        Player opponent = Bukkit.getPlayer(challenge.getOpponentId());
        
        if (challenger == null || opponent == null) {
            return;
        }
        
        challenge.setChallengerOriginalLocation(challenger.getLocation());
        challenge.setOpponentOriginalLocation(opponent.getLocation());
        
        activeChallenges.put(challenge.getChallengerId(), challenge);
        activeChallenges.put(challenge.getOpponentId(), challenge);
        
        challenge.setState(Challenge.State.LOOT_PLACING);
        
        new BukkitRunnable() {
            int countdown = 3;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    String message = plugin.getLanguageManager().getMessage("teleport.countdown", "%time%", String.valueOf(countdown));
                    challenger.sendMessage(plugin.getLanguageManager().getPrefix() + " " + message);
                    opponent.sendMessage(plugin.getLanguageManager().getPrefix() + " " + message);
                    countdown--;
                } else {
                    Location chestLoc = plugin.getConfigManager().getChestLocation();
                    if (chestLoc != null) {
                        challenger.teleport(chestLoc);
                        opponent.teleport(chestLoc);
                        
                        challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("teleport.to_loot_area"));
                        opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("teleport.to_loot_area"));
                        challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.place_hint"));
                        opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.place_hint"));
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public boolean playerDone(Player player) {
        Challenge challenge = getActiveChallenge(player.getUniqueId());
        if (challenge == null || challenge.getState() != Challenge.State.LOOT_PLACING) {
            return false;
        }
        
        Player opponent = null;
        UUID opponentId = challenge.getOtherPlayer(player.getUniqueId());
        if (opponentId != null) {
            opponent = Bukkit.getPlayer(opponentId);
        }
        
        if (challenge.isChallenger(player.getUniqueId())) {
            if (challenge.isChallengerDone()) {
                return false;
            }
            challenge.setChallengerDone(true);
            if (opponent != null) {
                opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.opponent_done",
                        "%player%", player.getName()));
            }
        } else {
            if (challenge.isOpponentDone()) {
                return false;
            }
            challenge.setOpponentDone(true);
            if (opponent != null) {
                opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.opponent_done",
                        "%player%", player.getName()));
            }
        }
        
        if (challenge.isBothDone()) {
            saveChestItems(challenge);
            challenge.setState(Challenge.State.CONFIRMING);
            
            Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
            Player opponent2 = Bukkit.getPlayer(challenge.getOpponentId());
            
            if (challenger != null && opponent2 != null) {
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.both_done"));
                opponent2.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.both_done"));
            }
        }
        
        return true;
    }
    
    private void saveChestItems(Challenge challenge) {
        Location chestLoc = plugin.getConfigManager().getChestLocation();
        if (chestLoc == null) {
            return;
        }
        
        Block block = chestLoc.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) block.getState();
            Inventory inv = chest.getInventory();
            
            List<ItemStack> allItems = new ArrayList<>();
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    allItems.add(item.clone());
                }
            }
            
            challenge.setChallengerItems(allItems);
            challenge.setOpponentItems(new ArrayList<>());
            
            inv.clear();
        }
    }
    
    public boolean playerConfirm(Player player) {
        Challenge challenge = getActiveChallenge(player.getUniqueId());
        if (challenge == null || challenge.getState() != Challenge.State.CONFIRMING) {
            return false;
        }
        
        if (!challenge.isBothDone()) {
            return false;
        }
        
        Player opponent = null;
        UUID opponentId = challenge.getOtherPlayer(player.getUniqueId());
        if (opponentId != null) {
            opponent = Bukkit.getPlayer(opponentId);
        }
        
        if (challenge.isChallenger(player.getUniqueId())) {
            if (challenge.isChallengerConfirmed()) {
                return false;
            }
            challenge.setChallengerConfirmed(true);
            if (opponent != null) {
                opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("confirm.opponent_confirmed",
                        "%player%", player.getName()));
            }
        } else {
            if (challenge.isOpponentConfirmed()) {
                return false;
            }
            challenge.setOpponentConfirmed(true);
            if (opponent != null) {
                opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("confirm.opponent_confirmed",
                        "%player%", player.getName()));
            }
        }
        
        if (challenge.isBothConfirmed()) {
            startBattle(challenge);
        }
        
        return true;
    }
    
    private void startBattle(Challenge challenge) {
        Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
        Player opponent = Bukkit.getPlayer(challenge.getOpponentId());
        
        if (challenger == null || opponent == null) {
            return;
        }
        
        new BukkitRunnable() {
            int countdown = 3;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    String message = plugin.getLanguageManager().getMessage("teleport.to_battlefield", "%time%", String.valueOf(countdown));
                    challenger.sendMessage(plugin.getLanguageManager().getPrefix() + " " + message);
                    opponent.sendMessage(plugin.getLanguageManager().getPrefix() + " " + message);
                    countdown--;
                } else {
                    Location fieldLoc = plugin.getConfigManager().getFieldLocation();
                    if (fieldLoc != null) {
                        challenger.teleport(fieldLoc);
                        opponent.teleport(fieldLoc);
                        
                        new BukkitRunnable() {
                            int battleCountdown = 3;
                            
                            @Override
                            public void run() {
                                if (battleCountdown > 0) {
                                    String msg = plugin.getLanguageManager().getMessage("teleport.countdown", "%time%", String.valueOf(battleCountdown));
                                    challenger.sendMessage(plugin.getLanguageManager().getPrefix() + " " + msg);
                                    opponent.sendMessage(plugin.getLanguageManager().getPrefix() + " " + msg);
                                    battleCountdown--;
                                } else {
                                    challenge.setState(Challenge.State.BATTLING);
                                    Bukkit.broadcastMessage(plugin.getLanguageManager().getPrefixedMessage("teleport.battle_start"));
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0L, 20L);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public boolean playerStop(Player player) {
        Challenge challenge = getActiveChallenge(player.getUniqueId());
        if (challenge == null) {
            return false;
        }
        
        if (challenge.getState() == Challenge.State.BATTLING) {
            return false;
        }
        
        endChallengeWithRefund(challenge);
        return true;
    }
    
    private void endChallengeWithRefund(Challenge challenge) {
        Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
        Player opponent = Bukkit.getPlayer(challenge.getOpponentId());
        
        returnItemsToPlayer(challenger, challenge.getChallengerItems());
        returnItemsToPlayer(opponent, challenge.getOpponentItems());
        
        if (challenger != null) {
            Location loc = challenge.getChallengerOriginalLocation();
            if (loc != null) {
                savedLocations.put(challenger.getUniqueId(), loc);
            }
            challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("stop.success"));
        }
        
        if (opponent != null) {
            Location loc = challenge.getOpponentOriginalLocation();
            if (loc != null) {
                savedLocations.put(opponent.getUniqueId(), loc);
            }
            opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("stop.success"));
        }
        
        activeChallenges.remove(challenge.getChallengerId());
        activeChallenges.remove(challenge.getOpponentId());
        challenge.setState(Challenge.State.ENDED);
    }
    
    private void returnItemsToPlayer(Player player, List<ItemStack> items) {
        if (player == null || items == null || items.isEmpty()) {
            return;
        }
        
        for (ItemStack item : items) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }
    
    public void playerBack(Player player) {
        Location loc = savedLocations.remove(player.getUniqueId());
        if (loc != null) {
            player.teleport(loc);
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("back.success"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("back.no_saved_location"));
        }
    }
    
    public void handlePlayerDeath(Player deadPlayer) {
        Challenge challenge = getActiveChallenge(deadPlayer.getUniqueId());
        if (challenge == null || challenge.getState() != Challenge.State.BATTLING) {
            return;
        }
        
        UUID winnerId = challenge.getOtherPlayer(deadPlayer.getUniqueId());
        Player winner = Bukkit.getPlayer(winnerId);
        
        if (winner != null) {
            endChallengeWithWinner(challenge, winner, deadPlayer);
        }
    }
    
    public void handlePlayerQuit(Player player) {
        Challenge challenge = getActiveChallenge(player.getUniqueId());
        if (challenge == null) {
            return;
        }
        
        if (challenge.getState() == Challenge.State.BATTLING) {
            UUID winnerId = challenge.getOtherPlayer(player.getUniqueId());
            Player winner = Bukkit.getPlayer(winnerId);
            
            offlinePlayers.add(player.getUniqueId());
            
            if (winner != null) {
                endChallengeWithWinner(challenge, winner, player);
            }
        } else if (challenge.getState() != Challenge.State.ENDED) {
            endChallengeWithRefund(challenge);
        }
    }
    
    public void handlePlayerJoin(Player player) {
        if (offlinePlayers.remove(player.getUniqueId())) {
            Location loc = savedLocations.remove(player.getUniqueId());
            if (loc != null) {
                player.teleport(loc);
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("back.success"));
            }
        }
    }
    
    private void endChallengeWithWinner(Challenge challenge, Player winner, Player loser) {
        List<ItemStack> allItems = new ArrayList<>();
        allItems.addAll(challenge.getChallengerItems());
        allItems.addAll(challenge.getOpponentItems());
        
        returnItemsToPlayer(winner, allItems);
        
        Bukkit.broadcastMessage(plugin.getLanguageManager().getPrefixedMessage("battle.winner",
                "%winner%", winner.getName(), "%loser%", loser.getName()));
        
        Location winnerLoc = challenge.isChallenger(winner.getUniqueId()) ? 
                challenge.getChallengerOriginalLocation() : challenge.getOpponentOriginalLocation();
        Location loserLoc = challenge.isChallenger(loser.getUniqueId()) ? 
                challenge.getChallengerOriginalLocation() : challenge.getOpponentOriginalLocation();
        
        if (winnerLoc != null) {
            savedLocations.put(winner.getUniqueId(), winnerLoc);
        }
        if (loserLoc != null) {
            savedLocations.put(loser.getUniqueId(), loserLoc);
        }
        
        activeChallenges.remove(challenge.getChallengerId());
        activeChallenges.remove(challenge.getOpponentId());
        challenge.setState(Challenge.State.ENDED);
    }
    
    public void adminStop() {
        List<Challenge> challenges = new ArrayList<>(activeChallenges.values().stream()
                .distinct().collect(Collectors.toList()));
        
        for (Challenge challenge : challenges) {
            Player challenger = Bukkit.getPlayer(challenge.getChallengerId());
            Player opponent = Bukkit.getPlayer(challenge.getOpponentId());
            
            if (challenger != null) {
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.stop_success"));
            }
            if (opponent != null) {
                opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.stop_success"));
            }
            
            activeChallenges.remove(challenge.getChallengerId());
            activeChallenges.remove(challenge.getOpponentId());
            challenge.setState(Challenge.State.ENDED);
        }
    }
    
    public void adminWin(Player winner) {
        Challenge challenge = getActiveChallenge(winner.getUniqueId());
        if (challenge == null) {
            return;
        }
        
        UUID loserId = challenge.getOtherPlayer(winner.getUniqueId());
        Player loser = Bukkit.getPlayer(loserId);
        
        if (loser != null) {
            endChallengeWithWinner(challenge, winner, loser);
        }
    }
    
    public boolean isInChallenge(UUID playerId) {
        return activeChallenges.containsKey(playerId) || pendingChallenges.containsKey(playerId);
    }
    
    public Challenge getActiveChallenge(UUID playerId) {
        return activeChallenges.get(playerId);
    }
    
    public Challenge getPendingChallenge(UUID playerId) {
        return pendingChallenges.get(playerId);
    }
    
    public void cleanup() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        for (Challenge challenge : activeChallenges.values().stream().distinct().collect(Collectors.toList())) {
            endChallengeWithRefund(challenge);
        }
        activeChallenges.clear();
        pendingChallenges.clear();
    }
}
