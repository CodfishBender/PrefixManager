package com.prefixmanager.handlers;

import com.prefixmanager.PrefixManager;
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
        PrefixManager.sendMessage(Bukkit.getPlayer(uuid), "Prefix updated: " + prefix, false);
    }

    /**
     * Get a list of all prefix nodes as strings, belonging to a user.
     * @param user The Luckperms User.
     * @param priority The weight of the node. null for all priority.
     * @return A list of all user prefix nodes.
     */
    public List<String> loadUserPrefixes(User user, Integer priority) {
        if (user == null) return null;

        // The list to hold prefix strings
        List<String> prefixList = new ArrayList<>();

        // Get all PREFIX node values
        for (PrefixNode node : user.getNodes(NodeType.PREFIX)) {
            // Filter by priority
            if (priority != null && node.getPriority() != priority) continue;
            // Add prefix to the list
            prefixList.add(node.getMetaValue());
        }
        // Make sure list order is consistent
        Collections.sort(prefixList);
        return prefixList;
    }
    /**
     * @param uuid The UUID of the user.
     */
    public List<String> loadUserPrefixes(UUID uuid, int priority) {
        return loadUserPrefixes(PrefixManager.luckPerms.getUserManager().getUser(uuid), priority);
    }

    /**
     * Insert a new prefix into the metadata of the user.
     * @param uuid The UUID of the user.
     * @param newPrefix The prefix to add.
     * @return Returns true if successful. Returns false if unsuccessful.
     */
    public boolean addPrefixToUser(UUID uuid, String newPrefix) {
        try {
            // Add the prefix to priority 0
            PrefixManager.luckPerms.getUserManager().modifyUser(uuid, user -> user.data().add(PrefixNode.builder(newPrefix, 0).build()));
            return true;
        } catch(Exception e) {
            // Log exception and return false
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
    public boolean removePrefixFromUser(UUID uuid, String oldPrefix) {
        try {
            PrefixManager.luckPerms.getUserManager().modifyUser(uuid, user -> {
                PrefixNode node = user.getNodes(NodeType.PREFIX).stream().filter(e -> Objects.equals(e.getMetaValue(), oldPrefix)).findFirst().orElse(null);
                if (node == null) {
                    PrefixManager.log(Level.SEVERE, "Failed to remove prefix. User " + uuid + " does not have prefix: " + oldPrefix);
                    throw new NullPointerException();
                }
                user.data().remove(node);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Makes sure if the user has a 100 priority prefix, it also exists at 0 priority.
     *
     * @param user The Luckperms user to check.
     * @return true if prefix was updated.
     */
    public boolean integrityCheck(User user) {
        // Get applied prefix
        String prefix = user.getCachedData().getMetaData().getPrefixes().get(100);
        // Skip if no applied prefix
        if (prefix.isEmpty()) return false;
        // Skip if prefix exists at priority 0
        if (user.getNodes(NodeType.PREFIX).stream().anyMatch(e -> e.getPriority() == 0 && Objects.equals(e.getMetaValue(), prefix))) return false;

        // Add the prefix to priority 0
        PrefixManager.luckPerms.getUserManager().modifyUser(user.getUniqueId(), u -> u.data().add(PrefixNode.builder(prefix, 0).build()));
        PrefixManager.log("Added existing prefix " + prefix + " to " + user.getUsername());
        return true;
    }
}
