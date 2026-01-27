import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Map;

public class DrawingPanel extends StackPane {

    public enum TileShape {
        SQUARE,
        TRIANGLE,
        HEXAGON
    }

    public static class GridColors {
        public Color walkable = Color.WHITE;
        public Color blocked = Color.DARKRED;
        public Color start = Color.GREEN;
        public Color goal = Color.RED;
        public Color openSet = Color.LIGHTBLUE;
        public Color closedSet = Color.DARKBLUE;
        public Color path = Color.LIGHTGREEN;
    }

    public final Canvas canvas;
    private final GraphicsContext gc;
    private int gridWidth = 20;
    private int gridHeight = 20;
    Grid grid;
    double tileWidth;
    double tileHeight;
    GridColors colors = new GridColors();
    public TileShape tileShape = TileShape.HEXAGON;
    public PathSearch pathSearch;

    private double hexRadius;
    private double hexWidth;
    private double hexHeight;
    double gridOffsetX = 0;
    double gridOffsetY = 0;
    private double gridPixelWidth;
    private double gridPixelHeight;


    public DrawingPanel() {
        canvas = new Canvas();

        gc = canvas.getGraphicsContext2D();
        grid = new Grid(gridWidth, gridHeight);
        // THIS LINE IS CRITICAL
        getChildren().add(canvas);

        // Resize canvas with panel
        canvas.widthProperty().bind(
                widthProperty().subtract(insetsProperty().get().getLeft())
        );
        canvas.heightProperty().bind(
                heightProperty().subtract(insetsProperty().get().getTop())
        );
        pathSearch = new PathSearch();
        pathSearch.Initialize(grid);
        pathSearch.Enter(0, 0, gridWidth - 1, gridHeight - 1);

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
        drawNeighborLines();
    }

    private void drawHexagon(double centerX, double centerY, double radius) {
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }

        gc.fillPolygon(xPoints, yPoints, 6);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    private void DrawGrid() {

        updateLayout();
        for (int x = 0; x < grid.GetWidth(); x++) {
            for (int y = 0; y < grid.GetHeight(); y++) {
                Tile tile = grid.GetTile(x, y);

                // square tile offsets
                double px = x * tileWidth;
                double py = y * tileHeight;

                // Pointy-top hex layout
                double centerX = x * 1.5 * hexRadius + hexRadius;
                double centerY = y * hexHeight + (x % 2 == 1 ? hexHeight / 2 : 0);

                SetTileFill(tile);

                gc.setStroke(Color.BLACK);
                switch (tileShape) {
                    case SQUARE -> {
                        gc.fillRect(px, py, tileWidth, tileHeight);
                        gc.strokeRect(px, py, tileWidth, tileHeight);
                    }
                    case HEXAGON -> {
                        double[] c = getHexCenter(x, y);
                        drawHexagon(c[0], c[1], hexRadius);
                    }
                }
            }
        }
    }

    private void drawLineBetweenTiles(Tile a, Tile b) {
        double x1, y1, x2, y2;

        switch (tileShape) {

            case SQUARE -> {
                double tileW = canvas.getWidth() / gridWidth;
                double tileH = canvas.getHeight() / gridHeight;

                x1 = a.colum * tileW + tileW / 2;
                y1 = a.row * tileH + tileH / 2;

                x2 = b.colum * tileW + tileW / 2;
                y2 = b.row * tileH + tileH / 2;
            }

            case HEXAGON -> {
                double[] c1 = getHexCenter(a.colum, a.row);
                double[] c2 = getHexCenter(b.colum, b.row);

                x1 = c1[0];
                y1 = c1[1];
                x2 = c2[0];
                y2 = c2[1];
            }

            default -> {
                return;
            }
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
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

            switch (tileShape) {
                case SQUARE -> {
                    int x = (int) (e.getX() / tileWidth);
                    int y = (int) (e.getY() / tileHeight);
                    Tile tile = grid.GetTile(x, y);
                    if (tile != null) {
                        tile.walkable = !tile.walkable;
                        draw();
                    }
                }
                case HEXAGON -> {
                    // Convert mouse position to grid-local space
                    double mx = e.getX() - gridOffsetX;
                    double my = e.getY() - gridOffsetY;

                    if (mx < 0 || my < 0) return;

                    int col = (int) (mx / (1.5 * hexRadius));
                    int row = (int) ((my - ((col % 2 == 1) ? hexHeight / 2 : 0)) / hexHeight);
                    Tile tile = grid.GetTile(col, row);
                    if (tile != null) {
                        tile.walkable = !tile.walkable;
                        draw();
                    }
                }
            }
        });
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridSize(int width, int height) {
        gridWidth = width;
        gridHeight = height;
        rebuildGrid();
    }

    private void rebuildGrid() {
        grid = new Grid(gridWidth, gridHeight);
        pathSearch.ResetSearch();
        pathSearch.Initialize(grid);
        pathSearch.Enter(0, 0, gridWidth - 1, gridHeight - 1);

        draw();
    }

    private void updateLayout() {
        tileWidth = canvas.getWidth() / gridWidth;
        tileHeight = canvas.getHeight() / gridHeight;
        double rFromWidth =
                canvas.getWidth() / (gridWidth * 1.5 + 0.5);
        double rFromHeight =
                canvas.getHeight() / (gridHeight * Math.sqrt(3));

        hexRadius = Math.min(rFromWidth, rFromHeight);
        hexWidth = hexRadius * 2 * .75;
        hexHeight = Math.sqrt(3) * hexRadius;

        // Total grid pixel size
        double gridPixelWidth = (gridWidth - 1) * 1.5 * hexRadius + 2 * hexRadius;

        double gridPixelHeight = gridHeight * hexHeight + hexHeight / 2;

        // Center grid in canvas
        gridOffsetX = (canvas.getWidth() - gridPixelWidth) / 2;
        gridOffsetY = (canvas.getHeight() - gridPixelHeight) / 2;
    }

    private double[] getHexCenter(int col, int row) {
        double x = col * 1.5 * hexRadius + hexRadius + gridOffsetX;
        double y = row * hexHeight + (col % 2 == 1 ? hexHeight / 2 : 0) + gridOffsetY;
        return new double[]{x, y};
    }

    private void SetTileFill(Tile tile) {
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
    }
}
