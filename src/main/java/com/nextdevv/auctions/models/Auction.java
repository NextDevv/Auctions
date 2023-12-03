package com.nextdevv.auctions.models;

import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.commands.AuctionOwner;
import com.nextdevv.auctions.config.Messages;
import com.nextdevv.auctions.utils.Broadcast;
import com.nextdevv.auctions.utils.ClassSerializer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
public class Auction implements Serializable {
    private final AuctionOwner auctionOwner;
    private final double increment;
    private double startingPrice;
    private double buyNowPrice;
    @Setter
    private int duration;
    private final int originalDuration;
    private final String serializedItem;
    private final List<Bid> bids = new ArrayList<>();
    @Setter
    private boolean isStarted = false;

    public Auction(@NotNull AuctionOwner auctionOwner, double increment, double startingPrice, double buyNowPrice, int duration, ItemStack item) {
        this.auctionOwner = auctionOwner;
        this.increment = increment;
        this.startingPrice = startingPrice;
        this.buyNowPrice = buyNowPrice;
        this.duration = duration;
        this.serializedItem = serializeItem(item);
        this.originalDuration = duration;
    }

    private String serializeItem(ItemStack item) {
        return ClassSerializer.ToStringItemStack(item);
    }

    public ItemStack deserializeItem() {
        return ClassSerializer.FromStringItemStack(serializedItem);
    }

    public void placeBid(@NotNull Bid bid) {
        // If the bidder has already a bid increase the amount of that bid, if not add a new bid
        Auctions plugin = Auctions.getPlugin(Auctions.class);
        Messages messages = plugin.getConfigurationManager().getMessages();
        Bid existingBid = getBid(bid.getBidder().getUuid());
        if(existingBid != null) {
            existingBid.setAmount(existingBid.getAmount() + bid.getAmount());
        } else {
            bids.add(bid);
            existingBid = bid;
        }
        Broadcast.send(messages.bidPlacedBySomeone
                .replace("%player%", existingBid.getBidder().getName())
                .replace("%amount%", plugin.getEconomy().format(existingBid.getAmount())));
    }

    public Bid getBid(@NotNull String uuid) {
        for(Bid bid : bids) {
            if(bid.getBidder().getUuid().equals(uuid)) return bid;
        }
        return null;
    }

    public void incrementPrice() {
        startingPrice += increment;
        buyNowPrice += increment;
    }

    public void removeBid(Bid bid) {
        if(bid == null) return;
        bids.remove(bid);
    }

    public Bid getHighestBid() {
        if(bids.isEmpty()) return null; else if (bids.size() == 1) return bids.get(0);
        return bids.stream().max(Comparator.comparingDouble(Bid::getAmount)).orElse(null);
    }

    public boolean hasBids() {
        return !bids.isEmpty();
    }

    public boolean hasBuyNowPrice() {
        return buyNowPrice > 0;
    }
}
