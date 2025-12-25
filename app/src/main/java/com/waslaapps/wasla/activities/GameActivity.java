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
import com.waslaapps.wasla.models.Level;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class GameActivity extends AppCompatActivity {

   private static final String TAG = "GameActivity";

   private TextView questionText;
   private GridLayout answerGrid;
   private FlexboxLayout keyboardLayout;

   private TextView[] answerCells;

   private Level currentLevel;
   private Level.Clue currentClue;

   private int currentInputIndex;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);

      questionText = findViewById(R.id.question_text);
      answerGrid = findViewById(R.id.answer_grid);
      keyboardLayout = findViewById(R.id.keyboard_layout);

      int levelIndex = getIntent().getIntExtra("levelIndex", -1);
      int clueId = getIntent().getIntExtra("clueId", -1);

      if (levelIndex == -1 || clueId == -1) {
         Toast.makeText(this, "Invalid game data", Toast.LENGTH_LONG).show();
         finish();
         return;
      }

      loadLevel(levelIndex, clueId);
   }

   // ======================
   // Load level + clue
   // ======================
   private void loadLevel(int levelIndex, int clueId) {
      try {
         InputStream is = getAssets().open("levels.json");
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         Gson gson = new Gson();

         Type listType = new TypeToken<List<Level>>() {}.getType();
         List<Level> levels = gson.fromJson(reader, listType);

         currentLevel = levels.get(levelIndex);

         for (Level.Clue clue : currentLevel.clues) {
            if (clue.id == clueId) {
               currentClue = clue;
               break;
            }
         }

         if (currentClue == null) {
            Toast.makeText(this, "Clue not found", Toast.LENGTH_LONG).show();
            finish();
            return;
         }

         questionText.setText(currentClue.clueText);

         currentInputIndex = currentClue.length - 1;

         setupAnswerGrid();
         setupKeyboard();

      } catch (Exception e) {
         Log.e(TAG, "Error loading level", e);
         Toast.makeText(this, "Error loading game", Toast.LENGTH_LONG).show();
         finish();
      }
   }

   // ======================
   // Answer boxes
   // ======================
   private void setupAnswerGrid() {
      answerGrid.removeAllViews();
      answerGrid.setColumnCount(currentClue.length);

      answerCells = new TextView[currentClue.length];

      for (int i = 0; i < currentClue.length; i++) {
         TextView cell = new TextView(this);
         cell.setLayoutParams(new ViewGroup.LayoutParams(50, 50));
         cell.setGravity(Gravity.CENTER);
         cell.setTextSize(20);
         cell.setBackgroundResource(R.drawable.grid_cell_bg);
         cell.setText("");
         answerCells[i] = cell;
         answerGrid.addView(cell);
      }
   }

   // ======================
   // Keyboard
   // ======================
   private void setupKeyboard() {
      keyboardLayout.removeAllViews();

      Set<String> letters = new LinkedHashSet<>();

      for (int i = 0; i < currentClue.length; i++) {
         int row = currentClue.row + ("vertical".equals(currentClue.direction) ? i : 0);
         int col = currentClue.col + ("horizontal".equals(currentClue.direction) ? i : 0);
         letters.add(currentLevel.getGrid().get(row).get(col));
      }

      List<String> keyboardLetters = new ArrayList<>(letters);

      List<String> arabic = Arrays.asList(
              "ا","ب","ت","ث","ج","ح","خ","د","ذ","ر",
              "ز","س","ش","ص","ض","ط","ظ","ع","غ","ف",
              "ق","ك","ل","م","ن","ه","و","ي"
      );

      Random random = new Random();
      while (keyboardLetters.size() < 12) {
         String l = arabic.get(random.nextInt(arabic.size()));
         if (!keyboardLetters.contains(l)) keyboardLetters.add(l);
      }

      Collections.shuffle(keyboardLetters);

      for (String letter : keyboardLetters) {
         Button b = new Button(this);
         b.setText(letter);
         b.setOnClickListener(v -> handleInput(letter));
         keyboardLayout.addView(b);
      }
   }

   // ======================
   // Input logic (RTL)
   // ======================
   private void handleInput(String letter) {
      if (currentInputIndex < 0) return;

      int pos = currentClue.length - 1 - currentInputIndex;

      int row = currentClue.row + ("vertical".equals(currentClue.direction) ? pos : 0);
      int col = currentClue.col + ("horizontal".equals(currentClue.direction) ? pos : 0);

      String correct = currentLevel.getGrid().get(row).get(col);

      if (correct.equals(letter)) {
         answerCells[currentInputIndex].setText(letter);
         currentInputIndex--;
      } else {
         Toast.makeText(this, "خطأ", Toast.LENGTH_SHORT).show();
      }

      if (currentInputIndex < 0) {
         Toast.makeText(this, "✔️ صحيح", Toast.LENGTH_LONG).show();
      }
   }
}
