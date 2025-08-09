package com.waslaapps.wasla.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.data.model.Level;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.List;

public class LevelLoader {

    public static List<Level> loadLevels(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open("levels/" + fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Level>>(){}.getType();
            return gson.fromJson(json, listType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
