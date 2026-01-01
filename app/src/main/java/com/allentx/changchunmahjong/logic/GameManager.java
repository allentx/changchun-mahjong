package com.allentx.changchunmahjong.logic;

import android.util.Log;
import com.allentx.changchunmahjong.model.Player;
import com.allentx.changchunmahjong.model.Table;
import com.allentx.changchunmahjong.model.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static final String TAG = "GameManager";
    private final Table table;
    private final Random random;

    public GameManager() {
        this.table = new Table();
        this.random = new Random();
    }

    private int currentPlayerIndex;

    // Returns tile drawn, or null if wall empty
    public Tile drawTile() {
        Tile t = table.drawFromWall();
        if (t != null) {
            table.getPlayer(currentPlayerIndex).addTile(t);
        }
        return t;
    }

    public void discardTile(int playerIndex, Tile tile) {
        Player p = table.getPlayer(playerIndex);
        if (p.getHand().contains(tile)) {
            p.removeTile(tile);
            p.addDiscard(tile);
            table.addDiscard(tile); // Add to communal area
        }
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public void advanceTurn() {
        // Turn sequence: 0 (East) -> 1 (North) -> 2 (West) -> 3 (South) -> 0
        currentPlayerIndex = (currentPlayerIndex + 1) % 4;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Table getTable() {
        return table;
    }

    /**
     * Complete Start Game Flow:
     * 1. Shuffle
     * 2. Roll Dice 1 (Determine Wall Owner)
     * 3. Roll Dice 2 (Determine Breach Point)
     * 4. Deal Tiles (13/14)
     */
    public void startGame(int bankerIndex) {
        Log.d(TAG, "Starting Game with Banker: " + bankerIndex);
        table.setBankerIndex(bankerIndex);
        table.startRound(); // Shuffles and resets

        // --- 2. Roll Dice 1 ---
        // Range 2-12
        int dice1 = rollDice();
        Log.d(TAG, "Dice 1: " + dice1);

        // Determine Wall Owner.
        // Count from Banker (East). 1=East, 2=North, 3=West, 4=South, 5=East...
        // Formula: (BankerIndex + (dice1 - 1)) % 4
        int wallOwnerIndex = (table.getBankerIndex() + (dice1 - 1)) % 4;
        Log.d(TAG, "Wall Owner Index: " + wallOwnerIndex);

        // --- 3. Roll Dice 2 ---
        int dice2 = rollDice();
        Log.d(TAG, "Dice 2: " + dice2);

        // Breach Point: Count 'dice2' stacks from right of Wall Owner.
        // In a physical game, this determines WHERE in the wall we start taking.
        // In our logical List<Tile> wall, we can simulate this by rotating the list
        // OR simply by popping from the top, assuming 'shuffle' did the randomization.
        // BUT to be "Authentic" (visually), we might want to track the index.
        // For logic (v1), we will just accept the shuffled list as the "Breach
        // Corrected" list.
        // If we want to simulate the "Cut", we can shift the list.

        // Let's shift the list to simulate the Cut.
        // There are 34 stacks per wall (136 / 4 = 34).
        // Total stacks = 68 (since 2 high). Total tiles = 136.
        // "Right side" depends on orientation.
        // For simplicity: We treat the List<Tile> as a continuous ring starting from
        // East Wall left.
        // We will just Collections.rotate the list based on the dice to strictly follow
        // "Random Cut".

        // Cut amount ~ (wallOwner * 34) + dice2 * 2;
        // This is complex to perfect without visual wall data.
        // For now, SHUFFLE is sufficient randomness.
        // We will proceed to Deal.

        // --- 4. Deal ---
        dealTiles();

        // --- 5. Sort Hands ---
        for (int i = 0; i < 4; i++) {
            table.getPlayer(i).sortHand();
        }

        // Game is now in "Playing" state (Dealer needs to discard)
        Log.d(TAG, "Deal Complete. Banker Hand Size: " + table.getPlayer(table.getBankerIndex()).getHand().size());
    }

    private void dealTiles() {
        // Standard deal: 4 rounds of 4 tiles -> 16 tiles each? No.
        // 3 rounds of 4 tiles = 12 tiles.
        // Then "Jump Deal" (Tiao Pai): Banker takes 2, others take 1.

        List<Tile> wall = table.getWall();
        int banker = table.getBankerIndex();

        // 3 rounds of 4
        for (int r = 0; r < 3; r++) {
            for (int p = 0; p < 4; p++) {
                int seat = (banker + p) % 4; // Follows 0->1->2->3 sequence
                for (int t = 0; t < 4; t++) {
                    table.getPlayer(seat).addTile(wall.remove(0));
                }
            }
        }

        // Tiao Pai (Jump Deal)
        // Banker takes 1st and 3rd remaining (conceptually)
        // Others take 1.
        // Order: Banker takes 1, Next takes 1... NO.
        // "Banker takes 2, Others 1".
        // Usually: Banker takes top of stack 1 and 3.
        // Then next player takes top of stack.
        // Simplified Logic:
        // Banker draws 1.
        // Every player draws 1.
        // Banker now has 12 + 1 + 1 = 14.
        // Others have 12 + 1 = 13.

        // Round 4: Everyone takes 1
        for (int p = 0; p < 4; p++) {
            int seat = (banker + p) % 4;
            table.getPlayer(seat).addTile(wall.remove(0));
        }

        // Round 5: Banker takes one more
        table.getPlayer(banker).addTile(wall.remove(0));

        // Set current turn to Banker
        currentPlayerIndex = banker;
    }

    private int rollDice() {
        // 2 dice, 1-6 each
        return (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
    }
}
