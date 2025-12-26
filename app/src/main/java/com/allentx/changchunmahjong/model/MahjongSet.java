package com.allentx.changchunmahjong.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MahjongSet {
    private final List<Tile> tiles;

    public MahjongSet() {
        tiles = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        tiles.clear();
        // Add Wan, Tiao, Tong (1-9) * 4
        for (Tile.Suit suit : new Tile.Suit[] { Tile.Suit.WAN, Tile.Suit.TIAO, Tile.Suit.TONG }) {
            for (int rank = 1; rank <= 9; rank++) {
                for (int i = 0; i < 4; i++) {
                    tiles.add(new Tile(suit, rank));
                }
            }
        }
        // Add Zi (1-7) * 4
        for (int rank = 1; rank <= 7; rank++) {
            for (int i = 0; i < 4; i++) {
                tiles.add(new Tile(Tile.Suit.ZI, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(tiles);
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public int getSize() {
        return tiles.size();
    }
}
