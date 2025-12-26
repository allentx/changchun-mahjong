package com.allentx.changchunmahjong.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public static final int SEAT_EAST = 0;
    public static final int SEAT_SOUTH = 1;
    public static final int SEAT_WEST = 2;
    public static final int SEAT_NORTH = 3;

    private final MahjongSet mahjongSet;
    private final Player[] players;
    private final List<Tile> wall;
    private final List<Tile> discards; // Added discards list
    private int bankerIndex; // Dealer

    // Dice handling for wall breaking
    private int dice1_Sum;
    private int dice2_Sum;

    public Table() {
        mahjongSet = new MahjongSet();
        players = new Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new Player(i);
        }
        wall = new ArrayList<>();
        discards = new ArrayList<>(); // Initialize discards list
        bankerIndex = SEAT_EAST; // Default start
    }

    // Call this to start a new round -> SHUFFLE ONLY
    public void startRound() {
        // 1. Shuffle
        mahjongSet.shuffle();
        wall.clear();
        wall.addAll(mahjongSet.getTiles());

        // 2. Clear players and table discards
        discards.clear();
        for (Player p : players) {
            p.getHand().clear();
            p.getMelds().clear();
            p.getDiscards().clear();
        }

        // Dice should be set by GameManager, but we can reset them
        dice1_Sum = 0;
        dice2_Sum = 0;
    }

    public Tile drawFromWall() {
        if (wall.isEmpty())
            return null; // Draw game?
        return wall.remove(0); // Removing from "Head" of the list
    }

    public Player getPlayer(int index) {
        return players[index];
    }

    public int getBankerIndex() {
        return bankerIndex;
    }

    public void setBankerIndex(int index) {
        this.bankerIndex = index;
    }

    public List<Tile> getWall() {
        return wall;
    }

    public void addDiscard(Tile tile) {
        discards.add(tile);
    }

    public List<Tile> getDiscards() {
        return discards;
    }
}
