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
        Map<Tile, Double> scores = getKeepValues(hand, tableDiscards, myMelds, allMeldsTiles);
        if (scores.isEmpty())
            return null;

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

    public static boolean shouldAiMeld(List<Tile> currentHand,
            List<com.allentx.changchunmahjong.model.Meld> currentMelds,
            Tile targetTile, com.allentx.changchunmahjong.model.Meld.Type type,
            List<Tile> tableDiscards, List<Tile> allMeldsTiles) {

        // 1. If it's a win, ALWAYS meld (though Hu is checked separately, this is for
        // safety)
        List<Tile> nextHand = new ArrayList<>(currentHand);
        if (type != com.allentx.changchunmahjong.model.Meld.Type.AN_GANG) {
            nextHand.add(targetTile);
        }
        if (RuleValidatorHelper.isHu(nextHand, currentMelds))
            return true;

        // 2. Limit melds to 3 for flexibility unless it's a win
        if (currentMelds.size() >= 3)
            return false;

        // 3. Evaluation: Does melting improve our "Outs" or "Keep Value"?
        // For now, keep it simple: Peng/Gang is usually good. Chi is conditional.
        if (type == com.allentx.changchunmahjong.model.Meld.Type.PENG
                || type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG) {
            return true;
        }

        if (type == com.allentx.changchunmahjong.model.Meld.Type.CHI) {
            // Only Chi if it completes a sequence that was "stagnant" or if hand is close
            // to winning
            // Simplified: if we have more than 7 tiles of the same suit, Chi is better.
            return true; // For now, Chi is still valued.
        }

        return true;
    }

    public static Map<Tile, Double> getKeepValues(List<Tile> hand, List<Tile> tableDiscards,
            List<com.allentx.changchunmahjong.model.Meld> myMelds, List<Tile> allMeldsTiles) {
        if (hand == null || hand.isEmpty())
            return new HashMap<>();

        boolean hasPengGang = false;
        for (com.allentx.changchunmahjong.model.Meld m : myMelds) {
            if (m.getType() != com.allentx.changchunmahjong.model.Meld.Type.CHI) {
                hasPengGang = true;
                break;
            }
        }

        List<Tile> visibleTiles = new ArrayList<>(tableDiscards);
        visibleTiles.addAll(allMeldsTiles);
        visibleTiles.addAll(hand);

        Map<Tile, Double> scores = new HashMap<>();
        List<Tile> allPossibleTiles = getAllPossibleTiles();

        for (Tile t : hand) {
            double baseScore = calculateKeepValue(t, hand, visibleTiles, myMelds);

            // Simulation: If we discard 't', what is our winning potential?
            List<Tile> remainingHand = new ArrayList<>(hand);
            remainingHand.remove(t);

            int outs = countAvailableOuts(remainingHand, myMelds, allPossibleTiles, visibleTiles);
            if (outs > 0) {
                // Massive bonus for staying in Tenpai (Ready) state
                // This 't' is a bad tile to keep because the OTHER tiles are part of a winning
                // wait.
                // So 't' should have a LOW keep value (meaning it's the discard).
                // Wait, if scores[t] is keep value, then lower = discard.
                // So if discarding 't' gives us outs, then baseScore should be lowered or
                // other tiles' scores should be raised.
                // Let's think: calculateKeepValue is for 't'.
                // If discarding 't' is GOOD, then t's keep value should be LOW.
                baseScore -= (500 + outs * 50);
            }

            scores.put(t, baseScore);
        }
        return scores;
    }

    private static List<Tile> getAllPossibleTiles() {
        List<Tile> all = new ArrayList<>();
        for (Tile.Suit s : Tile.Suit.values()) {
            if (s == Tile.Suit.ZI) {
                for (int r = 1; r <= 7; r++)
                    all.add(new Tile(s, r));
            } else {
                for (int r = 1; r <= 9; r++)
                    all.add(new Tile(s, r));
            }
        }
        return all;
    }

    private static int countAvailableOuts(List<Tile> hand, List<com.allentx.changchunmahjong.model.Meld> melds,
            List<Tile> allPossibleTiles, List<Tile> visible) {
        int outs = 0;
        for (Tile candidate : allPossibleTiles) {
            // How many are left?
            int visibleCount = count(visible, candidate);
            if (visibleCount >= 4)
                continue; // None left in wall/others hands

            List<Tile> testHand = new ArrayList<>(hand);
            testHand.add(candidate);
            if (RuleValidatorHelper.isHu(testHand, melds)) {
                outs += (4 - visibleCount); // Add all remaining physical copies
            }
        }
        return outs;
    }

    private static double calculateKeepValue(Tile target, List<Tile> hand, List<Tile> visible,
            List<com.allentx.changchunmahjong.model.Meld> melds) {
        double score = 0;

        boolean hasPengGang = false;
        for (com.allentx.changchunmahjong.model.Meld m : melds) {
            if (m.getType() != com.allentx.changchunmahjong.model.Meld.Type.CHI) {
                hasPengGang = true;
                break;
            }
        }

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
            boolean hasOtherYaoJiu = false;
            // Check Hand
            for (Tile h : hand) {
                if (h != target && (h.getSuit() == Tile.Suit.ZI || h.getRank() == 1 || h.getRank() == 9)) {
                    hasOtherYaoJiu = true;
                    break;
                }
            }
            // Check Melds
            if (!hasOtherYaoJiu) {
                for (com.allentx.changchunmahjong.model.Meld m : melds) {
                    for (Tile mt : m.getTiles()) {
                        if (mt.getSuit() == Tile.Suit.ZI || mt.getRank() == 1 || mt.getRank() == 9) {
                            hasOtherYaoJiu = true;
                            break;
                        }
                    }
                    if (hasOtherYaoJiu)
                        break;
                }
            }

            if (!hasOtherYaoJiu) {
                score += 100; // Keep the only Yao Jiu tile across hand and melds
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

            // Check Hand
            for (Tile h : hand) {
                if (h.getSuit() == Tile.Suit.WAN)
                    hasWan = true;
                if (h.getSuit() == Tile.Suit.TIAO)
                    hasTiao = true;
                if (h.getSuit() == Tile.Suit.TONG)
                    hasTong = true;
            }
            // Check Melds
            for (com.allentx.changchunmahjong.model.Meld m : melds) {
                Tile mt = m.getTiles().get(0); // All tiles in a meld have same suit unless it's a weird win
                if (mt.getSuit() == Tile.Suit.WAN)
                    hasWan = true;
                if (mt.getSuit() == Tile.Suit.TIAO)
                    hasTiao = true;
                if (mt.getSuit() == Tile.Suit.TONG)
                    hasTong = true;
            }

            int countOfTargetSuitInHand = 0;
            for (Tile h : hand) {
                if (h.getSuit() == targetSuit)
                    countOfTargetSuitInHand++;
            }

            boolean suitInMelds = false;
            for (com.allentx.changchunmahjong.model.Meld m : melds) {
                if (m.getTiles().get(0).getSuit() == targetSuit) {
                    suitInMelds = true;
                    break;
                }
            }

            // Enhanced Suit Protection:
            // If we have no melds of this suit, we must protect the few tiles we have
            // to ensure we can fulfill the "Three Suits" requirement later.
            if (!suitInMelds) {
                if (countOfTargetSuitInHand == 1) {
                    score += 400; // Final tile: Absolute extreme protection to avoid losing the suit
                } else if (countOfTargetSuitInHand == 2) {
                    score += 300; // Very high protection: losing this would make the suit "critical"
                } else if (countOfTargetSuitInHand == 3) {
                    score += 150; // Scarcity protection: prevent tossing single tiles of a required suit
                }
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
