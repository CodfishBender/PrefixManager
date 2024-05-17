package com.prefixmanager;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;

public class StorageHandler {

    /**
     * Effectively displays a stored prefix by duplicating it to priority 100 and removing any other prefix at priority 100.
     * @param uuid The UUID of the user to change the prefix.
     * @param prefix The prefix to be displayed.
     */
    public void updateDisplayedPrefix(UUID uuid, String prefix) {

        // Write to luckperms user database
        PrefixManager.luckPerms.getUserManager().modifyUser(uuid, user -> {

            // Iterate over existing prefix nodes
            for (PrefixNode node : user.getNodes(NodeType.PREFIX)) {
                // Skip invalid nodes
                if (node == null) continue;
                // Remove node if priority is 100
                if (node.getPriority() == 100) user.data().remove(node);
            }
            // Add the new prefix with weight 100
            user.data().add(PrefixNode.builder(prefix, 100).build());
        });

        // Send messages
        String m = "New prefix: " + prefix;
        PrefixManager.sendMessage(Bukkit.getPlayer(uuid), m);
        PrefixManager.log(m);
    }

    /**
     * Get a list of all prefix nodes as strings, belonging to a user.
     * @param user The Luckperms User.
     * @return A list of all user prefix nodes.
     */
    public List<String> loadUserPrefixes(User user) {
        if (user == null) return null;

        // The list to hold prefix strings
        List<String> prefixList = new ArrayList<>();

        // Get all PREFIX node values
        for (PrefixNode node : user.getNodes(NodeType.PREFIX)) {
            prefixList.add(node.getMetaValue());
        }
        // Make sure list order is consistent
        Collections.sort(prefixList);
        return prefixList;
    }
    /**
     * @param uuid The UUID of the user.
     */
    public List<String> loadUserPrefixes(UUID uuid) {
        return loadUserPrefixes(PrefixManager.luckPerms.getUserManager().getUser(uuid));
    }

    /**
     * Get a list of all cached prefix nodes as a sorted map, defined as [ Priority, Prefix ].
     * @param user The luckperms user to lookup.
     * @return Returns a sorted map of prefixes. Returns null if user is null.
     */
    public SortedMap<Integer, String> loadCachedPrefixes(User user) {
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefixes();
    }
    /**
     * @param uuid The UUID of the user.
     */
    public SortedMap<Integer, String> loadCachedPrefixes(UUID uuid) {
        return loadCachedPrefixes(PrefixManager.luckPerms.getUserManager().getUser(uuid));
    }

    /**
     * Insert a new prefix into the metadata of the user.
     * @param uuid The UUID of the user.
     * @param newPrefix The prefix to add.
     * @return Returns true if successful. Returns false if unsuccessful.
     */
    public boolean addPrefixToUser(UUID uuid, String newPrefix) {
        try {
            PrefixManager.luckPerms.getUserManager().modifyUser(uuid, user -> user.data().add(PrefixNode.builder(newPrefix, 0).build()));
            return true;
        } catch(Exception e) {
            PrefixManager.log(Level.SEVERE, e.toString());
            return false;
        }
    }

    /**
     * Remove an existing prefix from the metadata of the user.
     * @param uuid The UUID of the user.
     * @param oldPrefix The prefix to remove.
     * @return The prefix that was added. Simply returns newPrefix arg if successful. Returns null if unsuccessful.
     */
    public String removePrefixFromUser(UUID uuid, int oldPrefix) {
        /* TODO: Remove prefix from meta data */
        String prefixes = loadUserPrefixes(uuid).get(oldPrefix);
        User user = PrefixManager.luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        return null;
    }
}
