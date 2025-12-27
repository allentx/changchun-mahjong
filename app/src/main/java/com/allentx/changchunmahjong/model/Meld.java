package com.allentx.changchunmahjong.model;

import java.util.List;

public class Meld {
    public enum Type {
        CHI, // Sequence from upper house
        PENG, // Triplet from any house
        MING_GANG, // Exposed Quad from discard
        AN_GANG, // Hidden Quad from hand
        BU_GANG // Added Quad (was Peng, self-drew 4th)
    }

    private final Type type;
    private final Tile firstTile; // For Chi: lowest rank; For others: the tile type
    private final List<Tile> tiles;
    private final int fromPlayer; // Seat index of who fed the tile (-1 if self/AnGang)

    public Meld(Type type, List<Tile> tiles, int fromPlayer) {
        this.type = type;
        this.tiles = tiles;
        this.fromPlayer = fromPlayer;
        if (tiles != null && !tiles.isEmpty()) {
            java.util.Collections.sort(this.tiles);
            this.firstTile = this.tiles.get(0);
        } else {
            this.firstTile = null;
        }
    }

    public Type getType() {
        return type;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public int getFromPlayer() {
        return fromPlayer;
    }

    public Tile getFirstTile() {
        return firstTile;
    }
}
