import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.Random;

import static java.lang.Math.sqrt;

public class DrawingPanel extends StackPane {

    public enum TileShape {
        SQUARE,
        TRIANGLE,
        HEXAGON
    }

    public enum TileSelect {
        NONE,
        START,
        GOAL,
        STATE,
        WEIGHT
    }

    public static class GridColors {
        public Color weighted;
        public Color blocked = Color.DARKRED;
        public Color start = Color.GREEN;
        public Color goal = Color.RED;
        public Color openSet = Color.LIGHTBLUE;
        public Color closedSet = Color.DARKBLUE;
        public Color path = Color.LIGHTGREEN;
        public Color Current = Color.ORANGE;
    }

    public final Canvas canvas;
    private final GraphicsContext gc;
    private int gridWidth = 20;
    private int gridHeight = 20;
    public Grid grid;

    double tileWidth;
    double tileHeight;
    GridColors colors = new GridColors();
    public TileShape tileShape = TileShape.SQUARE;
    public TileSelect tileSelect = TileSelect.NONE;
    public PathSearch pathSearch;
    public boolean isShowingNeighbors = false;

    private double hexRadius;
    private double hexHeight;
    double gridOffsetX = 0;
    double gridOffsetY = 0;

    double triangleSide;
    double triangleHeight;
    private double triangleStartX;
    private double triangleStartY;

    private boolean isPainting = false;
    private boolean paintWalkable = false;
    private Tile lastTile = null;

    Random random = new Random();

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
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawGrid();
        DrawLinePath();
        if (isShowingNeighbors) {
            drawNeighborLines();
        }
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
                gc.setLineWidth(2);
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

