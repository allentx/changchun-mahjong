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
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexWrap;
import android.view.ViewGroup;
import java.util.ArrayList;
import com.allentx.changchunmahjong.model.Player;
import com.allentx.changchunmahjong.logic.SmartAiStrategy;

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

        // Check if Banker (Human) has Hu or An Gang at start
        List<Tile> hand = gameManager.getTable().getPlayer(0).getHand();
        if (!hand.isEmpty()) {
            lastDrawnTile = hand.get(hand.size() - 1);
        }
        boolean canHu = RuleValidatorHelper.isHu(hand, gameManager.getTable().getPlayer(0).getMelds());
        boolean canAnGang = RuleValidatorHelper.canAnGang(hand);
        if (canHu || canAnGang) {
            showActions(false, false, canAnGang, canHu);
        }

        refreshUI();

        binding.btnHu.setOnClickListener(v -> executeHu());
        binding.btnPeng.setOnClickListener(v -> executePeng());
        binding.btnGang.setOnClickListener(v -> executeGang());
        binding.btnChi.setOnClickListener(v -> executeChi());
        binding.btnPass.setOnClickListener(v -> executePass());

        soundManager = com.allentx.changchunmahjong.util.SoundManager.getInstance(this);
    }

    private com.allentx.changchunmahjong.util.SoundManager soundManager;

    private boolean isSoundEnabled() {
        android.content.SharedPreferences prefs = getSharedPreferences("mahjong_prefs", MODE_PRIVATE);
        return prefs.getBoolean("sound_enabled", true);
    }

    private void announceVoice(String text) {
        if (isSoundEnabled()) {
            soundManager.announce(text);
        }
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

            // Add Spacer: Reduced from 24 to 12
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(12, 1));
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
        String wallCount = String.format(getString(R.string.wall_count), gameManager.getTable().getWall().size());

        binding.tvStatusLeft.setText(turn + " | " + wallCount);
    }

    private void addTileToLayout(Tile tile, boolean highlight) {
        View tileView = createTileView(tile, highlight, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(96, 132);
        params.setMargins(1, 0, 1, 0); // Reduced from 4 to 1
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
            meldGroup.setOrientation(LinearLayout.HORIZONTAL);
            meldGroup.setPadding(4, 4, 4, 4);

            List<Tile> tiles = new java.util.ArrayList<>(meld.getTiles());
            java.util.Collections.sort(tiles);

            for (Tile t : tiles) {
                View tileView = createTileView(t, false, 0);
                // Human: 72x96 (was 120x160), AI & Discards: 80x110
                int size = (playerIndex == 0) ? 72 : 80;
                int height = (playerIndex == 0) ? 96 : 110;
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
        List<Tile> allDiscards = gameManager.getTable().getDiscards();

        // Group by type and track counts
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (Tile t : allDiscards) {
            String key = t.getSuit().name() + "_" + t.getRank();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        // Create ordered list of unique tiles (newest last appearance first)
        java.util.List<Tile> uniqueTilesOrdered = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = allDiscards.size() - 1; i >= 0; i--) {
            Tile t = allDiscards.get(i);
            String key = t.getSuit().name() + "_" + t.getRank();
            if (!seen.contains(key)) {
                uniqueTilesOrdered.add(t);
                seen.add(key);
            }
        }

        for (Tile t : uniqueTilesOrdered) {
            String key = t.getSuit().name() + "_" + t.getRank();
            int count = counts.get(key);

            View discardView = createTileView(t, false, count);
            com.google.android.flexbox.FlexboxLayout.LayoutParams params = new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                    80, 110);
            params.setMargins(4, 4, 4, 4);
            discardView.setLayoutParams(params);
            binding.flexDiscards.addView(discardView);
        }

        // Auto-scroll to top to see newest
        binding.scrollDiscards.post(() -> binding.scrollDiscards.fullScroll(View.FOCUS_UP));
    }

    private void showCenteredToast(String message) {
        binding.tvStatusRight.setText(message);
        binding.tvStatusRight.setAlpha(1.0f);
        binding.tvStatusRight.animate()
                .alpha(0.3f)
                .setDuration(5000)
                .start();
    }

    // Composite View Creator
    private View createTileView(Tile t, boolean highlight, int count) {
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

        // Count Badge
        if (count > 1) {
            TextView tvCount = new TextView(this);
            tvCount.setText("x" + count);
            tvCount.setTextColor(Color.RED);
            tvCount.setShadowLayer(4, 1, 1, Color.WHITE);
            tvCount.setTextSize(14f); // Changed from 14sp to 14f for float literal
            tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
            android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.END;
            lp.setMargins(0, 0, 4, 4);
            container.addView(tvCount, lp);
        }

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
            showCenteredToast(getString(R.string.not_your_turn));
            return;
        }

        if (selectedTile == tile && selectedView == view) {
            // Double click -> Discard
            gameManager.discardTile(0, tile);
            selectedTile = null;
            selectedView = null;
            lastDrawnTile = null; // Clear highlight after discard

            // Start interruption check sequence instead of direct turn advancement
            if (!checkAllInterruptions(tile, 0, false)) {
                gameManager.advanceTurn();
                refreshUI();
                new android.os.Handler().postDelayed(() -> simulateAiTurn(gameManager.getCurrentPlayerIndex()), 1000);
            }
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
        if (gameManager.getCurrentPlayerIndex() != playerIndex) {
            // Turn was stolen or shifted (e.g. by a human Peng/Chi/Gang)
            return;
        }
        if (gameManager.getTable().getWall().isEmpty()) {
            showGameOverDialog("流局", "牌墙已空，本局结束。", null, null);
            return;
        }

        final String[] names = getResources().getStringArray(R.array.player_names);

        Tile drawn = gameManager.drawTile();
        if (drawn != null) {
            if (RuleValidatorHelper.isHu(gameManager.getTable().getPlayer(playerIndex).getHand(),
                    gameManager.getTable().getPlayer(playerIndex).getMelds())) {
                announceVoice("胡");
                showGameOverDialog("胡了！", names[playerIndex] + " 自摸胡了！", gameManager.getTable().getPlayer(playerIndex),
                        drawn);
                return;
            }

            // Check AI An Gang
            Tile anGangTile = RuleValidatorHelper
                    .getAnGangTile(gameManager.getTable().getPlayer(playerIndex).getHand());
            if (anGangTile != null && gameManager.getTable().getPlayer(playerIndex).getMelds().size() < 3) {
                performAiMeld(playerIndex, anGangTile, -1, com.allentx.changchunmahjong.model.Meld.Type.AN_GANG);
                return;
            }

            // 1. Collect all "visible" tiles for AI to consider
            List<Tile> allDiscards = gameManager.getTable().getDiscards();
            List<Tile> allMelds = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                for (com.allentx.changchunmahjong.model.Meld m : gameManager.getTable().getPlayer(i).getMelds()) {
                    allMelds.addAll(m.getTiles());
                }
            }

            // 2. Use Smart Strategy to pick best discard
            Player ai = gameManager.getTable().getPlayer(playerIndex);
            Tile recommended = SmartAiStrategy.recommendDiscard(
                    ai.getHand(),
                    allDiscards,
                    ai.getMelds(),
                    allMelds);

            final Tile toDiscard = (recommended != null) ? recommended : drawn;

            gameManager.discardTile(playerIndex, toDiscard);
            String msg = String.format(getString(R.string.ai_discard), names[playerIndex], toDiscard.getChineseName());
            showCenteredToast(msg);
            refreshUI();

            new android.os.Handler().postDelayed(() -> {
                if (checkAllInterruptions(toDiscard, playerIndex, false)) {
                    return;
                }

                // No interruption, continue loop
                gameManager.advanceTurn();
                refreshUI();

                int nextOwner = gameManager.getCurrentPlayerIndex();
                if (nextOwner != 0) {
                    new android.os.Handler().postDelayed(() -> simulateAiTurn(nextOwner), 1000);
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
            // PENG, MING_GANG, or AN_GANG
            int count = (type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG
                    || type == com.allentx.changchunmahjong.model.Meld.Type.AN_GANG) ? 4 : 3;
            for (int i = 0; i < count; i++)
                meldList.add(tile);

            int toRemove = (type == com.allentx.changchunmahjong.model.Meld.Type.AN_GANG) ? 4 : (count - 1);
            for (int i = 0; i < toRemove; i++) {
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
        else if (type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG
                || type == com.allentx.changchunmahjong.model.Meld.Type.AN_GANG)
            actionName = "杠";
        else if (type == com.allentx.changchunmahjong.model.Meld.Type.CHI)
            actionName = "吃";

        showCenteredToast(names[aiIndex] + " " + actionName + "！");
        announceVoice(actionName);

        gameManager.setCurrentPlayerIndex(aiIndex);

        // If Gang, AI must draw ANOTHER tile first
        if (type == com.allentx.changchunmahjong.model.Meld.Type.MING_GANG
                || type == com.allentx.changchunmahjong.model.Meld.Type.AN_GANG) {
            gameManager.drawTile(); // Add to hand
        }

        refreshUI();

        // AI must discard after meld
        new android.os.Handler().postDelayed(() -> {
            Player p = gameManager.getTable().getPlayer(aiIndex);
            List<Tile> hand = p.getHand();
            if (!hand.isEmpty()) {
                // 1. Collect all "visible" tiles
                List<Tile> allDiscards = gameManager.getTable().getDiscards();
                List<Tile> allMeldsTiles = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    for (com.allentx.changchunmahjong.model.Meld m : gameManager.getTable().getPlayer(i).getMelds()) {
                        allMeldsTiles.addAll(m.getTiles());
                    }
                }

                // 2. Use Smart Strategy
                Tile recommended = SmartAiStrategy.recommendDiscard(hand, allDiscards, p.getMelds(), allMeldsTiles);
                final Tile toDiscard = (recommended != null) ? recommended : hand.get(0);

                gameManager.discardTile(aiIndex, toDiscard);
                showCenteredToast(names[aiIndex] + " 打出 " + toDiscard.getChineseName());

                refreshUI();
                new android.os.Handler().postDelayed(() -> {
                    if (!checkAllInterruptions(toDiscard, aiIndex, false)) {
                        gameManager.advanceTurn();
                        refreshUI();
                        int next = gameManager.getCurrentPlayerIndex();
                        if (next != 0) {
                            simulateAiTurn(next);
                        } else {
                            drawForPlayer();
                        }
                    }
                }, 500);
            }
        }, 1000);
    }

    private boolean checkAllInterruptions(Tile discarded, int fromPlayer, boolean skipHuman) {
        lastDiscardFromPlayer = fromPlayer;
        interruptedTile = discarded;
        String[] names = getResources().getStringArray(R.array.player_names);

        // --- Priority LEVEL 1: HU (Win) ---
        for (int i = 1; i <= 3; i++) {
            int t = (fromPlayer + i) % 4;
            if (t == 0) {
                if (skipHuman)
                    continue;
                Player human = gameManager.getTable().getPlayer(0);
                List<Tile> hand = new java.util.ArrayList<>(human.getHand());
                hand.add(discarded);
                if (RuleValidatorHelper.isHu(hand, human.getMelds())) {
                    boolean canChi = (fromPlayer == 3) && RuleValidatorHelper.canChi(human.getHand(), discarded);
                    boolean canPeng = RuleValidatorHelper.canPeng(human.getHand(), discarded);
                    boolean canGang = RuleValidatorHelper.canMingGang(human.getHand(), discarded);
                    showActions(canChi, canPeng, canGang, true);
                    return true;
                }
            } else {
                Player ai = gameManager.getTable().getPlayer(t);
                List<Tile> aiHand = new java.util.ArrayList<>(ai.getHand());
                aiHand.add(discarded);
                if (RuleValidatorHelper.isHu(aiHand, ai.getMelds())) {
                    announceVoice("胡");
                    showGameOverDialog("胡了！",
                            names[t] + " 胡了 " + names[fromPlayer] + " 的一张 " + discarded.getChineseName() + "！",
                            ai, discarded);
                    return true;
                }
            }
        }

        // --- Priority LEVEL 2: PENG / GANG ---
        for (int i = 1; i <= 3; i++) {
            int t = (fromPlayer + i) % 4;
            if (t == 0) {
                if (skipHuman)
                    continue;
                Player human = gameManager.getTable().getPlayer(0);
                if (human.getMelds().size() < 3) {
                    boolean canPeng = RuleValidatorHelper.canPeng(human.getHand(), discarded);
                    boolean canGang = RuleValidatorHelper.canMingGang(human.getHand(), discarded);
                    if (canPeng || canGang) {
                        boolean canChi = (fromPlayer == 3) && RuleValidatorHelper.canChi(human.getHand(), discarded);
                        showActions(canChi, canPeng, canGang, false);
                        return true;
                    }
                }
            } else {
                Player ai = gameManager.getTable().getPlayer(t);
                if (ai.getMelds().size() < 3) {
                    if (RuleValidatorHelper.canMingGang(ai.getHand(), discarded)) {
                        performAiMeld(t, discarded, fromPlayer, com.allentx.changchunmahjong.model.Meld.Type.MING_GANG);
                        return true;
                    }
                    if (RuleValidatorHelper.canPeng(ai.getHand(), discarded)) {
                        performAiMeld(t, discarded, fromPlayer, com.allentx.changchunmahjong.model.Meld.Type.PENG);
                        return true;
                    }
                }
            }
        }

        // --- Priority LEVEL 3: CHI ---
        int nextIndex = (fromPlayer + 1) % 4;
        if (nextIndex == 0) {
            if (!skipHuman) {
                Player human = gameManager.getTable().getPlayer(0);
                if (human.getMelds().size() < 3 && RuleValidatorHelper.canChi(human.getHand(), discarded)) {
                    showActions(true, false, false, false);
                    return true;
                }
            }
        } else {
            Player ai = gameManager.getTable().getPlayer(nextIndex);
            if (ai.getMelds().size() < 3 && RuleValidatorHelper.canChi(ai.getHand(), discarded)) {
                performAiMeld(nextIndex, discarded, fromPlayer, com.allentx.changchunmahjong.model.Meld.Type.CHI);
                return true;
            }
        }

        return false;
    }

    private void drawForPlayer() {
        interruptedTile = null;
        lastDiscardFromPlayer = -1;
        Tile playerDrawn = gameManager.drawTile();
        lastDrawnTile = playerDrawn;
        if (playerDrawn != null) {
            String msg = String.format(getString(R.string.you_drew_tile), playerDrawn.getChineseName());
            showCenteredToast(msg);

            // Check self-draw Hu or An Gang
            boolean canHu = RuleValidatorHelper.isHu(gameManager.getTable().getPlayer(0).getHand(),
                    gameManager.getTable().getPlayer(0).getMelds());
            boolean canAnGang = RuleValidatorHelper.canAnGang(gameManager.getTable().getPlayer(0).getHand());
            if (canHu || canAnGang) {
                showActions(false, false, canAnGang, canHu);
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
        showCenteredToast("吃！请打出一张牌。");
        announceVoice("吃");
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
        showCenteredToast("碰！请打出一张牌。");
        announceVoice("碰");
    }

    private void executeGang() {
        Tile anGangTile = RuleValidatorHelper.getAnGangTile(gameManager.getTable().getPlayer(0).getHand());

        if (anGangTile != null && (interruptedTile == null
                || !RuleValidatorHelper.canMingGang(gameManager.getTable().getPlayer(0).getHand(), interruptedTile))) {
            // An Gang case
            List<Tile> meldList = new java.util.ArrayList<>();
            for (int i = 0; i < 4; i++)
                meldList.add(anGangTile);
            for (int i = 0; i < 4; i++)
                gameManager.getTable().getPlayer(0).removeTile(anGangTile);

            com.allentx.changchunmahjong.model.Meld meld = new com.allentx.changchunmahjong.model.Meld(
                    com.allentx.changchunmahjong.model.Meld.Type.AN_GANG, meldList, -1);
            gameManager.getTable().getPlayer(0).addMeld(meld);

            showCenteredToast("暗杠！请补牌。");
        } else {
            // Ming Gang case (from discard)
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
            showCenteredToast("杠！请补牌。");
        }

        gameManager.setCurrentPlayerIndex(0);
        hideActions();
        refreshUI();
        announceVoice("杠");

        // DRAW REPLACEMENT TILE
        new android.os.Handler().postDelayed(this::drawForPlayer, 1000);
    }

    private void executePass() {
        hideActions();
        interruptedTile = null;

        if (gameManager.getCurrentPlayerIndex() == 0) {
            // Self-draw case: Just hide actions and wait for human to discard.
            refreshUI();
            return;
        }

        // Interruption case: Move to next priority (AI)
        if (interruptedTile != null) {
            // Check if any AI wants it after human passed
            if (checkAllInterruptions(interruptedTile, lastDiscardFromPlayer, true)) {
                return;
            }
        }

        // Resume AI cycle
        gameManager.advanceTurn();
        refreshUI();

        if (gameManager.getCurrentPlayerIndex() != 0) {
            new android.os.Handler().postDelayed(() -> simulateAiTurn(gameManager.getCurrentPlayerIndex()), 1000);
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
        announceVoice("胡");
        showGameOverDialog("🎉 你赢了！ 🎉", "恭喜你胡牌了！", gameManager.getTable().getPlayer(0),
                interruptedTile != null ? interruptedTile : lastDrawnTile);
    }

    private void showGameOverDialog(String title, String message, com.allentx.changchunmahjong.model.Player winner,
            Tile winningTile) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(false);

        if (winner != null) {
            android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
            FlexboxLayout flexboxLayout = new FlexboxLayout(this);
            flexboxLayout.setFlexWrap(FlexWrap.WRAP);
            flexboxLayout.setPadding(32, 16, 32, 16);

            // 1. Add Melds
            List<com.allentx.changchunmahjong.model.Meld> sortedMelds = new java.util.ArrayList<>(winner.getMelds());
            java.util.Collections.sort(sortedMelds, (m1, m2) -> {
                if (m1.getFirstTile() == null || m2.getFirstTile() == null)
                    return 0;
                return m1.getFirstTile().compareTo(m2.getFirstTile());
            });

            for (com.allentx.changchunmahjong.model.Meld meld : sortedMelds) {
                LinearLayout meldGroup = new LinearLayout(this);
                meldGroup.setOrientation(LinearLayout.HORIZONTAL);
                for (Tile t : meld.getTiles()) {
                    View tileView = createTileView(t, false, 0);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(80, 110);
                    lp.setMargins(2, 2, 2, 2);
                    tileView.setLayoutParams(lp);
                    meldGroup.addView(tileView);
                }
                flexboxLayout.addView(meldGroup);
                // Spacer between melds
                View gap = new View(this);
                flexboxLayout.addView(gap, new com.google.android.flexbox.FlexboxLayout.LayoutParams(12, 1));
            }

            // 2. Add Hand Tiles
            List<Tile> handToDisplay = new java.util.ArrayList<>(winner.getHand());
            if (winningTile != null && handToDisplay.size() % 3 == 2) {
                // If winning tile is in hand (self-draw), remove one instance to display it
                // separately at the end
                for (int i = 0; i < handToDisplay.size(); i++) {
                    if (handToDisplay.get(i).equals(winningTile)) {
                        handToDisplay.remove(i);
                        break;
                    }
                }
            }
            java.util.Collections.sort(handToDisplay);

            for (Tile t : handToDisplay) {
                View tileView = createTileView(t, false, 0);
                com.google.android.flexbox.FlexboxLayout.LayoutParams lp = new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                        80, 110);
                lp.setMargins(2, 2, 2, 2);
                tileView.setLayoutParams(lp);
                flexboxLayout.addView(tileView);
            }

            // 3. Add the Winning Tile (if win was from discard, it's not in
            // winner.getHand())
            if (winningTile != null) {
                // Large spacer
                View gap = new View(this);
                flexboxLayout.addView(gap, new com.google.android.flexbox.FlexboxLayout.LayoutParams(32, 1));

                View tileView = createTileView(winningTile, true, 0); // Highlight winning tile
                com.google.android.flexbox.FlexboxLayout.LayoutParams lp = new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                        80, 110);
                lp.setMargins(2, 2, 2, 2);
                tileView.setLayoutParams(lp);
                flexboxLayout.addView(tileView);
            }

            scrollView.addView(flexboxLayout);
            builder.setView(scrollView);
        }

        builder.setPositiveButton("再来一局", (dialog, which) -> {
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
        Player human = gameManager.getTable().getPlayer(0);
        boolean hu = RuleValidatorHelper.isHu(human.getHand(), human.getMelds());
        showCenteredToast(hu ? "胡了！你赢了！" : getString(R.string.not_hu_yet));
    }
}
