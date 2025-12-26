package com.allentx.changchunmahjong.model;

import java.util.Objects;

public class Tile implements Comparable<Tile> {
    public enum Suit {
        WAN, // Characters (1-9)
        TIAO, // Bamboo (1-9)
        TONG, // Dots (1-9)
        ZI // Winds/Dragons
    }

    public static final int ID_DONG = 1;
    public static final int ID_NAN = 2;
    public static final int ID_XI = 3;
    public static final int ID_BEI = 4;
    public static final int ID_ZHONG = 5;
    public static final int ID_FA = 6;
    public static final int ID_BAI = 7;

    private final Suit suit;
    private final int rank; // 1-9 for numbered, 1-7 for ZI (ESWNZFB)
    private final int id; // Unique ID for finding resources (0-33)

    public Tile(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.id = calculateId();
    }

    private int calculateId() {
        switch (suit) {
            case WAN:
                return rank - 1;
            case TIAO:
                return 9 + (rank - 1);
            case TONG:
                return 18 + (rank - 1);
            case ZI:
                return 27 + (rank - 1);
            default:
                return 0;
        }
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    // The logical ID (0-33)
    public int getId() {
        return id;
    }

    public boolean isNumber() {
        return suit != Suit.ZI;
    }

    // Helper to check if this tile is strictly next to other (this = other + 1)
    public boolean isNext(Tile other) {
        if (this.suit != other.suit || !isNumber())
            return false;
        return this.rank == other.rank + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tile tile = (Tile) o;
        return rank == tile.rank && suit == tile.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return suit + ":" + rank;
    }

    @Override
    public int compareTo(Tile o) {
        int suitCompare = this.suit.compareTo(o.suit);
        if (suitCompare != 0)
            return suitCompare;
        return Integer.compare(this.rank, o.rank);
    }
}
