package com.allentx.changchunmahjong.logic;

import com.allentx.changchunmahjong.model.Meld;
import com.allentx.changchunmahjong.model.Tile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuleValidatorHelper {

    /**
     * Check if the hand + validMelds + newTile constitutes a Hu (Win).
     * 
     * @param handTiles  Tiles in hand (excluding fixed melds)
     * @param fixedMelds Exposed melds (Chi/Peng/Gang)
     * @param newTile    The tile just drawn or irrelevant if checking strict hand.
     *                   If checking validity after draw, `handTiles` should include
     *                   it, or pass it here.
     *                   Let's assume handTiles INCLUDES the new tile for simplicity
     *                   in recursion.
     */
    public static boolean isHu(List<Tile> handTiles) {
        // Sort first
        List<Tile> sortedHand = new ArrayList<>(handTiles);
        Collections.sort(sortedHand);

        // 1. Check 7 Pairs (Qi Dui)
        if (isQiDui(sortedHand))
            return true;

        // 2. Check Standard 4 Melds + 1 Pair
        // We know we need 14 tiles total (including fixed melds).
        // Standard check acts only on the "Standing" tiles.
        // If we have M fixed melds, we need (4 - M) sets + 1 pair in hand.
        // Remainder tiles in hand should be: 14 - 3*M.
        // e.g. 0 fixed -> 14 tiles -> 4 sets + 1 pair
        // 1 fixed -> 11 tiles -> 3 sets + 1 pair

        return isStandardHu(sortedHand);
    }

    private static boolean isQiDui(List<Tile> hand) {
        if (hand.size() != 14)
            return false;
        for (int i = 0; i < 14; i += 2) {
            if (!hand.get(i).equals(hand.get(i + 1)))
                return false;
        }
        return true;
    }

    private static boolean isStandardHu(List<Tile> hand) {
        // Basic requirement: Size % 3 == 2 (e.g., 2, 5, 8, 11, 14)
        if (hand.size() % 3 != 2)
            return false;

        // Try every unique tile as the Pair
        List<Tile> uniqueTiles = new ArrayList<>();
        for (Tile t : hand) {
            if (!uniqueTiles.contains(t))
                uniqueTiles.add(t);
        }

        for (Tile pairTile : uniqueTiles) {
            // Count occurrences
            int count = 0;
            for (Tile t : hand)
                if (t.equals(pairTile))
                    count++;
            if (count >= 2) {
                // Clone and remove pair
                List<Tile> remaining = new ArrayList<>(hand);
                remaining.remove(pairTile);
                remaining.remove(pairTile); // Remove only 2

                if (canFormSets(remaining))
                    return true;
            }
        }
        return false;
    }

    // Recursive backtracking to check if tiles can form valid Sets (Groups of 3)
    private static boolean canFormSets(List<Tile> tiles) {
        if (tiles.isEmpty())
            return true;

        Tile first = tiles.get(0);

        // 1. Try Koutsu (Triplet)
        // Find 2 others equal to first
        int count = 0;
        for (Tile t : tiles)
            if (t.equals(first))
                count++;

        if (count >= 3) {
            List<Tile> nextTiles = new ArrayList<>(tiles);
            nextTiles.remove(first);
            nextTiles.remove(first); // Remove by object, relies on equals
            nextTiles.remove(first);
            if (canFormSets(nextTiles))
                return true;
        }

        // 2. Try Shuntsu (Sequence) - Only for numbered suits
        if (first.isNumber()) {
            Tile t2 = findAlso(tiles, first.getSuit(), first.getRank() + 1);
            Tile t3 = findAlso(tiles, first.getSuit(), first.getRank() + 2);

            if (t2 != null && t3 != null) {
                List<Tile> nextTiles = new ArrayList<>(tiles);
                nextTiles.remove(first);
                nextTiles.remove(t2);
                nextTiles.remove(t3);
                if (canFormSets(nextTiles))
                    return true;
            }
        }

        return false;
    }

    private static Tile findAlso(List<Tile> tiles, Tile.Suit suit, int rank) {
        for (Tile t : tiles) {
            if (t.getSuit() == suit && t.getRank() == rank)
                return t;
        }
        return null;
    }

    // --- Action Checkers ---

    public static boolean canChi(List<Tile> hand, Tile target) {
        // Can only Chi if target allows forming a sequence.
        // Combinations: (target-2, target-1), (target-1, target+1), (target+1,
        // target+2)
        if (!target.isNumber())
            return false;

        boolean c1 = has(hand, target.getSuit(), target.getRank() - 2)
                && has(hand, target.getSuit(), target.getRank() - 1);
        boolean c2 = has(hand, target.getSuit(), target.getRank() - 1)
                && has(hand, target.getSuit(), target.getRank() + 1);
        boolean c3 = has(hand, target.getSuit(), target.getRank() + 1)
                && has(hand, target.getSuit(), target.getRank() + 2);

        return c1 || c2 || c3;
    }

    public static boolean canPeng(List<Tile> hand, Tile target) {
        int count = 0;
        for (Tile t : hand) {
            if (t.equals(target))
                count++;
        }
        return count >= 2;
    }

    public static boolean canMingGang(List<Tile> hand, Tile target) {
        int count = 0;
        for (Tile t : hand) {
            if (t.equals(target))
                count++;
        }
        return count >= 3;
    }

    public static boolean canAnGang(List<Tile> hand) {
        // Check for ANY 4-of-a-kind
        // Since hand is typically sorted or we iterate
        for (Tile t : hand) {
            int count = 0;
            for (Tile check : hand)
                if (check.equals(t))
                    count++;
            if (count == 4)
                return true;
        }
        return false;
    }

    private static boolean has(List<Tile> hand, Tile.Suit suit, int rank) {
        for (Tile t : hand) {
            if (t.getSuit() == suit && t.getRank() == rank)
                return true;
        }
        return false;
    }
}
