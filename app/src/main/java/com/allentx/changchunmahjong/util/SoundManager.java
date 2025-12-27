package com.allentx.changchunmahjong.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class SoundManager {
    private static SoundManager instance;
    private TextToSpeech tts;
    private boolean initialized = false;

    private SoundManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.CHINESE);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    initialized = true;
                }
            }
        });
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void announce(String text) {
        if (initialized && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
