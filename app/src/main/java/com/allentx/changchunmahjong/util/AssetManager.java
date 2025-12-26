package com.allentx.changchunmahjong.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import com.allentx.changchunmahjong.R;

public class AssetManager {
    private static AssetManager instance;

    private Bitmap[] tilesWan;
    private Bitmap[] tilesTiao;
    private Bitmap[] tilesTong;
    private Bitmap[] tilesZi;
    private boolean loaded = false;

    private AssetManager() {
    }

    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void preload(Context context) {
        if (loaded)
            return;

        tilesWan = new Bitmap[9];
        tilesWan[0] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_1));
        tilesWan[1] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_2));
        tilesWan[2] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_3));
        tilesWan[3] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_4));
        tilesWan[4] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_5));
        tilesWan[5] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_6));
        tilesWan[6] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_7));
        tilesWan[7] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_8));
        tilesWan[8] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.wan_9));

        tilesTiao = new Bitmap[9];
        tilesTiao[0] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_1));
        tilesTiao[1] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_2));
        tilesTiao[2] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_3));
        tilesTiao[3] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_4));
        tilesTiao[4] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_5));
        tilesTiao[5] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_6));
        tilesTiao[6] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_7));
        tilesTiao[7] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_8));
        tilesTiao[8] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiao_9));

        tilesTong = new Bitmap[9];
        tilesTong[0] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_1));
        tilesTong[1] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_2));
        tilesTong[2] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_3));
        tilesTong[3] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_4));
        tilesTong[4] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_5));
        tilesTong[5] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_6));
        tilesTong[6] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_7));
        tilesTong[7] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_8));
        tilesTong[8] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.tong_9));

        tilesZi = new Bitmap[7];
        tilesZi[0] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_1));
        tilesZi[1] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_2));
        tilesZi[2] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_3));
        tilesZi[3] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_4));
        tilesZi[4] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_5));
        tilesZi[5] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_6));
        tilesZi[6] = trimBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.zi_7));

        loaded = true;
    }

    public Bitmap getWan(int index) {
        return (tilesWan != null && index >= 0 && index < 9) ? tilesWan[index] : null;
    }

    public Bitmap getTiao(int index) {
        return (tilesTiao != null && index >= 0 && index < 9) ? tilesTiao[index] : null;
    }

    public Bitmap getTong(int index) {
        return (tilesTong != null && index >= 0 && index < 9) ? tilesTong[index] : null;
    }

    public Bitmap getZi(int index) {
        return (tilesZi != null && index >= 0 && index < 7) ? tilesZi[index] : null;
    }

    private Bitmap trimBitmap(Bitmap input) {
        if (input == null)
            return null;
        Bitmap bmp = null;
        try {
            bmp = input.copy(Bitmap.Config.ARGB_8888, true);
        } catch (OutOfMemoryError e) {
            System.gc();
            return input;
        }
        input.recycle();
        if (bmp == null)
            return null;

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int top = height, bottom = 0, left = width, right = 0;
        boolean foundContent = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bmp.getPixel(x, y);
                boolean isWhite = (Color.red(pixel) > 200 && Color.green(pixel) > 200 && Color.blue(pixel) > 200);
                if (isWhite) {
                    bmp.setPixel(x, y, Color.TRANSPARENT);
                } else {
                    foundContent = true;
                    if (y < top)
                        top = y;
                    if (y > bottom)
                        bottom = y;
                    if (x < left)
                        left = x;
                    if (x > right)
                        right = x;
                }
            }
        }

        if (!foundContent)
            return bmp;

        top = Math.max(0, top - 1);
        left = Math.max(0, left - 1);
        bottom = Math.min(height, bottom + 2);
        right = Math.min(width, right + 2);

        Bitmap result = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
        if (result != bmp)
            bmp.recycle();
        return result;
    }
}
