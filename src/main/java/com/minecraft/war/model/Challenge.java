package com.minecraft.war.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Challenge {
    
    public enum State {
        WAITING,
        LOOT_PLACING,
        CONFIRMING,
        BATTLING,
        ENDED
    }
    
    private final UUID challengerId;
    private final String challengerName;
    private UUID opponentId;
    private String opponentName;
    private final boolean isPublic;
    private State state;
    private Location challengerOriginalLocation;
    private Location opponentOriginalLocation;
    private boolean challengerDone;
    private boolean opponentDone;
    private boolean challengerConfirmed;
    private boolean opponentConfirmed;
    private List<ItemStack> challengerItems;
    private List<ItemStack> opponentItems;
    private long stateStartTime;
    
    public Challenge(Player challenger, boolean isPublic) {
        this.challengerId = challenger.getUniqueId();
        this.challengerName = challenger.getName();
        this.isPublic = isPublic;
        this.state = State.WAITING;
        this.challengerDone = false;
        this.opponentDone = false;
        this.challengerConfirmed = false;
        this.opponentConfirmed = false;
        this.challengerItems = new ArrayList<>();
        this.opponentItems = new ArrayList<>();
        this.stateStartTime = System.currentTimeMillis();
    }
    
    public Challenge(Player challenger, Player opponent) {
        this(challenger, false);
        this.opponentId = opponent.getUniqueId();
        this.opponentName = opponent.getName();
    }
    
    public UUID getChallengerId() {
        return challengerId;
    }
    
    public String getChallengerName() {
        return challengerName;
    }
    
    public UUID getOpponentId() {
        return opponentId;
    }
    
    public String getOpponentName() {
        return opponentName;
    }
    
    public void setOpponent(Player opponent) {
        this.opponentId = opponent.getUniqueId();
        this.opponentName = opponent.getName();
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        this.state = state;
        this.stateStartTime = System.currentTimeMillis();
    }
    
    public long getStateStartTime() {
        return stateStartTime;
    }
    
    public Location getChallengerOriginalLocation() {
        return challengerOriginalLocation;
    }
    
    public void setChallengerOriginalLocation(Location location) {
        this.challengerOriginalLocation = location.clone();
    }
    
    public Location getOpponentOriginalLocation() {
        return opponentOriginalLocation;
    }
    
    public void setOpponentOriginalLocation(Location location) {
        this.opponentOriginalLocation = location.clone();
    }
    
    public boolean isChallengerDone() {
        return challengerDone;
    }
    
    public void setChallengerDone(boolean done) {
        this.challengerDone = done;
    }
    
    public boolean isOpponentDone() {
        return opponentDone;
    }
    
    public void setOpponentDone(boolean done) {
        this.opponentDone = done;
    }
    
    public boolean isChallengerConfirmed() {
        return challengerConfirmed;
    }
    
    public void setChallengerConfirmed(boolean confirmed) {
        this.challengerConfirmed = confirmed;
    }
    
    public boolean isOpponentConfirmed() {
        return opponentConfirmed;
    }
    
    public void setOpponentConfirmed(boolean confirmed) {
        this.opponentConfirmed = confirmed;
    }
    
    public List<ItemStack> getChallengerItems() {
        return challengerItems;
    }
    
    public void setChallengerItems(List<ItemStack> items) {
        this.challengerItems = new ArrayList<>(items);
    }
    
    public List<ItemStack> getOpponentItems() {
        return opponentItems;
    }
    
    public void setOpponentItems(List<ItemStack> items) {
        this.opponentItems = new ArrayList<>(items);
    }
    
    public boolean isPlayerInChallenge(UUID playerId) {
        return playerId.equals(challengerId) || playerId.equals(opponentId);
    }
    
    public boolean isChallenger(UUID playerId) {
        return playerId.equals(challengerId);
    }
    
    public boolean isOpponent(UUID playerId) {
        return playerId.equals(opponentId);
    }
    
    public UUID getOtherPlayer(UUID playerId) {
        if (playerId.equals(challengerId)) {
            return opponentId;
        } else if (playerId.equals(opponentId)) {
            return challengerId;
        }
        return null;
    }
    
    public Location getOriginalLocation(UUID playerId) {
        if (playerId.equals(challengerId)) {
            return challengerOriginalLocation;
        } else if (playerId.equals(opponentId)) {
            return opponentOriginalLocation;
        }
        return null;
    }
    
    public boolean isBothDone() {
        return challengerDone && opponentDone;
    }
    
    public boolean isBothConfirmed() {
        return challengerConfirmed && opponentConfirmed;
    }
}
