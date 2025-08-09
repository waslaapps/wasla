package com.waslaapps.wasla.data;

import android.content.Context;
import com.google.gson.Gson;
import com.waslaapps.wasla.data.model.Level;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LevelLoader {
    public static Level loadLevel(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open("levels/" + fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new Gson().fromJson(json, Level.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
