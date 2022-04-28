package com.napier.set08122;

import java.util.List;

public class GameData implements java.io.Serializable
{
    private boolean completed;
    private int counter, moveNo;
    private int[][] grid, gridCopy, original;
    private List<int[]> hints, moves;

    public boolean getCompleted() { return completed; }
    public int getCounter() { return counter; }
    public int getMoveNo() { return moveNo; }
    public int[][] getGrid() { return grid; }
    public int[][] getGridCopy() { return gridCopy; }
    public int[][] getOriginal() { return original; }
    public List<int[]> getHints() { return hints; }
    public List<int[]> getMoves() { return moves; }

    public GameData(int[][] grid, int[][] gridCopy, int[][] original,
                    int counter, int moveNo, boolean completed,
                    List<int[]> hints, List<int[]> moves)
    {

        this.grid = grid;
        this.gridCopy = gridCopy;
        this.original = original;
        this.counter = counter;
        this.moveNo = moveNo;
        this.completed = completed;
        this.hints = hints;
        this.moves = moves;
    }
}
