package com.nextdevv.auctions.redis;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.config.Messages;
import com.nextdevv.auctions.managers.AuctionManager;
import com.nextdevv.auctions.models.Auction;
import com.nextdevv.auctions.models.Bid;
import com.nextdevv.auctions.redis.redisdata.RedisAbstract;
import com.nextdevv.auctions.redis.redisdata.RedisPubSub;
import com.nextdevv.auctions.utils.AuctionList;
import com.nextdevv.auctions.utils.Broadcast;
import com.nextdevv.auctions.utils.ClassSerializer;
import io.lettuce.core.RedisClient;

import java.io.File;
import java.util.UUID;


public class RedisImpl extends RedisAbstract {
    private final Auctions plugin;
    private final Gson gson;


	public RedisImpl(RedisClient lettuceRedisClient, int size, Auctions plugin) {
		super(lettuceRedisClient, size);
		gson = new Gson().newBuilder().setPrettyPrinting().create();
		this.plugin = plugin;
		subscribe();
	}

    public void publish(JsonObject jsonObject) {
        jsonObject.addProperty("server", getServerName());
        getConnectionAsync(c -> c.publish("auction", jsonObject.toString()));
    }

    public void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<String, String>() {
                @Override
                public void message(String channel, String message) {
                    Messages messages = plugin.getConfigurationManager().getMessages();

                    JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
                    if(jsonObject.get("server").getAsString().equals(getServerName())) return;
                    String event = jsonObject.get("event").getAsString().toLowerCase();
                    switch (event) {
                        case "start":
                            // plugin.getAuctionManager().startAuction((Auction) ClassSerializer.FromString(jsonObject.get("auction").getAsString()));
                            break;
                        case "end":
                            plugin.getAuctionManager().end((Auction) ClassSerializer.FromString(jsonObject.get("auction").getAsString()), true);
                            break;
                        case "force_end":
                            plugin.getAuctionManager().end((Auction) ClassSerializer.FromString(jsonObject.get("auction").getAsString()), false);
                        case "bid":
                            Bid bid = (Bid) ClassSerializer.FromString(jsonObject.get("bid").getAsString());
                            if(bid == null) {
                                System.out.println("Bid is null");
                                return;
                            }

                            plugin.getAuctionManager().getCurrentAuction().placeBid(bid);
                            break;
                        case "cancel_bid":
                            plugin.getAuctionManager().getCurrentAuction().removeBid((Bid) ClassSerializer.FromString(jsonObject.get("bid").getAsString()));
                            break;
                        case "queue":
                            Auction auction1 = (Auction) ClassSerializer.FromString(jsonObject.get("auction").getAsString());
                            plugin.getAuctionManager().getAuctionQueue().add(auction1);
                            break;
                        case "remove":
                            System.out.println("REMOVE");
                            Auction auction = (Auction) ClassSerializer.FromString(jsonObject.get("auction").getAsString());
                            System.out.println(auction.getAuctionOwner().getName());
                            System.out.println(ClassSerializer.FromStringItemStack(auction.getSerializedItem()).serialize());
                            plugin.getAuctionManager().removeAuction(auction);
                            break;
                    }
                }
            });
			c.async().subscribe("auction");
        });
    }

	public String getServerName() {
        return new File(System.getProperty("user.dir")).getName();
    }
}
