package com.prefixmanager;

import net.luckperms.api.context.ContextSetFactory;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class StorageHandler {

    public void updatePlayerPrefix(HumanEntity player, String prefix) {
        if (!(player instanceof Player)) return;

        // Build node to insert into database
        PrefixNode node = PrefixNode.builder(prefix, 100).build();

        // Write to luckperms user database
        PrefixManager.luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
            // Reset all weights

            for (PrefixNode p:loadUserPrefixes(user).values()) {
                // Skip if not weight 100
                if (p.getPriority() != 100) continue;
                // Remove the old node
                user.data().remove(p);
            }
            user.data().add(node);
        });

        // Send messages
        String m = "New prefix: " + prefix;
        player.sendMessage(m);
        PrefixManager.log(m);
    }

    /**
     * Get a sorted map of all prefix nodes as strings, belonging to a user.
     * @param uuid The UUID of the user.
     * @return A list of all user prefix nodes.
     */
    SortedMap<Integer, String> loadUserPrefixes(UUID uuid) {
        User user = PrefixManager.luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefixes();
    }
    SortedMap<Integer, String> loadUserPrefixes(User user) {
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefixes();
    }

    /**
     * Insert a new prefix into the meta data of the user.
     * @param uuid The UUID of the user.
     * @param newPrefix The prefix to add.
     * @return The prefix that was added. Simply returns newPrefix arg if successful. Returns null if unsuccessful.
     */
    public String addPrefixToUser(UUID uuid, String newPrefix) {

        User user = PrefixManager.luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;

        try {
            PrefixNode node = PrefixNode.builder(newPrefix, 0).build();
            user.data().add(node);
            return newPrefix;
        } catch(Exception e) {
            PrefixManager.log(Level.SEVERE, e.toString());
            return null;
        }

    }

    public String removePrefixFromUser(UUID uuid, int index) {
        User user = PrefixManager.luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        return null;
    }

}
