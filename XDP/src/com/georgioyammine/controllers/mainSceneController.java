package com.georgioyammine.controllers;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import com.georgioyammine.classes.XMLDiffAndPatch;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	@FXML
	JFXTextField updateRootBox;
	@FXML
	JFXTextField insertContainedBox;
	@FXML
	JFXTextField deleteContainedBox;
	@FXML
	JFXTextField leafOpBox;
	@FXML
	JFXTextField attrNameBox;
	@FXML
	JFXTextField attrValBox;
	@FXML
	JFXTextField textTokenBox;
	@FXML JFXProgressBar progressBarReverse;
	@FXML JFXProgressBar progressBarPatch;

	String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();

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

		updateRootBox.setPromptText(XMLDiffAndPatch.updateRootName + "");
		insertContainedBox.setPromptText(XMLDiffAndPatch.insertContained + "");
		deleteContainedBox.setPromptText(XMLDiffAndPatch.deleteContained + "");
		leafOpBox.setPromptText(XMLDiffAndPatch.deleteOrInsertLeaf + "");
		attrNameBox.setPromptText(XMLDiffAndPatch.attributeNameCost + "");
		attrValBox.setPromptText(XMLDiffAndPatch.attributeValueCost + "");
		textTokenBox.setPromptText(XMLDiffAndPatch.contentTokenCost + "");

		progressBar1.setProgress(0.0);
		progressBarReverse.setProgress(0.0);
		progressBarPatch.setProgress(0.0);
		updateRootBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					updateRootBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		insertContainedBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					insertContainedBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		deleteContainedBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					deleteContainedBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		leafOpBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					leafOpBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		attrNameBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					attrNameBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		attrValBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					attrValBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		textTokenBox.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					textTokenBox.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		int a,b,c,d,e,f,g;
			try {
				String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();

				Scanner scan = new Scanner(new File(currentPath + "\\" + "costs.cfg"));
				String urSaved = scan.nextLine();
				a = Integer.parseInt(urSaved.substring(urSaved.indexOf(":")+1));
				String insertCSaved = scan.nextLine();
				b =Integer.parseInt(insertCSaved.substring(insertCSaved.indexOf(":")+1));
				String deleteCSaved = scan.nextLine();
				c = Integer.parseInt(deleteCSaved.substring(deleteCSaved.indexOf(":")+1));
				String leafOpSaved = scan.nextLine();
				d = Integer.parseInt(leafOpSaved.substring(leafOpSaved.indexOf(":")+1));
				String attrNameSaved = scan.nextLine();
				e = Integer.parseInt(attrNameSaved.substring(attrNameSaved.indexOf(":")+1));
				String attrValSaved = scan.nextLine();
				f = Integer.parseInt(attrValSaved.substring(attrValSaved.indexOf(":")+1));
				String textToken = scan.nextLine();
				g = Integer.parseInt(textToken.substring(textToken.indexOf(":")+1));
				scan.close();
				XMLDiffAndPatch.updateRootName = a;
				XMLDiffAndPatch.insertContained = b;
				XMLDiffAndPatch.deleteContained = c;
				XMLDiffAndPatch.deleteOrInsertLeaf = d;
				XMLDiffAndPatch.attributeNameCost = e;
				XMLDiffAndPatch.attributeValueCost = f;
				XMLDiffAndPatch.contentTokenCost = g;
			} catch (NumberFormatException | FileNotFoundException ex) {
//				ex.printStackTrace();
			}


	}

	@FXML
	public void handleLoad1Diff() {
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.setInitialDirectory(new File(currentPath));
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		file = fil_chooser.showOpenDialog(null);
//		JFileChooser fileChooserx = new JFileChooser();
//		fileChooserx.setCurrentDirectory(new File(currentPath));
//		fileChooserx.show

		if (file != null) {

			abs1Diff.setText(file.getAbsolutePath());
			currentPath = file.getAbsoluteFile().getParent();
		}

	}

	@FXML
	public void handleLoad2Diff() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs2Diff.setText(file.getAbsolutePath());
			currentPath = file.getAbsoluteFile().getParent();
		}
	}

	@FXML
	public void handleLoad1Reverse() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs1Reverse.setText(file.getAbsolutePath());
			currentPath = file.getAbsoluteFile().getParent();
		}
	}

	@FXML
	public void handleLoad1Patch() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs2Patch.setText(file.getAbsolutePath());
			currentPath = file.getAbsoluteFile().getParent();
		}
	}

	@FXML
	public void handleLoad2Patch() {
		Stage stage = new Stage();
		FileChooser fil_chooser = new FileChooser();
		fil_chooser.getExtensionFilters().add(new ExtensionFilter("XML", "*.xml"));
		fil_chooser.setInitialDirectory(new File(currentPath));
		file = fil_chooser.showOpenDialog(stage);

		if (file != null) {

			abs1Patch.setText(file.getAbsolutePath());
			currentPath = file.getAbsoluteFile().getParent();
		}
	}

	@FXML
	public void getDiff() {
		try {
			processDiff.restart();
			progressBar1.progressProperty().bind(XMLDiffAndPatch.getProgressProperty());
		} catch (Exception e) {
//			e.printStackTrace();
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
					updateProgress(1.0, 1.0);
					System.out.println(System.currentTimeMillis() - t1 + "ms");
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
			progressBarReverse.progressProperty().bind(XMLDiffAndPatch.getProgressReverseProperty());
		} catch (Exception e) {
//			e.printStackTrace();
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
					System.out.println(System.currentTimeMillis() - t1 + "ms");
					Platform.runLater(() -> {
						reverseCheck
								.setText(result.isEmpty() ? "File is not reversible!" : "File reversed sucessfully!");
						reversePath.setText(result);
						Toolkit.getDefaultToolkit().beep();
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
			progressBarPatch.progressProperty().bind(XMLDiffAndPatch.getProgressPatchProperty());
		} catch (Exception e) {
//			e.printStackTrace();
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
					System.out.println(System.currentTimeMillis() - t1 + "ms");
					Platform.runLater(() -> {
						patchCheck.setText(result.get(0));
						patchPath.setText(result.get(1));
						Toolkit.getDefaultToolkit().beep();
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

	@FXML
	public void applySettingsHandle() {
		if (!updateRootBox.getText().isEmpty())
			XMLDiffAndPatch.updateRootName = Integer.parseInt(updateRootBox.getText());
		if (!insertContainedBox.getText().isEmpty())
			XMLDiffAndPatch.insertContained = Integer.parseInt(insertContainedBox.getText());
		if (!deleteContainedBox.getText().isEmpty())
			XMLDiffAndPatch.deleteContained = Integer.parseInt(deleteContainedBox.getText());
		if (!leafOpBox.getText().isEmpty())
			XMLDiffAndPatch.deleteOrInsertLeaf = Integer.parseInt(leafOpBox.getText());
		if (!attrNameBox.getText().isEmpty())
			XMLDiffAndPatch.attributeNameCost = Integer.parseInt(attrNameBox.getText());
		if (!attrValBox.getText().isEmpty())
			XMLDiffAndPatch.attributeValueCost = Integer.parseInt(attrValBox.getText());
		if (!textTokenBox.getText().isEmpty())
			XMLDiffAndPatch.contentTokenCost = Integer.parseInt(textTokenBox.getText());
		setDefaultCosts();

			try {
				PrintWriter pw = new PrintWriter(new FileOutputStream(new File("costs.cfg")));
				pw.println("updateRootName:" + XMLDiffAndPatch.updateRootName);
				pw.println("insertContained:" + XMLDiffAndPatch.insertContained);
				pw.println("deleteContained:" + XMLDiffAndPatch.deleteContained);
				pw.println("deleteOrInsert:" + XMLDiffAndPatch.deleteOrInsertLeaf);
				pw.println("attrName:" + XMLDiffAndPatch.attributeNameCost);
				pw.println("attrValue:" + XMLDiffAndPatch.attributeValueCost);
				pw.println("contentToken:" + XMLDiffAndPatch.contentTokenCost);
				pw.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}

	}

	private void clearTextNodes() {
		updateRootBox.clear();
		insertContainedBox.clear();
		deleteContainedBox.clear();
		leafOpBox.clear();
		attrNameBox.clear();
		attrValBox.clear();
		textTokenBox.clear();
		updateRootBox.clear();


	}

	@FXML
	public void setDefaultCosts() {
		clearTextNodes();
		updateRootBox.setPromptText(XMLDiffAndPatch.updateRootName + "");
		insertContainedBox.setPromptText(XMLDiffAndPatch.insertContained + "");
		deleteContainedBox.setPromptText(XMLDiffAndPatch.deleteContained + "");
		leafOpBox.setPromptText(XMLDiffAndPatch.deleteOrInsertLeaf + "");
		attrNameBox.setPromptText(XMLDiffAndPatch.attributeNameCost + "");
		attrValBox.setPromptText(XMLDiffAndPatch.attributeValueCost + "");
		textTokenBox.setPromptText(XMLDiffAndPatch.contentTokenCost + "");
	}

}
