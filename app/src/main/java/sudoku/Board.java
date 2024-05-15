package sudoku;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class Board {
    private int[][] board;
    private Stack<Move> moveHistory = new Stack<>();
    private List<String> enteredValues = new ArrayList<>();

    public Board() {
        board = new int[9][9];
    }

    private static class Move {
        int row, col, value;

        Move(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    public static Board loadBoard(InputStream in) throws IllegalArgumentException {
        Board board = new Board();
        Scanner scanner = new Scanner(in);
        try {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    board.setCell(row, col, scanner.nextInt());
                }
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Invalid input file");
        } finally {
            scanner.close();
        }
        return board;
    }

    public boolean isLegal(int row, int col, int value) {
        return value >= 1 && value <= 9 && getPossibleValues(row, col).contains(value);
    }

    public void setCell(int row, int col, int value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 1 and 9 (or 0 to reset a value)");
        }
        if (value != 0 && !getPossibleValues(row, col).contains(value)) {
            throw new IllegalArgumentException("Value " + value + " is not possible for this cell");
        }
        moveHistory.push(new Move(row, col, board[row][col]));  // Track move for undo
        if (value != 0) {
            enteredValues.add(String.format("Row: %d, Col: %d, Value: %d", row, col, value));
        }
        board[row][col] = value;
    }

    public void undoMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            if (lastMove.value != 0) {
                enteredValues.remove(enteredValues.size() - 1);
            }
            board[lastMove.row][lastMove.col] = lastMove.value;
        }
    }

    public void clearHistory() {
        moveHistory.clear();
        enteredValues.clear();
    }

    public List<String> getEnteredValues() {
        return new ArrayList<>(enteredValues);
    }

    public String showHint() {
        StringBuilder hints = new StringBuilder();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    Set<Integer> possibleValues = getPossibleValues(row, col);
                    if (possibleValues.size() == 1) {
                        hints.append(String.format("Row: %d, Col: %d, Value: %d\n", row, col, possibleValues.iterator().next()));
                    }
                }
            }
        }
        return hints.toString();
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public boolean hasValue(int row, int col) {
        return getCell(row, col) > 0;
    }

    public Set<Integer> getPossibleValues(int row, int col) {
        Set<Integer> possibleValues = new HashSet<>();
        for (int i = 1; i <= 9; i++) {
            possibleValues.add(i);
        }
        // check the row
        for (int c = 0; c < 9; c++) {
            possibleValues.remove(getCell(row, c));
        }
        // check the column
        for (int r = 0; r < 9; r++) {
            possibleValues.remove(getCell(r, col));
        }
        // check the 3x3 square
        int startRow = row / 3 * 3;
        int startCol = col / 3 * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                possibleValues.remove(getCell(r, c));
            }
        }
        return possibleValues;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                sb.append(getCell(row, col));
                if (col < 8) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
