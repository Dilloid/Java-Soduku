package com.napier.set08122;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import com.sun.jna.Function;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;

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

    private static final String[] INSTRUCTIONS = new String[] {
            "   \u001B[1;35mWelcome to Dilloid's Sudoku!\u001B[0m",
            "   ",
            "   Each space in the grid is represented by a",
            "   code consisting of one letter and one number.",
            "   For example, the square in the centre of the",
            "   grid is called \u001B[36mE5\u001B[0m. Use these codes when",
            "   typing commands in order to choose which box",
            "   you are interacting with. Type \u001B[32mhelp\u001B[0m to see",
            "   the full list of available commands.",
            "   ",
            "   Examples:",
            "   \"\u001B[32mset\u001B[0m \u001B[36mB4\u001B[0m \u001B[33m7\u001B[0m\"" +
                    " - Fill square \u001B[36mB4\u001B[0m with the number \u001B[33m7\u001B[0m.",
            "   \"\u001B[32mclear\u001B[0m \u001B[36mF9\u001B[0m\"" +
                    " - Delete the number in square \u001B[36mF9\u001B[0m.",
    };

    private static final String[] HELPTEXT = new String[] {
            "   \u001B[32mnew\u001B[0m \u001B[31m<easy|normal>\u001B[0m - Start a new game with the selected difficulty.",
            "   \u001B[32msave\u001B[0m \u001B[31m<filename>\u001B[0m - Save the current game with your chosen filename.",
            "   \u001B[32mload\u001B[0m \u001B[31m<filename>\u001B[0m - Load the game saved under the provided filename.",
            "   ",
            "   \u001B[32mset\u001B[0m \u001B[36m<box>\u001B[0m \u001B[33m<value>\u001B[0m - Fill box <box> with the number <value>.",
            "   \u001B[32mclear\u001B[0m \u001B[36m<box>\u001B[0m - Delete the number in box <box>.",
            "   \u001B[32mundo\u001B[0m - Undo the last move.",
            "   \u001B[32mredo\u001B[0m - Redo the move you last undid.",
            "   \u001B[32mcheck\u001B[0m - Check if your solution is correct.",
            "   ",
            "   \u001B[32mhelp\u001B[0m - Show this information.",
            "   \u001B[32mins\u001B[0m - Show the instructions again.",
            "   \u001B[32mexit\u001B[0m - Exit the game."
    };

    private static String[] display = INSTRUCTIONS;
    private static String jarPath, savePath, msg = "";
    private static boolean completed;
    private static int counter, moveNo;
    private static int[][] grid, gridCopy, original;
    private static List<int[]> hints, moves;

    public static void main(String[] args)
    {
        enableWindows10AnsiSupport();
        createSaveDir();

        newGame(16);
        printGrid();

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.print(msg);
            msg = "";
            System.out.print("\n Please input a command:\n \u001B[32m> ");
            String[] input = scanner.nextLine().split(" ");

            if (input.length > 0)
            {
                if (input[0].equalsIgnoreCase("check"))
                {
                    if (testPuzzle(grid))
                    {
                        completed = true;
                        msg = "\n \u001B[35mCongratulations, you win!" +
                                "\n Use \u001B[32msave \u001B[31m<filename> \u001B[35mto save this game," +
                                "\n or use \u001B[32mnew \u001B[31m<easy|medium> \u001B[35mto start a new game.\u001B[0m\n";
                    }
                    else
                    {
                        msg = "\n \u001B[31mSorry, this solution is incorrect :(\u001B[0m\n";
                    }
                }
                else if (input[0].equalsIgnoreCase("set"))
                {
                    if (input.length > 1)
                    {
                        if (input[1].length() == 2)
                        {
                            int col = charToInt(input[1].charAt(0));
                            int row = Integer.parseInt(String.valueOf(input[1].charAt(1))) - 1;

                            if (col != -1 && row >= 0 && row <= 8 && !isHint(row, col))
                            {
                                if (input.length > 2)
                                {
                                    if (input[2].matches("^[0-9]+$"))
                                    {
                                        int newValue = Integer.parseInt(input[2]);

                                        if (newValue >= 1 && newValue <= 9)
                                        {
                                            if (moves.size() > moveNo)
                                            {
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
                        }
                        else
                        {
                            msg = "\n \u001B[31mInvalid box code!\u001B[0m\n";
                        }
                    }
                    else
                    {
                        msg = "\n \u001B[31mNot enough arguments!\u001B[0m\n";
                    }
                }
                else if (input[0].equalsIgnoreCase("clear"))
                {
                    if (input.length > 1)
                    {
                        if (input[1].length() == 2)
                        {
                            int col = charToInt(input[1].charAt(0));
                            int row = Integer.parseInt(String.valueOf(input[1].charAt(1))) - 1;

                            if (col != -1 && row >= 0 && row <= 8 && !isHint(row, col))
                            {
                                if (moves.size() > moveNo)
                                {
                                    for (int i = moves.size() - 1; i >= moveNo; i--)
                                        moves.remove(i);
                                }

                                int lastValue = grid[row][col];
                                grid[row][col] = 0;
                                moves.add(new int[]{row, col, 0, lastValue});
                                moveNo++;
                            }
                        }
                        else
                        {
                            msg = "\n \u001B[31mInvalid box code!\u001B[0m\n";
                        }
                    }
                    else
                    {
                        msg = "\n \u001B[31mNot enough arguments!\u001B[0m\n";
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
                else if (input[0].equalsIgnoreCase("new"))
                {
                    if (input.length > 1)
                    {
                        int rounds = 0;

                        if (input[1].equalsIgnoreCase("easy"))
                            rounds = 1;
                        else if (input[1].equalsIgnoreCase("normal"))
                            rounds = 16;
                        else
                            msg = "\n \u001B[31mInvalid difficulty! Choose easy or normal.\u001B[0m\n";

                        if (rounds > 0)
                            newGame(rounds);
                    }
                    else
                    {
                        msg = "\n \u001B[31mNot enough arguments!\u001B[0m\n";
                    }
                }
                else if (input[0].equalsIgnoreCase("save"))
                {
                    createSaveDir();

                    if (input.length > 1)
                    {
                        try
                        {
                            GameData save = new GameData(grid, gridCopy, original, counter,
                                                         moveNo, completed, hints, moves);

                            String filePath = savePath + input[1] + ".ser";
                            File saveFile = new File(filePath);
                            saveFile.createNewFile();

                            FileOutputStream fileOut = new FileOutputStream(saveFile, false);
                            ObjectOutputStream out = new ObjectOutputStream(fileOut);
                            out.writeObject(save);
                            out.close();
                            fileOut.close();
                        }
                        catch (Exception e)
                        {
                            msg = "\n \u001B[31mInvalid filename!\u001B[0m\n";
                        }
                    }
                    else
                    {
                        msg = "\n \u001B[31mNot enough arguments!\u001B[0m\n";
                    }
                }
                else if (input[0].equalsIgnoreCase("load"))
                {
                    createSaveDir();

                    if (input.length > 1)
                    {
                        try
                        {
                            GameData load = null;

                            String filePath = savePath + input[1] + ".ser";
                            FileInputStream fileIn = new FileInputStream(filePath);
                            ObjectInputStream in = new ObjectInputStream(fileIn);
                            load = (GameData) in.readObject();
                            in.close();
                            fileIn.close();

                            grid = load.getGrid();
                            gridCopy = load.getGridCopy();
                            original = load.getOriginal();
                            counter = load.getCounter();
                            moveNo = load.getMoveNo();
                            completed = load.getCompleted();
                            hints = load.getHints();
                            moves = load.getMoves();
                        }
                        catch (Exception e)
                        {
                            msg = "\n \u001B[31mInvalid filename!\u001B[0m\n";
                        }
                    }
                    else
                    {
                        msg = "\n \u001B[31mNot enough arguments!\u001B[0m\n";
                    }
                }
                else if (input[0].equalsIgnoreCase("help"))
                {
                    display = HELPTEXT;
                }
                else if (input[0].equalsIgnoreCase("ins"))
                {
                    display = INSTRUCTIONS;
                }
                else if (input[0].equalsIgnoreCase("exit"))
                {
                    System.out.println("\n \u001B[35mThank you for playing!\u001B[0m");
                    System.exit(0);
                }
            }

            printGrid();
        }
    }
    private static void printGrid()
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.print("\n \u001B[36m    A   B   C     D   E   F     G   H   I\u001B[0m");
        System.out.print("\n   ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐");

        int ins = 0;
        for (int row = 0; row < 9; row++)
        {
            System.out.print("\n \u001B[36m" + (row + 1) + "\u001B[0m");

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

            System.out.print(" |" + display[ins++]);

            if (row == 2 || row == 5)
            {
                System.out.print("\n");
                System.out.println("   └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘" + display[ins++]);
                System.out.print("   ┌───┬───┬───┐ ┌───┬───┬───┐ ┌───┬───┬───┐" + display[ins++]);
            }
        }

        System.out.print("\n   └───┴───┴───┘ └───┴───┴───┘ └───┴───┴───┘\n");
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

    private static void newGame(int difficulty)
    {
        grid = new int[9][9];
        gridCopy = new int[9][9];
        original = new int[9][9];
        counter = 0;
        moveNo = 0;
        completed = false;
        hints = new ArrayList<>();
        moves = new ArrayList<>();

        for (int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++)
                grid[row][col] = 0;

        generateSolution(grid);
        original = grid;

        removeNumbers(difficulty);
        hints = getFilledSquares(grid);
        moves = new ArrayList<int[]>();
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

    private static void removeNumbers(int totalRounds)
    {
        List<int[]> filledSquares = getFilledSquares(grid);
        int filledCount = filledSquares.size();
        int rounds = totalRounds;

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

    public static void createSaveDir()
    {
        try
        {
            jarPath = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            savePath = jarPath.replaceAll(jarPath.substring(jarPath.lastIndexOf("/") + 1), "") + "saves/";

            File saveDir = new File(savePath);
            if (!saveDir.exists())
                saveDir.mkdir();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /* Windows 10 supports Ansi codes. However, it's still experimental and not enabled by default.
     * This method enables the necessary Windows 10 feature.
     *
     * More info: https://stackoverflow.com/a/51681675/675577
     * Code source: https://stackoverflow.com/a/52767586/675577
     * Reported issue: https://github.com/PowerShell/PowerShell/issues/11449#issuecomment-569531747
     */
    private static void enableWindows10AnsiSupport() {
        Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
        DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
        HANDLE hOut = (HANDLE) GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

        DWORDByReference p_dwMode = new DWORDByReference(new DWORD(0));
        Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
        GetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, p_dwMode});

        int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
        DWORD dwMode = p_dwMode.getValue();
        dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
        Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
        SetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, dwMode});
    }
}
