package io.github.jgcodes.butool;

import io.github.jgcodes.butool.Controller.CopySettings.Converter;
import io.github.jgcodes.butool.gui.TaskProgressDialog;
import io.github.jgcodes.butool.util.CopyWorker;
import io.github.jgcodes.butool.gui.SVGIcons;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Controller {
	public enum CopySettings {
		SKIP, OVERWRITE, ASK;
		public static class Converter extends StringConverter<CopySettings> {
			@Override
			public String toString(CopySettings object) {
				if (object == null) return "";
				return switch (object) {
					case SKIP -> "Do not overwrite files";
					case OVERWRITE -> "Always replace existing files";
					case ASK -> "Ask me for each file";
				};
			}

			@Override
			public CopySettings fromString(String string) {
				return switch (string) {
					case "Do not overwrite files" -> SKIP;
					case "Always replace existing files" -> OVERWRITE;
					case "Ask me for each file" -> ASK;
					default -> throw new IllegalArgumentException("what the heck?");
				};
			}
		}
	}

	@FXML public TextField iFileField;
	@FXML public TextField oFileField;

	@FXML private Button oFileBtn;
	@FXML private Button iFileBtn;

	@FXML private DatePicker startPicker;
	@FXML private DatePicker endPicker;

	@FXML private ComboBox<CopySettings> copyBox;

	public DirectoryChooser inDC;
	public DirectoryChooser outDC;

	private ObjectProperty<Task<?>> activeWorker;

	public Controller() {
		inDC = new DirectoryChooser();
		outDC = new DirectoryChooser();

		activeWorker = new SimpleObjectProperty<>();
	}

	@FXML public void initialize() {
		SVGPath path = new SVGPath();

		iFileBtn.setGraphic(SVGIcons.ofName("folder-icon", 16, 16));
		oFileBtn.setGraphic(SVGIcons.ofName("folder-icon", 16, 16));

		copyBox.setItems(FXCollections.observableArrayList(CopySettings.values()));
		copyBox.getSelectionModel().select(CopySettings.ASK);
		copyBox.setConverter(new Converter());

		startPicker.valueProperty().addListener(this::onSetStart);
		endPicker.valueProperty().addListener(this::onSetEnd);
	}

	public void browseInput(ActionEvent actionEvent) {
		File existing = new File(iFileField.getText());
		if (existing.isDirectory())
			inDC.setInitialDirectory(existing);
		else if (existing.isFile())
			inDC.setInitialDirectory(existing.getParentFile());

		File result = inDC.showDialog(iFileBtn.getScene().getWindow());
		if (result != null)
			iFileField.setText(result.getAbsoluteFile().toString());
	}

	public void browseOutput(ActionEvent actionEvent) {
		File existing = new File(oFileField.getText());
		if (existing.isDirectory())
			outDC.setInitialDirectory(existing);
		else if (existing.isFile())
			outDC.setInitialDirectory(existing.getParentFile());

		File result = outDC.showDialog(iFileBtn.getScene().getWindow());
		if (result != null)
			oFileField.setText(result.getAbsoluteFile().toString());
	}

	public void onSetStart(ObservableValue<? extends LocalDate> o, LocalDate oldStartDate, LocalDate startDate) {
		ObjectProperty<LocalDate> endDateProp = endPicker.valueProperty();
		if (endDateProp.get() == null) return;

		if (endDateProp.get().isBefore(startDate)) {
			endDateProp.set(startDate);
		}
	}

	public void onSetEnd(ObservableValue<? extends LocalDate> o, LocalDate oldEndDate, LocalDate endDate) {
		ObjectProperty<LocalDate> startDateProp = startPicker.valueProperty();
		if (startDateProp.get() == null) return;

		if (startDateProp.get().isAfter(endDate)) {
			startDateProp.set(endDate);
		}
	}

	public void startWorker(ActionEvent e) {
    Path inPath = Path.of(iFileField.getText());
    Path outPath = Path.of(oFileField.getText());

    if (!inPath.isAbsolute()|| !outPath.isAbsolute()) {
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.setHeaderText("Parameter missing");
    	alert.setContentText("You need to specify an input and output path. What, is this supposed to magically guess " +
				"what you want?");
    	return;
		}

		if (activeWorker.get() == null) {
			// create worker
			CopyWorker worker = new CopyWorker(
				inPath,
				outPath,
				startPicker.getValue(),
				endPicker.getValue(),
				copyBox.getValue()
			);
			activeWorker.set(worker);
			// clear active worker when finished
			EventHandler<WorkerStateEvent> clearPropHandler = event -> {
				activeWorker.set(null);
			};
			worker.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, clearPropHandler);
			worker.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, clearPropHandler);
			worker.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, clearPropHandler);

			System.out.println("Starting worker...");

			//start and show dialog
			TaskProgressDialog dialog = new TaskProgressDialog(worker);
			Thread workerThread = new Thread(worker);
			workerThread.start();
			dialog.show();
		}
	}
}
