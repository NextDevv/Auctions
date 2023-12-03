package com.nextdevv.auctions.models;

import com.nextdevv.auctions.Auctions;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.io.Serializable;

@Getter
public class Bidder implements Serializable {
    private final String name;
    private final String uuid;

    public Bidder(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public void giveMoney(double amount) {
        Player player = Auctions.getPlugin(Auctions.class).getServer().getPlayer(uuid);
        if(player != null)
            Auctions.getPlugin(Auctions.class).getEconomy().depositPlayer(player, amount);
    }
}
