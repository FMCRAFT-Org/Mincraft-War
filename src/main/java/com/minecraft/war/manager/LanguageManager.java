package com.minecraft.war.manager;

import com.minecraft.war.WarPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageManager {
    
    private final WarPlugin plugin;
    private File languageFile;
    private FileConfiguration languageConfig;
    private final Map<String, String> messages;
    
    public LanguageManager(WarPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadLanguage();
    }
    
    private void loadLanguage() {
        languageFile = new File(plugin.getDataFolder(), "Language.yml");
        
        if (!languageFile.exists()) {
            plugin.saveResource("Language.yml", false);
        }
        
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        loadMessages();
    }
    
    private void loadMessages() {
        messages.put("prefix", languageConfig.getString("prefix", "&6[战争]"));
        
        messages.put("challenge.public", languageConfig.getString("challenge.public"));
        messages.put("challenge.public_success", languageConfig.getString("challenge.public_success"));
        messages.put("challenge.private", languageConfig.getString("challenge.private"));
        messages.put("challenge.private_success", languageConfig.getString("challenge.private_success"));
        messages.put("challenge.already_in_challenge", languageConfig.getString("challenge.already_in_challenge"));
        messages.put("challenge.target_in_challenge", languageConfig.getString("challenge.target_in_challenge"));
        messages.put("challenge.no_permission", languageConfig.getString("challenge.no_permission"));
        messages.put("challenge.player_not_found", languageConfig.getString("challenge.player_not_found"));
        messages.put("challenge.cannot_challenge_self", languageConfig.getString("challenge.cannot_challenge_self"));
        
        messages.put("accept.success", languageConfig.getString("accept.success"));
        messages.put("accept.no_challenge", languageConfig.getString("accept.no_challenge"));
        messages.put("accept.not_your_challenge", languageConfig.getString("accept.not_your_challenge"));
        
        messages.put("decline.success", languageConfig.getString("decline.success"));
        messages.put("decline.no_challenge", languageConfig.getString("decline.no_challenge"));
        messages.put("decline.not_your_challenge", languageConfig.getString("decline.not_your_challenge"));
        
        messages.put("teleport.countdown", languageConfig.getString("teleport.countdown"));
        messages.put("teleport.to_loot_area", languageConfig.getString("teleport.to_loot_area"));
        messages.put("teleport.to_battlefield", languageConfig.getString("teleport.to_battlefield"));
        messages.put("teleport.battle_start", languageConfig.getString("teleport.battle_start"));
        
        messages.put("loot.place_hint", languageConfig.getString("loot.place_hint"));
        messages.put("loot.done_success", languageConfig.getString("loot.done_success"));
        messages.put("loot.already_done", languageConfig.getString("loot.already_done"));
        messages.put("loot.both_done", languageConfig.getString("loot.both_done"));
        messages.put("loot.chest_locked", languageConfig.getString("loot.chest_locked"));
        messages.put("loot.no_active_challenge", languageConfig.getString("loot.no_active_challenge"));
        
        messages.put("confirm.success", languageConfig.getString("confirm.success"));
        messages.put("confirm.already_confirmed", languageConfig.getString("confirm.already_confirmed"));
        messages.put("confirm.both_confirmed", languageConfig.getString("confirm.both_confirmed"));
        messages.put("confirm.not_done_placing", languageConfig.getString("confirm.not_done_placing"));
        
        messages.put("stop.success", languageConfig.getString("stop.success"));
        messages.put("stop.return_loot", languageConfig.getString("stop.return_loot"));
        messages.put("stop.opponent_stop", languageConfig.getString("stop.opponent_stop"));
        messages.put("stop.not_in_challenge", languageConfig.getString("stop.not_in_challenge"));
        messages.put("stop.already_confirmed", languageConfig.getString("stop.already_confirmed"));
        
        messages.put("back.success", languageConfig.getString("back.success"));
        messages.put("back.no_saved_location", languageConfig.getString("back.no_saved_location"));
        messages.put("back.not_in_challenge", languageConfig.getString("back.not_in_challenge"));
        
        messages.put("battle.death", languageConfig.getString("battle.death"));
        messages.put("battle.quit", languageConfig.getString("battle.quit"));
        messages.put("battle.winner", languageConfig.getString("battle.winner"));
        messages.put("battle.loser", languageConfig.getString("battle.loser"));
        
        messages.put("admin.no_permission", languageConfig.getString("admin.no_permission"));
        messages.put("admin.box_set", languageConfig.getString("admin.box_set"));
        messages.put("admin.field_set", languageConfig.getString("admin.field_set"));
        messages.put("admin.stop_success", languageConfig.getString("admin.stop_success"));
        messages.put("admin.win_success", languageConfig.getString("admin.win_success"));
        messages.put("admin.player_not_in_challenge", languageConfig.getString("admin.player_not_in_challenge"));
        messages.put("admin.not_chest", languageConfig.getString("admin.not_chest"));
        
        messages.put("help.header", languageConfig.getString("help.header"));
        messages.put("help.footer", languageConfig.getString("help.footer"));
        
        messages.put("error.console_only", languageConfig.getString("error.console_only"));
        messages.put("error.player_only", languageConfig.getString("error.player_only"));
        messages.put("error.unknown_command", languageConfig.getString("error.unknown_command"));
        messages.put("error.config_error", languageConfig.getString("error.config_error"));
    }
    
    public String getMessage(String key) {
        String message = messages.get(key);
        if (message == null) {
            return ChatColor.RED + "Message not found: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return message;
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    public String getPrefixedMessage(String key) {
        return getPrefix() + " " + getMessage(key);
    }
    
    public String getPrefixedMessage(String key, String... placeholders) {
        return getPrefix() + " " + getMessage(key, placeholders);
    }
    
    public List<String> getMessageList(String key) {
        List<String> list = languageConfig.getStringList(key);
        List<String> coloredList = new ArrayList<>();
        for (String line : list) {
            coloredList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return coloredList;
    }
    
    public void reload() {
        loadLanguage();
    }
}
