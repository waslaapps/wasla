// MainActivity.java
package com.waslaapps.wasla.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.waslaapps.wasla.R;
import com.waslaapps.wasla.activities.LevelSelectActivity;

public class MainActivity extends AppCompatActivity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main); // Make sure this has a "Play" button

      Button playButton = findViewById(R.id.play_button);  // Must match your XML button ID
      playButton.setOnClickListener(v -> {
         Intent intent = new Intent(MainActivity.this, LevelSelectActivity.class);
         startActivity(intent);
      });
   }
}
