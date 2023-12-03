package com.nextdevv.auctions.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
@Configuration
public class Settings {

    @Comment("redis[s]://[password@]host[:port][/database][?option=value]")
    public String redisUri = "redis://localhost:6379/0?timeout=20s&clientName=Auctions";

    @Comment("Max queue size for auction items")
    public int maxQueueSize = 4;

    @Comment("Time between new auctions in seconds")
    public int timeBetween = 5;

    @Comment("Minimum cancel time in seconds")
    public int minCancelTime = 20;

    @Comment("Auction fee to start an auction")
    public double auctionFee = 1000;

    public int maxAuctionTime = 60;
    public int minAuctionTime = 10;

    public int auctionInterval = 10;

    public int startCountdownAt = 5;


    public Database database = new Database();
    public record Database(String host, int port, String name, String username, String password, String additional) {
        public Database() {
            this("localhost", 3317, "claimbank_test", "root", "Roberto123/...", "useSSL=false&useUnicode=true&characterEncoding=utf8");
        }
    }
}
