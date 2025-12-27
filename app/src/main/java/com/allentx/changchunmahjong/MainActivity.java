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

        // Setup AssetManager Preloading
        com.allentx.changchunmahjong.util.AssetManager assetManager = com.allentx.changchunmahjong.util.AssetManager
                .getInstance();
        if (!assetManager.isLoaded()) {
            binding.btnStartGame.setEnabled(false);
            binding.btnStartGame.setText(R.string.resources_loading);

            new Thread(() -> {
                assetManager.preload(getApplicationContext());
                runOnUiThread(() -> {
                    binding.btnStartGame.setEnabled(true);
                    binding.btnStartGame.setText(R.string.start_game);
                });
            }).start();
        }

        binding.btnStartGame.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, GameActivity.class);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> showSettingsDialog());
    }

    private void showSettingsDialog() {
        android.content.SharedPreferences prefs = getSharedPreferences("mahjong_prefs", MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean("sound_enabled", true);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.settings);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        androidx.appcompat.widget.SwitchCompat switchSound = new androidx.appcompat.widget.SwitchCompat(this);
        switchSound.setText("开启语音语音 (吃/碰/杠/胡)");
        switchSound.setChecked(soundEnabled);

        layout.addView(switchSound);
        builder.setView(layout);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            prefs.edit().putBoolean("sound_enabled", switchSound.isChecked()).apply();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}
