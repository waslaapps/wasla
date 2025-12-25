package com.waslaapps.wasla.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.R;
import com.waslaapps.wasla.models.Level;  // Import your Level model

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class GameActivity extends AppCompatActivity {

   private static final String TAG = "GameActivity";

   private GridLayout gridLayout;
   private FlexboxLayout keyboardLayout;
   private TextView[] answerCells;

   private int clueRow, clueCol, clueLength;
   private String clueDirection;

   private Level currentLevel;

   private int currentInputIndex;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);

      gridLayout = findViewById(R.id.answer_grid);
      keyboardLayout = findViewById(R.id.keyboard_layout);

      int levelIndex = getIntent().getIntExtra("levelIndex", 0);
      clueRow = getIntent().getIntExtra("row", 0);
      clueCol = getIntent().getIntExtra("col", 0);
      clueDirection = getIntent().getStringExtra("direction");
      clueLength = getIntent().getIntExtra("length", 0);

      Log.d(TAG, "onCreate: levelIndex=" + levelIndex + ", row=" + clueRow + ", col=" + clueCol
              + ", direction=" + clueDirection + ", length=" + clueLength);

      if (!"horizontal".equals(clueDirection) && !"vertical".equals(clueDirection)) {
         Toast.makeText(this, "Invalid clue direction", Toast.LENGTH_LONG).show();
         finish();
         return;
      }

      currentInputIndex = clueLength - 1;

      loadLevel(levelIndex);
   }

   private void loadLevel(int levelIndex) {
      try {
         InputStream is = getAssets().open("levels.json");
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder builder = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
            builder.append(line);
         }
         reader.close();
         is.close();

         Gson gson = new Gson();
         Type listType = new TypeToken<List<Level>>() {}.getType();
         List<Level> levels = gson.fromJson(builder.toString(), listType);

         currentLevel = levels.get(levelIndex);

         int maxRows = currentLevel.getGrid().size();
         int maxCols = currentLevel.getGrid().get(0).size();

         if ("vertical".equals(clueDirection) && (clueRow + clueLength > maxRows)) {
            Log.e(TAG, "Vertical clue out of bounds");
            Toast.makeText(this, "Error: Clue out of grid bounds (vertical)", Toast.LENGTH_LONG).show();
            finish();
            return;
         }

         if ("horizontal".equals(clueDirection) && (clueCol + clueLength > maxCols)) {
            Log.e(TAG, "Horizontal clue out of bounds");
            Toast.makeText(this, "Error: Clue out of grid bounds (horizontal)", Toast.LENGTH_LONG).show();
            finish();
            return;
         }

         setupMiniGrid();
         setupKeyboard();

      } catch (Exception e) {
         Log.e(TAG, "Error loading level", e);
         Toast.makeText(this, "Error loading level", Toast.LENGTH_LONG).show();
         finish();
      }
   }

   private void setupMiniGrid() {
      gridLayout.removeAllViews();

      gridLayout.setColumnCount(clueLength);
      answerCells = new TextView[clueLength];

      for (int i = 0; i < clueLength; i++) {
         TextView cell = new TextView(this);
         cell.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
         cell.setGravity(Gravity.CENTER);
         cell.setTextSize(20);
         cell.setBackgroundResource(R.drawable.grid_cell_bg);
         cell.setText("");
         answerCells[i] = cell;
         gridLayout.addView(cell);
      }
   }

   private void setupKeyboard() {
      keyboardLayout.removeAllViews();

      Set<String> correctLetters = new LinkedHashSet<>();

      for (int i = 0; i < clueLength; i++) {
         int row = clueRow + ("vertical".equals(clueDirection) ? i : 0);
         int col = clueCol + ("horizontal".equals(clueDirection) ? i : 0);
         String letter = currentLevel.getGrid().get(row).get(col);
         if (!letter.isEmpty()) correctLetters.add(letter);
      }

      List<String> keyboardLetters = new ArrayList<>(correctLetters);

      List<String> allArabicLetters = Arrays.asList(
              "ا", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ذ", "ر",
              "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ع", "غ", "ف",
              "ق", "ك", "ل", "م", "ن", "ه", "و", "ي"
      );

      Random rand = new Random();
      while (keyboardLetters.size() < 12) {
         String randomLetter = allArabicLetters.get(rand.nextInt(allArabicLetters.size()));
         if (!keyboardLetters.contains(randomLetter)) {
            keyboardLetters.add(randomLetter);
         }
      }

      Collections.shuffle(keyboardLetters);

      for (String letter : keyboardLetters) {
         Button key = new Button(this);
         key.setText(letter);
         key.setOnClickListener(v -> handleInput(letter));
         keyboardLayout.addView(key);
      }
   }

   private void handleInput(String letter) {
      if (currentInputIndex < 0) return;

      TextView cell = answerCells[currentInputIndex];

      int row = clueRow + ("vertical".equals(clueDirection) ? (clueLength - 1 - currentInputIndex) : 0);
      int col = clueCol + ("horizontal".equals(clueDirection) ? (clueLength - 1 - currentInputIndex) : 0);

      String correctLetter = currentLevel.getGrid().get(row).get(col);

      if (correctLetter.equals(letter)) {
         cell.setText(letter);
         currentInputIndex--;
      } else {
         Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
         return;
      }

      if (currentInputIndex < 0) {
         StringBuilder userAnswer = new StringBuilder();
         for (int i = clueLength - 1; i >= 0; i--) {
            userAnswer.append(answerCells[i].getText().toString());
         }

         if (userAnswer.toString().equals(getCorrectAnswer())) {
            Toast.makeText(this, "✔️ Correct answer!", Toast.LENGTH_LONG).show();
            // TODO: handle success (e.g., finish activity or next clue)
         } else {
            Toast.makeText(this, "❌ Wrong answer, try again", Toast.LENGTH_SHORT).show();
            clearAnswer();
         }
      }
   }

   private String getCorrectAnswer() {
      StringBuilder answer = new StringBuilder();
      for (int i = 0; i < clueLength; i++) {
         int row = clueRow + ("vertical".equals(clueDirection) ? i : 0);
         int col = clueCol + ("horizontal".equals(clueDirection) ? i : 0);
         answer.append(currentLevel.getGrid().get(row).get(col));
      }
      return answer.toString();
   }

   private void clearAnswer() {
      for (int i = 0; i < clueLength; i++) {
         answerCells[i].setText("");
      }
      currentInputIndex = clueLength - 1;
   }
}
