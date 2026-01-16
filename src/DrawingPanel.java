import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class DrawingPanel extends StackPane {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private double Grid = 10;

    public DrawingPanel() {
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();

        getChildren().add(canvas);

        // Resize handling
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        canvas.widthProperty().addListener((obs, o, n) -> draw());
        canvas.heightProperty().addListener((obs, o, n) -> draw());

        draw();
    }

    private void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        DrawGrid();
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

    private void DrawGrid(){
        double tilewidth = canvas.getWidth() / Grid;
        double tileheight = canvas.getHeight() / Grid;

        gc.setStroke(Color.BLACK);

        for(int i = 0; i < Grid; i++){
            for(int j = 0; j < Grid; j++){
                double tileX = tilewidth * i;
                double tileY = tileheight * j;
                gc.setFill(Color.WHITE);
                gc.strokeRect(tileX, tileY, tilewidth, tileheight);
            }
        }
    }
}
