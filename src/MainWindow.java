import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
	int stepCount = 0;

	private Label nodesLabel = makeStatusLabel("Visited: ", "0");
	private Label algorithmLabel = makeStatusLabel("Algorithm: ", "BFS");
	private Label timeLabel = makeStatusLabel("Time: ", "0 ms");
	private Label statusLabel = new Label("● Ready");
	private Label stepLabel = makeStatusLabel("Steps: ", "0");

	private Button stateBtn;
	private Button weightBtn;

	private final MazeGenerator mazeGen = new MazeGenerator();
	private final FileChooser fileChooser = new FileChooser();

	public MainWindow(Stage stage) {
		this.stage = stage;
		drawingPanel = new DrawingPanel();

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
		BorderPane root = new BorderPane();
		root.getStyleClass().add("scene-root");

		root.setTop(buildTop());
		root.setCenter(drawingPanel);
		root.setBottom(buildStatusBar());

		Scene scene = new Scene(root, 1280, 900);
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
	
		stage.setTitle("Path Planner");
		stage.setScene(scene);
		stage.show();
	}

	private VBox buildTop() {
		return new VBox(buildMenuBar(), buildToolBar());
	}

	private MenuBar buildMenuBar() {
		MenuBar mb = new MenuBar();
		mb.getStyleClass().add("menu-bar");

		mb.getMenus().addAll(buildFileMenu(), buildViewMenu(), buildGridMenu(), buildSearchMenu(), buildSettingsMenu());
		return mb;
	}

	private Menu buildFileMenu() {
		Menu m = new Menu("File");

		MenuItem newItem = new MenuItem("New");
		newItem.setOnAction(e -> drawingPanel.NewGrid());

		MenuItem openItem = new MenuItem("Open");
		openItem.setOnAction(e -> {
			File file = fileChooser.showOpenDialog(stage);
			if (file == null)
				return;
			Grid newGrid = MapIO.load(file);
			drawingPanel.grid = newGrid;
			drawingPanel.pathSearch.Initialize(drawingPanel.grid);
			drawingPanel.pathSearch.Enter(0, 0, drawingPanel.grid.GetWidth() - 1, drawingPanel.grid.GetHeight() - 1);
			drawingPanel.draw();
		});

		MenuItem saveItem = new MenuItem("Save");
		saveItem.setOnAction(e -> {
			fileChooser.setTitle("Save Map");
			fileChooser.setInitialFileName("newMap.txt");
			File file = fileChooser.showSaveDialog(stage);
			if (file == null)
				return;
			MapIO.save(file, drawingPanel.grid);
		});

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.setOnAction(e -> stage.close());

		m.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);
		return m;
	}

	private Menu buildViewMenu() {
		Menu m = new Menu("View");

		MenuItem randomize = new MenuItem("Randomize Tile State");
		randomize.setOnAction(e -> drawingPanel.RandomizeTileState());

		MenuItem randomizeWeights = new MenuItem("Randomize Tile Weights");
		randomizeWeights.setOnAction(e -> drawingPanel.RandomizeTileWeights());

		MenuItem resetGrid = new MenuItem("Reset Grid");
		resetGrid.setOnAction(e -> drawingPanel.ResetGrid());

		MenuItem mazeItem = new MenuItem("Generate Maze");
		mazeItem.setOnAction(e -> generateMaze());

		m.getItems().addAll(randomize, randomizeWeights, resetGrid, new SeparatorMenuItem(), mazeItem);
		return m;
	}

	private Menu buildGridMenu() {
		Menu m = new Menu("Grid");

		MenuItem sq4 = new MenuItem("Square Grid 4 Directions");
		sq4.setOnAction(e -> {
			drawingPanel.pathSearch.DIRS = PathSearch.Directions.SQUARE_FOUR_DIR;
			drawingPanel.tileShape = DrawingPanel.TileShape.SQUARE;
			reinitAndReset();
		});

		MenuItem sq8 = new MenuItem("Square Grid 8 Directions");
		sq8.setOnAction(e -> {
			drawingPanel.pathSearch.DIRS = PathSearch.Directions.SQUARE_EIGHT_DIR;
			drawingPanel.tileShape = DrawingPanel.TileShape.SQUARE;
			reinitAndReset();
		});

		MenuItem hex6 = new MenuItem("Hexagon Grid 6 Directions");
		hex6.setOnAction(e -> {
			drawingPanel.pathSearch.DIRS = PathSearch.Directions.HEX_SIX_DIR;
			drawingPanel.tileShape = DrawingPanel.TileShape.HEXAGON;
			reinitAndReset();
		});

		MenuItem tri12 = new MenuItem("Triangle Grid 12 Directions");
		tri12.setOnAction(e -> {
			drawingPanel.pathSearch.DIRS = PathSearch.Directions.TRIANGLE_TWELVE_DIR;
			drawingPanel.tileShape = DrawingPanel.TileShape.TRIANGLE;
			reinitAndReset();
		});

		MenuItem clearItem = new MenuItem("Clear");
		clearItem.setOnAction(e -> ResetGrid());

		m.getItems().addAll(sq4, sq8, hex6, tri12, new SeparatorMenuItem(), clearItem);
		return m;
	}

	private Menu buildSearchMenu() {
		Menu m = new Menu("Search Method");
		ToggleGroup group = new ToggleGroup();

		addAlgoItem(m, group, "Breadth First", PathSearch.SearchMethod.BFS, "BFS", true);
		addAlgoItem(m, group, "Depth First", PathSearch.SearchMethod.DFS, "DFS", false);
		addAlgoItem(m, group, "Greedy Best First", PathSearch.SearchMethod.GREEDY, "Greedy", false);
		addAlgoItem(m, group, "Uniform Cost", PathSearch.SearchMethod.UNIFORM, "Uniform", false);
		addAlgoItem(m, group, "A* Search", PathSearch.SearchMethod.ASTAR, "A*", false);

		return m;
	}

	private void addAlgoItem(Menu m, ToggleGroup g, String label, PathSearch.SearchMethod method, String displayName,
			boolean selected) {
		RadioMenuItem item = new RadioMenuItem(label);
		item.setToggleGroup(g);
		item.setSelected(selected);
		item.setOnAction(e -> {
			drawingPanel.pathSearch.Search = method;
			algorithmLabel.setText("Algorithm: " + displayName);
			ResetGrid();
		});
		m.getItems().add(item);
	}

	private Menu buildSettingsMenu() {
		Menu m = new Menu("Settings");
		MenuItem openSettings = new MenuItem("Open Settings");
		openSettings.setOnAction(e -> new Settings(drawingPanel, this).show());
		m.getItems().add(openSettings);
		return m;
	}

	// ── Tool bar ─────────────────────────────────────────────
	private ToolBar buildToolBar() {
		ToolBar tb = new ToolBar();
		tb.getStyleClass().add("tool-bar");

		// Playback group
		Button play = accentBtn("▶  Play", "btn-play");
		Button pause = accentBtn("⏸  Pause", "btn-pause");
		Button step = toolBtn("⏭  Step");
		Button ff = toolBtn("⏩  Fast Fwd");
		Button reset = accentBtn("↺  Reset", "btn-reset");

		play.setOnAction(e -> startSearch());
		pause.setOnAction(e -> {
			if (searchTimeline != null)
				searchTimeline.pause();
			statusLabel.setText("⏸ Paused");
		});
		step.setOnAction(e -> stepForward());
		ff.setOnAction(e -> {
			while (drawingPanel.pathSearch.UpdateStep()) {
			}
			drawingPanel.draw();
		});
		reset.setOnAction(e -> ResetGrid());

		// Tool group
		Button showN = toolBtn("Show Neighbors");
		Button startT = toolBtn("Start Tile");
		Button goalT = toolBtn("Goal Tile");
		stateBtn  = toolBtn("Tiles Walkable");
        weightBtn = toolBtn("Remove Weights");

        showN.setOnAction(e -> {
            drawingPanel.isShowingNeighbors = !drawingPanel.isShowingNeighbors;
            drawingPanel.draw();
        });
        startT.setOnAction(e -> drawingPanel.tileSelect = DrawingPanel.TileSelect.START);
        goalT.setOnAction(e  -> drawingPanel.tileSelect = DrawingPanel.TileSelect.GOAL);

        stateBtn.setOnAction(e -> {
            if (drawingPanel.tileSelect == DrawingPanel.TileSelect.STATE) {
                drawingPanel.tileSelect = DrawingPanel.TileSelect.NONE;
                stateBtn.setText("Tiles Walkable");
            } else {
                drawingPanel.tileSelect = DrawingPanel.TileSelect.STATE;
                stateBtn.setText("Tiles Unwalkable");
            }
        });

        weightBtn.setOnAction(e -> {
            if (drawingPanel.tileSelect == DrawingPanel.TileSelect.WEIGHT) {
                drawingPanel.tileSelect = DrawingPanel.TileSelect.NONE;
                weightBtn.setText("Remove Weights");
            } else {
                drawingPanel.tileSelect = DrawingPanel.TileSelect.WEIGHT;
                weightBtn.setText("Add Weights");
            }
        });

		tb.getItems().addAll(play, pause, step, ff, reset, new Separator(), showN, startT, goalT, new Separator(),
				stateBtn, weightBtn);
		return tb;
	}

	private Button toolBtn(String text) {
		Button b = new Button(text);
		b.getStyleClass().add("button");
		return b;
	}

	private Button accentBtn(String text, String styleClass) {
		Button b = new Button(text);
		b.getStyleClass().addAll("button", styleClass);
		return b;
	}

	private HBox buildStatusBar() {
		HBox bar = new HBox();
		bar.getStyleClass().add("status-bar");
		bar.setAlignment(Pos.CENTER_LEFT);

		statusLabel.getStyleClass().addAll("status-label", "status-ready");

		// Spacer pushes status to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		bar.getChildren().addAll(statusLabel, spacer, algorithmLabel, sep(), timeLabel, sep(), nodesLabel, sep(),
				stepLabel);
		return bar;
	}

	private Label sep() {
		Label l = new Label(" | ");
		l.getStyleClass().add("status-label");
		return l;
	}

	private void generateMaze() {
		mazeGen.Start(drawingPanel.grid, 0, 0);
		boolean generating;
		while (true) {
			PathSearch.Directions dirs = drawingPanel.pathSearch.DIRS;
			if (dirs == PathSearch.Directions.SQUARE_FOUR_DIR || dirs == PathSearch.Directions.SQUARE_EIGHT_DIR) {
				generating = mazeGen.MazeStep(drawingPanel.grid, dirs);
			} else if (dirs == PathSearch.Directions.HEX_SIX_DIR) {
				generating = mazeGen.HexStep(drawingPanel.grid);
			} else {
				generating = mazeGen.TriangleStep(drawingPanel.grid);
			}
			if (!generating)
				break;
		}
		mazeGen.ConnectSpecialTile(drawingPanel.pathSearch.StartTile);
		mazeGen.ConnectSpecialTile(drawingPanel.pathSearch.GoalTile);
		drawingPanel.draw();
	}

	private void reinitAndReset() {
		drawingPanel.pathSearch.Initialize(drawingPanel.grid);
		drawingPanel.pathSearch.Enter(0, 0, drawingPanel.getGridWidth() - 1, drawingPanel.getGridHeight() - 1);
		ResetGrid();
	}

	private void startSearch() {
		if (searchTimeline != null) {
			searchTimeline.stop();
		}
		long startTime = System.nanoTime();
		setStatus("● Running", "status-running");

		searchTimeline = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> {
			boolean running = drawingPanel.pathSearch.UpdateStep();
			drawingPanel.draw();
			stepCount++;
			
			nodesLabel.setText("Visited: " + drawingPanel.pathSearch.VisitedNodes.size());
			stepLabel.setText("Setp: " + stepCount);
			long endTime = System.nanoTime();
			long durationMs = (endTime - startTime) / 1_000_000;
			timeLabel.setText("Time: " + durationMs + " ms");

			if (!running) {
				searchTimeline.stop();
				endTime = System.nanoTime();
				durationMs = (endTime - startTime) / 1_000_000;
				timeLabel.setText("Time: " + durationMs + " ms");
				setStatus("● Done", "status-done");
			}
		}));

		searchTimeline.setCycleCount(Animation.INDEFINITE);
		searchTimeline.play();
	}

	private void stepForward() {
		if (drawingPanel.pathSearch == null)
			return;
		if (drawingPanel.pathSearch.StartTile == null)
			return;
		if (drawingPanel.pathSearch.GoalTile == null)
			return;

		boolean running = drawingPanel.pathSearch.UpdateStep();
		drawingPanel.draw();
		stepCount++;
		stepLabel.setText("Steps: " + stepCount);
        nodesLabel.setText("Visited: " + drawingPanel.pathSearch.VisitedNodes.size());
        setStatus("● Step", "status-running");
        
		
		if (!running && searchTimeline != null) {
			searchTimeline.stop();
			setStatus("● Done", "status-done");
		}
	}

	private void ResetGrid() {
		// Stop the running search first
		if (searchTimeline != null) {
			searchTimeline.stop();
		}

		// Reset the search
		drawingPanel.pathSearch.ResetSearch();
		stepLabel.setText("Steps: 0");
        nodesLabel.setText("Visited: 0");
        timeLabel.setText("Time: 0 ms");
        setStatus("● Ready", "status-ready");

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
	
	private void setStatus(String text, String styleClass) {
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("status-ready", "status-running", "status-done");
        statusLabel.getStyleClass().add(styleClass);
    }

	private Label makeStatusLabel(String prefix, String init) {
		Label l = new Label(prefix + init);
		l.getStyleClass().add("status-label");
		return l;
	}

}
