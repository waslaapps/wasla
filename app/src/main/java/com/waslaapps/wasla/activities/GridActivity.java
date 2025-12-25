package com.waslaapps.wasla.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.R;
import com.waslaapps.wasla.models.Level;
import com.waslaapps.wasla.models.Level.Clue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class GridActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView[][] gridViews;

    private List<Level> levels;
    private Level currentLevel;

    // Swipe tracking
    private float startX, startY;
    private int startRow = -1, startCol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridLayout = findViewById(R.id.grid);
        gridLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int levelIndex = getIntent().getIntExtra("levelIndex", -1);

        if (levelIndex < 0) {
            Toast.makeText(this, "Invalid level", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadLevels();
        loadLevel(levelIndex);
    }

    // =====================
    // Load all levels
    // =====================
    private void loadLevels() {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(getAssets().open("levels.json")))) {

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Level>>() {}.getType();
            levels = gson.fromJson(reader, listType);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load levels", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadLevel(int index) {
        if (levels == null || index < 0 || index >= levels.size()) {
            Toast.makeText(this, "Level not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentLevel = levels.get(index);
        setupGrid();
    }

    // =====================
    // Build crossword grid
    // =====================
    private void setupGrid() {
        List<List<String>> grid = currentLevel.getGrid();

        int rows = grid.size();
        int cols = grid.get(0).size();

        gridLayout.removeAllViews();
        gridLayout.setRowCount(rows);
        gridLayout.setColumnCount(cols);

        gridViews = new TextView[rows][cols];

        int cellSize = getResources().getDisplayMetrics().widthPixels / cols;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                TextView cell = new TextView(this);
                cell.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize));
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(22);
                cell.setBackgroundResource(R.drawable.grid_cell_bg);

                String letter = grid.get(r).get(c);

                if (letter == null || letter.isEmpty()) {
                    cell.setVisibility(View.INVISIBLE);
                } else {
                    cell.setText(letter);
                    cell.setVisibility(View.VISIBLE);
                }

                final int row = r;
                final int col = c;

                cell.setOnTouchListener((v, event) -> handleTouch(event, row, col));

                gridLayout.addView(cell);
                gridViews[r][c] = cell;
            }
        }
    }

    // =====================
    // Touch handling (SWIPE)
    // =====================
    private boolean handleTouch(MotionEvent event, int row, int col) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                startRow = row;
                startCol = col;
                clearHighlights();
                return true;

            case MotionEvent.ACTION_UP:
                float dx = event.getX() - startX;
                float dy = event.getY() - startY;
                handleSwipe(startRow, startCol, dx, dy);
                return true;
        }
        return false;
    }

    // =====================
    // Determine swipe direction
    // =====================
    private void handleSwipe(int row, int col, float dx, float dy) {

        if (Math.abs(dx) < 30 && Math.abs(dy) < 30) return;

        String direction = Math.abs(dx) > Math.abs(dy)
                ? "horizontal"
                : "vertical";

        Clue clue = findClue(row, col, direction);

        if (clue == null) {
            Toast.makeText(this, "لا توجد كلمة هنا", Toast.LENGTH_SHORT).show();
            return;
        }

        highlightClue(clue);
        openGame(clue);
    }

    // =====================
    // Find clue by direction
    // =====================
    private Clue findClue(int row, int col, String direction) {

        if (currentLevel.clues == null) return null;

        for (Clue clue : currentLevel.clues) {
            if (!direction.equals(clue.direction)) continue;
            if (coversCell(clue, row, col)) return clue;
        }
        return null;
    }

    private boolean coversCell(Clue clue, int row, int col) {
        if ("horizontal".equals(clue.direction)) {
            return row == clue.row &&
                    col >= clue.col &&
                    col < clue.col + clue.length;
        } else {
            return col == clue.col &&
                    row >= clue.row &&
                    row < clue.row + clue.length;
        }
    }

    // =====================
    // Highlight word
    // =====================
    private void highlightClue(Clue clue) {
        clearHighlights();

        for (int i = 0; i < clue.length; i++) {
            int r = clue.row + ("vertical".equals(clue.direction) ? i : 0);
            int c = clue.col + ("horizontal".equals(clue.direction) ? i : 0);
            gridViews[r][c].setBackgroundResource(R.drawable.grid_cell_highlight_bg);
        }
    }

    private void clearHighlights() {
        if (gridViews == null) return;

        for (TextView[] row : gridViews) {
            for (TextView cell : row) {
                if (cell != null) {
                    cell.setBackgroundResource(R.drawable.grid_cell_bg);
                }
            }
        }
    }

    // =====================
    // Open GameActivity (✅ CORRECT)
    // =====================
    private void openGame(Clue clue) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("levelIndex", levels.indexOf(currentLevel));
        intent.putExtra("clueId", clue.id); // ✅ ONLY THIS
        startActivity(intent);
    }
}
