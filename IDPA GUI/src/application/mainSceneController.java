package application;

import java.awt.Checkbox;
import java.io.File;
import java.util.ArrayList;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class mainSceneController {

	@FXML
	JFXCheckBox check1;
	@FXML
	JFXCheckBox check11;
	@FXML
	JFXButton load1;
	@FXML
	JFXButton diffBtn;
	@FXML
	JFXButton load2;
	@FXML
	JFXButton load11;
	@FXML
	JFXButton reverseBtn;
	@FXML
	JFXButton load22;
	@FXML
	JFXButton load3;
	@FXML
	JFXButton patchBtn;
	@FXML
	JFXButton load4;

	File file;
	@FXML
	Label abs2;
	@FXML
	Label abs1;
	@FXML
	Label abs4;
	@FXML
	Label abs3;
	@FXML
	Label abs6;
	@FXML
	Label abs5;
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

	final Clipboard clipboard = Clipboard.getSystemClipboard();
	final ClipboardContent content = new ClipboardContent();


	@FXML
	public void handleLoad1() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs1.setText(file.getAbsolutePath());
		}

	}

	@FXML
	public void handleLoad2() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs2.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad3() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs3.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad4() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs4.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad5() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs5.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void handleLoad6() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs6.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void getDiff() {
		try {
			ArrayList<String> result = XMLDiffAndPatch.TEDandEditScript(abs1.getText(), abs2.getText(),
					check1.isSelected());
			resultDiff.setText(
					"Distance :" + result.get(0) + " | " + "Similarity :" + result.get(1) + "%");
			diffPath.setText(result.get(2));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@FXML
	public void reverseDiff() {
	}

	@FXML
	public void patch() {
	}

	@FXML
	public void copyDiffPath() {
		content.putString(diffPath.getText());
		clipboard.setContent(content);
	}

	@FXML
	public void copyReversePath() {
		content.putString(reversePath.getText());
		clipboard.setContent(content);
	}

	@FXML
	public void copyPatchPath() {
		content.putString(patchPath.getText());
		clipboard.setContent(content);
	}

}
