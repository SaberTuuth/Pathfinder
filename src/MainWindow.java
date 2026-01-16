import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainWindow {
    private final Stage stage;
    private final DrawingPanel drawingPanel;

    public MainWindow(Stage stage){
        this.stage = stage;
        this.drawingPanel = new DrawingPanel();
        SetupUI();
    }

    private void SetupUI(){
        // --- 1. Create UI components ---
       /* Canvas canvas = new Canvas(500, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane canvasPane = new StackPane(canvas);
        // Bind canvas size to container
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        // Redraw on resize
        canvas.widthProperty().addListener((obs, o, n) -> draw(gc, canvas));
        canvas.heightProperty().addListener((obs, o, n) -> draw(gc, canvas));

        // Buttons

        // --- 2. Create layout ---
        BorderPane root = new BorderPane();
        root.setCenter(canvasPane);   // canvas in center// Bind canvas size to parent

        // --- 3. Create scene ---
        Scene scene = new Scene(root, 600, 450); // include toolbar in height

        // --- 4. Set scene to stage ---
        stage.setScene(scene);
        stage.setTitle("Path Finder");

        // --- 5. Show stage ---
        stage.show();

        // --- 6. (Optional) Event handling after setup) ---*/
        // Toolbar buttons
        Button runBtn = new Button("Run");
        Button resetBtn = new Button("Reset");

        ToolBar toolbar = new ToolBar(runBtn, resetBtn);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(drawingPanel);

        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setTitle("Path Planner");
    }

    public void show() {
        stage.show();
    }

}
