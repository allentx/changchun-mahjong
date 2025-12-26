package com.allentx.changchunmahjong;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.allentx.changchunmahjong.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup listeners
        binding.btnStartGame.setOnClickListener(v -> {
            com.allentx.changchunmahjong.logic.GameManager gm = new com.allentx.changchunmahjong.logic.GameManager();
            gm.startGame();

            // Display Human (Seat 0) Hand
            StringBuilder sb = new StringBuilder();
            sb.append("Player 0 Hand:\n");
            for (com.allentx.changchunmahjong.model.Tile t : gm.getTable().getPlayer(0).getHand()) {
                sb.append(t.toString()).append(" ");
            }
            sb.append("\n\n");

            // Check Hu
            boolean isHu = com.allentx.changchunmahjong.logic.RuleValidatorHelper
                    .isHu(gm.getTable().getPlayer(0).getHand());
            sb.append("Is Hu? ").append(isHu).append("\n");

            binding.textView.setText(sb.toString());
        });

        binding.btnSettings.setOnClickListener(v -> {
            // TODO: Open Settings
        });
    }
}
