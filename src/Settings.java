import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Settings {

    private final Stage stage;


    public Settings(DrawingPanel drawingPanel, MainWindow mainWindow) {
        stage = new Stage();
        stage.setTitle("Settings");
        stage.initModality(Modality.APPLICATION_MODAL);

        // --- Layout ---
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        // ---------------- GRID SIZE ----------------
        Label gridLabel = new Label("Grid Size:");
        Spinner<Integer> gridWidthSpinner =
                new Spinner<>(5, 100, drawingPanel.getGridWidth());
        gridWidthSpinner.setEditable(true);

        Spinner<Integer> gridHeightSpinner =
                new Spinner<>(5, 100, drawingPanel.getGridHeight());
        gridHeightSpinner.setEditable(true);

        // ---------------- SPEED ----------------
        Label speedLabel = new Label("Iteration Speed (ms):");
        Slider speedSlider = new Slider(10, 500, mainWindow.getStepDelay());
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);

        // --- Buttons ---
        ColorPicker walkablePicker = new ColorPicker(drawingPanel.colors.walkable);
        ColorPicker blockedPicker  = new ColorPicker(drawingPanel.colors.blocked);
        ColorPicker openPicker     = new ColorPicker(drawingPanel.colors.openSet);
        ColorPicker closedPicker   = new ColorPicker(drawingPanel.colors.closedSet);
        ColorPicker pathPicker     = new ColorPicker(drawingPanel.colors.path);
        ColorPicker startPicker     = new ColorPicker(drawingPanel.colors.start);
        ColorPicker goalPicker     = new ColorPicker(drawingPanel.colors.goal);

        // ---------------- APPLY BUTTON ----------------
        Button applyBtn = new Button("Apply");
        applyBtn.setOnAction(e -> {

            // Stop search before applying
            mainWindow.stopSearch();

            // Apply grid size
            drawingPanel.setGridSize(gridWidthSpinner.getValue(), gridHeightSpinner.getValue());

            // Apply speed
            mainWindow.setStepDelay((int) speedSlider.getValue());

            // Apply colors
            drawingPanel.colors.walkable = walkablePicker.getValue();
            drawingPanel.colors.blocked  = blockedPicker.getValue();
            drawingPanel.colors.openSet  = openPicker.getValue();
            drawingPanel.colors.closedSet = closedPicker.getValue();
            drawingPanel.colors.path     = pathPicker.getValue();
            drawingPanel.colors.start     = startPicker.getValue();
            drawingPanel.colors.goal     = goalPicker.getValue();

            drawingPanel.draw();
        });

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> stage.close());

        // ---------------- LAYOUT ----------------
        grid.add(gridLabel, 0, 0);
        grid.add(gridWidthSpinner, 1, 0);

        grid.add(speedLabel, 0, 1);
        grid.add(speedSlider, 1, 1);

        grid.add(new Label("Walkable Tile:"), 0, 2);
        grid.add(walkablePicker, 1, 2);

        grid.add(new Label("Blocked Tile:"), 0, 3);
        grid.add(blockedPicker, 1, 3);

        grid.add(new Label("Open Set:"), 0, 4);
        grid.add(openPicker, 1, 4);

        grid.add(new Label("Closed Set:"), 0, 5);
        grid.add(closedPicker, 1, 5);

        grid.add(new Label("Final Path:"), 0, 6);
        grid.add(pathPicker, 1, 6);

        grid.add(new Label("Start Tile:"), 0, 7);
        grid.add(startPicker, 1, 7);

        grid.add(new Label("Goal Tile:"), 0, 8);
        grid.add(goalPicker, 1, 8);

        grid.add(applyBtn, 0, 9);
        grid.add(closeBtn, 1, 9);

        grid.add(new Label("Grid Height"), 0, 10);
        grid.add(gridHeightSpinner, 1, 10);

        Scene scene = new Scene(grid, 400, 400);
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }
}
