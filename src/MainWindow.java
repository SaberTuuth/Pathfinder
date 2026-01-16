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
