import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Map;

public class DrawingPanel extends StackPane {
    public final Canvas canvas;
    private final GraphicsContext gc;
    private int gridSize = 20;
    Grid grid;
    double tileWidth;
    double tileHeight;
    GridColors colors = new GridColors();

    public static class GridColors {
        public Color walkable = Color.WHITE;
        public Color blocked  = Color.DARKRED;
        public Color start    = Color.GREEN;
        public Color goal     = Color.RED;
        public Color openSet  = Color.LIGHTBLUE;
        public Color closedSet = Color.DARKBLUE;
        public Color path     = Color.LIGHTGREEN;
    }


    public PathSearch pathSearch;

    public DrawingPanel() {
        canvas = new Canvas();

        gc = canvas.getGraphicsContext2D();
        grid = new Grid(gridSize);
        // THIS LINE IS CRITICAL
        getChildren().add(canvas);

        // Resize canvas with panel
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        pathSearch = new PathSearch();
        pathSearch.Initialize(grid);
        pathSearch.Enter(0, 0, gridSize - 1, gridSize - 1);

        // Redraw on resize
        canvas.widthProperty().addListener((obs, o, n) -> draw());
        canvas.heightProperty().addListener((obs, o, n) -> draw());

        setupMouseHandlers();

        draw();
    }

    public void draw() {
        updateLayout();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawGrid();
        //drawNeighborLines();

    }

    private void drawHexagon(double centerX, double centerY, double radius) {
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }

        gc.setFill(Color.ORANGE);
        gc.fillPolygon(xPoints, yPoints, 6);

        gc.setStroke(Color.BLACK);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    private void DrawGrid() {
        updateLayout();
        gc.setStroke(Color.BLACK);
        for (int x = 0; x < grid.GetSize(); x++) {
            for (int y = 0; y < grid.GetSize(); y++) {
                Tile tile = grid.GetTile(x, y);

                double px = x * tileWidth;
                double py = y * tileHeight;

                if (!tile.walkable) {
                    gc.setFill(colors.blocked);
                } else if (tile.isStart) {
                    gc.setFill(colors.start);
                } else if (tile.isGoal) {
                    gc.setFill(colors.goal);
                } else if (tile.inFinalPath) {
                    gc.setFill(colors.path);
                } else if (tile.inClosedSet) {
                    gc.setFill(colors.closedSet);
                } else if (tile.inOpenSet) {
                    gc.setFill(colors.openSet);
                } else {
                    gc.setFill(colors.walkable);
                }

                gc.fillRect(px, py, tileWidth, tileHeight);
                gc.strokeRect(px, py, tileWidth, tileHeight);
            }
        }
    }

    private void drawLineBetweenTiles(Tile a, Tile b) {
        double tileW = canvas.getWidth() / gridSize;
        double tileH = canvas.getHeight() / gridSize;

        double x1 = a.colum * tileW + tileW / 2;
        double y1 = a.row * tileH + tileH / 2;

        double x2 = b.colum * tileW + tileW / 2;
        double y2 = b.row * tileH + tileH / 2;

        gc.setStroke(Color.RED);       // line color
        gc.setLineWidth(2);            // line thickness
        gc.strokeLine(x1, y1, x2, y2);
    }

    private void drawNeighborLines() {
        for (Map.Entry<Tile, PathSearch.SearchNode> entry : pathSearch.Nodes.entrySet()) {
            Tile tile = entry.getKey();
            PathSearch.SearchNode node = entry.getValue();
            for (PathSearch.SearchNode neighbor : node.neighbors) {
                Tile nTile = neighbor.tile;
                drawLineBetweenTiles(tile, nTile);
            }
        }
    }

    public Canvas GetCanvas() {
        return canvas;
    }

    private void setupMouseHandlers() {
        canvas.setOnMouseClicked(e -> {
            System.out.println("Clicked at: " + e.getX() + ", " + e.getY());
            int x = (int) (e.getX() / tileWidth);
            int y = (int) (e.getY() / tileHeight);

            Tile tile = grid.GetTile(x, y);
            if (tile != null) {
                tile.walkable = !tile.walkable;
                draw();
            }
        });
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int size) {
        gridSize = size;
        rebuildGrid();
    }

    private void rebuildGrid() {
        grid = new Grid(gridSize);
        pathSearch.ResetSearch();
        pathSearch.Initialize(grid);
        pathSearch.Enter(0, 0, gridSize - 1, gridSize - 1);

        draw();
    }

    private void updateLayout() {
        tileWidth = canvas.getWidth() / gridSize;
        tileHeight = canvas.getHeight() / gridSize;
    }

}
