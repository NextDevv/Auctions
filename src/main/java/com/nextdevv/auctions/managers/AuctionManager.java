package com.nextdevv.auctions.managers;

import com.google.gson.JsonObject;
import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.commands.AuctionCommand;
import com.nextdevv.auctions.config.Messages;
import com.nextdevv.auctions.enums.AuctionEvent;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.auctions.commands.AuctionOwner;
import com.nextdevv.auctions.models.AuctionLog;
import com.nextdevv.auctions.models.Bid;
import com.nextdevv.auctions.models.Bidder;
import com.nextdevv.auctions.utils.AuctionList;
import com.nextdevv.auctions.utils.Broadcast;
import com.nextdevv.auctions.utils.ClassSerializer;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AuctionManager {
    @Getter
    private Auction currentAuction;
    private final Auctions plugin = Auctions.getPlugin(Auctions.class);

    @Getter
    @Setter
    private AuctionList auctionQueue = new AuctionList();

    @Getter
    private HashMap<UUID, Boolean> preconditionMap = new HashMap<>();

    public void removeAuction(Auction auction) {
        auctionQueue.remove(auction);
    }

    public void setCurrentAuction(Auction auction) {
        currentAuction = auction;
    }

    public boolean playerHasAlreadyQueuedOrStartedAnAuction(Player player) {
        if (auctionQueue.isEmpty()) {
            return false;
        }

        String playerUUID = player.getUniqueId().toString();

        for (Auction auction : auctionQueue.getAuctions()) {
            if (auction.getAuctionOwner().getUuid().equals(playerUUID)) {
                return true;
            }
        }

        return currentAuction != null && currentAuction.getAuctionOwner().getUuid().equals(playerUUID);
    }


    public void updater() {
        clock();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentAuction != null) {
                    handleOngoingAuctionClock();
                } else {
                    handleNextAuction();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void handleOngoingAuctionClock() {
        if (currentAuction.hasBids() && currentAuction.hasBuyNowPrice()) {
            Bid highestBid = currentAuction.getHighestBid();
            if (highestBid != null && highestBid.getAmount() >= currentAuction.getBuyNowPrice()) {
                handleAuctionEndClock();
            }
        }
    }

    private void handleNextAuction() {
        if (!auctionQueue.isEmpty()) {
            int timeBetween = plugin.getConfigurationManager().getSettings().timeBetween;
            currentAuction = auctionQueue.get(0);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                CompletableFuture<Boolean> success = startAuction(currentAuction);

                success.thenAccept(aBoolean -> {
                    if (aBoolean) {
                        handleAuctionStart();
                    }
                });
            }, timeBetween * 20L);
        }
    }

    private void handleAuctionStart() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.START.name());
        jsonObject.addProperty("auction", ClassSerializer.ToString(currentAuction));
        plugin.getRedisManager().publish(jsonObject);
    }

    private void handleAuctionEndClock() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.END.name());
        jsonObject.addProperty("auction", ClassSerializer.ToString(currentAuction));
        plugin.getRedisManager().publish(jsonObject);

        end(currentAuction, true);
    }

    public void addToQueue(Auction auction) {
        auctionQueue.add(auction);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.QUEUE.name());
        jsonObject.addProperty("auction", ClassSerializer.ToString(auction));
        plugin.getRedisManager().publish(jsonObject);
    }

    public void removeFromQueue(Auction auction) {
        auctionQueue.remove(auction);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.REMOVE.name());
        jsonObject.addProperty("auction", ClassSerializer.ToString(auction));
        plugin.getRedisManager().publish(jsonObject);
    }

    public CompletableFuture<Boolean> startAuction(Auction auction) {
        return CompletableFuture.supplyAsync(() -> {
            AuctionOwner auctionOwner = auction.getAuctionOwner();
            Player player = Bukkit.getPlayer(auctionOwner.getUuid());

            if (player != null) {
                if (hasEnoughItems(player.getInventory(), auction.deserializeItem())) {
                    player.getInventory().removeItem(auction.deserializeItem());
                } else {
                    handleInsufficientItems(player);
                    return false;
                }

                plugin.getEconomy().withdrawPlayer(player, plugin.getConfigurationManager().getSettings().auctionFee);
            }

            currentAuction = auction;
            currentAuction.setStarted(true);

            broadcastStart(
                    plugin.getConfigurationManager().getMessages().auctionStarted
                            .replace("%player%", auction.getAuctionOwner().getName())
                            .replace("%price%", plugin.getEconomy().format(auction.getStartingPrice()))
                            .replace("%time%", String.valueOf(auction.getDuration())),
                    auction.deserializeItem(),
                    auction
            );

            removeFromQueue(currentAuction);
            return true;
        });
    }

    private boolean hasEnoughItems(Inventory inventory, ItemStack itemStack) {
        return inventory.containsAtLeast(itemStack, itemStack.getAmount());
    }

    private void handleInsufficientItems(Player player) {
        currentAuction = null;
        removeFromQueue(currentAuction);
        send(player, plugin.getConfigurationManager().getMessages().itemNotFoundInInventory);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.FORCE_END.name());
        jsonObject.addProperty("auction", ClassSerializer.ToString(currentAuction));
        plugin.getRedisManager().publish(jsonObject);
    }


    public void broadcastStart(String message, ItemStack item, Auction auction) {
        Messages messages = plugin.getConfigurationManager().getMessages();
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);

        TextComponent textComponent = new TextComponent(getItemName(item));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/auction view-item"));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', messages.clickToViewItem)).create()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            String[] messageParts = formattedMessage.split("%item%");

            String firstPart = replacePlaceholders(messageParts[0], auction);
            String secondPart = replacePlaceholders(messageParts[1], auction);

            TextComponent firstTextComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', firstPart));
            TextComponent secondTextComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', secondPart));

            player.spigot().sendMessage(firstTextComponent, textComponent, secondTextComponent);
        }
    }

    private String replacePlaceholders(String message, Auction auction) {
        return message
                .replace("%price%", plugin.getEconomy().format(auction.getStartingPrice()))
                .replace("%duration%", String.valueOf(auction.getDuration()))
                .replace("%buy_now_price%", plugin.getEconomy().format(auction.getBuyNowPrice()))
                .replace("%increment%", plugin.getEconomy().format(auction.getIncrement()));
    }


    public void send(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void clock() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentAuction != null && currentAuction.isStarted()) {
                    handleOngoingAuction();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void handleOngoingAuction() {
        if (currentAuction.getDuration() > 0) {
            currentAuction.setDuration(currentAuction.getDuration() - 1);
            currentAuction.incrementPrice();

            int remainingTime = currentAuction.getDuration();
            double highestBid = currentAuction.getHighestBid() != null ? currentAuction.getHighestBid().getAmount() : 0;
            int interval = plugin.getConfigurationManager().getSettings().auctionInterval;
            int startCountdownAt = plugin.getConfigurationManager().getSettings().startCountdownAt;

            String timeMessage;
            if (remainingTime <= startCountdownAt) {
                timeMessage = plugin.getConfigurationManager().getMessages().lastSecondsMessage;
            } else if (remainingTime % interval == 0) {
                timeMessage = plugin.getConfigurationManager().getMessages().timeLeft;
            } else {
                return; // No need to broadcast if not in the last 5 seconds or at intervals of X seconds
            }

            Broadcast.send(
                    timeMessage
                            .replace("%time%", String.valueOf(remainingTime))
                            .replace("%price%", plugin.getEconomy().format(currentAuction.getStartingPrice()))
                            .replace("%highest_bid%", plugin.getEconomy().format(highestBid))
            );
        } else {
            handleAuctionEnd();
        }
    }
    private void handleAuctionEnd() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.END.name());
        jsonObject.addProperty("auction", ClassSerializer.ToString(currentAuction));
        plugin.getRedisManager().publish(jsonObject);

        end(currentAuction, true);
    }


    public void end(Auction auction, boolean message) {
        if (auction == null) {
            return;
        }

        removeFromQueue(auction);
        currentAuction = null;

        if (auction.hasBids()) {
            handleBids(auction, message);
        } else {
            handleNoBids(auction, message);
        }

        recordAuctionLog(auction);
    }

    private void handleBids(Auction auction, boolean message) {
        Bid highestBid = auction.getHighestBid();
        if (highestBid != null) {
            giveItemToPlayer(highestBid.getBidder().getUuid(), auction);
        }

        for (Bid bid : auction.getBids()) {
            Player bidder = Bukkit.getPlayer(bid.getBidder().getUuid());
            if (bidder != null && bid != auction.getHighestBid()) {
                plugin.getEconomy().depositPlayer(bidder, bid.getAmount());
            }
        }

        if (message) {
            Messages messages = plugin.getConfigurationManager().getMessages();
            Broadcast.send(messages.auctionEnded
                    .replace("%player%", auction.getAuctionOwner().getName())
                    .replace("%item%", getItemName(auction.deserializeItem()))
                    .replace("%price%", plugin.getEconomy().format(auction.getHighestBid().getAmount()))
            );

            AuctionOwner auctionOwner = auction.getAuctionOwner();
            Player player = Bukkit.getPlayer(auctionOwner.getUuid());
            if (player != null) {
                plugin.getEconomy().depositPlayer(player, auction.getHighestBid().getAmount());
            }
        }
    }

    private void handleNoBids(Auction auction, boolean message) {
        if (message) {
            Messages messages = plugin.getConfigurationManager().getMessages();
            Broadcast.send(messages.endedWithoutBids
                    .replace("%player%", auction.getAuctionOwner().getName())
                    .replace("%item%", getItemName(auction.deserializeItem()))
            );
        }
        giveItemToPlayer(auction.getAuctionOwner().getUuid(), auction);
    }

    private void recordAuctionLog(Auction auction) {
        Date date = new Date();
        String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);

        Bid highestBid = auction.getHighestBid() != null ? auction.getHighestBid() : new Bid(new Bidder("none", "none"), 0);

        AuctionLog log = new AuctionLog(
                auction.getAuctionOwner(),
                highestBid,
                formattedDate,
                auction.getSerializedItem(),
                auction.getBids().size()
        );

        plugin.getDataSource().insertAuctionLog(log);
    }

    private void giveItemToPlayer(String uuid, Auction auction) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            player.getInventory().addItem(auction.deserializeItem());
        }
    }

    public String getItemName(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null && itemMeta.hasDisplayName() && isNotBlank(itemMeta.getDisplayName())) {
            return itemMeta.getDisplayName();
        } else {
            String itemType = item.serialize().get("type").toString().toLowerCase();
            return formatTypeName(itemType);
        }
    }

    private String formatTypeName(String itemType) {
        return itemType.replace("_", " ")
                .replaceFirst(String.valueOf(itemType.charAt(0)), String.valueOf(Character.toUpperCase(itemType.charAt(0))));
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
