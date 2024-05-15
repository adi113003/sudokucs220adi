# Sudoku using JavaFX

1. loadBoard() method should throw an exception if the file is not a valid sudoku board
1. when saving: check if the file already exists, and ask the user if they want to overwrite it
1. Undo the last move
    * requires a way to store a stack of moves
1. Undo, show values entered: show all the values we've entered since we loaded the board
1. Hint, Show Hint: highlight all cells where only one legal value is possible
1. on right-click handler: show a list of possible values that can go in this square

## Timer Feature:
The timer will start when the application starts and will display the elapsed time on the interface.
## Color Coding Feature:
Cells with valid entries will be colored green, while invalid entries will be colored red.
This will provide immediate visual feedback to the user about the correctness of their input.
## Auto Solver:
Auto Solver Method using backtracking algorithm.
## Statistics
Track and display statistics such as the number of puzzles solved, average time per puzzle, etc.
