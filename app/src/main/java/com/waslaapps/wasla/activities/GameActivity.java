package com.waslaapps.wasla.activities;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.waslaapps.wasla.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GameActivity extends AppCompatActivity {

   private GridLayout gridLayout;
   private FlexboxLayout keyboardLayout;
   private String[][] grid;
   private TextView[][] gridViews;
   private List<Position> positions;

   private int currentInputIndex = 0;
   private StringBuilder userInput = new StringBuilder();
   private String correctAnswer = "";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);

      gridLayout = findViewById(R.id.grid);
      keyboardLayout = findViewById(R.id.keyboard_layout);

      int levelIndex = getIntent().getIntExtra("levelIndex", 0);
      positions = (List<Position>) getIntent().getSerializableExtra("positions");

      loadGrid(levelIndex);
      setupGrid();
      setupKeyboard();
   }

   private void loadGrid(int levelIndex) {
      try {
         InputStream is = getAssets().open("levels.json");
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder builder = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) builder.append(line);

         reader.close();
         is.close();

         Gson gson = new Gson();
         Level[] levels = gson.fromJson(builder.toString(), Level[].class);
         grid = levels[levelIndex].grid;

         StringBuilder answer = new StringBuilder();
         for (Position pos : positions) {
            answer.append(grid[pos.row][pos.col]);
         }
         correctAnswer = answer.toString();

      } catch (Exception e) {
         e.printStackTrace();
         Toast.makeText(this, "خطأ في تحميل المستوى", Toast.LENGTH_SHORT).show();
         finish();
      }
   }

   private void setupGrid() {
      int size = grid.length;
      gridLayout.setColumnCount(size);
      gridViews = new TextView[size][size];

      for (int i = 0; i < size; i++) {
         for (int j = 0; j < size; j++) {
            TextView cell = new TextView(this);
            cell.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
            cell.setBackgroundResource(R.drawable.grid_cell_bg);
            cell.setTextSize(20);
            cell.setGravity(android.view.Gravity.CENTER);
            gridViews[i][j] = cell;
            gridLayout.addView(cell);
         }
      }

      // Pre-fill answer positions with empty boxes
      for (Position pos : positions) {
         gridViews[pos.row][pos.col].setText("");
      }
   }

   private void setupKeyboard() {
      keyboardLayout.removeAllViews();
      List<String> letters = new ArrayList<>();

      // Add all answer letters
      for (char c : correctAnswer.toCharArray()) {
         letters.add(String.valueOf(c));
      }

      // Fill up with random Arabic letters
      List<String> allArabic = Arrays.asList("ا","ب","ت","ث","ج","ح","خ","د","ذ","ر","ز","س","ش","ص","ض","ط","ظ","ع","غ","ف","ق","ك","ل","م","ن","ه","و","ي");
      Random random = new Random();
      while (letters.size() < 12) {
         String r = allArabic.get(random.nextInt(allArabic.size()));
         if (!letters.contains(r)) letters.add(r);
      }

      Collections.shuffle(letters);

      for (String letter : letters) {
         Button key = new Button(this);
         key.setText(letter);
         key.setOnClickListener(v -> handleInput(letter));
         keyboardLayout.addView(key);
      }
   }

   private void handleInput(String letter) {
      if (currentInputIndex >= positions.size()) return;

      Position pos = positions.get(currentInputIndex);
      gridViews[pos.row][pos.col].setText(letter);
      userInput.append(letter);
      currentInputIndex++;

      if (userInput.length() == correctAnswer.length()) {
         if (userInput.toString().equals(correctAnswer)) {
            Toast.makeText(this, "✔️ الإجابة صحيحة!", Toast.LENGTH_LONG).show();
            // You can add level complete logic here
         } else {
            Toast.makeText(this, "❌ الإجابة غير صحيحة، حاول مرة أخرى", Toast.LENGTH_SHORT).show();
            clearAnswer();
         }
      }
   }

   private void clearAnswer() {
      for (Position pos : positions) {
         gridViews[pos.row][pos.col].setText("");
      }
      userInput.setLength(0);
      currentInputIndex = 0;
   }

   // Helper classes
   public static class Level {
      public String[][] grid;
   }

   public static class Position implements java.io.Serializable {
      public int row, col;
      public Position(int row, int col) {
         this.row = row;
         this.col = col;
      }
   }
}
