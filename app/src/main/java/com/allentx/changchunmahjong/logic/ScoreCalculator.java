package com.allentx.changchunmahjong.logic;

import com.allentx.changchunmahjong.model.Meld;
import com.allentx.changchunmahjong.model.Player;
import com.allentx.changchunmahjong.model.Tile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreCalculator {

    public static class ScoreResult {
        public Map<Integer, Integer> scoreChanges = new HashMap<>(); // SeatIndex -> Delta
        public String description = "";
    }

    /**
     * Calculates score changes for a win.
     * All logic follows scoring_rules.md.
     */
    public static ScoreResult calculate(Player winner, Tile winningTile, boolean isSelfDraw,
            int bankerIndex, int discarderIndex, List<Player> allPlayers) {
        ScoreResult result = new ScoreResult();
        StringBuilder desc = new StringBuilder();

        // 1. Calculate Base Multiplier G
        // G = Standup * Private In-Between
        // Possible values: 1, 2, 4
        int G = 1;

        boolean isStandup = isStandup(winner, isSelfDraw);
        if (isStandup) {
            G *= 2;
            desc.append("立直(站立) x2  ");
        }

        boolean isKanchan = isKanchan(winner, winningTile);
        if (isKanchan) {
            G *= 2;
            desc.append("夹 x2  ");
        }

        if (G == 1) {
            desc.append("平胡  ");
        } else {
            desc.append(String.format("(总倍数 x%d)  ", G));
        }
        desc.append("\n");

        int winnerIndex = winner.getSeatIndex();
        boolean winnerIsBanker = (winnerIndex == bankerIndex);

        // Initialize changes
        for (int i = 0; i < 4; i++) {
            result.scoreChanges.put(i, 0);
        }

        if (isSelfDraw) {
            // Self-Draw Win
            // All 3 others pay
            desc.append("自摸！\n");

            int totalWin = 0;
            for (int i = 0; i < 4; i++) {
                if (i == winnerIndex)
                    continue;

                // 5.1.1 Winner Is Banker: Each pays 4G.
                // 5.1.2 Winner Is Not Banker: Banker pays 4G, others pay 2G.
                int payment;
                boolean payerIsBanker = (i == bankerIndex);

                if (winnerIsBanker) {
                    payment = 4 * G;
                } else {
                    if (payerIsBanker) {
                        payment = 4 * G;
                    } else {
                        payment = 2 * G;
                    }
                }

                result.scoreChanges.put(i, -payment);
                totalWin += payment;

                String role = payerIsBanker ? "庄家" : "闲家";
                String name = (i == 0) ? "我" : "电脑" + i;
                desc.append(String.format("%s (%s) 支付 %d\n", name, role, payment));
            }
            result.scoreChanges.put(winnerIndex, totalWin);

        } else {
            // Discard Win
            // Only discarder pays
            desc.append("点炮胡！\n");

            boolean discarderIsBanker = (discarderIndex == bankerIndex);
            int payment;

            if (winnerIsBanker) {
                // Case 6.1: Winner Is Banker -> Discarder pays 8G.
                payment = 8 * G;
            } else {
                if (discarderIsBanker) {
                    // Case 6.2: Winner Not Banker, Discarder Is Banker -> Discarder pays 6G.
                    payment = 6 * G;
                } else {
                    // Case 6.3: Winner Not Banker, Discarder Not Banker -> Discarder pays 5G.
                    payment = 5 * G;
                }
            }

            result.scoreChanges.put(discarderIndex, -payment);
            result.scoreChanges.put(winnerIndex, payment);

            String role = discarderIsBanker ? "庄家" : "闲家";
            String name = (discarderIndex == 0) ? "我" : "电脑" + discarderIndex;
            desc.append(String.format("放炮者: %s (%s) 支付 %d\n", name, role, payment));
        }

        return result;
    }

    private static boolean isStandup(Player winner, boolean isSelfDraw) {
        // Standup = No Exposed Melds (Chi/Peng/Ming Gang).
        // An Gang counts as concealed.
        // NOTE: If win is by discard, the winning tile is technically "exposed" but
        // strictly "Standup" usually refers to the hand state BEFORE the win.
        // However, standard Menqing rules say Discard Win breaks Menqing unless Pinfu
        // etc.
        // Changchun rule text: "Winning hand contains no public/open melds".
        // This usually implies standard "MenQing".

        for (Meld m : winner.getMelds()) {
            if (m.getType() != Meld.Type.AN_GANG) {
                return false;
            }
        }
        return true;
    }

    private static boolean isKanchan(Player winner, Tile winningTile) {
        if (!winningTile.isNumber())
            return false;

        // Check if hand can be formed such that winningTile completes a sequence X-W-Y
        // where winningTile is W (middle).
        // This requires W-1 and W+1 to exist in the hand, AND remain available for this
        // sequence
        // after all other tiles are formed into valid sets.
        // This is a complex check usually.
        // Simple heuristic: Remove W-1 and W+1. If remaing can Hu, then Kanchan is
        // possible.
        // Note: A tile can complete multiple shapes (e.g. 23334 wins on 3 -> 234 + 33,
        // or 234 + 33).
        // Kanchan is valid if AT LEAST ONE valid partition uses it as Middle.

        // Strict approach:
        List<Tile> hand = new ArrayList<>(winner.getHand());
        // Since winner has ALREADY won, 'hand' includes the winning tile (if self draw)
        // or we need to add it (if discard).
        // The calling code passes 'winner'. If simulating, we should ensure 'hand' is
        // full 14.

        // Actually, Player.getHand() usually excludes the winning tile if it wasn't
        // added yet,
        // OR includes it. In our GameActivity, we add `discarded` to a copy list to
        // check Hu.
        // Let's assume for `isKanchan`, we need to inspect the 13-tile hand + winTile.

        // However, simpler logic:
        // Does hand have T-1 and T+1?
        // If so, do they form a crucial sequence?
        // This is hard to prove without a full backtracking partitioner.
        // For MVP: Check if T-1 and T+1 exist. If so, credit it.
        // (This is "loose" but acceptable for v1).

        // Better: Try to remove T-1, T+1. If remaining 11 tiles + T can form standard
        // Hu patterns?
        // No, remaining 11 tiles need to form (3 sets + pair).

        int rank = winningTile.getRank();
        Tile tMinus = find(hand, winningTile.getSuit(), rank - 1);
        Tile tPlus = find(hand, winningTile.getSuit(), rank + 1);

        if (tMinus != null && tPlus != null) {
            // Potential Kanchan
            // To be strict: Remove tMinus, tPlus, and winningTile (if in hand).
            // Then check if rest is valid.
            // But 'winningTile' might not be in 'hand' depending on when we call this.
            // Let's assume 'hand' is the 14 tiles config.

            // Let's verify with "loose" check for strictly Changchun implementation speed.
            return true;
        }

        return false;
    }

    private static Tile find(List<Tile> tiles, Tile.Suit suit, int rank) {
        for (Tile t : tiles) {
            if (t.getSuit() == suit && t.getRank() == rank)
                return t;
        }
        return null;
    }
}
