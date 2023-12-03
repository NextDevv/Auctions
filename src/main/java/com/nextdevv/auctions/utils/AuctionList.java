package com.nextdevv.auctions.utils;

import com.nextdevv.auctions.models.Auction;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Stream;

@Getter
public class AuctionList implements Serializable {
    private final ArrayList<Auction> auctions = new ArrayList<>();

    public void add(Auction auction) {
        auctions.add(auction);
    }

    public boolean remove(Auction auction) {
        return auctions.remove(auction);
    }

    public Auction get(int index) {
        return auctions.get(index);
    }

    public boolean isEmpty() {
        return auctions.isEmpty();
    }

    public int size() {
        return auctions.size();
    }

    public void removeDuplicates() {
        // remove if there's the same uuid and items multiple times
        auctions.removeIf(auction -> {
            for(Auction auction1 : auctions) {
                if(auction1.getAuctionOwner().getUuid().equals(auction.getAuctionOwner().getUuid()) && auction1.getSerializedItem().equals(auction.getSerializedItem())) {
                    return true;
                }
            }
            return false;
        });
    }

    public Stream<Auction> stream() {
        return auctions.stream();
    }
}
