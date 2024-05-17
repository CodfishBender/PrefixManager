package com.prefixmanager;

import net.luckperms.api.context.ContextSetFactory;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageHandler {

    /**
     * Changes the desired prefix to a weight of 100 and sets all other prefixes to 0
     * @param player The player the prefixes will be changed upon
     * @param prefix The prefix to be changed to the weight of 100
     */
    public void updatePlayerPrefix(HumanEntity player, String prefix) {
        if (!(player instanceof Player)) return;

        // Write to luckperms user database
        PrefixManager.luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
            // Iterate over existing prefix nodes
            for (PrefixNode node : user.getNodes(NodeType.PREFIX)) {
                // If it's a prefix node, set its weight to 0
                if (node == null) return;

                // Remove the node
                user.data().remove(node);

                // Check if the node is the prefix clicked on. If not add it with a weight of 0;
                if (!node.getMetaValue().equalsIgnoreCase(prefix)) {
                   user.data().add(PrefixNode.builder(node.getMetaValue(), 0).build());
                }
            }
            // Add the new prefix with weight 100
            user.data().add(PrefixNode.builder(prefix, 100).build());
        });

        // Send messages
        String m = "New prefix: " + prefix;
        player.sendMessage(m);
        PrefixManager.log(m);
    }

    /**
     * Get a list of all prefix nodes as strings, belonging to a user.
     * @param uuid The UUID of the user.
     * @return A list of all user prefix nodes.
     */
    public List<String> loadUserPrefixes(UUID uuid) {
        // Where the prefixes will be stored by name
        List<String> prefixList = new ArrayList<>();

        User user = PrefixManager.luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;

        // Check each node by filter of prefix and add the metavalue (prefix) to the list
        for (PrefixNode node : user.getNodes(NodeType.PREFIX)) {
            prefixList.add(node.getMetaValue());
        }
        return prefixList;
    }
    SortedMap<Integer, String> loadUserPrefixes(User user) {
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefixes();
    }

    /**
     * Insert a new prefix into the metadata of the user.
     * @param uuid The UUID of the user.
     * @param newPrefix The prefix to add.
     * @return The prefix that was added. Simply returns newPrefix arg if successful. Returns null if unsuccessful.
     */
    public void addPrefixToUser(UUID uuid, String newPrefix) {
        PrefixManager.luckPerms.getUserManager().modifyUser(uuid, user -> {
            user.data().add(PrefixNode.builder(newPrefix, 0).build());
        });
    }

    // You can do this - :)
    public String removePrefixFromUser(UUID uuid, int index) {
        User user = PrefixManager.luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        return null;
    }
}
