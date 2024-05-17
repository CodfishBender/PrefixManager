package com.prefixmanager.commands;

import com.prefixmanager.PrefixManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CommandPrefixManager implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check permission
        if (!sender.hasPermission("prefixmanager.admin")) {
            sender.sendMessage("No permission.");
            return false;
        }
        if (args.length == 0) return false;

        // prefixmanager add <player> <prefix>
        if (args[0].equals("add")) {
            if (args.length > 2) {
                UUID p = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                PrefixManager.storage.addPrefixToUser(p, args[2]);
                PrefixManager.sendMessage(sender, args[2] + "&f has been added to &e" + args[1] + "&f's prefixes.");
                return true;
            } else {
                PrefixManager.sendMessage(sender, "&cNot enough arguments");
                return false;
            }
        }
        else if (args[0].equals("list") || args[0].equals("get")) {
            if (args.length > 1) {
                UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                List<String> prefixes = (List<String>) PrefixManager.storage.loadUserPrefixes(uuid);
                if (prefixes == null || prefixes.isEmpty()) {
                    PrefixManager.sendMessage(sender, "&e" + args[1] + "&7 has no stored prefixes.");
                    return true;
                }
                PrefixManager.sendMessage(sender, "&e" + args[1] + "&f's prefixes:");
                for (int i = 0; i < prefixes.size(); i++) {
                    PrefixManager.sendMessage(sender, "&7" + i + " - " + prefixes.get(i), false);
                }
                return true;
            }
        }
        else if (args[0].equals("remove")) {
            if (args.length > 2) {
                UUID p = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                int index;
                try {
                    index = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    PrefixManager.sendMessage(sender, "&cPrefix index must be a number. See with /pfm list <player>");
                    return false;
                }
                String result = PrefixManager.storage.removePrefixFromUser(p, index);
                PrefixManager.sendMessage(sender, result);
                return true;
            } else {
                PrefixManager.sendMessage(sender, "&cNot enough arguments");
                return false;
            }
        }

        PrefixManager.sendMessage(sender,"&cUnknown argument: &f" + args[0]);
        return false;
    }
}
