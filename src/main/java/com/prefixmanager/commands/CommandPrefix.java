package com.prefixmanager.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.prefixmanager.PrefixManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.context.Context;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.query.Flag;
import net.luckperms.api.track.Track;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandPrefix implements CommandExecutor {

    List<UUID> checkedUsers = new ArrayList<>();
    BukkitScheduler scheduler = Bukkit.getScheduler();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Cannot run from console
        if (sender instanceof ConsoleCommandSender) {
            PrefixManager.log("You can only run this command from in game.");
            return false;
        }
        // Check permission
        if (!sender.hasPermission("prefixmanager.prefix")) {
            sender.sendMessage("No permission.");
            return false;
        }

        // Get player data
        Player player = (Player) sender;
        User user = PrefixManager.luckPerms.getUserManager().getUser(((Player) sender).getUniqueId());
        if (user == null) return false;

        // Has the players prefix been checked already?
        if (!checkedUsers.contains(player.getUniqueId())) {
            // Add to list of checked players
            checkedUsers.add(player.getUniqueId());

            // Run integrity check and delay opening GUI to allow for the database to update
            if (PrefixManager.storage.integrityCheck(user)) {
                // Prefix was updated...
                // Send message to player
                PrefixManager.sendMessage(player, "Syncing existing prefix...");

                // Delay opening GUI
                // Ideally the scheduler should only ever run once for each applicable player
                scheduler.runTaskLater(PrefixManager.instance, () -> {
                    customPrefixGui(player, user);
                    PrefixManager.sendMessage(player, "Done!");
                }, 60L);
                return true;
            }
        }

        customPrefixGui(player, user);
        return true;
    }

    /**
     * Load the GUI to the player with the prefixes from the luckperms user.
     * @param player The bukkit player.
     * @param user The luckperms user.
     */
    private void customPrefixGui(Player player, User user) {
        // Get prefixes
        List<String> prefixes = PrefixManager.storage.loadUserPrefixes(user, 0);

        // Close any open gui
        player.closeInventory(InventoryCloseEvent.Reason.PLAYER);

        // Create GUI
        ChestGui gui = new ChestGui(5, "Custom Prefixes");
        StaticPane pane = new StaticPane(0, 0, 9, 6);

        // Build re-usable lore string
        String currentPrefixLore = "\n\n&fCurrent:\n" + user.getCachedData().getMetaData().getPrefix() + player.getName();

        ContextSet serverContextSet = ImmutableContextSet.builder().add("server",PrefixManager.luckPerms.getServerName()).build();

        // Populate the gui border
        for (int i = 0; i < 45; i++) {
            GuiItem guiItem = null;
            if (i == 9) {
                // Create VIP item
                ItemStack item = createGuiItemStack(Material.DIAMOND, "&5VIP Prefix", "\n&fShow/hide your VIP tag if you have VIP.\n\n&5[&eVIP&5]" + currentPrefixLore);
                // Add GUI element
                guiItem = new GuiItem(item, e -> {
                    // Cancel click event
                    e.setCancelled(true);

                    // Toggle hide_vip
                    Node node = user.getNodes().stream().filter(n -> n.getKey().equals("group.vip_hide")).findFirst().orElse(null);
                    if (node == null) {
                        user.data().add(Node.builder("group.vip_hide").context(serverContextSet).build());
                        PrefixManager.sendMessage(player, "Hiding VIP prefix...");
                    } else {
                        user.data().remove(node);
                        PrefixManager.sendMessage(player, "Showing VIP prefix...");
                    }
                    PrefixManager.luckPerms.getUserManager().saveUser(user);

                    // Reload GUI
                    reloadPrefixGUI(player, user);
                });
            }
            else if (i == 10) {
                // Create rank icon
                String currentRank = getHighestPrefixOnTrack(user, "ranks");
                String currentPrestige = getHighestPrefixOnTrack(user, "prestige");
                ItemStack item = createGuiItemStack(Material.EMERALD, "&6Rank Prefix", "\n&fShow/hide your current rank.\n\n" + currentPrestige + currentRank + currentPrefixLore);
                // Add GUI element
                guiItem = new GuiItem(item, e -> {
                    // Cancel click event
                    e.setCancelled(true);

                    // Toggle hide_vip
                    Node node = user.getNodes().stream().filter(n -> n.getKey().equals("group.hide_rank")).findFirst().orElse(null);
                    if (node == null) {
                        user.data().add(Node.builder("group.hide_rank").context(serverContextSet).build());
                        PrefixManager.sendMessage(player, "Hiding rank prefix...");
                    } else {
                        user.data().remove(node);
                        PrefixManager.sendMessage(player, "Showing rank prefix...");
                    }
                    PrefixManager.luckPerms.getUserManager().saveUser(user);

                    // Reload GUI
                    reloadPrefixGUI(player, user);
                });
            }
            else if (i == 11) {
                // Create rank icon
                ItemStack item = createGuiItemStack(Material.NAME_TAG, "&eCustom Prefix", "\n&fHide your custom prefix." + currentPrefixLore);
                // Add GUI element
                guiItem = new GuiItem(item, e -> {
                    // Cancel click event
                    e.setCancelled(true);

                    // Get applied prefix
                    PrefixNode prefixNode = user.getNodes(NodeType.PREFIX).stream().filter(n -> n.getPriority() == 100).findFirst().orElse(null);
                    if (prefixNode != null) user.data().remove(prefixNode);

                    PrefixManager.sendMessage(player, "Hiding custom prefix...");

                    // Reload GUI
                    reloadPrefixGUI(player, user);
                });
            }
            else if (i < 9 || i >= 36) {
                // Create border item
                ItemStack item = createGuiItemStack(Material.GRAY_STAINED_GLASS_PANE, "", "");
                // Cancel click event
                guiItem = new GuiItem(item, e -> e.setCancelled(true));
            }

            // Add gui item to gui pane
            if (guiItem != null) pane.addItem(guiItem, Slot.fromIndex(i));
        }

        // Iterate through all prefixes
        for (int i = 0; i < prefixes.size(); i++) {
            // Prefix to use
            String prefix = prefixes.get(i);

            // Add description from config
            String desc = PrefixManager.config.prefixDescMap.get(prefix);
            desc = (desc == null) ? "" : "\n\n" + desc;

            // Create item stack
            ItemStack itemStack = createGuiItemStack(Material.NAME_TAG, "&eCustom Prefix", "\n&fApply this custom prefix:\n\n" + prefix + desc + currentPrefixLore);

            // Create GUI item
            GuiItem guiItem = new GuiItem(itemStack, e -> {
                // Cancel the click event
                e.setCancelled(true);
                // Apply the prefix to the player
                PrefixManager.storage.updateDisplayedPrefix(e.getWhoClicked().getUniqueId(), prefix);

                // Reload GUI
                PrefixManager.sendMessage(player, "Updating custom prefix...");
                reloadPrefixGUI(player, user);
            });
            // Add gui item to pane
            pane.addItem(guiItem, Slot.fromIndex(12 + i));
        }

        // Display the GUI
        gui.addPane(pane);
        gui.update();
        gui.show(player);
    }

    /**
     * Create a basic item stack to use with GuiItem.
     * @param material ItemStack material.
     * @param name Name of the ItemStack.
     * @param lore Lore of the ItemStack.
     * @return The ItemStack that is created.
     */
    private ItemStack createGuiItemStack(Material material, String name, String lore) {
        // Create item stack
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        // Item name
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        // Item lore
        // Split lore into components
        List<Component> loreList = new ArrayList<>();
        for (String loreLine:lore.split("\n")) {
            loreList.add(LegacyComponentSerializer.legacyAmpersand().deserialize(loreLine));
        }
        // set meta
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private void reloadPrefixGUI(Player player, User user) {
        // Reopen inventory, refresh preview
        // Close GUI
        player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
        // Schedule GUI to open in 2 seconds
        scheduler.runTaskLater(PrefixManager.instance, () -> customPrefixGui(player, user), 40L);
    }

    /**
     * Get a users' highest prefix on a specific track.
     * @param user The Luckperms user.
     * @param trackString The name of the track.
     * @return The prefix from the track.
     */
    private String getHighestPrefixOnTrack(User user, String trackString) {
        Track track = PrefixManager.luckPerms.getTrackManager().getTrack(trackString);
        Validate.notNull(track, "yeet");
        List<String> playerGroups = user.getInheritedGroups(user.getQueryOptions().toBuilder().flag(Flag.RESOLVE_INHERITANCE, false).build())
                        .stream()
                        .map(Group::getName)
                        .toList();
        String tr = track.getGroups()
                .stream()
                .sorted(Collections.reverseOrder())
                .filter(playerGroups::contains)
                .filter(e -> !e.equals("hide_rank"))
                .findFirst().orElse("default");
        return PrefixManager.luckPerms.getGroupManager().getGroup(tr).getCachedData().getMetaData().getPrefix();
    }
}
