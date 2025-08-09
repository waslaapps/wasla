package com.waslaapps.wasla.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.models.Level;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class LevelLoader {
    public static List<Level> loadLevels(Context context) {
        try {
            InputStream is = context.getAssets().open("levels.json");
            InputStreamReader reader = new InputStreamReader(is);
            Type listType = new TypeToken<List<Level>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}