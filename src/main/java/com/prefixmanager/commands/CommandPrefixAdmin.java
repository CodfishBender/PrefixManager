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

        // prefixmanager add <player> <prefix>
        if (args[0].equals("add")) {

            if (minArgs(sender,args,3)) return false;

            // Get player UUID
            UUID p = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

            // Add the prefix and send message
            PrefixManager.sendMessage(sender, (PrefixManager.storage.addPrefixToUser(p, args[2])) ? args[2] + "&f has been added to &e" + args[1] + "&f's prefixes." : "Failed to add " + args[2] + "&f to &e" + args[1] + "&f's prefixes. See console for details.");

            return true;
        }
        // prefixmanager list/get <player>
        // List all prefixes from a user, indexed by list order
        else if (args[0].equals("list")) {

            if (minArgs(sender,args,2)) return false;

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
        else if (args[0].equals("remove")) {

            if (minArgs(sender,args,3)) return false;

            // Get offline player UUID
            UUID p = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

            // Remove the prefix and send message
            PrefixManager.sendMessage(sender, (PrefixManager.storage.removePrefixFromUser(p, args[2])) ? args[2] + "&f has been removed from &e" + args[1] + "&f's prefixes." : "Failed to remove " + args[2] + "&f from &e" + args[1] + "&f's prefixes. See console for details.");
            return true;
        }
        else if (args[0].equals("reload")) {
            PrefixManager.sendMessage(sender, (PrefixManager.config.loadConfig()) ? "Config reloaded." : "Config failed to reload! Please check console for errors.");
            return true;
        }

        PrefixManager.sendMessage(sender,"&cUnknown argument: &f" + args[0]);
        return false;
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
}
