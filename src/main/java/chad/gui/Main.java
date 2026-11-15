package chad.gui;

import java.io.IOException;

import chad.Chad;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * A GUI for Chad using FXML.
 */
public class Main extends Application {
    private static final String FILE_PATH = "data/data.csv";
    private static final String WINDOW_TITLE = "Chad";

    private Chad chad;

    @Override
    public void start(Stage stage) {
        chad = new Chad(FILE_PATH);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            stage.setScene(scene);
            fxmlLoader.<MainWindow>getController().setChad(chad); // inject the Chad instance

            stage.setTitle(WINDOW_TITLE);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