    private void DrawLinePath() {
        if (pathSearch.CurrentTile == null) {
            return;
        }

        while (pathSearch.CurrentTile.parent != null) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            drawLineBetweenTiles(pathSearch.CurrentTile.searchNode.tile, pathSearch.CurrentTile.parent.searchNode.tile);
            pathSearch.CurrentTile = pathSearch.CurrentTile.parent;
        }
    }

    private void drawLineBetweenTiles(Tile a, Tile b) {
        double x1, y1, x2, y2;

        switch (tileShape) {

            case SQUARE -> {
                double tileW = canvas.getWidth() / gridWidth;
                double tileH = canvas.getHeight() / gridHeight;

                x1 = a.row * tileW + tileW / 2;
                y1 = a.colum * tileH + tileH / 2;

                x2 = b.row * tileW + tileW / 2;
                y2 = b.colum * tileH + tileH / 2;
            }

            case HEXAGON -> {
                double[] c1 = getHexCenter(a.row, a.colum);
                double[] c2 = getHexCenter(b.row, b.colum);

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

    public void drawNeighborLines() {
        for (Map.Entry<Tile, PathSearch.SearchNode> entry : pathSearch.Nodes.entrySet()) {
            Tile tile = entry.getKey();
            if (tile.blocked) {
                continue;
            }
            PathSearch.SearchNode node = entry.getValue();
            for (PathSearch.SearchNode neighbor : node.neighbors) {
                Tile nTile = neighbor.tile;
                drawLineBetweenTiles(tile, nTile);
            }
        }
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(e -> {
            double mx = e.getX();
            double my = e.getY();
            Tile tile = getTileFromMouse(mx, my);
            lastTile = null;

            if (tile != null && tileSelect.equals(TileSelect.START)) {
                SetStartTile(tile);
                pathSearch.Initialize(grid);
                pathSearch.Enter(tile.row, tile.colum, pathSearch.GoalTile.tile.row, pathSearch.GoalTile.tile.colum);
                tileSelect = TileSelect.NONE;
                draw();
                return;
            } else if (tile != null && tileSelect.equals(TileSelect.GOAL)) {
                SetGoalTile(tile);
                pathSearch.Initialize(grid);
                pathSearch.Enter(pathSearch.StartTile.tile.row, pathSearch.StartTile.tile.colum, tile.row, tile.colum);
                tileSelect = TileSelect.NONE;
                draw();
            }
            isPainting = true;

            if (e.isPrimaryButtonDown()) {
                paintWalkable = false; // left click = wall
                paintTile(tile);
            }
            if (e.isSecondaryButtonDown()) {
                paintWalkable = true; // right click = weight painting
                paintWeight(tile);
            }

            draw();

        });
        canvas.setOnMouseDragged(e -> {

            if (!isPainting) return;

            Tile tile = getTileFromMouse(e.getX(), e.getY());
            if (tile == null || tile == lastTile) return;

            if (paintWalkable)
                paintWeight(tile);
            else
                paintTile(tile);

            draw();
        });
        canvas.setOnMouseReleased(e -> {
            isPainting = false;
            lastTile = null;
        });
    }

    private void paintTile(Tile tile) {

        if (tile.isStart || tile.isGoal)
            return;
        
        if(tileSelect.equals(TileSelect.STATE)) {
        	tile.walkable = false;
        }else {
        	tile.walkable = true;
        }
    }

    private void paintWeight(Tile tile) {

        if (tile.blocked || !tile.walkable)
            return;

        if(tileSelect.equals(TileSelect.WEIGHT)) {
        	if (tile.weight < 10) {
                tile.weight++;
        	}
        }
        else {
        	if(tile.weight > 1) {
        		tile.weight--;
        	}
        }
        lastTile = tile;
    }

    private Tile getTileFromMouse(double mx, double my) {

        switch (tileShape) {
            case SQUARE -> {
                int x = (int) (mx / tileWidth);
                int y = (int) (my / tileHeight);
                return grid.GetTile(x, y);
            }
            case HEXAGON -> {
                return getTileAtHex(mx, my);
            }
            case TRIANGLE -> {
                return getTileAtTriangle(mx, my);
            }
        }

        return null;
    }

    private void SetStartTile(Tile tile) {
        if (!tile.walkable) {
            return;
        }
        if (pathSearch.StartTile != null) {
            pathSearch.StartTile.tile.isStart = false;
            pathSearch.VisitedNodes.clear();
            pathSearch.openPriorityQueue.clear();
            pathSearch.openQueue.clear();
            pathSearch.openStack.clear();
        }
        pathSearch.StartTile.tile = tile;
        pathSearch.StartTile.tile.isStart = true;
    }

    private void SetGoalTile(Tile tile) {
        if (!tile.walkable) {
            return;
        }
        if (pathSearch.GoalTile != null) {
            pathSearch.GoalTile.tile.isGoal = false;
        }
        pathSearch.GoalTile.tile = tile;
        pathSearch.GoalTile.tile.isGoal = true;
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
            x[0] = cx;
            y[0] = cy - h / 2;
            x[1] = cx - triangleSide / 2;
            y[1] = cy + h / 2;
            x[2] = cx + triangleSide / 2;
            y[2] = cy + h / 2;
        } else {
            x[0] = cx;
            y[0] = cy + h / 2;
            x[1] = cx - triangleSide / 2;
            y[1] = cy - h / 2;
            x[2] = cx + triangleSide / 2;
            y[2] = cy - h / 2;
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
        if (pathSearch.CurrentTile != null && tile == pathSearch.CurrentTile.searchNode.tile) {
            gc.setFill(colors.Current);
        } else if (!tile.walkable) {
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
        } else if (tile.blocked) {
            gc.setFill(Color.BLACK);
        } else {
            double t = (tile.weight - 1) / 9.0; // normalize 1–10
            colors.weighted = Color.gray(1.0 - t);
            gc.setFill(colors.weighted);
        }
    }

    public void RandomizeTileState() {
        ResetGridState();
        for (int i = 0; i < grid.GetWidth(); i++) {
            for (int j = 0; j < grid.GetHeight(); j++) {
                Tile tile = grid.GetTile(i, j);
                if (tile.isGoal || tile.isStart) {
                    continue;
                }
                int choice = random.nextInt(1, 101);

                if (choice >= 75) {
                    tile.walkable = !tile.walkable;
                }
            }
        }
        draw();
    }

    public void RandomizeTileWeights() {
        ResetGridWeights();
        for (int i = 0; i < grid.GetWidth(); i++) {
            for (int j = 0; j < grid.GetHeight(); j++) {
                Tile tile = grid.GetTile(i, j);
                if (tile.isGoal || tile.isStart || !tile.walkable || tile.blocked) {
                    continue;
                }
                int choice = random.nextInt(1, 101);

                if (choice >= 35) {
                    choice = random.nextInt(1, 10);
                    tile.weight = choice;
                }
            }
        }
        draw();
    }

    private void ResetGridState() {
        for (int x = 0; x < grid.GetWidth(); x++) {
            for (int y = 0; y < grid.GetHeight(); y++) {
                Tile tile = grid.GetTile(x, y);
                if (tile.isStart || tile.isGoal || tile.blocked) {
                    continue;
                }
                tile.walkable = true;
            }
        }
    }

    private void ResetGridWeights() {
        for (int x = 0; x < grid.GetWidth(); x++) {
            for (int y = 0; y < grid.GetHeight(); y++) {
                Tile tile = grid.GetTile(x, y);
                if (tile.isStart || tile.isGoal || tile.blocked) {
                    continue;
                }
                tile.weight = 1;
            }
        }
    }

    public void ResetGrid() {
        for (int x = 0; x < grid.GetWidth(); x++) {
            for (int y = 0; y < grid.GetHeight(); y++) {
                Tile tile = grid.GetTile(x, y);
                if (tile.isStart || tile.isGoal || tile.blocked) {
                    continue;
                }
                tile.weight = 1;
                tile.walkable = true;
            }
        }
        draw();
    }

    public void NewGrid() {
        setGridSize(20, 20);
    }
}
