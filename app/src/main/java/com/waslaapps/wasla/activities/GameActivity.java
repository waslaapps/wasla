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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class GameActivity extends AppCompatActivity {

   private static final String TAG = "GameActivity";

   // UI Components
   private GridLayout gridLayout;
   private FlexboxLayout keyboardLayout;
   private TextView[][] answerGrid;

   // Current clue info
   private int clueRow, clueCol, clueLength;
   private String clueDirection;

   // Level data
   private Level currentLevel;

   // Input tracking (right to left)
   private int currentInputIndex;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);

      gridLayout = findViewById(R.id.grid);
      keyboardLayout = findViewById(R.id.keyboard_layout);

      // Get clue parameters from intent
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

      // Initialize current input index for right-to-left input
      currentInputIndex = clueLength - 1;

      loadLevel(levelIndex);
   }

   // ========================
   // Load level JSON and parse
   // ========================
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

         // Check clue bounds before proceeding
         int maxRows = currentLevel.grid.length;
         int maxCols = currentLevel.grid[0].length;

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

   // ======================
   // Setup mini grid (single word)
   // ======================
   private void setupMiniGrid() {
      gridLayout.removeAllViews();

      gridLayout.setColumnCount(clueLength);
      answerGrid = new TextView[1][clueLength]; // single row grid

      for (int i = 0; i < clueLength; i++) {
         TextView cell = new TextView(this);
         cell.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
         cell.setGravity(Gravity.CENTER);
         cell.setTextSize(20);
         cell.setBackgroundResource(R.drawable.grid_cell_bg);
         cell.setText("");
         answerGrid[0][i] = cell;
         gridLayout.addView(cell);
      }
   }

   // =================
   // Setup keyboard buttons
   // =================
   private void setupKeyboard() {
      keyboardLayout.removeAllViews();

      Set<String> correctLetters = new LinkedHashSet<>();  // maintain insertion order

      // Collect clue letters exactly once, in order from clue start to end
      for (int i = 0; i < clueLength; i++) {
         int row = clueRow + ("vertical".equals(clueDirection) ? i : 0);
         int col = clueCol + ("horizontal".equals(clueDirection) ? i : 0);
         String correct = currentLevel.grid[row][col];
         if (!correct.isEmpty()) correctLetters.add(correct);
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

   // =====================
   // Handle input (Right to Left)
   // =====================
   private void handleInput(String letter) {
      if (currentInputIndex < 0) return; // All letters filled

      TextView cell = answerGrid[0][currentInputIndex];

      int row = clueRow + ("vertical".equals(clueDirection) ? (clueLength - 1 - currentInputIndex) : 0);
      int col = clueCol + ("horizontal".equals(clueDirection) ? (clueLength - 1 - currentInputIndex) : 0);

      String correctLetter = currentLevel.grid[row][col];

      if (correctLetter.equals(letter)) {
         cell.setText(letter);
         currentInputIndex--;
      } else {
         Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
         return; // Do not advance index on wrong input
      }

      if (currentInputIndex < 0) {
         // Check full answer when complete
         StringBuilder userAnswer = new StringBuilder();
         for (int i = clueLength - 1; i >= 0; i--) {
            userAnswer.append(answerGrid[0][i].getText().toString());
         }

         if (userAnswer.toString().equals(getCorrectAnswer())) {
            Toast.makeText(this, "✔️ Correct answer!", Toast.LENGTH_LONG).show();
            // TODO: Add level complete logic here
         } else {
            Toast.makeText(this, "❌ Wrong answer, try again", Toast.LENGTH_SHORT).show();
            clearAnswer();
         }
      }
   }

   // =================
   // Get correct answer string from grid
   // =================
   private String getCorrectAnswer() {
      StringBuilder answer = new StringBuilder();
      for (int i = 0; i < clueLength; i++) {
         int row = clueRow + ("vertical".equals(clueDirection) ? i : 0);
         int col = clueCol + ("horizontal".equals(clueDirection) ? i : 0);
         answer.append(currentLevel.grid[row][col]);
      }
      return answer.toString();
   }

   // =================
   // Clear current answer inputs
   // =================
   private void clearAnswer() {
      for (int i = 0; i < clueLength; i++) {
         answerGrid[0][i].setText("");
      }
      currentInputIndex = clueLength - 1;
   }

   // =================
   // Model class for Level JSON
   // =================
   public static class Level {
      public int id;
      public String[][] grid;
   }
}
