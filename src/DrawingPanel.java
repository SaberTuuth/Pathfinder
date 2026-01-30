import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.util.Map;
import static java.lang.Math.sqrt;

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
    public TileShape tileShape = TileShape.TRIANGLE;
    public PathSearch pathSearch;

    private double hexRadius;
    private double hexHeight;
    double gridOffsetX = 0;
    double gridOffsetY = 0;

    double triangleSide;
    double triangleHeight;
    private double triangleStartX;
    private double triangleStartY;

    public DrawingPanel() {
        canvas = new Canvas();

        gc = canvas.getGraphicsContext2D();
        grid = new Grid(gridWidth, gridHeight);

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

    private void drawTriangle(double centerX, double centerY, double side, boolean pointingUp) {
        double triangleHeight = side * Math.sqrt(3) / 2;

        double[] xPoints = new double[3];
        double[] yPoints = new double[3];

        if (pointingUp) {
            // Top vertex
            xPoints[0] = centerX;
            yPoints[0] = centerY - triangleHeight / 2;

            // Bottom left
            xPoints[1] = centerX - side / 2;
            yPoints[1] = centerY + triangleHeight / 2;

            // Bottom right
            xPoints[2] = centerX + side / 2;
            yPoints[2] = centerY + triangleHeight / 2;
        } else {
            // Bottom vertex
            xPoints[0] = centerX;
            yPoints[0] = centerY + triangleHeight / 2;

            // Top left
            xPoints[1] = centerX - side / 2;
            yPoints[1] = centerY - triangleHeight / 2;

            // Top right
            xPoints[2] = centerX + side / 2;
            yPoints[2] = centerY - triangleHeight / 2;
        }

        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
    }

    private void DrawGrid() {

        updateLayout();
        for (int x = 0; x < grid.GetWidth(); x++) {
            for (int y = 0; y < grid.GetHeight(); y++) {
                Tile tile = grid.GetTile(x, y);

                // square tile offsets
                double px = x * tileWidth;
                double py = y * tileHeight;

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
                    case TRIANGLE -> {
                        double cx = triangleStartX + x * (triangleSide / 2);
                        double cy = triangleStartY + y * triangleHeight;
                        boolean pointingUp = ((x + y) % 2 == 0);
                        drawTriangle(cx, cy, triangleSide, pointingUp);
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

            case TRIANGLE -> {
                double[] t1 = getTriangleCenter(a.row, a.colum);
                double[] t2 = getTriangleCenter(b.row, b.colum);

                x1 = t1[0];
                y1 = t1[1];
                x2 = t2[0];
                y2 = t2[1];
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
            double mx = e.getX();
            double my = e.getY();
            Tile tile = null;

            switch (tileShape) {
                case SQUARE -> {
                    int x = (int) (mx / tileWidth);
                    int y = (int) (my / tileHeight);
                    tile = grid.GetTile(x, y);
                }
                case HEXAGON -> tile = getTileAtHex(mx, my);
                case TRIANGLE -> tile = getTileAtTriangle(mx, my);
                default -> { }
            }

            if (tile != null) {
                tile.walkable = !tile.walkable;
                draw();
            }
        });
    }

    private Tile getTileAtHex(double mx, double my) {
        double lx = mx - gridOffsetX;
        double ly = my - gridOffsetY;
        if (lx < 0 || ly < 0) return null;

        int col = (int) Math.round((lx - hexRadius) / (1.5 * hexRadius));
        int row = (int) Math.round((ly - (col % 2 == 1 ? hexHeight / 2 : 0)) / hexHeight);

        if (col < 0 || col >= gridWidth || row < 0 || row >= gridHeight) return null;

        double[] center = getHexCenter(col, row);
        if (!pointInHex(mx, my, center[0], center[1], hexRadius)) return null;

        return grid.GetTile(col, row);
    }

    private boolean pointInHex(double px, double py, double cx, double cy, double radius) {
        double[] x = new double[6];
        double[] y = new double[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            x[i] = cx + radius * Math.cos(angle);
            y[i] = cy + radius * Math.sin(angle);
        }
        return pointInPolygon(px, py, x, y, 6);
    }

    private Tile getTileAtTriangle(double mx, double my) {
        int col = (int) Math.round((mx - triangleStartX) / (triangleSide / 2));
        int row = (int) Math.round((my - triangleStartY) / triangleHeight);

        int[] dcol = {0, -1, 1, 0, 0};
        int[] drow = {0, 0, 0, -1, 1};
        for (int i = 0; i < dcol.length; i++) {
            int c = col + dcol[i];
            int r = row + drow[i];
            if (c >= 0 && c < gridWidth && r >= 0 && r < gridHeight && pointInTriangle(mx, my, c, r)) {
                return grid.GetTile(c, r);
            }
        }
        return null;
    }

    private boolean pointInTriangle(double px, double py, int col, int row) {
        double cx = triangleStartX + col * (triangleSide / 2);
        double cy = triangleStartY + row * triangleHeight;
        boolean pointingUp = (col + row) % 2 == 0;
        double h = triangleSide * Math.sqrt(3) / 2;

        double[] x = new double[3];
        double[] y = new double[3];
        if (pointingUp) {
            x[0] = cx;               y[0] = cy - h / 2;
            x[1] = cx - triangleSide / 2;  y[1] = cy + h / 2;
            x[2] = cx + triangleSide / 2;  y[2] = cy + h / 2;
        } else {
            x[0] = cx;               y[0] = cy + h / 2;
            x[1] = cx - triangleSide / 2;  y[1] = cy - h / 2;
            x[2] = cx + triangleSide / 2;  y[2] = cy - h / 2;
        }
        return pointInPolygon(px, py, x, y, 3);
    }

    private boolean pointInPolygon(double px, double py, double[] x, double[] y, int n) {
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if (((y[i] > py) != (y[j] > py)) &&
                    (px < (x[j] - x[i]) * (py - y[i]) / (y[j] - y[i]) + x[i])) {
                inside = !inside;
            }
        }
        return inside;
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

        double rFromWidth = canvas.getWidth() / (gridWidth * 1.5 + 0.5);
        double rFromHeight = canvas.getHeight() / ((gridHeight + 0.5) * sqrt(3));

        hexRadius = Math.min(rFromWidth, rFromHeight);
        hexHeight = sqrt(3) * hexRadius;

        // Total grid pixel size
        double gridPixelWidth = (gridWidth - 1) * 1.5 * hexRadius + 2 * hexRadius;
        double gridPixelHeight = (gridHeight + 0.5) * hexHeight;

        // Center grid in canvas.
        gridOffsetX = Math.max(0, (canvas.getWidth() - gridPixelWidth) / 2);

        double minOffsetY = hexHeight / 2;
        double maxOffsetY = canvas.getHeight() - gridHeight * hexHeight;
        double centeredOffsetY = (canvas.getHeight() - gridPixelHeight) / 2 + hexHeight / 2;
        gridOffsetY = Math.max(minOffsetY, Math.min(maxOffsetY, centeredOffsetY));

        // --- Triangles ---
        double sideFromWidth =
                canvas.getWidth() / (gridWidth * 0.5);

        double sideFromHeight =
                (canvas.getHeight() / gridHeight) * 2 / Math.sqrt(3);

        triangleSide = Math.min(sideFromWidth, sideFromHeight);
        triangleHeight = triangleSide * Math.sqrt(3) / 2;

        // Same centering as DrawGrid so lines match triangle positions
        double totalWidth = (gridWidth - 1) * (triangleSide / 2) + triangleSide;
        double totalHeight = gridHeight * triangleHeight;
        triangleStartX = (canvas.getWidth() - totalWidth) / 2 + triangleSide / 2;
        triangleStartY = (canvas.getHeight() - totalHeight) / 2 + triangleHeight / 2;
    }

    private double[] getHexCenter(int col, int row) {
        double x = col * 1.5 * hexRadius + hexRadius + gridOffsetX;
        double y = row * hexHeight + (col % 2 == 1 ? hexHeight / 2 : 0) + gridOffsetY;
        return new double[]{x, y};
    }

    private double[] getTriangleCenter(double col, double row) {
        double x = triangleStartX + col * (triangleSide / 2);
        double y = triangleStartY + row * triangleHeight;
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
