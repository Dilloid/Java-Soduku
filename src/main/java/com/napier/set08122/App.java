package com.napier.set08122;

import java.util.*;

public class App
{
    /* Example Grid
        A   B   C     D   E   F     G   H   I
      ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐
    1 | X | X | X | | X | X | X | | X | X | X |
    2 | X | X | X | | X | X | X | | X | X | X |
    3 | X | X | X | | X | X | X | | X | X | X |
      └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘
      ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐
    4 | X | X | X | | X | X | X | | X | X | X |
    5 | X | X | X | | X | X | X | | X | X | X |
    6 | X | X | X | | X | X | X | | X | X | X |
      └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘
      ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐
    7 | X | X | X | | X | X | X | | X | X | X |
    8 | X | X | X | | X | X | X | | X | X | X |
    9 | X | X | X | | X | X | X | | X | X | X |
      └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘
    */

    public static String[] instructions = new String[]{
            "   Welcome to Dilloid's Sudoku!",
            "   ",
            "   Each space in the grid is represented by a",
            "   code consisting of one letter and one number.",
            "   For example, the square in the centre of the",
            "   grid is called \u001B[36mE5\u001B[0m. Use these codes when",
            "   typing commands in order to choose which square",
            "   you are interacting with. Type \u001B[32mcheck\u001B[0m to see",
            "   if your solution is correct.",
            "   ",
            "   Examples:",
            "   \"\u001B[32mset\u001B[0m \u001B[36mB4\u001B[0m \u001B[33m7\u001B[0m\"" +
                    " - Fill square \u001B[36mB4\u001B[0m with the number \u001B[33m7\u001B[0m.",
            "   \"\u001B[32mclear\u001B[0m \u001B[36mF9\u001B[0m\"" +
                    " - Delete the number in square \u001B[36mF9\u001B[0m.",
    };

    public static int counter = 0, moveNo = 0;
    public static int[][] grid, gridCopy, original;
    public static List<int[]> hints, moves;

