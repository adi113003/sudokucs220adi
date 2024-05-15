package sudoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Sudoku extends Application {
    private Board board = new Board();
    public static final int SIZE = 9;
    private VBox root;
    private TextField[][] textFields = new TextField[SIZE][SIZE];
    private int width = 800;
    private int height = 800;
    private boolean updatingBoard = false;
    private Timer timer;
    private Label timerLabel;
    private int secondsElapsed;

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new VBox();

        timerLabel = new Label("Time: 00:00");
        root.getChildren().add(timerLabel);
        startTimer();

        root.getChildren().add(createMenuBar(primaryStage));

        GridPane gridPane = new GridPane();
        root.getChildren().add(gridPane);
        gridPane.getStyleClass().add("grid-pane");

        // create a 9x9 grid of text fields
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                textFields[row][col] = new TextField();
                TextField textField = textFields[row][col];

                textField.setId(row + "-" + col);
                gridPane.add(textField, col, row);

                if (row % 3 == 2 && col % 3 == 2) {
                    textField.getStyleClass().add("bottom-right-border");
                } else if (col % 3 == 2) {
                    textField.getStyleClass().add("right-border");
                } else if (row % 3 == 2) {
                    textField.getStyleClass().add("bottom-border");
                }

                textField.setOnMouseClicked(event -> {
                    if (textField.getStyleClass().contains("text-field-selected")) {
                        textField.getStyleClass().remove("text-field-selected");
                    } else {
                        textField.getStyleClass().add("text-field-selected");
                    }
                });

                textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        textField.getStyleClass().remove("text-field-selected");
                    }
                });

                textField.setOnContextMenuRequested(event -> {
                    String id = textField.getId();
                    String[] parts = id.split("-");
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    String possibleValues = board.getPossibleValues(r, c).toString();

                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Possible Values");
                    alert.setHeaderText(String.format("Possible values for cell [%d, %d]:", r, c));
                    alert.setContentText(possibleValues.isEmpty() ? "None" : possibleValues);
                    alert.showAndWait();
                });

                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (updatingBoard) return;
                    if (!newValue.matches("[1-9]?")) {
                        textField.setText(oldValue);
                    }
                    String id = textField.getId();
                    String[] parts = id.split("-");
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);

                    if (newValue.length() > 0) {
                        try {
                            System.out.printf("Setting cell %d, %d to %s\n", r, c, newValue);
                            int value = Integer.parseInt(newValue);
                            if (board.isLegal(r, c, value)) {
                                textField.getStyleClass().add("text-field-valid");
                                textField.getStyleClass().remove("text-field-invalid");
                            } else {
                                textField.getStyleClass().add("text-field-invalid");
                                textField.getStyleClass().remove("text-field-valid");
                            }
                            board.setCell(r, c, value);
                            textField.getStyleClass().remove("text-field-selected");
                        } catch (NumberFormatException e) {
                            // ignore
                        } catch (IllegalArgumentException e) {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Invalid Value");
                            alert.setHeaderText(null);
                            alert.setContentText(e.getMessage());
                            alert.showAndWait();
                        }
                    } else {
                        textField.getStyleClass().remove("text-field-valid");
                        textField.getStyleClass().remove("text-field-invalid");
                        board.setCell(r, c, 0);
                    }
                });
            }
        }

        root.setOnKeyPressed(event -> {
            System.out.println("Key pressed: " + event.getCode());
            switch (event.getCode()) {
                case ESCAPE:
                    for (int row = 0; row < SIZE; row++) {
                        for (int col = 0; col < SIZE; col++) {
                            TextField textField = textFields[row][col];
                            textField.getStyleClass().remove("text-field-selected");
                        }
                    }
                    break;
                default:
                    System.out.println("You typed key: " + event.getCode());
                    break;
            }
        });

        Scene scene = new Scene(root, width, height);

        URL styleURL = getClass().getResource("/style.css");
        String stylesheet = styleURL.toExternalForm();
        scene.getStylesheets().add(stylesheet);
        primaryStage.setTitle("Sudoku");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("onCloseRequest");
            stopTimer();
        });
    }

    private void updateBoard() {
        updatingBoard = true;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                TextField textField = textFields[row][col];
                int value = board.getCell(row, col);
                if (value > 0) {
                    textField.setText(Integer.toString(value));
                    if (board.isLegal(row, col, value)) {
                        textField.getStyleClass().add("text-field-valid");
                        textField.getStyleClass().remove("text-field-invalid");
                    } else {
                        textField.getStyleClass().add("text-field-invalid");
                        textField.getStyleClass().remove("text-field-valid");
                    }
                } else {
                    textField.setText("");
                    textField.getStyleClass().remove("text-field-valid");
                    textField.getStyleClass().remove("text-field-invalid");
                }
            }
        }
        updatingBoard = false;
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menubar");

        Menu fileMenu = new Menu("File");

        addMenuItem(fileMenu, "Load from file", () -> {
            System.out.println("Load from file");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("../puzzles"));
            File sudokuFile = fileChooser.showOpenDialog(primaryStage);
            if (sudokuFile != null) {
                System.out.println("Selected file: " + sudokuFile.getName());

                try {
                    board = Board.loadBoard(new FileInputStream(sudokuFile));
                    updateBoard();
                } catch (Exception e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Unable to load sudoku board from file " + sudokuFile.getName());
                    alert.setHeaderText(e.getMessage());
                    alert.setContentText(e.getMessage());
                    e.printStackTrace();
                    if (e.getCause() != null) e.getCause().printStackTrace();

                    alert.showAndWait();
                }
            }
        });

        addMenuItem(fileMenu, "Save to text", () -> {
            System.out.println("Save puzzle to text");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("../puzzles"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                System.out.println("Selected file: " + file.getName());
                try {
                    if (file.exists()) {
                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("File already exists");
                        alert.setHeaderText("Do you want to overwrite the existing file?");
                        alert.setContentText("The file " + file.getName() + " already exists. Do you want to overwrite it?");
                        alert.showAndWait();
                        if (alert.getResult().getButtonData().isCancelButton()) {
                            return;
                        }
                    }

                    writeToFile(file, board.toString());
                } catch (Exception e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Unable to save to file");
                    alert.setHeaderText("Unsaved changes detected!");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });

        addMenuItem(fileMenu, "Print Board", () -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Board");
            alert.setHeaderText(null);
            alert.setContentText(board.toString());
            alert.showAndWait();
        });

        fileMenu.getItems().add(new SeparatorMenuItem());

        addMenuItem(fileMenu, "Exit", () -> {
            System.out.println("Exit");
            primaryStage.close();
        });

        menuBar.getMenus().add(fileMenu);

        Menu editMenu = new Menu("Edit");

        addMenuItem(editMenu, "Undo", () -> {
            System.out.println("Undo");
            board.undoMove();
            updateBoard();
        });

        addMenuItem(editMenu, "Show values entered", () -> {
            System.out.println("Show all the values we've entered since we loaded the board");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Entered Values");
            alert.setHeaderText(null);
            alert.setContentText(String.join("\n", board.getEnteredValues()));
            alert.showAndWait();
        });

        menuBar.getMenus().add(editMenu);

        Menu hintMenu = new Menu("Hints");

        addMenuItem(hintMenu, "Show hint", () -> {
            System.out.println("Show hint");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Hints");
            alert.setHeaderText(null);
            alert.setContentText(board.showHint());
            alert.showAndWait();
        });

        menuBar.getMenus().add(hintMenu);

        return menuBar;
    }

    private static void writeToFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

    private void addMenuItem(Menu menu, String name, Runnable action) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(event -> action.run());
        menu.getItems().add(menuItem);
    }

    private void startTimer() {
        timer = new Timer(true);
        secondsElapsed = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    secondsElapsed++;
                    int minutes = secondsElapsed / 60;
                    int seconds = secondsElapsed % 60;
                    timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
