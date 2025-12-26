package com.allentx.changchunmahjong;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.allentx.changchunmahjong.databinding.ActivityGameBinding;
import com.allentx.changchunmahjong.logic.GameManager;
import com.allentx.changchunmahjong.logic.RuleValidatorHelper;
import com.allentx.changchunmahjong.model.Tile;
import java.util.List;
import com.allentx.changchunmahjong.R;

public class GameActivity extends AppCompatActivity {

    private ActivityGameBinding binding;
    private GameManager gameManager;
    private Tile selectedTile;
    private View selectedView;
    private Tile lastDrawnTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gameManager = new GameManager();
        gameManager.startGame();

        hideActions();
        refreshUI();

        binding.btnHu.setOnClickListener(v -> executeHu());
        binding.btnPeng.setOnClickListener(v -> executePeng());
        binding.btnGang.setOnClickListener(v -> executeGang());
        binding.btnChi.setOnClickListener(v -> executeChi());
        binding.btnPass.setOnClickListener(v -> executePass());
    }

    private void hideActions() {
        binding.layoutActions.setVisibility(View.GONE);
    }

    private void showActions(boolean canChi, boolean canPeng, boolean canGang, boolean canHu) {
        binding.layoutActions.setVisibility(View.VISIBLE);
        binding.btnChi.setVisibility(canChi ? View.VISIBLE : View.GONE);
        binding.btnPeng.setVisibility(canPeng ? View.VISIBLE : View.GONE);
        binding.btnGang.setVisibility(canGang ? View.VISIBLE : View.GONE);
        binding.btnHu.setVisibility(canHu ? View.VISIBLE : View.GONE);
    }

    private int lastDiscardFromPlayer = -1;
    private Tile interruptedTile = null;

    private void refreshUI() {
        // 1. Secret Hand
        binding.layoutHand.removeAllViews();
        List<Tile> fullHand = gameManager.getTable().getPlayer(0).getHand();
        List<Tile> sortedHand = new java.util.ArrayList<>(fullHand);

        if (lastDrawnTile != null && sortedHand.contains(lastDrawnTile)) {
            sortedHand.remove(lastDrawnTile);
            java.util.Collections.sort(sortedHand);

            for (Tile tile : sortedHand) {
                addTileToLayout(tile, false);
            }

            // Add Spacer
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(24, 1));
            binding.layoutHand.addView(spacer);

            // Add last drawn
            addTileToLayout(lastDrawnTile, true);
        } else {
            java.util.Collections.sort(sortedHand);
            for (Tile tile : sortedHand) {
                addTileToLayout(tile, false);
            }
        }

        // 2. Exposed Melds
        refreshMelds();

        // 3. Discards
        refreshDiscards();

        int turnOwner = gameManager.getCurrentPlayerIndex();
        String turn = (turnOwner == 0) ? getString(R.string.turn_you) : getString(R.string.turn_ai);
        String info = turn + " | "
                + String.format(getString(R.string.wall_count), gameManager.getTable().getWall().size());
        binding.tvGameInfo.setText(info);
    }

    private void addTileToLayout(Tile tile, boolean highlight) {
        View tileView = createTileView(tile, highlight);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(160, 220);
        params.setMargins(6, 0, 6, 0);
        tileView.setLayoutParams(params);
        tileView.setOnClickListener(v -> onTileClicked(tile, tileView));
        binding.layoutHand.addView(tileView);
    }

    private void refreshMelds() {
        refreshMeldArea(binding.layoutExposed, 0);
        refreshMeldArea(binding.layoutAi1Melds, 1);
        refreshMeldArea(binding.layoutAi2Melds, 2);
        refreshMeldArea(binding.layoutAi3Melds, 3);
    }

    private void refreshMeldArea(com.google.android.flexbox.FlexboxLayout layout, int playerIndex) {
        layout.removeAllViews();
        List<com.allentx.changchunmahjong.model.Meld> melds = gameManager.getTable().getPlayer(playerIndex).getMelds();
        for (com.allentx.changchunmahjong.model.Meld meld : melds) {
            LinearLayout meldGroup = new LinearLayout(this);
            // AI 1 and 3 are vertical
            meldGroup.setOrientation(
                    (playerIndex == 1 || playerIndex == 3) ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            meldGroup.setPadding(4, 4, 4, 4);

            List<Tile> tiles = new java.util.ArrayList<>(meld.getTiles());
            java.util.Collections.sort(tiles); // Standardize display order (e.g. 1-2-3)

            for (Tile t : tiles) {
                View tileView = createTileView(t, false);
                // Human: 120x160, AI & Discards: 80x110
                int size = (playerIndex == 0) ? 120 : 80;
                int height = (playerIndex == 0) ? 160 : 110;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, height);
                params.setMargins(2, 2, 2, 2);
                tileView.setLayoutParams(params);
                meldGroup.addView(tileView);
            }
            layout.addView(meldGroup);
        }
    }

    private void refreshDiscards() {
        binding.flexDiscards.removeAllViews();
        List<Tile> discards = new java.util.ArrayList<>(gameManager.getTable().getDiscards());
        java.util.Collections.reverse(discards); // Newest first (at the top)

        for (Tile t : discards) {
            View discardView = createTileView(t, false);
            com.google.android.flexbox.FlexboxLayout.LayoutParams params = new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                    80, 110);
            params.setMargins(2, 2, 2, 2);
            discardView.setLayoutParams(params);
            binding.flexDiscards.addView(discardView);
        }

        // Auto-scroll to top to see newest
        binding.scrollDiscards.post(() -> binding.scrollDiscards.fullScroll(View.FOCUS_UP));
    }

    // Composite View Creator
    private View createTileView(Tile t, boolean highlight) {
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        container.setBackgroundResource(R.drawable.tile_bg);

        if (highlight) {
            // Very explicit highlight: Red border overlay
            View border = new View(this);
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setStroke(8, Color.RED);
            border.setBackground(gd);
            container.addView(border, new android.widget.FrameLayout.LayoutParams(-1, -1));
        }

        // Image Rendering
        android.widget.ImageView iv = new android.widget.ImageView(this);
        android.graphics.Bitmap bmp = getTileBitmap(t);
        if (bmp != null) {
            iv.setImageBitmap(bmp);
            iv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        }
        container.addView(iv, new android.widget.FrameLayout.LayoutParams(-1, -1));

        return container;
    }

    private android.graphics.Bitmap getTileBitmap(Tile t) {
        if (t == null)
            return null;
        int index = t.getRank() - 1;
        com.allentx.changchunmahjong.util.AssetManager am = com.allentx.changchunmahjong.util.AssetManager
                .getInstance();

        switch (t.getSuit()) {
            case WAN:
                return am.getWan(index);
            case TIAO:
                return am.getTiao(index);
            case TONG:
                return am.getTong(index);
            case ZI:
                return am.getZi(index);
        }
        return null;
    }

    private void onTileClicked(Tile tile, View view) {
        if (gameManager.getCurrentPlayerIndex() != 0) {
            Toast.makeText(this, R.string.not_your_turn, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTile == tile && selectedView == view) {
            // Double click -> Discard
            gameManager.discardTile(0, tile);
            selectedTile = null;
            selectedView = null;
            lastDrawnTile = null; // Clear highlight after discard

            gameManager.advanceTurn();
            refreshUI();

            // Start AI sequence
            new android.os.Handler().postDelayed(() -> simulateAiTurn(1), 1000);
        } else {
            // Select
            if (selectedView != null)
                selectedView.setAlpha(1.0f);
            selectedTile = tile;
            selectedView = view;
            view.setAlpha(0.6f);
        }
    }

    private void simulateAiTurn(final int playerIndex) {
        if (gameManager.getTable().getWall().isEmpty()) {
            showGameOverDialog("流局", "牌墙已空，本局结束。");
            return;
        }

        final String[] names = getResources().getStringArray(R.array.player_names);

        Tile drawn = gameManager.drawTile();
        if (drawn != null) {
            if (RuleValidatorHelper.isHu(gameManager.getTable().getPlayer(playerIndex).getHand())) {
                showGameOverDialog("胡了！", names[playerIndex] + " 自摸胡了！");
                return;
            }

            gameManager.discardTile(playerIndex, drawn);
            String msg = String.format(getString(R.string.ai_discard), names[playerIndex], drawn.getChineseName());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            refreshUI();

            new android.os.Handler().postDelayed(() -> {
                // Check if human player 0 can interrupt
                if (checkPlayerInterruption(drawn, playerIndex)) {
                    return;
                }

                // AI Hu Check: Can any other AI win from this discard?
                for (int i = 1; i <= 3; i++) {
                    if (i == playerIndex)
                        continue;
                    List<Tile> aiHand = new java.util.ArrayList<>(gameManager.getTable().getPlayer(i).getHand());
                    aiHand.add(drawn);
                    if (RuleValidatorHelper.isHu(aiHand)) {
                        showGameOverDialog("胡了！",
                                names[i] + " 胡了 " + names[playerIndex] + " 的一张 " + drawn.getChineseName() + "！");
                        return;
                    }
                }

                // Check if other AI can Peng/Gang
                for (int i = 1; i <= 3; i++) {
                    if (i == playerIndex)
                        continue;
                    List<Tile> aiHand = gameManager.getTable().getPlayer(i).getHand();
                    if (RuleValidatorHelper.canMingGang(aiHand, drawn)) {
                        performAiMeld(i, drawn, playerIndex, com.allentx.changchunmahjong.model.Meld.Type.MING_GANG);
                        return;
                    }
                    if (RuleValidatorHelper.canPeng(aiHand, drawn)) {
                        performAiMeld(i, drawn, playerIndex, com.allentx.changchunmahjong.model.Meld.Type.PENG);
                        return;
                    }
                }

                // AI Chi Check
                int nextIndex = (playerIndex + 1) % 4;
                if (nextIndex != 0) { // If next is AI
                    List<Tile> nextHand = gameManager.getTable().getPlayer(nextIndex).getHand();
                    if (RuleValidatorHelper.canChi(nextHand, drawn)) {
                        performAiMeld(nextIndex, drawn, playerIndex, com.allentx.changchunmahjong.model.Meld.Type.CHI);
                        return;
                    }
                }

                // No interruption, continue loop
                gameManager.advanceTurn();
                refreshUI();

                if (gameManager.getCurrentPlayerIndex() != 0) {
                    new android.os.Handler().postDelayed(() -> simulateAiTurn(gameManager.getCurrentPlayerIndex()),
                            1000);
                } else {
                    drawForPlayer();
                }
            }, 500);
            return;
        }
        gameManager.advanceTurn();
        refreshUI();
    }

    private void performAiMeld(int aiIndex, Tile tile, int fromPlayer,
            com.allentx.changchunmahjong.model.Meld.Type type) {
        gameManager.getTable().getDiscards().remove(tile);
        List<Tile> meldList = new java.util.ArrayList<>();

        if (type == com.allentx.changchunmahjong.model.Meld.Type.CHI) {
            meldList.add(tile);
            List<Tile> hand = gameManager.getTable().getPlayer(aiIndex).getHand();
            if (has(hand, tile.getSuit(), tile.getRank() - 1) && has(hand, tile.getSuit(), tile.getRank() - 2)) {
                meldList.add(removeAndGet(hand, tile.getSuit(), tile.getRank() - 1));
                meldList.add(removeAndGet(hand, tile.getSuit(), tile.getRank() - 2));
            } else if (has(hand, tile.getSuit(), tile.getRank() - 1) && has(hand, tile.getSuit(), tile.getRank() + 1)) {
                meldList.add(removeAndGet(hand, tile.getSuit(), tile.getRank() - 1));
                meldList.add(removeAndGet(hand, tile.getSuit(), tile.getRank() + 1));
            } else {
                meldList.add(removeAndGet(hand, tile.getSuit(), tile.getRank() + 1));
                meldList.add(removeAndGet(hand, tile.getSuit(), tile.getRank() + 2));
            }
        } else {
            int count = (type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG) ? 4 : 3;
            for (int i = 0; i < count; i++)
                meldList.add(tile);
            for (int i = 0; i < count - 1; i++) {
                gameManager.getTable().getPlayer(aiIndex).removeTile(tile);
            }
        }

        com.allentx.changchunmahjong.model.Meld meld = new com.allentx.changchunmahjong.model.Meld(type, meldList,
                fromPlayer);
        gameManager.getTable().getPlayer(aiIndex).addMeld(meld);

        String[] names = getResources().getStringArray(R.array.player_names);
        String actionName = "鸣";
        if (type == com.allentx.changchunmahjong.model.Meld.Type.PENG)
            actionName = "碰";
        else if (type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG)
            actionName = "杠";
        else if (type == com.allentx.changchunmahjong.model.Meld.Type.CHI)
            actionName = "吃";

        Toast.makeText(this, names[aiIndex] + " " + actionName + "！", Toast.LENGTH_SHORT).show();

        gameManager.setCurrentPlayerIndex(aiIndex);

        // If Gang, AI must draw ANOTHER tile first
        if (type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG) {
            gameManager.drawTile(); // Add to hand
        }

        refreshUI();

        // AI must discard after meld
        new android.os.Handler().postDelayed(() -> {
            List<Tile> hand = gameManager.getTable().getPlayer(aiIndex).getHand();
            if (!hand.isEmpty()) {
                Tile toDiscard = hand.get(0);
                gameManager.discardTile(aiIndex, toDiscard);
                Toast.makeText(this, names[aiIndex] + " 打出 " + toDiscard.getChineseName(), Toast.LENGTH_SHORT).show();

                refreshUI();
                new android.os.Handler().postDelayed(() -> {
                    if (!checkPlayerInterruption(toDiscard, aiIndex)) {
                        gameManager.advanceTurn();
                        refreshUI();
                        if (gameManager.getCurrentPlayerIndex() != 0) {
                            simulateAiTurn(gameManager.getCurrentPlayerIndex());
                        } else {
                            drawForPlayer();
                        }
                    }
                }, 500);
            }
        }, 1000);
    }

    private boolean checkPlayerInterruption(Tile discarded, int fromPlayer) {
        lastDiscardFromPlayer = fromPlayer;
        interruptedTile = discarded;
        List<Tile> hand = new java.util.ArrayList<>(gameManager.getTable().getPlayer(0).getHand());

        // Check Hu first (Win from discard)
        hand.add(discarded);
        boolean canHu = RuleValidatorHelper.isHu(hand);

        hand.remove(discarded);
        boolean canPeng = RuleValidatorHelper.canPeng(hand, discarded);
        boolean canGang = RuleValidatorHelper.canMingGang(hand, discarded);
        // Chi only possible if from the player to the left (player 3 in our clockwise
        // sequence 0->1->2->3)
        boolean canChi = (fromPlayer == 3) && RuleValidatorHelper.canChi(hand, discarded);

        if (canChi || canPeng || canGang || canHu) {
            showActions(canChi, canPeng, canGang, canHu);
            return true;
        }
        return false;
    }

    private void drawForPlayer() {
        Tile playerDrawn = gameManager.drawTile();
        lastDrawnTile = playerDrawn;
        if (playerDrawn != null) {
            String msg = String.format(getString(R.string.you_drew_tile), playerDrawn.getChineseName());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            // Check self-draw Hu
            if (RuleValidatorHelper.isHu(gameManager.getTable().getPlayer(0).getHand())) {
                showActions(false, false, false, true);
            }
        }
        refreshUI();
    }

    private void executeChi() {
        List<Tile> discards = gameManager.getTable().getDiscards();
        if (discards.isEmpty())
            return;
        Tile t = discards.remove(discards.size() - 1);

        List<Tile> hand = gameManager.getTable().getPlayer(0).getHand();
        List<Tile> meldList = new java.util.ArrayList<>();
        meldList.add(t);

        // Find which neighbors to remove
        if (has(hand, t.getSuit(), t.getRank() - 1) && has(hand, t.getSuit(), t.getRank() - 2)) {
            meldList.add(removeAndGet(hand, t.getSuit(), t.getRank() - 1));
            meldList.add(removeAndGet(hand, t.getSuit(), t.getRank() - 2));
        } else if (has(hand, t.getSuit(), t.getRank() - 1) && has(hand, t.getSuit(), t.getRank() + 1)) {
            meldList.add(removeAndGet(hand, t.getSuit(), t.getRank() - 1));
            meldList.add(removeAndGet(hand, t.getSuit(), t.getRank() + 1));
        } else { // (has(hand, t.getSuit(), t.getRank() + 1) && has(hand, t.getSuit(),
                 // t.getRank() + 2))
            meldList.add(removeAndGet(hand, t.getSuit(), t.getRank() + 1));
            meldList.add(removeAndGet(hand, t.getSuit(), t.getRank() + 2));
        }

        com.allentx.changchunmahjong.model.Meld meld = new com.allentx.changchunmahjong.model.Meld(
                com.allentx.changchunmahjong.model.Meld.Type.CHI, meldList, lastDiscardFromPlayer);
        gameManager.getTable().getPlayer(0).addMeld(meld);

        gameManager.setCurrentPlayerIndex(0);
        hideActions();
        refreshUI();
        Toast.makeText(this, "吃！请打出一张牌。", Toast.LENGTH_SHORT).show();
    }

    private void executePeng() {
        List<Tile> discards = gameManager.getTable().getDiscards();
        if (discards.isEmpty())
            return;
        Tile t = discards.remove(discards.size() - 1);

        List<Tile> meldList = new java.util.ArrayList<>();
        meldList.add(t);
        meldList.add(t);
        meldList.add(t);

        gameManager.getTable().getPlayer(0).removeTile(t);
        gameManager.getTable().getPlayer(0).removeTile(t);

        com.allentx.changchunmahjong.model.Meld meld = new com.allentx.changchunmahjong.model.Meld(
                com.allentx.changchunmahjong.model.Meld.Type.PENG, meldList, lastDiscardFromPlayer);
        gameManager.getTable().getPlayer(0).addMeld(meld);

        gameManager.setCurrentPlayerIndex(0);
        hideActions();
        refreshUI();
        Toast.makeText(this, "碰！请打出一张牌。", Toast.LENGTH_SHORT).show();
    }

    private void executeGang() {
        List<Tile> discards = gameManager.getTable().getDiscards();
        if (discards.isEmpty())
            return;
        Tile t = discards.remove(discards.size() - 1);

        List<Tile> meldList = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++)
            meldList.add(t);

        for (int i = 0; i < 3; i++)
            gameManager.getTable().getPlayer(0).removeTile(t);

        com.allentx.changchunmahjong.model.Meld meld = new com.allentx.changchunmahjong.model.Meld(
                com.allentx.changchunmahjong.model.Meld.Type.MING_GANG, meldList, lastDiscardFromPlayer);
        gameManager.getTable().getPlayer(0).addMeld(meld);

        gameManager.setCurrentPlayerIndex(0);
        hideActions();
        refreshUI();
        Toast.makeText(this, "杠！请补牌。", Toast.LENGTH_SHORT).show();

        // DRAW REPLACEMENT TILE
        new android.os.Handler().postDelayed(this::drawForPlayer, 1000);
    }

    private void executePass() {
        hideActions();
        // Resume AI cycle
        gameManager.advanceTurn();
        refreshUI();

        if (lastDiscardFromPlayer < 3) {
            new android.os.Handler().postDelayed(() -> simulateAiTurn(lastDiscardFromPlayer + 1), 1000);
        } else {
            drawForPlayer();
        }
    }

    private boolean has(List<Tile> hand, Tile.Suit suit, int rank) {
        for (Tile t : hand)
            if (t.getSuit() == suit && t.getRank() == rank)
                return true;
        return false;
    }

    private Tile removeAndGet(List<Tile> hand, Tile.Suit suit, int rank) {
        for (int i = 0; i < hand.size(); i++) {
            Tile t = hand.get(i);
            if (t.getSuit() == suit && t.getRank() == rank) {
                return hand.remove(i);
            }
        }
        return null;
    }

    private void executeHu() {
        hideActions();
        showGameOverDialog("🎉 你赢了！ 🎉", "恭喜你胡牌了！");
    }

    private void showGameOverDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("再来一局", (dialog, which) -> {
                    gameManager = new GameManager();
                    gameManager.startGame();
                    lastDrawnTile = null;
                    hideActions();
                    refreshUI();
                })
                .setNegativeButton("退出", (dialog, which) -> finish())
                .show();
    }

    private void checkHu() {
        boolean hu = RuleValidatorHelper.isHu(gameManager.getTable().getPlayer(0).getHand());
        Toast.makeText(this, hu ? "胡了！你赢了！" : getString(R.string.not_hu_yet), Toast.LENGTH_SHORT).show();
    }
}