    public static void main(String[] args)
    {
        grid = new int[9][9];

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                grid[row][col] = 0;
            }
        }

        generateSolution(grid);
        original = grid;

        removeNumbers();
        hints = getFilledSquares(grid);
        moves = new ArrayList<int[]>();

        printGrid();

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.print("\nMore Commands:" +
                             "\n\u001B[32mundo\u001B[0m - Undo the last move." +
                             "\n\u001B[32mredo\u001B[0m - Redo the move you last undid." +
                             "\n\u001B[32mexit\u001B[0m - Exit the game.\n");

            System.out.print("\nPlease input a command:\n\u001B[32m> ");
            String[] input = scanner.nextLine().split(" ");

            if (input[0].equalsIgnoreCase("check"))
            {
                if (testPuzzle(grid))
                {
                    System.out.print("\nCongratulations, you win!\n");
                    break;
                }
            }
            else if (input[0].equalsIgnoreCase("set"))
            {
                int col = charToInt(input[1].charAt(0));
                int row = Integer.parseInt(String.valueOf(input[1].charAt(1))) - 1;

                if (col != -1 && row >= 0 && row <= 8 && !isHint(row, col))
                {
                    if (input[2] != null && input[2].matches("^[0-9]+$"))
                    {
                        int newValue = Integer.parseInt(input[2]);

                        if (newValue >= 1 && newValue <= 9) {
                            if (moves.size() > moveNo) {
                                for (int i = moves.size() - 1; i >= moveNo; i--)
                                    moves.remove(i);
                            }

                            int lastValue = grid[row][col];
                            grid[row][col] = newValue;
                            moves.add(new int[]{row, col, newValue, lastValue});
                            moveNo++;
                        }
                    }
                }
            }
            else if (input[0].equalsIgnoreCase("clear"))
            {
                int col = charToInt(input[1].charAt(0));
                int row = Integer.parseInt(String.valueOf(input[1].charAt(1))) - 1;

                if (col != -1 && row >= 0 && row <= 8 && !isHint(row, col))
                {
                    if (moves.size() > moveNo)
                    {
                        for (int i = moves.size()-1; i >= moveNo; i--)
                            moves.remove(i);
                    }

                    int lastValue = grid[row][col];
                    grid[row][col] = 0;
                    moves.add(new int[]{row, col, 0, lastValue});
                    moveNo++;
                }
            }
            else if (input[0].equalsIgnoreCase("undo"))
            {
                if (moveNo > 0)
                {
                    int[] lastMove = moves.get(moveNo - 1);
                    int row = lastMove[0];
                    int col = lastMove[1];
                    grid[row][col] = lastMove[3];
                    moveNo--;
                }
            }
            else if (input[0].equalsIgnoreCase("redo"))
            {
                if (moves.size() > moveNo)
                {
                    int[] redoMove = moves.get(moveNo);
                    int row = redoMove[0];
                    int col = redoMove[1];
                    grid[row][col] = redoMove[2];
                    moveNo++;
                }
            }
            else if (input[0].equalsIgnoreCase("exit"))
            {
                System.exit(0);
            }

            printGrid();
        }
    }
    private static void printGrid()
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.print("\n\u001B[36m    A   B   C     D   E   F     G   H   I\u001B[0m");
        System.out.print("\n  ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐");


        int ins = 0;
        for (int row = 0; row < 9; row++)
        {
            System.out.print("\n\u001B[36m" + (row + 1) + "\u001B[0m");

            for (int col = 0; col < 9; col++)
            {
                if (grid[row][col] == 0)
                    System.out.print(" |  ");
                else if (isHint(row, col))
                    System.out.print(" | \u001B[33m" + grid[row][col] + "\u001B[0m");
                else
                    System.out.print(" | " + grid[row][col]);

                if (col == 2 || col == 5)
                {
                    System.out.print(" |");
                }
            }

            System.out.print(" |" + instructions[ins++]);

            if (row == 2 || row == 5)
            {
                System.out.print("\n");
                System.out.println("  └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘" + instructions[ins++]);
                System.out.print("  ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐" + instructions[ins++]);
            }
        }

        System.out.print("\n  └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘\n");
    }

    private static boolean testPuzzle(int[][] grid)
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int n = grid[row][col];
                grid[row][col] = 0;

                if (!validLocation(grid, row, col, n))
                    return false;
                else
                    grid[row][col] = n;
            }
        }

        return true;
    }

    private static boolean generateSolution(int[][] grid)
    {
        int row = 0, col = 0;
        List<Integer> numList = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        for (int i = 0; i < 81; i++)
        {
            row = i / 9;
            col = i % 9;

            if (grid[row][col] == 0)
            {
                Collections.shuffle(numList);

                for (int n : numList)
                {
                    if (validLocation(grid, row, col, n))
                    {
                        grid[row][col] = n;

                        int[] nextEmpty = findEmptySquare(grid);
                        if (nextEmpty[0] == -1 && nextEmpty[1] == -1)
                            return true;
                        else if (generateSolution(grid))
                            return true;
                    }
                }

                break;
            }
        }

        grid[row][col] = 0;
        return false;
    }

    private static boolean solvePuzzle(int[][] grid)
    {
        int row = 0, col = 0;

        for (int i = 0; i < 81; i++)
        {
            row = i / 9;
            col = i % 9;

            if (grid[row][col] == 0)
            {
                for (int n = 1; n < 10; n++)
                {
                    if (validLocation(grid, row, col, n))
                    {
                        grid[row][col] = n;

                        int[] nextEmpty = findEmptySquare(grid);
                        if (nextEmpty[0] == -1 && nextEmpty[1] == -1)
                        {
                            counter++;
                            break;
                        }
                        else if (solvePuzzle(grid))
                            return true;
                    }
                }

                break;
            }
        }

        grid[row][col] = 0;
        return false;
    }

    private static void removeNumbers()
    {
        List<int[]> filledSquares = getFilledSquares(grid);
        int filledCount = filledSquares.size();
        int rounds = 12;

        while (rounds > 0 && filledCount >= 17)
        {
            int[] filledSquare = filledSquares.get(0);
            int row = filledSquare[0];
            int col = filledSquare[1];

            filledSquares.remove(0);
            filledCount--;

            int removedSquare = grid[row][col];

            grid[row][col] = 0;
            gridCopy = grid;
            counter = 0;

            solvePuzzle(gridCopy);

            if (counter != 1)
            {
                grid[row][col] = removedSquare;
                filledCount++;
                rounds--;
            }
        }

        return;
    }

    private static List<int[]> getFilledSquares(int[][] grid)
    {
        List<int[]> filled = new ArrayList<>();

        for (int i = 0; i < 81; i++)
        {
            int row = i / 9;
            int col = i % 9;

            if (grid[row][col] != 0)
            {
                int[] s = {row, col};
                filled.add(s);
            }
        }

        Collections.shuffle(filled);
        return filled;
    }

    private static int[] findEmptySquare(int[][] grid)
    {
        int[] r = {-1, -1};

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                if (grid[i][j] == 0)
                {
                    r[0] = i;
                    r[1] = j;

                    return r;
                }

        return r;
    }

    private static boolean isHint(int row, int col)
    {
        for (int[] h : hints)
            if (h[0] == row && h[1] == col)
                return true;

        return false;
    }

    private static boolean validLocation(int[][] grid, int row, int col, int n)
    {
        if (existsInRow(grid, row, n))
            return false;
        else if (existsInColumn(grid, col, n))
            return false;
        else if (existsInSubgrid(grid, row, col, n))
            return false;

        return true;
    }

    private static boolean existsInRow(int[][] grid, int row, int n)
    {
        for (int i = 0; i < 9; i++)
            if (grid[row][i] == n)
                return true;

        return false;
    }

    private static boolean existsInColumn(int[][] grid, int col, int n)
    {
        for (int i = 0; i < 9; i++)
                if (grid[i][col] == n)
                    return true;

        return false;
    }

    private static boolean existsInSubgrid(int[][] grid, int row, int col, int n)
    {
        int subRow = (row / 3) * 3;
        int subCol = (col / 3) * 3;

		for (int i = subRow; i < subRow + 3; i++)
            for (int j = subCol; j < subCol + 3; j++)
                if (grid[i][j] == n)
                    return true;

		return false;
    }

    private static int charToInt(char c)
    {
        switch (c)
        {
            case 'A':
            case 'a':
                return 0;
            case 'B':
            case 'b':
                return 1;
            case 'C':
            case 'c':
                return 2;
            case 'D':
            case 'd':
                return 3;
            case 'E':
            case 'e':
                return 4;
            case 'F':
            case 'f':
                return 5;
            case 'G':
            case 'g':
                return 6;
            case 'H':
            case 'h':
                return 7;
            case 'I':
            case 'i':
                return 8;
        }

        return -1;
    }
}
