import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;

public class MainWindow {

    private final Stage stage;
    private final DrawingPanel drawingPanel;
    private Timeline searchTimeline;
    private int stepDelay = 50;
    Label nodesLabel;
    Label algorithmLabel;
    Label timeLabel;
    Label statusLabel;

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.drawingPanel = new DrawingPanel();
        SetupUI();
    }

    private void SetupUI() {

        MenuBar menuBar = new MenuBar();
        FileChooser chooser = new FileChooser();
        Menu fileMenu = new Menu("File");

        MenuItem newMap = new MenuItem("New");
        newMap.setOnAction(e ->{
            drawingPanel.NewGrid();
        });

        MenuItem loadMap = new MenuItem("Open");
        loadMap.setOnAction(e -> {
            File file = chooser.showOpenDialog(stage);
            Grid newGrid = MapIO.load(file);
            drawingPanel.grid = newGrid;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0, 0, drawingPanel.grid.GetWidth() - 1, drawingPanel.grid.GetHeight() - 1);
            drawingPanel.draw();
        });

        MenuItem saveMap = new MenuItem("Save");
        saveMap.setOnAction(e -> {
            chooser.setTitle("Save Map");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );

            chooser.setInitialFileName("newMap.txt");

            File file = chooser.showSaveDialog(Main.mainStage);

            if (file == null)
                return;

            MapIO.save(file, drawingPanel.grid);
        });

        MenuItem close = new MenuItem("Close");
        close.setOnAction(e ->{
            stage.close();
        });
        fileMenu.getItems().addAll(newMap, loadMap, saveMap, close);

        Menu viewMenu = new Menu("View");
        MenuItem randomize = new MenuItem("Randomize Tile State");
        randomize.setOnAction(e -> {
            drawingPanel.RandomizeTileState();
        });

        MenuItem randomizeWeights = new MenuItem("Randomize Tile Weights");
        randomizeWeights.setOnAction(e -> {
            drawingPanel.RandomizeTileWeights();
        });

        MenuItem reset = new MenuItem("Reset Grid");
        reset.setOnAction(e -> {
            drawingPanel.ResetGrid();
        });

        viewMenu.getItems().addAll(randomize, randomizeWeights, reset);

        Menu gridMenu = new Menu("Grid");
        MenuItem squareGrid_4_DIRS = new MenuItem("Square Grid 4 Directions");
        squareGrid_4_DIRS.setOnAction(e -> {
            drawingPanel.pathSearch.DIRS = PathSearch.Directions.SQUARE_FOUR_DIR;
            drawingPanel.tileShape = DrawingPanel.TileShape.SQUARE;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0, 0, drawingPanel.getGridWidth() - 1, drawingPanel.getGridHeight() - 1);
            ResetGrid();
        });

        MenuItem squareGrid_8_DIRS = new MenuItem("Square Grid 8 Directions");
        squareGrid_8_DIRS.setOnAction(e -> {
            drawingPanel.pathSearch.DIRS = PathSearch.Directions.SQUARE_EIGHT_DIR;
            drawingPanel.tileShape = DrawingPanel.TileShape.SQUARE;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0, 0, drawingPanel.getGridWidth() - 1, drawingPanel.getGridHeight() - 1);
            ResetGrid();
        });

        MenuItem hexGrid_6_DIRS = new MenuItem("Hexagon Grid 6 Directions");
        hexGrid_6_DIRS.setOnAction(e -> {
            drawingPanel.pathSearch.DIRS = PathSearch.Directions.HEX_SIX_DIR;
            drawingPanel.tileShape = DrawingPanel.TileShape.HEXAGON;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0, 0, drawingPanel.getGridWidth() - 1, drawingPanel.getGridHeight() - 1);
            ResetGrid();
        });

        MenuItem triGrid_6_DIRS = new MenuItem("Triangle Grid 12 Directions");
        triGrid_6_DIRS.setOnAction(e -> {
            drawingPanel.pathSearch.DIRS = PathSearch.Directions.TRIANGLE_TWELVE_DIR;
            drawingPanel.tileShape = DrawingPanel.TileShape.TRIANGLE;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0, 0, drawingPanel.getGridWidth() - 1, drawingPanel.getGridHeight() - 1);
            ResetGrid();
        });
        gridMenu.getItems().addAll(squareGrid_4_DIRS, squareGrid_8_DIRS, hexGrid_6_DIRS, triGrid_6_DIRS);

        Menu searchMenu = new Menu("Search Method");
        MenuItem breathFirst = new MenuItem("Breath First");
        breathFirst.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.BFS;
            algorithmLabel.setText("Algorithm: BFS");
            ResetGrid();
        });

        MenuItem depthFirst = new MenuItem("Depth First");
        depthFirst.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.DFS;
            algorithmLabel.setText("Algorithm: DFS");
            ResetGrid();
        });

        MenuItem greedy = new MenuItem("Greedy Best First");
        greedy.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.GREEDY;
            algorithmLabel.setText("Algorithm: Greedy");
            ResetGrid();
        });

        MenuItem uniform = new MenuItem("Uniform Cost");
        uniform.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.UNIFORM;
            algorithmLabel.setText("Algorithm: Uniform-Cost");
            ResetGrid();
        });

        MenuItem AStar = new MenuItem("A* Search");
        AStar.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.ASTAR;
            algorithmLabel.setText("Algorithm: A*");
            ResetGrid();
        });

        searchMenu.getItems().addAll(breathFirst, depthFirst, greedy, uniform, AStar);

        Menu settingsMenu = new Menu("Settings");
        MenuItem openSettingsItem = new MenuItem("Open Settings");
        openSettingsItem.setOnAction(e -> {
            Settings settings = new Settings(drawingPanel, this);
            settings.show();
        });

        settingsMenu.getItems().add(openSettingsItem);
        menuBar.getMenus().addAll(
                fileMenu,
                viewMenu,
                gridMenu,
                searchMenu,
                settingsMenu
        );

        Button runBtn = new Button("Play");
        runBtn.setOnAction(e -> {
            startSearch();
        });

        Button pause = new Button("Pause");
        pause.setOnAction(e ->{
            if(searchTimeline != null){
                searchTimeline.pause();
                statusLabel.setText("Paused...");
            }
        });

        Button stepBtn = new Button("Step");
        stepBtn.setOnAction(e -> {
            stepForward();
        });

        Button fastForwardBtn = new Button("Fast Forward");
        fastForwardBtn.setOnAction(e -> {
            while (true) {
                boolean running = drawingPanel.pathSearch.UpdateStep();
                if (!running) {
                    break;
                }
            }
            drawingPanel.draw();
        });

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(e -> {
            ResetGrid();
        });

        Button showNeighbors = new Button("Show Neighbors");
        showNeighbors.setOnAction(e -> {
            drawingPanel.isShowingNeighbors = !drawingPanel.isShowingNeighbors;
            drawingPanel.draw();
        });

        Button startTileBtn = new Button("Start Tile");
        startTileBtn.setOnAction(e -> {
            drawingPanel.tileSelect = DrawingPanel.TileSelect.START;
        });

        Button goalTileBtn = new Button("Goal Tile");
        goalTileBtn.setOnAction(e -> {
            drawingPanel.tileSelect = DrawingPanel.TileSelect.GOAL;
        });

        ToolBar toolbar = new ToolBar(runBtn, pause, stepBtn, fastForwardBtn, resetBtn, showNeighbors, startTileBtn, goalTileBtn);

        statusLabel = new Label("Ready");
        algorithmLabel = new Label("Algorithm: BFS");
        timeLabel = new Label("Time: 0 ms");
        nodesLabel = new Label("Visited: 0");

        HBox statusBar = new HBox(20); // spacing
        statusBar.getChildren().addAll(statusLabel, algorithmLabel, timeLabel, nodesLabel);

        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, toolbar));
        root.setCenter(drawingPanel);
        root.setBottom(statusBar);
        Scene scene = new Scene(root, 1200, 800);

        stage.setScene(scene);
        stage.setTitle("Path Planner");
    }

    public void show() {
        stage.show();
    }

    private void startSearch() {
        if (searchTimeline != null) {
            searchTimeline.stop();
        }
        long startTime = System.nanoTime();
        statusLabel.setText("Running...");

        searchTimeline = new Timeline(
                new KeyFrame(Duration.millis(stepDelay), e -> {
                    boolean running = drawingPanel.pathSearch.UpdateStep();
                    drawingPanel.draw();
                    nodesLabel.setText("Visited: " + drawingPanel.pathSearch.VisitedNodes.size());
                    long endTime = System.nanoTime();
                    long durationMs = (endTime - startTime) / 1_000_000;
                    timeLabel.setText("Time: " + durationMs + " ms");

                    if (!running) {
                        searchTimeline.stop();
                        endTime = System.nanoTime();
                        durationMs = (endTime - startTime) / 1_000_000;
                        timeLabel.setText("Time: " + durationMs + " ms");
                    }
                })
        );

        searchTimeline.setCycleCount(Animation.INDEFINITE);
        searchTimeline.play();
    }

    private void stepForward() {
        if (drawingPanel.pathSearch == null) return;
        if (drawingPanel.pathSearch.StartTile == null) return;
        if (drawingPanel.pathSearch.GoalTile == null) return;

        boolean running = drawingPanel.pathSearch.UpdateStep();
        drawingPanel.draw();
        statusLabel.setText("Single Iteration");
        nodesLabel.setText("Visited: " + drawingPanel.pathSearch.VisitedNodes.size());

        // Optional: stop timeline if user was stepping manually
        if (!running && searchTimeline != null) {
            searchTimeline.stop();
        }
    }

    private void ResetGrid() {
        // Stop the running search first
        if (searchTimeline != null) {
            searchTimeline.stop();
        }

        // Reset the search
        drawingPanel.pathSearch.ResetSearch();
        statusLabel.setText("Ready");
        timeLabel.setText("Time: 0 ms");
        nodesLabel.setText("Visited: 0");

        // Redraw the panel
        drawingPanel.draw();
    }

    public int getStepDelay() {
        return stepDelay;
    }

    public void setStepDelay(int delay) {
        stepDelay = delay;
    }

    public void stopSearch() {
        if (searchTimeline != null) {
            searchTimeline.stop();
        }
    }

}
