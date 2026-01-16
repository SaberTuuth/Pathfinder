import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;

import javafx.scene.control.Button;

import java.awt.*;

import static java.awt.Color.*;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        // --- 1. Create UI components ---
        Canvas canvas = new Canvas(500, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw an initial square
        gc.fillRect(5, 5, 20, 20);

        // Buttons

        // --- 2. Create layout ---
        BorderPane root = new BorderPane();
        root.setCenter(canvas);   // canvas in center

        // --- 3. Create scene ---
        Scene scene = new Scene(root, 600, 450); // include toolbar in height

        // --- 4. Set scene to stage ---
        stage.setScene(scene);
        stage.setTitle("Path Finder");

        // --- 5. Show stage ---
        stage.show();

        // --- 6. (Optional) Event handling after setup) ---
    }

    public static void main(String[] args) {
        launch(args);
    }
}
