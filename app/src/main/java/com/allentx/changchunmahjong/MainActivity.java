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

        // Setup listeners
        binding.btnStartGame.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, GameActivity.class);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> {
            // TODO: Open Settings
        });
    }
}
