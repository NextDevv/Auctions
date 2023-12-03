package com.nextdevv.auctions.commands.gui;

import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.kgui.api.KGui;
import com.nextdevv.kgui.item.KItemStack;
import com.nextdevv.kgui.models.GuiBorder;
import com.nextdevv.kgui.models.GuiButton;
import com.nextdevv.kgui.models.Pages;
import com.nextdevv.kgui.utils.Alignment;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class AuctionsQueueGui {
    private final Auctions PLUGIN = Auctions.getPlugin(Auctions.class);

    public void open(Player player) {
        KGui kGui = PLUGIN.getKGui();

        GuiBorder border = new GuiBorder();
        border.setDefaultItemStack(new KItemStack().builder()
                .setMaterial(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .build()
        );

        HashMap<ItemStack, Auction> auctions = getAuctions();
        Pages pages = new Pages().builder().setMaxPerPages(28).autoCreatePages(auctions.keySet().stream().toList()).build();

        Inventory gui = kGui.builder(player)
                .setTitle("&6Auctions Queue")
                .setRows(6)
                .setBorder(border)
                .setPages(pages)
                .canInteract(false)
                .addItemStackClick(((itemStack, player1, builder, clickType) -> {
                    Auction auction = auctions.get(itemStack);
                    if(auction == null) return null;
                    openAuction(player1, auction, border);
                    return null;
                }))
                .build();
        player.openInventory(gui);
    }

    private void openAuction(Player player, Auction auction, GuiBorder border) {
        KGui kGui = PLUGIN.getKGui();
        Inventory gui = kGui.builder(player)
                .setItem(auction.deserializeItem(), 4)
                .setTitle(ChatColor.translateAlternateColorCodes('&', PLUGIN.getAuctionManager().getItemName(auction.deserializeItem())))
                .setRows(3)
                .setBorder(border)
                .addButton(Alignment.TOP_LEFT, new GuiButton().setItemStack(
                        new KItemStack().builder()
                                .setMaterial(Material.ARROW)
                                .setName("&6Back")
                                .build()
                ).onClick(((builder, player1) -> {
                    open(player1);
                    return null;
                })))
                .addButton(10, new GuiButton().setItemStack(
                        new KItemStack().builder()
                                .setMaterial(Material.REDSTONE_BLOCK)
                                .setName("&cRemove")
                                .build()
                ).onClick(((builder, player1) -> {
                    PLUGIN.getAuctionManager().removeFromQueue(auction);
                    open(player1);
                    return null;
                })))
                .addButton(12, new GuiButton().setItemStack(
                        new KItemStack().builder()
                                .setMaterial(Material.EMERALD_BLOCK)
                                .setName("&aStart")
                                .build()
                ).onClick(((builder, player1) -> {
                    PLUGIN.getAuctionManager().getCurrentAuction().setDuration(0);
                    PLUGIN.getAuctionManager().setCurrentAuction(auction);
                    return null;
                })))
                .setItem(new KItemStack().builder()
                        .setMaterial(Material.GOLD_INGOT)
                        .setName("&6Starting Price: &e" + PLUGIN.getEconomy().format(auction.getStartingPrice()))
                        .build(), 14)
                .setItem(new KItemStack().builder()
                        .setMaterial(Material.GOLD_NUGGET)
                        .setName("&6Increment: &e" + PLUGIN.getEconomy().format(auction.getIncrement()))
                        .build(), 16)
                .canInteract(false)
                .build();
        player.openInventory(gui);
    }

    private ItemStack modifyName(ItemStack itemStack, Auction auction) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', PLUGIN.getAuctionManager().getItemName(itemStack) + " &7- &6" + auction.getAuctionOwner().getName()));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private HashMap<ItemStack, Auction> getAuctions() {
        HashMap<ItemStack, Auction> auctions = new HashMap<>();
        for(Auction auction : PLUGIN.getAuctionManager().getAuctionQueue().getAuctions()) {
            auctions.put(modifyName(auction.deserializeItem(), auction), auction);
        }
        return auctions;
    }
}
