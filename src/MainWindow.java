import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.drawingPanel = new DrawingPanel();
        SetupUI();
    }

    private void SetupUI() {

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem loadMap = new MenuItem("Load Map");
        loadMap.setOnAction(e -> {
            String filePath = "map1.txt";
            File file = new File(filePath);
            Grid newGrid = MapIO.load(file);
            drawingPanel.grid = newGrid;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0, 0, drawingPanel.grid.GetWidth() - 1, drawingPanel.grid.GetHeight() - 1);
            drawingPanel.draw();
        });
        fileMenu.getItems().addAll(loadMap);

        Menu viewMenu = new Menu("View");
        MenuItem randomize = new MenuItem("Randomize Tile State");
        randomize.setOnAction(e ->{
            drawingPanel.RandomizeTileState();
        });

        MenuItem randomizeWeights = new MenuItem("Randomize Tile Weights");
        randomizeWeights.setOnAction(e ->{
            drawingPanel.RandomizeTileWeights();
        });

        MenuItem reset = new MenuItem("Reset Grid");
        reset.setOnAction(e ->{
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
            ResetGrid();
        });

        MenuItem depthFirst = new MenuItem("Depth First");
        depthFirst.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.DFS;
            ResetGrid();
        });

        MenuItem greedy = new MenuItem("Greedy Best First");
        greedy.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.GREEDY;
            ResetGrid();
        });

        MenuItem uniform = new MenuItem("Uniform Cost");
        uniform.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.UNIFORM;
            ResetGrid();
        });

        MenuItem AStar = new MenuItem("A* Search");
        AStar.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.ASTAR;
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

        Button runBtn = new Button("Run");
        runBtn.setOnAction(e -> {
            startSearch();
        });

        Button stepBtn = new Button("Step");
        stepBtn.setOnAction(e -> {
            stepForward();
        });

        Button fastForwardBtn = new Button("Fast Forward");
        fastForwardBtn.setOnAction(e ->{
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

        ToolBar toolbar = new ToolBar(runBtn, stepBtn, fastForwardBtn, resetBtn, showNeighbors, startTileBtn, goalTileBtn);

        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, toolbar));
        root.setCenter(drawingPanel);
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

        searchTimeline = new Timeline(
                new KeyFrame(Duration.millis(stepDelay), e -> {
                    boolean running = drawingPanel.pathSearch.UpdateStep();
                    drawingPanel.draw();

                    if (!running) {
                        searchTimeline.stop();
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
