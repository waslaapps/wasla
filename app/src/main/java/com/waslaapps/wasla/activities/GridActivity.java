package com.waslaapps.wasla.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class GridActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private List<Level> levels;
    private Level currentLevel;
    private TextView[][] gridViews;

    private List<Clue> cluesAtSelectedCell = new ArrayList<>();
    private int selectedClueIndex = -1;
    private int lastTappedRow = -1;
    private int lastTappedCol = -1;

    // Track last selected clue index to detect confirm tap
    private int lastConfirmedClueIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridLayout = findViewById(R.id.grid);
        gridLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        loadLevels();
        loadLevel(0); // Load first level, or pass from Intent
    }

    private void loadLevels() {
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
            Type listType = new TypeToken<List<Level>>(){}.getType();
            levels = gson.fromJson(builder.toString(), listType);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load levels", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLevel(int index) {
        if (levels == null || levels.size() <= index) return;
        currentLevel = levels.get(index);
        setupGrid();
    }

    private void setupGrid() {
        int rows = currentLevel.grid.length;
        int cols = currentLevel.grid[0].length;

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
                cell.setText(currentLevel.grid[r][c]);
                final int row = r;
                final int col = c;
                cell.setOnClickListener(v -> onCellClick(row, col));

                gridLayout.addView(cell);
                gridViews[r][c] = cell;
            }
        }
    }

    private void onCellClick(int row, int col) {
        // If tapped a different cell than last time, reset clues list & index
        if (row != lastTappedRow || col != lastTappedCol) {
            cluesAtSelectedCell.clear();
            selectedClueIndex = -1;
            lastConfirmedClueIndex = -1;

            // Find all clues covering this cell
            for (Clue clue : currentLevel.clues) {
                if (doesClueCoverCell(clue, row, col)) {
                    cluesAtSelectedCell.add(clue);
                }
            }
            lastTappedRow = row;
            lastTappedCol = col;
        }

        if (cluesAtSelectedCell.isEmpty()) {
            Toast.makeText(this, "لا توجد أدلة هنا", Toast.LENGTH_SHORT).show(); // No clues here
            clearHighlights();
            return;
        }

        // Cycle to next clue in the list
        selectedClueIndex = (selectedClueIndex + 1) % cluesAtSelectedCell.size();
        Clue clue = cluesAtSelectedCell.get(selectedClueIndex);

        highlightClue(clue);

        // If user taps same clue twice, open GameActivity
        if (lastConfirmedClueIndex == selectedClueIndex) {
            openGameForSelectedClue(clue);
        }
        lastConfirmedClueIndex = selectedClueIndex;
    }

    private boolean doesClueCoverCell(Clue clue, int row, int col) {
        if ("horizontal".equals(clue.direction)) {
            return (row == clue.row) && (col >= clue.col) && (col < clue.col + clue.length);
        } else if ("vertical".equals(clue.direction)) {
            return (col == clue.col) && (row >= clue.row) && (row < clue.row + clue.length);
        }
        return false;
    }

    private void highlightClue(Clue clue) {
        clearHighlights();

        int r = clue.row;
        int c = clue.col;

        if ("horizontal".equals(clue.direction)) {
            for (int i = 0; i < clue.length; i++) {
                animateCellHighlight(r, c + i);
            }
        } else {
            for (int i = 0; i < clue.length; i++) {
                animateCellHighlight(r + i, c);
            }
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

    private void animateCellHighlight(int row, int col) {
        if (row < 0 || col < 0 || row >= gridViews.length || col >= gridViews[0].length) return;
        TextView cell = gridViews[row][col];
        if (cell == null) return;
        cell.setBackgroundResource(R.drawable.grid_cell_highlight_bg);
    }

    private void openGameForSelectedClue(Clue clue) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("levelIndex", levels.indexOf(currentLevel));
        intent.putExtra("row", clue.row);
        intent.putExtra("col", clue.col);
        intent.putExtra("direction", clue.direction);
        intent.putExtra("length", clue.length);
        startActivity(intent);
    }

    // Model classes
    public static class Level {
        public int id;
        public String[][] grid;
        public List<Clue> clues;
    }

    public static class Clue {
        public int id;
        public int row;
        public int col;
        public String direction;
        public int length;
        public String answer;
        public String clueText;
    }
}
