package com.nextdevv.auctions.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.commands.gui.AuctionsQueueGui;
import com.nextdevv.auctions.commands.gui.LogsGui;
import com.nextdevv.auctions.commands.gui.ViewItemGui;
import com.nextdevv.auctions.config.Messages;
import com.nextdevv.auctions.config.Settings;
import com.nextdevv.auctions.db.DataSource;
import com.nextdevv.auctions.enums.SearchFilter;
import com.nextdevv.auctions.managers.AuctionManager;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.auctions.models.AuctionLog;
import com.nextdevv.auctions.utils.ClassSerializer;
import com.nextdevv.auctions.utils.Presets;
import com.nextdevv.kgui.api.KGui;
import com.nextdevv.kgui.item.KItemStack;
import com.nextdevv.kgui.models.GuiBorder;
import com.nextdevv.kgui.models.GuiButton;
import com.nextdevv.kgui.models.Pages;
import com.nextdevv.kgui.utils.Alignment;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctionCommand {
    private final Auctions PLUGIN = Auctions.getPlugin(Auctions.class);

    private Object nullOr(@Nullable Object o, @NotNull Object def) {
        return o == null ? def : o;
    }

    private List<String> formatMap(Map<String, Object> map) {
        return map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.toList());
    }

    @Command(name = "auction", desc = "Auction command")
    public void auction(@Sender CommandSender sender) {
        Messages messages = PLUGIN.getConfigurationManager().getMessages();

        String helpMessage = messages.help;
        send(sender, helpMessage);
    }

    @Require("auctions.admin")
    @Command(name = "force-end", desc = "Force end auction")
    public void forceEnd(@Sender CommandSender sender) {
        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        Auction auction = auctionManager.getCurrentAuction();
        if(auction == null) {
            send(sender, "There is no auction currently running");
            return;
        }

        auction.setDuration(0);
    }

    // TODO: FIX REMOVE AND START
    /*@Require("auctions.admin")
    @Command(name = "queue", desc = "Display the auctions queue")
    public void queue(@Sender CommandSender sender) {
        if(!(sender instanceof Player player)) {
            send(sender, "You must be a player to use this command");
            return;
        }

        AuctionsQueueGui auctionsQueueGui = new AuctionsQueueGui();
        auctionsQueueGui.open(player);
    }*/

    @Require("auctions.admin")
    @Command(name = "test", desc = "Test command")
    public void test(@Sender CommandSender sender) {
        if(!(sender instanceof Player)) {
            send(sender, "You must be a player to use this command");
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = item.getItemMeta();
        String displayName = itemMeta == null ? "null" : itemMeta.getDisplayName();
        // Display every info about the item
        send(sender, "Item: " + formatMap(item.serialize()));
    }

    // /auction start <increment> <startingPrice> <buyNowPrice> <duration>
    @Command(name = "start", desc = "Start auction", usage = "<increment> <starting price> <buy now price> <duration>")
    public void start(@Sender CommandSender sender, int increment, double startingPrice, @OptArg("100000") double buyNowPrice, @OptArg("60") int duration) {
        Messages messages = PLUGIN.getConfigurationManager().getMessages();
        Settings settings = PLUGIN.getConfigurationManager().getSettings();
        if(!(sender instanceof Player)) {
            send(messages.mustBePlayer, sender);
            return;
        }

        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        AuctionOwner auctionOwner = new AuctionOwner(sender.getName(), sender.getName());
        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
        if(item.serialize().get("type") == "AIR") {
            send(messages.mustHoldItem, sender);
            return;
        }

        if(duration < settings.minAuctionTime) {
            send(messages.minAuctionTime.replace("%time%", String.valueOf(settings.minAuctionTime)), sender);
            return;
        }

        if(duration > settings.maxAuctionTime) {
            send(messages.maxAuctionTime.replace("%time%", String.valueOf(settings.maxAuctionTime)), sender);
            return;
        }

        if(PLUGIN.getEconomy().getBalance((Player) sender) < settings.auctionFee) {
            send(messages.notEnoughMoney, sender);
            return;
        }

        // TODO: needs to be fixed
        /*if(auctionManager.playerHasAlreadyQueuedOrStartedAnAuction((Player) sender)) {
            send(messages.alreadyCreated, sender);
            return;
        }*/

        auctionManager.addToQueue(new Auction(auctionOwner, increment, startingPrice, buyNowPrice, duration, item));
        send(messages.auctionAddedToQueue, sender);
    }

    @Command(name = "view-item", desc = "View the current auction item")
    public void viewItem(@Sender CommandSender sender) {
        Messages messages = PLUGIN.getConfigurationManager().getMessages();
        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        Auction auction = auctionManager.getCurrentAuction();
        if(auction == null) {
            send(messages.noAuction, sender);
            return;
        }

        if(!(sender instanceof Player player)) {
            send(messages.mustBePlayer, sender);
            return;
        }

        ViewItemGui viewItemGui = new ViewItemGui();
        viewItemGui.open(player, auction);
    }

    @Require("auctions.admin")
    @Command(name = "serialize-item", desc = "Return the serialized string of the item")
    public void serializeItem(@Sender CommandSender sender) {
        if(!(sender instanceof Player)) {
            send(sender, "You must be a player to use this command");
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        String serializedItem = ClassSerializer.ToStringItemStack(item);
        TextComponent textComponent = new TextComponent("Serialized item [Click to copy]");
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, serializedItem));
        player.spigot().sendMessage(textComponent);
    }


    @Require("auctions.admin")
    @Command(name = "logs", desc = "Display the auction history of a player")
    public void logs(@Sender CommandSender sender) {
        LogsGui logsGui = new LogsGui();
        logsGui.open(sender, SearchFilter.ALL, "");
    }

    @Command(name = "cancel", desc = "Remove auction from queue")
    public void cancel(@Sender CommandSender sender) {
        Messages messages = PLUGIN.getConfigurationManager().getMessages();
        if(!(sender instanceof Player)) {
            send(messages.mustBePlayer, sender);
            return;
        }

        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        // Get the auction of the player that he queued
        Auction auction = auctionManager.getAuctionQueue().stream().filter(a -> a.getAuctionOwner().getUuid().equals(sender.getName())).findFirst().orElse(null);
        if(auction == null) {
            send(messages.noAuction, sender);
            return;
        }

        auctionManager.removeFromQueue(auction);
        send(messages.auctionCancelled, sender);
    }


    // /auction end
    @Command(name = "end", desc = "End auction")
    public void end(@Sender CommandSender sender) {
        Messages messages = PLUGIN.getConfigurationManager().getMessages();
        if(!(sender instanceof Player)) {
            send(sender, messages.mustBePlayer);
            return;
        }

        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        Auction auction = auctionManager.getCurrentAuction();
        if(auction == null) {
            send(sender, messages.noAuction);
            return;
        }

        if(!auction.getAuctionOwner().getUuid().equals(sender.getName())) {
            send(sender, messages.notYourAuction);
            return;
        }

        if(auction.getOriginalDuration() - auction.getDuration() > PLUGIN.getConfigurationManager().getSettings().minCancelTime) {
            send(sender, messages.cantCancelAuction);
            return;
        }

        auction.setDuration(0);
    }

    public void send(String message, CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void send(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
