package com.minecraft.war;

import com.minecraft.war.command.WarCommand;
import com.minecraft.war.listener.WarListener;
import com.minecraft.war.manager.ChallengeManager;
import com.minecraft.war.manager.ChestManager;
import com.minecraft.war.manager.ConfigManager;
import com.minecraft.war.manager.LanguageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WarPlugin extends JavaPlugin {
    
    private static WarPlugin instance;
    private LanguageManager languageManager;
    private ConfigManager configManager;
    private ChallengeManager challengeManager;
    private ChestManager chestManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        saveResource("Language.yml", false);
        
        languageManager = new LanguageManager(this);
        configManager = new ConfigManager(this);
        challengeManager = new ChallengeManager(this);
        chestManager = new ChestManager(this);
        
        getCommand("war").setExecutor(new WarCommand(this));
        
        getServer().getPluginManager().registerEvents(new WarListener(this), this);
        
        getLogger().info("War Plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (challengeManager != null) {
            challengeManager.cleanup();
        }
        
        getLogger().info("War Plugin has been disabled!");
    }
    
    public static WarPlugin getInstance() {
        return instance;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }
    
    public ChestManager getChestManager() {
        return chestManager;
    }
}
