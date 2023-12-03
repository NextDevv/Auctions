package com.nextdevv.auctions.commands.gui;

import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.kgui.api.KGui;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ViewItemGui {
    private final Auctions PLUGIN = Auctions.getPlugin(Auctions.class);
    public void open(Player player, Auction currentAuction) {
        KGui kGui = PLUGIN.getKGui();
        Inventory gui = kGui.builder(player)
                .setItem(currentAuction.deserializeItem(), 4)
                .setTitle(ChatColor.translateAlternateColorCodes('&', PLUGIN.getAuctionManager().getItemName(currentAuction.deserializeItem())))
                .setRows(1)
                .canInteract(false)
                .build();
        player.openInventory(gui);
    }
}
