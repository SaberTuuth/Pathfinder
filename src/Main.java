import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Main extends Application {
    public static Window mainStage;

    @Override
    public void start(Stage stage) {
        MainWindow window = new MainWindow((stage));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
