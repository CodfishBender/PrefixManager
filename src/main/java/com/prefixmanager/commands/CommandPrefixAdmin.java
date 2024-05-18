package com.prefixmanager.commands;

import com.prefixmanager.PrefixManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class CommandPrefixAdmin implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Check permission
        if (!sender.hasPermission("prefixmanager.admin")) {
            sender.sendMessage("No permission.");
            return false;
        }

        // Check args length
        if (minArgs(sender,args,1)) return false;

        // Handle sub command
        switch (args[0]) {
            case "add":
                return commandAdd(sender, args);
            case "addpreset":
                return commandAddPreset(sender, args);
            case "remove":
                return commandRemove(sender, args);
            case "list":
                return commandList(sender, args);
            case "reload":
                PrefixManager.sendMessage(sender, (PrefixManager.config.loadConfig()) ? "Config reloaded." : "Config failed to reload! Please check console for errors.");
                return true;
        }

        PrefixManager.sendMessage(sender,"&cUnknown argument: &f" + args[0]);
        return false;
    }

    /**
     * Add a prefix to a player using the config.
     * /prefixmanager addpreset <player> <prefix>
     */
    private boolean commandAddPreset(CommandSender sender, String[] args) {
        if (minArgs(sender,args,3)) return false;

        // Get prefix from config
        String prefix = PrefixManager.config.prefixKeyMap.get(args[2]);
        if (prefix == null) {
            PrefixManager.sendMessage(sender, "Cannot find preset prefix: " + args[2]);
            return false;
        }

        // Get player UUID
        UUID player = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

        // Add the prefix and send message
        PrefixManager.sendMessage(sender, (PrefixManager.storage.addPrefixToUser(player, prefix)) ? prefix + " &f(" + args[2] + ")&f has been added to &e" + args[1] + "&f's prefixes." : "Failed to add " + prefix + " &f(" + args[2] + ")&f to &e" + args[1] + "&f's prefixes. See console for details.");

        return true;
    }

    /**
     * Add a prefix to a player as a string. Prefix can include spaces.
     * /prefixmanager add <player> <prefix>
     */
    private boolean commandAdd(CommandSender sender, String[] args) {
        if (minArgs(sender,args,3)) return false;

        // Treat trailing args as 1 string
        String arg = trailingArgs(2, args);

        // Get player UUID
        UUID player = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

        // Add the prefix and send message
        PrefixManager.sendMessage(sender, (PrefixManager.storage.addPrefixToUser(player, String.valueOf(arg))) ? arg + "&f has been added to &e" + args[1] + "&f's prefixes." : "Failed to add " + arg + "&f to &e" + args[1] + "&f's prefixes. See console for details.");

        return true;
    }

    /**
     * Remove a prefix from the user by string. Prefix can include spaces.
     * /prefixmanager remove <player> <prefix>
     */
    private boolean commandRemove(CommandSender sender, String[] args) {
        if (minArgs(sender, args, 3)) return false;

        // Treat trailing args as 1 string
        String arg = trailingArgs(2, args);

        // Get offline player UUID
        UUID player = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

        // Remove the prefix and send message
        PrefixManager.sendMessage(sender, (PrefixManager.storage.removePrefixFromUser(player, String.valueOf(arg))) ? arg + "&f has been removed from &e" + args[1] + "&f's prefixes." : "Failed to remove " + arg + "&f from &e" + args[1] + "&f's prefixes. See console for details.");
        return true;
    }

    /**
     * Create intractable messages of all prefixes from a user.
     * /prefixmanager list <player>
     */
    private boolean commandList(CommandSender sender, String[] args) {
        if (minArgs(sender, args, 2)) return false;

        // Get player
        UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        // Get prefixes
        List<String> prefixes = PrefixManager.storage.loadUserPrefixes(uuid, 0);

        // Return if no prefixes
        if (prefixes.isEmpty()) {
            PrefixManager.sendMessage(sender, "&e" + args[1] + "&7 has no stored prefixes.");
            return true;
        }

        // List prefixes
        PrefixManager.sendMessage(sender, "&e" + args[1] + "&f's prefixes:");
        // Create clickable messages
        for (String prefix : prefixes) {
            Component message = text()
                    .append(text(" - ", GRAY))
                    .append(text(prefix))
                    .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize("Click to remove")))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/prefixmanager remove " + args[1] + " " + prefix))
                    .build();

            sender.sendMessage(message);
        }
        return true;
    }

    /**
     * Returns false if "args" length is less than "i". Displays a message to sender if not enough arguments.
     */
    private boolean minArgs(CommandSender sender, String[] args, Integer i) {
        if (args.length < i) {
            PrefixManager.sendMessage(sender, "&cNot enough arguments");
            return true;
        }
        return false;
    }

    /**
     * Converts the trailing array values into a single string.
     * @param index The index to start combining.
     * @param args The array to combine.
     * @return The combined string.
     */
    private String trailingArgs(Integer index, String[] args) {
        // Treat further args as the same string
        StringBuilder arg = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            if (i != index) arg.append(" ");
            arg.append(args[i]);
        }
        return String.valueOf(arg);
    }
}
