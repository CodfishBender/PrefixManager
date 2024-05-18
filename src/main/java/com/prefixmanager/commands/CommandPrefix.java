package com.prefixmanager.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.prefixmanager.PrefixManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
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
                BukkitScheduler scheduler = Bukkit.getScheduler();
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

        // Create border item
        ItemStack borderItem = createGuiItemStack(Material.GRAY_STAINED_GLASS_PANE, "", "");

        // Populate the gui border
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36) {
                pane.addItem(new GuiItem(borderItem, e -> e.setCancelled(true)), Slot.fromIndex(i));
            }
        }

        // Iterate through all prefixes
        for (int i = 0; i < prefixes.size(); i++) {
            // Prefix to use
            String prefix = prefixes.get(i);

            // Create item stack
            ItemStack itemStack = createGuiItemStack(Material.NAME_TAG, prefix, "Click to apply this prefix!\n\n&fCurrent:\n" + user.getCachedData().getMetaData().getPrefix() + player.getName());

            // Create GUI item
            GuiItem guiItem = new GuiItem(itemStack, e -> {
                // Cancel the click event
                e.setCancelled(true);
                // Apply the prefix to the player
                PrefixManager.storage.updateDisplayedPrefix(e.getWhoClicked().getUniqueId(), prefix);

                // Reopen inventory, refresh preview
                player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
                player.performCommand("prefixmanager:prefix");
            });
            // Add gui item to pane
            pane.addItem(guiItem, Slot.fromIndex(9 + i));
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
}
