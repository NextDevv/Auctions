package com.nextdevv.auctions.commands;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class AuctionOwner implements Serializable {
    private final String name;
    private final String uuid;

    public AuctionOwner(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }
}
