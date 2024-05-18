package com.prefixmanager.tabcompleter;

import com.prefixmanager.PrefixManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class PrefixManagerTabCompleter implements TabCompleter {

    List<String> complete = new ArrayList<>(); // Recycle frequently used variable
    private final List<String> subcommands = new ArrayList<>(Arrays.asList("add","addpreset","list","remove","reload"));

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return null;
        complete.clear();
        String lastArg = args[args.length - 1];

        switch (args.length) {
            case 1:
                complete.addAll(subcommands);
                break;
            case 2:
                complete.addAll(getPlayerList(args));
                break;
            case 3:
                if (args[0].equals("addpreset")) complete.addAll(filterList(lastArg, PrefixManager.config.prefixKeyMap.keySet()));
                break;
        }
        return complete;
    }


    /**
     * Get a filtered list based on the key provided.
     * @param key The argument to check against.
     * @param set The list to filter through.
     * @return String List that match the key.
     */
    List<String> filterList(String key, Set<String> set) {
        List<String> newList = new ArrayList<>();

        for (String s : set) {
            if (s.contains(key)) newList.add(s);
        }
        return newList;
    }

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
}
