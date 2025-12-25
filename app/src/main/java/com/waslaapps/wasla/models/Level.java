package com.waslaapps.wasla.models;

import java.util.List;

public class Level {
    private int id;
    private List<List<String>> grid;
    public List<Clue> clues;

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<String>> getGrid() {
        return grid;
    }

    public void setGrid(List<List<String>> grid) {
        this.grid = grid;
    }

    public List<Clue> getClues() {
        return clues;
    }

    public void setClues(List<Clue> clues) {
        this.clues = clues;
    }

    public static class Clue {
        private int id;
        public int row;
        public int col;
        public String direction; // "horizontal" or "vertical"
        public int length;
        private String answer;
        private String clueText;

        // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getClueText() {
            return clueText;
        }

        public void setClueText(String clueText) {
            this.clueText = clueText;
        }
    }
}
