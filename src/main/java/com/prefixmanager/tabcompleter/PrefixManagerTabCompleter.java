package com.prefixmanager.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrefixManagerTabCompleter implements TabCompleter {
    private List<String> subcommands = new ArrayList<>(Arrays.asList("gm","changeworld","reload"));

    List<String> getPlayerList(String[] args) {
        List<String> names = new ArrayList<>();

        Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
        for (Player player : Bukkit.getServer().getOnlinePlayers().toArray(players)) {
            if (player.getName().toLowerCase().contains(args[args.length - 1])) {
                names.add(player.getName());
            }
        }
        return names;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> ret = new ArrayList<>();
        switch (args.length) {
            case 1:
                ret.add("add");
                ret.add("list");
                ret.add("get");
                ret.add("remove");
                return ret;
            case 2:
                ret.addAll(getPlayerList(args));
                return ret;
        }
        return ret;
    }
}
