import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainWindow {
    private final Stage stage;
    private final DrawingPanel drawingPanel;
    private Timeline searchTimeline;
    private int stepDelay = 50;

    public MainWindow(Stage stage){
        this.stage = stage;
        this.drawingPanel = new DrawingPanel();
        SetupUI();
    }

    private void SetupUI(){

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");
        Menu gridMenu = new Menu("Grid");
        MenuItem squareGrid_4_DIRS = new MenuItem("Square Grid 4 Directions");
        squareGrid_4_DIRS.setOnAction(e ->{
            drawingPanel.pathSearch.DIRS = PathSearch.Directions.SQUARE_FOUR_DIR;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0,0,drawingPanel.getGridSize() - 1, drawingPanel.getGridSize() - 1);
            ResetGrid();
        });

        MenuItem squareGrid_8_DIRS = new MenuItem("Square Grid 8 Directions");
        squareGrid_8_DIRS.setOnAction(e -> {
            drawingPanel.pathSearch.DIRS = PathSearch.Directions.SQUARE_EIGHT_DIR;
            drawingPanel.pathSearch.Initialize(drawingPanel.grid);
            drawingPanel.pathSearch.Enter(0,0,drawingPanel.getGridSize() - 1, drawingPanel.getGridSize() - 1);
            ResetGrid();

        });
        gridMenu.getItems().addAll(squareGrid_4_DIRS, squareGrid_8_DIRS);

        Menu searchMenu = new Menu("Search Method");
        MenuItem breathFirst = new MenuItem("Breath First");
        breathFirst.setOnAction(e ->{
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.BFS;
            ResetGrid();
        });

        MenuItem depthFirst = new MenuItem("Depth First");
        depthFirst.setOnAction(e -> {
            drawingPanel.pathSearch.Search = PathSearch.SearchMethod.DFS;
            ResetGrid();
        });

        searchMenu.getItems().addAll(breathFirst, depthFirst);

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
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(e -> {
            ResetGrid();
        });

        ToolBar toolbar = new ToolBar(runBtn, resetBtn);

        BorderPane root = new BorderPane();
        root.setTop(new VBox( menuBar, toolbar));
        root.setCenter(drawingPanel);
        Scene scene = new Scene(root, 800, 800);

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

    private void ResetGrid(){
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
