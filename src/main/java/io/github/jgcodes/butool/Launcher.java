package io.github.jgcodes.butool;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

public class Launcher extends Application {
  //Alt + Shift + D (shortcut key up, i don't care about the non-shortcut modifier)
  private static final KeyCombination DEBUG_SHORTCUT = new KeyCodeCombination(
    KeyCode.D,
    ModifierValue.DOWN,
    ModifierValue.ANY,
    ModifierValue.DOWN,
    ModifierValue.ANY,
    ModifierValue.UP
  );

	private Controller controller;

	public static void main(String[] args) {
		launch(Launcher.class, args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		URL mainFxml = Launcher.class.getResource("/fxml/main.fxml");
		URL mainCss = Launcher.class.getResource("/fxml/main.css");

		FXMLLoader loader = new FXMLLoader(mainFxml);
		Parent root = loader.load();
		Scene scene = new Scene(root, 480, 420);

		this.controller = loader.getController(); {
			Preferences prefs = Preferences.userNodeForPackage(Controller.class);
			String inDCiDir = prefs.get("inDC.initialDirectory", null);
			String outDCiDir = prefs.get("outDC.initialDirectory", null);

			if (inDCiDir != null && !inDCiDir.isBlank())
				controller.inDC.setInitialDirectory(new File(inDCiDir));
			if (outDCiDir != null && !outDCiDir.isBlank())
				controller.outDC.setInitialDirectory(new File(outDCiDir));
		}


		scene.getStylesheets().add(mainCss.toExternalForm());
		stage.setScene(scene);

		stage.setTitle("Backup Tool");
		stage.getIcons().addAll(
			new Image(Launcher.class.getResource("/assets/icon-128.png").toExternalForm()),
			new Image(Launcher.class.getResource("/assets/icon-64.png").toExternalForm()),
			new Image(Launcher.class.getResource("/assets/icon-32.png").toExternalForm()),
			new Image(Launcher.class.getResource("/assets/icon-16.png").toExternalForm())
		);

		stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (DEBUG_SHORTCUT.match(event)) {
        System.out.printf("Window size: %dx%d \n", Math.round(stage.getWidth()), Math.round(stage.getHeight()));
      }
    });

		stage.show();
	}

	@Override
	public void stop() {
		Preferences prefs = Preferences.userNodeForPackage(Controller.class);

		prefs.put("inDC.initialDirectory", controller.iFileField.getText());
		prefs.put("outDC.initialDirectory", controller.oFileField.getText());
	}
}
