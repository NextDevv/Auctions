package com.nextdevv.auctions.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Bid implements Serializable {
    private final Bidder bidder;
    @Setter
    private double amount;

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
    }
}
