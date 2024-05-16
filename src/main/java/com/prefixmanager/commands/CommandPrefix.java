package com.prefixmanager.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.prefixmanager.PrefixManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandPrefix implements CommandExecutor {

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
        List<String> prefixes = PrefixManager.storage.loadUserPrefixes(player.getUniqueId());

        // Create GUI
        ChestGui gui = new ChestGui(5, "Prefixes");
        StaticPane pane = new StaticPane(0, 0, 9, 6);

        // If no player data, show nothing
        if (prefixes == null || prefixes.size() == 0) {
            gui.show(player);
            return true;
        }

        //int uses = playerData.getInt("uses");
        for (int i = 0; i < prefixes.size(); i++) {

            // Prefix to use
            String prefix = prefixes.get(i);

            // Create item stack
            ItemStack itemStack = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = itemStack.getItemMeta();
            // Set item name to prefix
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix));
            //Create lore
            List<String> lore = new ArrayList<>();
            lore.add("Click to apply this prefix!");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            // Create GUI item
            GuiItem guiItem = new GuiItem(itemStack, event -> {
                event.setCancelled(true);
                // Apply the prefix to the player
                PrefixManager.storage.updatePlayerPrefix(event.getWhoClicked(), prefix);
            });
            // Add gui item to pane
            pane.addItem(guiItem, Slot.fromIndex(i));
        }
        // Display the GUI
        gui.addPane(pane);
        gui.update();
        gui.show(player);
        return true;
    }
}
