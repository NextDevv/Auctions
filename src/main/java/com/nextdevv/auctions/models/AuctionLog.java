package com.nextdevv.auctions.models;

import com.nextdevv.auctions.commands.AuctionOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public record AuctionLog(@NotNull AuctionOwner auctionOwner, Bid winner, String formattedDate, String serializedItem, int totalBids) {
    public AuctionLog(@NotNull AuctionOwner auctionOwner, @Nullable Bid winner, Date date, String serializedItem, int totalBids) {
        this(auctionOwner, winner, date.toString(), serializedItem, totalBids);
    }
}
