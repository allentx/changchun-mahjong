package com.allentx.changchunmahjong.logic;

import com.allentx.changchunmahjong.model.Tile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartAiStrategy {

    /**
     * Recommends a tile to discard from the AI's hand.
     */
    public static Tile recommendDiscard(List<Tile> hand, List<Tile> tableDiscards,
            List<com.allentx.changchunmahjong.model.Meld> myMelds, List<Tile> allMeldsTiles) {
        if (hand == null || hand.isEmpty())
            return null;

        boolean hasPengGang = false;
        for (com.allentx.changchunmahjong.model.Meld m : myMelds) {
            if (m.getType() != com.allentx.changchunmahjong.model.Meld.Type.CHI) {
                hasPengGang = true;
                break;
            }
        }

        List<Tile> visibleTiles = new ArrayList<>(tableDiscards);
        visibleTiles.addAll(allMeldsTiles);
        // Important: Hand is also visible to the owner AI
        visibleTiles.addAll(hand);

        Map<Tile, Double> scores = new HashMap<>();
        for (Tile t : hand) {
            scores.put(t, calculateKeepValue(t, hand, visibleTiles, hasPengGang));
        }

        // Find tile with minimum score
        Tile bestToDiscard = hand.get(0);
        double minScore = Double.MAX_VALUE;

        // Shuffle hand to avoid predictable discards of identical valued tiles
        List<Tile> shuffledHand = new ArrayList<>(hand);
        Collections.shuffle(shuffledHand);

        for (Tile t : shuffledHand) {
            double score = scores.get(t);
            if (score < minScore) {
                minScore = score;
                bestToDiscard = t;
            }
        }

        return bestToDiscard;
    }

    private static double calculateKeepValue(Tile target, List<Tile> hand, List<Tile> visible, boolean hasPengGang) {
        double score = 0;

        // 1. Check for Triplets / Quadruplets
        int countInHand = count(hand, target);
        if (countInHand >= 3) {
            score += 100;
        } else if (countInHand == 2) {
            boolean isDragon = target.getSuit() == Tile.Suit.ZI && (target.getRank() == Tile.ID_ZHONG
                    || target.getRank() == Tile.ID_FA || target.getRank() == Tile.ID_BAI);

            // Priority: Need at least one Triplet (Peng/Gang/AnGang) to win.
            // If we don't have any yet, pairs are critical.
            if (!hasPengGang) {
                score += isDragon ? 180 : 150; // Higher value to encourage forming the first triplet
            } else {
                score += isDragon ? 150 : 40;
            }
        }

        // 2. Yao Jiu Requirement (Terminal or Honor)
        // Must have at least one 1, 9, or ZIPai in the final hand.
        if (target.getSuit() == Tile.Suit.ZI || target.getRank() == 1 || target.getRank() == 9) {
            // Check if we already have other Yao Jiu tiles
            boolean hasOtherYaoJiu = false;
            for (Tile h : hand) {
                if (h != target && (h.getSuit() == Tile.Suit.ZI || h.getRank() == 1 || h.getRank() == 9)) {
                    hasOtherYaoJiu = true;
                    break;
                }
            }
            if (!hasOtherYaoJiu) {
                score += 100; // Keep the only Yao Jiu tile
            } else {
                score += 20; // Slight preference for keeping terminal flexibility
            }
        }

        // 3. Three Suits Requirement (Must have WAN, TIAO, and TONG)
        // Ensure we don't discard the last tile of a suit we need.
        Tile.Suit targetSuit = target.getSuit();
        if (targetSuit != Tile.Suit.ZI) {
            boolean hasWan = false;
            boolean hasTiao = false;
            boolean hasTong = false;
            int countOfTargetSuit = 0;

            for (Tile h : hand) {
                if (h.getSuit() == Tile.Suit.WAN)
                    hasWan = true;
                if (h.getSuit() == Tile.Suit.TIAO)
                    hasTiao = true;
                if (h.getSuit() == Tile.Suit.TONG)
                    hasTong = true;
                if (h.getSuit() == targetSuit)
                    countOfTargetSuit++;
            }

            // Also check exposed melds for suit presence
            // (Passed in via visibility or could be simplified)
            // If we only have ONE tile of this suit, and we don't have all 3 suits yet, DO
            // NOT DISCARD.
            if (countOfTargetSuit == 1 && (!hasWan || !hasTiao || !hasTong)) {
                score += 200; // Critical suit protection
            }
        }

        // 4. Check for Sequences (only for numbered tiles)
        if (target.isNumber()) {
            Tile.Suit suit = target.getSuit();
            int rank = target.getRank();

            // Part of a sequence?
            boolean hasSeq = (has(hand, suit, rank - 1) && has(hand, suit, rank + 1)) ||
                    (has(hand, suit, rank - 2) && has(hand, suit, rank - 1)) ||
                    (has(hand, suit, rank + 1) && has(hand, suit, rank + 2));
            if (hasSeq) {
                score += 80;
            }

            // Sequence Waits (e.g. 2,3 looking for 1,4)
            boolean sideWait = has(hand, suit, rank - 1) || has(hand, suit, rank + 1);
            if (sideWait) {
                score += 25 * calculateAvailabilityModifier(target, hand, visible, "side");
            }

            // Gap Waits (e.g. 2,4 looking for 3)
            boolean gapWait = has(hand, suit, rank - 2) || has(hand, suit, rank + 2);
            if (gapWait) {
                score += 12 * calculateAvailabilityModifier(target, hand, visible, "gap");
            }
        } else {
            // Honors (ZI) are generally worth less unless they are part of a set
            if (countInHand == 1) {
                score -= 20;
            }
        }

        // 3. Global Availability Check
        // If the tile itself is very visible on the table, it's harder to get more
        // copies
        int totalVisible = count(visible, target);
        score -= (totalVisible * 5); // Subtract 5 points for every copy already seen

        return score;
    }

    private static double calculateAvailabilityModifier(Tile target, List<Tile> hand, List<Tile> visible, String type) {
        // Simple heuristic: if the tiles we need to finish this wait are all gone, the
        // wait is useless
        Tile.Suit suit = target.getSuit();
        int rank = target.getRank();

        List<Tile> needed = new ArrayList<>();
        if ("side".equals(type)) {
            if (has(hand, suit, rank - 1)) {
                needed.add(new Tile(suit, rank - 2));
                needed.add(new Tile(suit, rank + 1));
            } else {
                needed.add(new Tile(suit, rank - 1));
                needed.add(new Tile(suit, rank + 2));
            }
        } else if ("gap".equals(type)) {
            if (has(hand, suit, rank - 2))
                needed.add(new Tile(suit, rank - 1));
            else
                needed.add(new Tile(suit, rank + 1));
        }

        double availability = 1.0;
        int maxPossible = needed.size() * 4;
        int seen = 0;
        for (Tile n : needed) {
            if (n.getRank() < 1 || n.getRank() > 9) {
                seen += 4; // Out of bounds is "dead"
                continue;
            }
            seen += count(visible, n);
        }

        if (maxPossible > 0) {
            availability = (double) (maxPossible - seen) / maxPossible;
        }

        return Math.max(0, availability);
    }

    private static int count(List<Tile> list, Tile target) {
        int c = 0;
        for (Tile t : list)
            if (t.equals(target))
                c++;
        return c;
    }

    private static boolean has(List<Tile> hand, Tile.Suit suit, int rank) {
        if (rank < 1 || rank > 9)
            return false;
        for (Tile t : hand) {
            if (t.getSuit() == suit && t.getRank() == rank)
                return true;
        }
        return false;
    }
}
