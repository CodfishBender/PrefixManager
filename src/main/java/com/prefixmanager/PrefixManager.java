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

    @Override
    public void onEnable() {
        instance = this;
        storage = new StorageHandler();
        luckPerms = getServer().getServicesManager().load(LuckPerms.class);

        getCommand("prefix").setExecutor(new CommandPrefix());
        getCommand("prefixmanager").setExecutor(new CommandPrefixManager());
        getCommand("prefixmanager").setTabCompleter(new PrefixManagerTabCompleter());
    }

    public static void log(String s) {
        instance.getLogger().log(Level.INFO,s);
    }
    public static void log(Level l, String s) {
        instance.getLogger().log(l,s);
    }
    public static void sendMessage(CommandSender sender, String msg) {
        if (sender instanceof ConsoleCommandSender)
            log(msg);
        else if (sender instanceof Player)
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(PREFIX + msg));
    }
    public static void sendMessage(CommandSender sender, String msg, boolean prefix) {
        if (sender instanceof ConsoleCommandSender)
            log(msg);
        else if (sender instanceof Player)
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize((prefix) ? PREFIX + msg : msg));
    }
}
