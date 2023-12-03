package com.nextdevv.auctions.commands;

import com.google.gson.JsonObject;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.config.Messages;
import com.nextdevv.auctions.enums.AuctionEvent;
import com.nextdevv.auctions.managers.AuctionManager;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.auctions.models.Bid;
import com.nextdevv.auctions.models.Bidder;
import com.nextdevv.auctions.utils.Broadcast;
import com.nextdevv.auctions.utils.ClassSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BidCommand {
    private final Auctions PLUGIN = Auctions.getPlugin(Auctions.class);
    private final Messages messages = PLUGIN.getConfigurationManager().getMessages();

    @Command(name = "", desc = "Bid command", usage = "/bid <amount>")
    public void bid(@Sender Player sender, double amount) {
        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        Auction auction = auctionManager.getCurrentAuction();
        if(auction == null || !auction.isStarted()) {
            send(sender, messages.noAuction);
            return;
        }

        if(auction.getAuctionOwner().getUuid().equals(sender.getName())) {
            send(sender, messages.cantBidOnOwnAuction);
            return;
        }

        if(amount > PLUGIN.getEconomy().getBalance(sender)) {
            send(sender, messages.notEnoughMoney);
            return;
        }

        if(amount < auction.getStartingPrice()) {
            send(sender, messages.bidLessThanStartingPrice);
            return;
        }

        PLUGIN.getEconomy().withdrawPlayer(sender, amount);
        Bid bid = new Bid(new Bidder(sender.getName(), sender.getName()), amount);
        auctionManager.getCurrentAuction().placeBid(bid);
        send(sender, messages.bidPlaced);


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.BID.name());
        jsonObject.addProperty("bid", ClassSerializer.ToString(bid));
        PLUGIN.getRedisManager().publish(jsonObject);
    }

    @Command(name = "cancel", desc = "Cancel bid", usage = "/bid cancel")
    public void cancel(@Sender Player sender) {
        AuctionManager auctionManager = PLUGIN.getAuctionManager();
        Auction auction = auctionManager.getCurrentAuction();
        if(auction == null) {
            send(sender, messages.noAuction);
            return;
        }

        if(!auction.getAuctionOwner().getUuid().equals(sender.getName())) {
            send(sender, messages.cantBidOnOwnAuction);
            return;
        }

        if(auction.getOriginalDuration() - auction.getDuration() < PLUGIN.getConfigurationManager().getSettings().minCancelTime) {
            send(sender, messages.cantCancelBid);
            return;
        }

        Bid bid = auction.getBids().stream().filter(bid1 -> bid1.getBidder().getUuid().equals(sender.getUniqueId().toString())).findFirst().orElse(null);
        if(bid == null) {
            send(sender, messages.noBid);
            return;
        }

        auction.removeBid(bid);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", AuctionEvent.CANCEL_BID.name());
        jsonObject.addProperty("bid", ClassSerializer.ToString(bid));
        PLUGIN.getRedisManager().publish(jsonObject);

        send(sender, messages.bidCancelled);
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
