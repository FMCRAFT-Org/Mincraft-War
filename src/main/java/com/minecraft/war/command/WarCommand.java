package com.minecraft.war.command;

import com.minecraft.war.WarPlugin;
import com.minecraft.war.manager.ChallengeManager;
import com.minecraft.war.model.Challenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarCommand implements CommandExecutor {
    
    private final WarPlugin plugin;
    
    public WarCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "challenge":
                return handleChallenge(sender, args);
            case "accept":
                return handleAccept(sender);
            case "decline":
                return handleDecline(sender);
            case "done":
                return handleDone(sender);
            case "confirm":
                return handleConfirm(sender);
            case "stop":
                return handleStop(sender);
            case "back":
                return handleBack(sender);
            case "help":
                sendHelp(sender);
                return true;
            case "admin":
                return handleAdmin(sender, args);
            default:
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.unknown_command"));
                return true;
        }
    }
    
    private boolean handleChallenge(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player challenger = (Player) sender;
        
        if (!challenger.hasPermission("war.challenge")) {
            challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.no_permission"));
            return true;
        }
        
        if (plugin.getChallengeManager().isInChallenge(challenger.getUniqueId())) {
            challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.already_in_challenge"));
            return true;
        }
        
        if (!plugin.getConfigManager().hasChestLocation() || !plugin.getConfigManager().hasFieldLocation()) {
            challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.config_error"));
            return true;
        }
        
        if (args.length == 1) {
            if (plugin.getChallengeManager().createPublicChallenge(challenger)) {
                Bukkit.broadcastMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.public",
                        "%player%", challenger.getName()));
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.public_success"));
            }
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.player_not_found"));
                return true;
            }
            
            if (target.equals(challenger)) {
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.cannot_challenge_self"));
                return true;
            }
            
            if (plugin.getChallengeManager().isInChallenge(target.getUniqueId())) {
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.target_in_challenge"));
                return true;
            }
            
            if (plugin.getChallengeManager().createPrivateChallenge(challenger, target)) {
                target.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.private",
                        "%player%", challenger.getName()));
                challenger.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.private_success",
                        "%player%", target.getName()));
            }
        }
        
        return true;
    }
    
    private boolean handleAccept(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getChallengeManager().isInChallenge(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.already_in_challenge"));
            return true;
        }
        
        if (!plugin.getConfigManager().hasChestLocation() || !plugin.getConfigManager().hasFieldLocation()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.config_error"));
            return true;
        }
        
        if (plugin.getChallengeManager().acceptChallenge(player)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("accept.success"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("accept.no_challenge"));
        }
        
        return true;
    }
    
    private boolean handleDecline(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getChallengeManager().declineChallenge(player)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("decline.success"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("decline.no_challenge"));
        }
        
        return true;
    }
    
    private boolean handleDone(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getChallengeManager().playerDone(player)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.done_success"));
        } else {
            Challenge challenge = plugin.getChallengeManager().getActiveChallenge(player.getUniqueId());
            if (challenge == null) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.no_active_challenge"));
            } else if (challenge.isChallenger(player.getUniqueId()) && challenge.isChallengerDone()) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.already_done"));
            } else if (challenge.isOpponent(player.getUniqueId()) && challenge.isOpponentDone()) {
                player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.already_done"));
            }
        }
        
        return true;
    }
    
    private boolean handleConfirm(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        Challenge challenge = plugin.getChallengeManager().getActiveChallenge(player.getUniqueId());
        if (challenge == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("loot.no_active_challenge"));
            return true;
        }
        
        if (!challenge.isBothDone()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("confirm.not_done_placing"));
            return true;
        }
        
        if (plugin.getChallengeManager().playerConfirm(player)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("confirm.success"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("confirm.already_confirmed"));
        }
        
        return true;
    }
    
    private boolean handleStop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        Challenge challenge = plugin.getChallengeManager().getActiveChallenge(player.getUniqueId());
        if (challenge == null) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("stop.not_in_challenge"));
            return true;
        }
        
        if (challenge.getState() == Challenge.State.BATTLING) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("stop.already_confirmed"));
            return true;
        }
        
        if (plugin.getChallengeManager().playerStop(player)) {
            Player opponent = Bukkit.getPlayer(challenge.getOtherPlayer(player.getUniqueId()));
            if (opponent != null) {
                opponent.sendMessage(plugin.getLanguageManager().getPrefixedMessage("stop.opponent_stop"));
            }
        }
        
        return true;
    }
    
    private boolean handleBack(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getChallengeManager().playerBack(player);
        
        return true;
    }
    
    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("war.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.no_permission"));
            return true;
        }
        
        if (args.length < 2) {
            sendAdminHelp(sender);
            return true;
        }
        
        String adminCommand = args[1].toLowerCase();
        
        switch (adminCommand) {
            case "box":
                return handleAdminBox(sender);
            case "field":
                return handleAdminField(sender);
            case "stop":
                return handleAdminStop(sender);
            case "win":
                return handleAdminWin(sender, args);
            default:
                sendAdminHelp(sender);
                return true;
        }
    }
    
    private boolean handleAdminBox(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getChestManager().isPlayerStandingOnChest(player)) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.not_chest"));
            return true;
        }
        
        Location chestLoc = plugin.getChestManager().getChestLocationFromPlayer(player);
        if (chestLoc != null) {
            plugin.getConfigManager().setChestLocation(chestLoc);
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.box_set"));
        }
        
        return true;
    }
    
    private boolean handleAdminField(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getConfigManager().setFieldLocation(player.getLocation());
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.field_set"));
        
        return true;
    }
    
    private boolean handleAdminStop(CommandSender sender) {
        plugin.getChallengeManager().adminStop();
        sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.stop_success"));
        return true;
    }
    
    private boolean handleAdminWin(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + " " + 
                    plugin.getLanguageManager().getMessage("error.unknown_command"));
            return true;
        }
        
        Player winner = Bukkit.getPlayer(args[2]);
        if (winner == null) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("challenge.player_not_found"));
            return true;
        }
        
        Challenge challenge = plugin.getChallengeManager().getActiveChallenge(winner.getUniqueId());
        if (challenge == null) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.player_not_in_challenge"));
            return true;
        }
        
        plugin.getChallengeManager().adminWin(winner);
        sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("admin.win_success",
                "%player%", winner.getName()));
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.header"));
        for (String line : plugin.getLanguageManager().getMessageList("help.player")) {
            sender.sendMessage(line);
        }
        
        if (sender.hasPermission("war.admin")) {
            for (String line : plugin.getLanguageManager().getMessageList("help.admin")) {
                sender.sendMessage(line);
            }
        }
        
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.footer"));
    }
    
    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.header"));
        for (String line : plugin.getLanguageManager().getMessageList("help.admin")) {
            sender.sendMessage(line);
        }
        sender.sendMessage(plugin.getLanguageManager().getMessage("help.footer"));
    }
}
