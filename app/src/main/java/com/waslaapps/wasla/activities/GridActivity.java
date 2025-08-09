package com.waslaapps.wasla.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.waslaapps.wasla.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GridActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private Level level;
    private TextView[][] gridViews;
    private List<Position> highlightedPositions = new ArrayList<>();
    private boolean selectionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridLayout = findViewById(R.id.grid);

        int levelIndex = getIntent().getIntExtra("levelIndex", 0);
        loadLevel(levelIndex);
    }

    private void loadLevel(int index) {
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
            Type type = new TypeToken<List<Level>>() {}.getType();
            List<Level> levels = gson.fromJson(builder.toString(), type);

            level = levels.get(index);
            setupGrid();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupGrid() {
        int size = level.grid.length;
        gridLayout.setColumnCount(size);
        gridViews = new TextView[size][size];
        gridLayout.removeAllViews();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                String letter = level.grid[i][j];

                TextView cell = new TextView(this);
                cell.setLayoutParams(new ViewGroup.LayoutParams(140, 140));
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(20);
                cell.setText(letter.isEmpty() ? "" : letter);
                cell.setBackgroundResource(R.drawable.grid_cell_bg);

                int row = i, col = j;
                cell.setOnClickListener(v -> onCellClick(row, col));

                gridLayout.addView(cell);
                gridViews[i][j] = cell;
            }
        }
    }

    private void onCellClick(int row, int col) {
        String letter = level.grid[row][col];
        if (letter.isEmpty()) return;

        if (!selectionActive) {
            clearHighlights();
            if (!highlightWord(row, col)) return;
            selectionActive = true;
        } else {
            // Second click on highlighted word -> move to GameActivity
            for (Position pos : highlightedPositions) {
                if (pos.row == row && pos.col == col) {
                    launchGameActivity(pos.row, pos.col, pos.direction, pos.length);
                    break;
                }
            }
        }
    }

    private boolean highlightWord(int row, int col) {
        String[][] grid = level.grid;

        // Try horizontal first
        int start = col;
        while (start > 0 && !grid[row][start - 1].isEmpty()) start--;

        int end = col;
        while (end < grid.length - 1 && !grid[row][end + 1].isEmpty()) end++;

        if (end > start) {
            highlightedPositions.clear();
            for (int c = start; c <= end; c++) {
                final TextView cell = gridViews[row][c];
                int delay = (c - start) * 80;

                cell.animate()
                        .alpha(0f)
                        .setStartDelay(delay)
                        .setDuration(150)
                        .withEndAction(() -> {
                            cell.setBackgroundColor(Color.GREEN);
                            cell.setTextColor(Color.TRANSPARENT);
                            cell.setAlpha(1f);
                        })
                        .start();

                highlightedPositions.add(new Position(row, c, "H", end - start + 1));
            }
            return true;
        }

        // Try vertical
        start = row;
        while (start > 0 && !grid[start - 1][col].isEmpty()) start--;

        end = row;
        while (end < grid.length - 1 && !grid[end + 1][col].isEmpty()) end++;

        if (end > start) {
            highlightedPositions.clear();
            for (int r = start; r <= end; r++) {
                final TextView cell = gridViews[r][col];
                int delay = (r - start) * 80;

                cell.animate()
                        .alpha(0f)
                        .setStartDelay(delay)
                        .setDuration(150)
                        .withEndAction(() -> {
                            cell.setBackgroundColor(Color.GREEN);
                            cell.setTextColor(Color.TRANSPARENT);
                            cell.setAlpha(1f);
                        })
                        .start();

                highlightedPositions.add(new Position(r, col, "V", end - start + 1));
            }
            return true;
        }

        return false;
    }

    private void clearHighlights() {
        for (Position pos : highlightedPositions) {
            TextView cell = gridViews[pos.row][pos.col];
            cell.setBackgroundResource(R.drawable.grid_cell_bg);
            cell.setTextColor(Color.BLACK);
        }
        highlightedPositions.clear();
        selectionActive = false;
    }

    private void launchGameActivity(int row, int col, String direction, int length) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("levelIndex", getIntent().getIntExtra("levelIndex", 0));
        intent.putExtra("row", row);
        intent.putExtra("col", col);
        intent.putExtra("direction", direction);
        intent.putExtra("length", length);
        startActivity(intent);
    }

    static class Position {
        int row, col, length;
        String direction;

        Position(int row, int col, String direction, int length) {
            this.row = row;
            this.col = col;
            this.direction = direction;
            this.length = length;
        }
    }

    public static class Level {
        public String name;
        public String[][] grid;
    }
}
