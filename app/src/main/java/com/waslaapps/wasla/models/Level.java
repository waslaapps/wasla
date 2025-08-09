package com.waslaapps.wasla.models;

import java.util.List;

public class Level {
    public int id;
    public String[][] grid;
    public List<Clue> clues;

    public static class Clue {
        public int id;
        public int row;
        public int col;
        public String direction; // "horizontal" or "vertical"
        public int length;
        public String answer;
        public String clueText;
    }
}
