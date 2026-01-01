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
     *                   Strict Changchun Mahjong Hu validation.
     *                   Total tiles must be 14 (melds * 3 + hand).
     */
    public static boolean isHu(List<Tile> handTiles, List<Meld> melds) {
        // 1. Basic Size Check
        int totalTiles = (melds.size() * 3) + handTiles.size();
        // Exception: Gangs count as 3 tiles for set calculations but have 4 physical
        // tiles.
        // Our Meld model tiles list will have 4 tiles for Gang, but it's logically 1
        // set.
        // Let's count Logical Sets instead.
        int logicalTotal = melds.size() * 3 + handTiles.size();
        // If there's a Gang in melds, it has 4 tiles but counts as 1 set (3 tiles).
        // Actual physical tiles in melds:
        int physicalMeldTiles = 0;
        for (Meld m : melds)
            physicalMeldTiles += m.getTiles().size();
        int actualTotal = physicalMeldTiles + handTiles.size();

        // In Mahjong, you win with 14 tiles (or 14 + number of Gangs).
        // Let's simplify: physicalHand + (melds * 3) should be 14.
        if (logicalTotal != 14)
            return false;

        // 2. Suit/Special Rule Checks
        List<Tile> allTiles = new ArrayList<>(handTiles);
        for (Meld m : melds)
            allTiles.addAll(m.getTiles());

        if (!hasThreeSuits(allTiles))
            return false;
        if (!hasYaoJiu(allTiles))
            return false;

        // 3. Hu Pattern Checks
        List<Tile> sortedHand = new ArrayList<>(handTiles);
        Collections.sort(sortedHand);

        // A. Check 7 Pairs (Only if 0 melds)
        if (melds.isEmpty() && isQiDui(sortedHand))
            return true;

        // B. Check Standard: (4 - melds.size()) sets + 1 pair in hand
        int requiredSets = 4 - melds.size();
        boolean hasExposedPengGang = false;
        for (Meld m : melds) {
            if (m.getType() == Meld.Type.PENG || m.getType() == Meld.Type.MING_GANG
                    || m.getType() == Meld.Type.AN_GANG) {
                hasExposedPengGang = true;
                break;
            }
        }
        return checkStandardHu(sortedHand, requiredSets, hasExposedPengGang);
    }

    private static boolean hasThreeSuits(List<Tile> tiles) {
        boolean hasWan = false;
        boolean hasTiao = false;
        boolean hasTong = false;
        for (Tile t : tiles) {
            if (t.getSuit() == Tile.Suit.WAN)
                hasWan = true;
            if (t.getSuit() == Tile.Suit.TIAO)
                hasTiao = true;
            if (t.getSuit() == Tile.Suit.TONG)
                hasTong = true;
        }
        return hasWan && hasTiao && hasTong;
    }

    private static boolean hasYaoJiu(List<Tile> tiles) {
        for (Tile t : tiles) {
            if (!t.isNumber())
                return true; // Honors count as Yao Jiu
            if (t.getRank() == 1 || t.getRank() == 9)
                return true;
        }
        return false;
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

    private static boolean checkStandardHu(List<Tile> hand, int requiredSets, boolean alreadyHasPengGang) {
        // Try every unique tile as the pair
        List<Tile> uniqueTiles = new ArrayList<>();
        for (Tile t : hand) {
            boolean found = false;
            for (Tile u : uniqueTiles) {
                if (u.equals(t)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                uniqueTiles.add(t);
        }

        for (Tile pairTile : uniqueTiles) {
            int count = 0;
            for (Tile t : hand)
                if (t.equals(pairTile))
                    count++;

            if (count >= 2) {
                List<Tile> remaining = new ArrayList<>(hand);
                remaining.remove(pairTile);
                remaining.remove(pairTile);

                boolean isDragonPair = pairTile.getSuit() == Tile.Suit.ZI && (pairTile.getRank() == Tile.ID_ZHONG
                        || pairTile.getRank() == Tile.ID_FA || pairTile.getRank() == Tile.ID_BAI);

                if (canFormExactlyNSets(remaining, requiredSets, alreadyHasPengGang || isDragonPair)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canFormExactlyNSets(List<Tile> tiles, int n, boolean foundPengGang) {
        if (n == 0)
            return tiles.isEmpty() && foundPengGang;
        if (tiles.isEmpty())
            return n == 0 && foundPengGang;

        Tile first = tiles.get(0);

        // 1. Try Triplet (counts as Peng/Gang)
        int count = 0;
        for (Tile t : tiles)
            if (t.equals(first))
                count++;
        if (count >= 3) {
            List<Tile> next = new ArrayList<>(tiles);
            next.remove(first);
            next.remove(first);
            next.remove(first);
            if (canFormExactlyNSets(next, n - 1, true))
                return true;
        }

        // 2. Try Sequence
        if (first.isNumber()) {
            Tile t2 = findAlso(tiles, first.getSuit(), first.getRank() + 1);
            Tile t3 = findAlso(tiles, first.getSuit(), first.getRank() + 2);
            if (t2 != null && t3 != null) {
                List<Tile> next = new ArrayList<>(tiles);
                next.remove(first);
                next.remove(t2);
                next.remove(t3);
                if (canFormExactlyNSets(next, n - 1, foundPengGang))
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
        return getAnGangTile(hand) != null;
    }

    public static Tile getAnGangTile(List<Tile> hand) {
        // Check for ANY 4-of-a-kind
        for (Tile t : hand) {
            int count = 0;
            for (Tile check : hand)
                if (check.equals(t))
                    count++;
            if (count == 4)
                return t;
        }
        return null;
    }

    public static boolean isTenpai(List<Tile> hand, List<Meld> melds) {
        int logicalTotal = hand.size() + (melds.size() * 3);
        if (logicalTotal == 13) {
            // Try adding every possible tile to see if it results in a Hu
            for (Tile.Suit s : Tile.Suit.values()) {
                int maxRank = (s == Tile.Suit.ZI) ? 7 : 9;
                for (int r = 1; r <= maxRank; r++) {
                    List<Tile> testHand = new ArrayList<>(hand);
                    testHand.add(new Tile(s, r));
                    if (isHu(testHand, melds))
                        return true;
                }
            }
        } else if (logicalTotal == 14) {
            // Check if discarding any tile leaves us in Tenpai (waiting for 1)
            for (Tile t : hand) {
                List<Tile> subHand = new ArrayList<>(hand);
                subHand.remove(t);
                if (isTenpai(subHand, melds))
                    return true;
            }
        }
        return false;
    }

    public static List<Tile> getTenpaiDiscards(List<Tile> hand, List<Meld> melds) {
        List<Tile> discards = new ArrayList<>();
        int logicalTotal = hand.size() + (melds.size() * 3);
        if (logicalTotal != 14)
            return discards;

        for (Tile t : hand) {
            List<Tile> subHand = new ArrayList<>(hand);
            subHand.remove(t);
            if (isTenpai(subHand, melds)) {
                boolean alreadyAdded = false;
                for (Tile d : discards)
                    if (d.equals(t))
                        alreadyAdded = true;
                if (!alreadyAdded)
                    discards.add(t);
            }
        }
        return discards;
    }

    public static List<Tile> getOuts(List<Tile> hand, List<Meld> melds) {
        List<Tile> outs = new ArrayList<>();
        if (melds.size() * 3 + hand.size() != 13)
            return outs;

        for (Tile.Suit s : Tile.Suit.values()) {
            int maxRank = (s == Tile.Suit.ZI) ? 7 : 9;
            for (int r = 1; r <= maxRank; r++) {
                Tile candidate = new Tile(s, r);
                List<Tile> testHand = new ArrayList<>(hand);
                testHand.add(candidate);
                if (isHu(testHand, melds)) {
                    outs.add(candidate);
                }
            }
        }
        return outs;
    }

    public static boolean wouldGangAffectWait(List<Tile> hand, List<Meld> melds, Tile gangTile, boolean isAnGang) {
        List<Tile> originalOuts = getOuts(hand, melds);
        if (originalOuts.isEmpty())
            return false; // Not in Tenpai or doesn't have valid outs, allow Gang normally?
                          // (Usually locked means you ARE in Tenpai)

        // Simulate Gang
        List<Tile> testHand = new ArrayList<>(hand);
        List<Meld> testMelds = new ArrayList<>(melds);

        List<Tile> mTiles = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            mTiles.add(gangTile);

        if (isAnGang) {
            // Remove 4 from hand
            for (int i = 0; i < 4; i++)
                testHand.remove(gangTile);
        } else {
            // Remove 3 from hand (1 comes from discard)
            for (int i = 0; i < 3; i++)
                testHand.remove(gangTile);
        }
        testMelds.add(new Meld(isAnGang ? Meld.Type.AN_GANG : Meld.Type.MING_GANG, mTiles, -1));

        List<Tile> newOuts = getOuts(testHand, testMelds);

        // Compare sets: Gang is allowed if wait set remains unchanged
        if (originalOuts.size() != newOuts.size())
            return true;
        for (Tile out : originalOuts) {
            boolean found = false;
            for (Tile n : newOuts) {
                if (n.equals(out)) {
                    found = true;
                    break;
                }
            }
            if (!found)
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
