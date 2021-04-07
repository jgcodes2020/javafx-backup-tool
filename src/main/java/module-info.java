module io.github.jgcodes.butool {
	requires javafx.controls;
	requires javafx.fxml;

	requires java.prefs;

	opens io.github.jgcodes.butool to javafx.fxml, javafx.graphics;
	exports io.github.jgcodes.butool.gui;
}