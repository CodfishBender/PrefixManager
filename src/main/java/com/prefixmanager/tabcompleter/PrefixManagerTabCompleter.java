package com.prefixmanager.tabcompleter;

import com.prefixmanager.PrefixManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrefixManagerTabCompleter implements TabCompleter {
    private final List<String> subcommands = new ArrayList<>(Arrays.asList("add","list","remove","reload"));

    /**
     * Get a list of players filtered based on the argument provided.
     * @param args The argument to check against (takes the final string in the array).
     * @return String List of players that match the args.
     */
    List<String> getPlayerList(String[] args) {
        List<String> names = new ArrayList<>();

        for (Player player : PrefixManager.instance.getServer().getOnlinePlayers()) {
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
                ret.addAll(subcommands);
            case 2:
                ret.addAll(getPlayerList(args));
        }
        return ret;
    }
}
