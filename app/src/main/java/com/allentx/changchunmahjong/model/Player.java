package com.allentx.changchunmahjong.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private final List<Tile> hand;
    private final List<Meld> melds;
    private final List<Tile> discards;
    private final int seatIndex;
    
    public Player(int seatIndex) {
        this.seatIndex = seatIndex;
        this.hand = new ArrayList<>();
        this.melds = new ArrayList<>();
        this.discards = new ArrayList<>();
    }

    public void addTile(Tile tile) {
        hand.add(tile);
        sortHand();
    }
    
    public void removeTile(Tile tile) {
        // Need to be careful with object reference equality vs logical equality
        // Tile.equals uses logical equality (rank/suit)
        hand.remove(tile);
    }

    public void sortHand() {
        Collections.sort(hand);
    }

    public List<Tile> getHand() {
        return hand;
    }

    public List<Meld> getMelds() {
        return melds;
    }

    public List<Tile> getDiscards() {
        return discards;
    }

    public int getSeatIndex() {
        return seatIndex;
    }
    
    public void addMeld(Meld meld) {
        melds.add(meld);
    }
    
    public void addDiscard(Tile tile) {
        discards.add(tile);
    }
}
