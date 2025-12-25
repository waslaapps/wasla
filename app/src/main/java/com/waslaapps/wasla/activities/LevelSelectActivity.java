package com.waslaapps.wasla.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.R;
import com.waslaapps.wasla.adapters.LevelAdapter;
import com.waslaapps.wasla.models.Level;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class LevelSelectActivity extends AppCompatActivity {

   private List<Level> levels;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_level_select);

      loadLevelsFromJson();

      RecyclerView recyclerView = findViewById(R.id.levelRecyclerView);
      recyclerView.setLayoutManager(new LinearLayoutManager(this));

      LevelAdapter adapter = new LevelAdapter(levels, position -> {
         Intent intent = new Intent(LevelSelectActivity.this, GridActivity.class);
         intent.putExtra("levelIndex", position);
         startActivity(intent);
      });

      recyclerView.setAdapter(adapter);
   }

   private void loadLevelsFromJson() {
      try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(getAssets().open("levels.json")))) {
         Gson gson = new Gson();
         Type listType = new TypeToken<List<Level>>() {}.getType();
         levels = gson.fromJson(reader, listType);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
