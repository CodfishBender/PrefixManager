package com.prefixmanager;

import com.prefixmanager.commands.CommandPrefix;
import com.prefixmanager.commands.CommandPrefixManager;
import com.prefixmanager.tabcompleter.PrefixManagerTabCompleter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PrefixManager extends JavaPlugin {
    public static PrefixManager instance;
    public static StorageHandler storage;
    public static LuckPerms luckPerms;
    public static final String PREFIX = "&8[&ePrefixManager&8] &r";
    public static PrefixConfig config;

    @Override
    public void onEnable() {

        // Set static variables
        instance = this;
        luckPerms = checkLuckPermsExist();
        if (luckPerms == null) return;

        // Storage instance - most plugin logic happens here
        storage = new StorageHandler();

        // Create config
        this.saveDefaultConfig();
        config = new PrefixConfig();
        config.loadConfig();

        // Create classes
        getCommand("prefix").setExecutor(new CommandPrefix());
        getCommand("prefixmanager").setExecutor(new CommandPrefixManager());
        getCommand("prefixmanager").setTabCompleter(new PrefixManagerTabCompleter());
    }

    /**
     *  Check if LuckPerms exists on the server and enabled, if not, disable the plugin.
     */
    private LuckPerms checkLuckPermsExist() {
        // Check if LuckPerms exists on the server and enabled
        try {
            Class.forName("me.lucko.luckperms.bukkit.loader.BukkitLoaderPlugin");
            return this.getServer().getServicesManager().load(LuckPerms.class);
        } catch (ClassNotFoundException ignored) {
            log(Level.SEVERE, "LuckPerms not found. Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return null;
        }
    }

    public static void log(String s) {
        log(Level.INFO, s);
    }
    public static void log(Level l, String s) {
        instance.getLogger().log(l,s);
    }
    public static void sendMessage(CommandSender sender, String msg) {
        sendMessage(sender, msg, true);
    }
    public static void sendMessage(CommandSender sender, String msg, boolean prefix) {
        if (sender instanceof ConsoleCommandSender)
            log(msg);
        else if (sender instanceof Player)
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize((prefix) ? PREFIX + msg : msg));
    }
}
