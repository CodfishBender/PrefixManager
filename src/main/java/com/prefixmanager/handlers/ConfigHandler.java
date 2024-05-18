package com.prefixmanager.handlers;

import com.prefixmanager.PrefixManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.logging.Level;

public class ConfigHandler {

    public HashMap<String,String> prefixKeyMap = new HashMap<>();
    public HashMap<String,String> prefixDescMap = new HashMap<>();

    /**
     * Load the current config into the hashtable configPrefixes.
     */
    public boolean loadConfig() {

        // Place config values into a hash map

        ConfigurationSection configSection = PrefixManager.instance.getConfig().getConfigurationSection("prefixes");
        if (configSection == null) {
            PrefixManager.log(Level.SEVERE, "Config failed to load. Missing \"prefixes\" section.");
            return false;
        }

        try {
            for (String key:configSection.getKeys(false)) {
                String prefix = configSection.getString(key + ".prefix");
                String desc = configSection.getString(key + ".desc");
                prefixKeyMap.put(key, prefix);
                prefixDescMap.put(prefix, desc);
            }
        } catch (Exception e) {
            PrefixManager.log(Level.SEVERE, e.toString());
            return false;
        }

        return true;
    }
}
