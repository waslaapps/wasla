// LevelSelectActivity.java
package com.waslaapps.wasla.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.waslaapps.wasla.R;

public class LevelSelectActivity extends AppCompatActivity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_level_select);

      Button level1 = findViewById(R.id.level1_button);  // Replace with actual ID
      level1.setOnClickListener(v -> {
         Intent intent = new Intent(this, GridActivity.class);
         intent.putExtra("levelIndex", 0); // Pass selected level index
         startActivity(intent);
      });
   }
}
