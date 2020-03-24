package application;

import java.awt.Toolkit;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXProgressBar;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class mainSceneController {

	@FXML
	JFXButton load1Diff;
	@FXML
	JFXButton diffBtn;
	@FXML
	JFXButton load2Diff;
	@FXML
	JFXButton load1Reverse;
	@FXML
	JFXButton reverseBtn;
	@FXML
	JFXButton load2Reverse;
	@FXML
	JFXButton load1Patch;
	@FXML
	JFXButton patchBtn;
	@FXML
	JFXButton load2Patch;

	File file;
	@FXML
	Label abs2Diff;
	@FXML
	Label abs1Diff;
	@FXML

	Label abs1Reverse;
	@FXML
	Label abs1Patch;
	@FXML
	Label abs2Patch;
	@FXML
	Label resultDiff;
	@FXML
	Label diffPath;
	@FXML
	Label reversePath;
	@FXML
	Label reverseCheck;
	@FXML
	Label patchPath;
	@FXML
	Label patchCheck;
	@FXML
	JFXCheckBox bypass;
	@FXML
	JFXCheckBox reversible;

	ExecutorService executorService = Executors.newFixedThreadPool(3);

	final Clipboard clipboard = Clipboard.getSystemClipboard();
	final ClipboardContent content = new ClipboardContent();
	@FXML
	JFXProgressBar progressBar1;

	@FXML
	public void initialize() {
		resultDiff.setText("");
		diffPath.setText("");
		reversePath.setText("");
		reverseCheck.setText("");
		patchPath.setText("");
		patchCheck.setText("");
		abs1Diff.setText("");
		abs2Diff.setText("");
		abs1Reverse.setText("");
		abs2Patch.setText("");
		abs1Patch.setText("");
		progressBar1.setProgress(0.0);
	}

	@FXML
	public void handleLoad1Diff() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fil_chooser.setInitialDirectory(new File(currentPath));
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs1Diff.setText(file.getAbsolutePath());
		}

	}

	@FXML
	public void handleLoad2Diff() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs2Diff.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad1Reverse() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs1Reverse.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad1Patch() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs2Patch.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad2Patch() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs1Patch.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void getDiff() {
		try {
			processDiff.restart();
			progressBar1.progressProperty().bind(XMLDiffAndPatch.getProgressProperty());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("rawtypes")
	Service processDiff = new Service() {
		@Override
		protected Task createTask() {
			return new Task<Void>() {
				@Override
				public Void call() throws Exception {
					double t1 = System.currentTimeMillis();
					ArrayList<String> result = XMLDiffAndPatch.TEDandEditScript(abs1Diff.getText(), abs2Diff.getText(),
							reversible.isSelected());
					updateProgress(1.0,1.0 );
					System.out.println(System.currentTimeMillis()-t1+"ms");
					Platform.runLater(() -> {
						resultDiff.setText("Distance :" + result.get(0) + " | " + "Similarity :" + result.get(1) + "%");
						diffPath.setText(result.get(2));
						Toolkit.getDefaultToolkit().beep();
					});
					
					return null;
				}
			};
		}
	};

	@FXML
	public void reverseDiff() {
		try {
			processReverse.restart();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	Service processReverse = new Service() {
		@Override
		protected Task createTask() {
			return new Task<Void>() {
				@Override
				public Void call() throws Exception {
					double t1 = System.currentTimeMillis();
					String result = XMLDiffAndPatch.reverseXMLES(abs1Reverse.getText());
					System.out.println(System.currentTimeMillis()-t1+"ms");
					Platform.runLater(() -> {
						reverseCheck.setText(result.isEmpty()?"File is not reversible!":"File reversed sucessfully!");
						reversePath.setText(result);
					});

					return null;
				}
			};
		}
	};

	@FXML
	public void patch() {
		try {
			processPatch.restart();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	Service processPatch = new Service() {
		@Override
		protected Task createTask() {
			return new Task<Void>() {
				@Override
				public Void call() throws Exception {
					double t1 = System.currentTimeMillis();
					ArrayList<String> result = XMLDiffAndPatch.applyPatchXML(abs2Patch.getText(), abs1Patch.getText(),
							bypass.isSelected());
					System.out.println(System.currentTimeMillis()-t1+"ms");
					Platform.runLater(() -> {
						patchCheck.setText(result.get(0));
						patchPath.setText(result.get(1));
					});
					return null;
				}
			};
		}
	};

	@FXML
	public void copyHandle(MouseEvent event) {
		content.putString(((Label) event.getSource()).getText());
		clipboard.setContent(content);
	}

}
