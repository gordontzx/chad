package chad.gui;

import chad.Chad;
import chad.command.Command;
import chad.command.ExitCommand;
import chad.parser.Parser;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Chad chad;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/default.png"));
    private Image chadImage = new Image(this.getClass().getResourceAsStream("/images/gigachad.png"));

    /**
     * Initializes the main window.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        dialogContainer.getChildren().add(
                DialogBox.getDudeDialog(Chad.getHelloMessage(), chadImage)
        );
    }

    /** Injects the Chad instance */
    public void setChad(Chad s) {
        chad = s;
        chad.getLoadWarning().ifPresent(warning ->
                dialogContainer.getChildren().add(DialogBox.getDudeDialog(warning, chadImage)));
    }

    /**
     * Creates two dialog boxes, one for user input and the other for Chad's reply.
     * Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.isBlank()) {
            userInput.clear();
            return; // nothing typed, so don't send an empty message
        }
        Command cmd = Parser.parse(input);
        String response = chad.getResponse(cmd);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getDudeDialog(response, chadImage)
        );
        userInput.clear();

        if (cmd.isExit()) {
            assert cmd instanceof ExitCommand : "Command is instance of ExitCommand when exiting.";

            // Let user read message before closing
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> Platform.exit());
            delay.play();
        }
    }
}
