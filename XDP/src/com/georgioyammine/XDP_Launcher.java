package com.georgioyammine;

import java.util.Stack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class XDP_Launcher extends Application {
	static Stack<String> classes = new Stack<>();
	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("fxml/mainScene.fxml"));

		Scene scene = new Scene(root, 800, 450);

		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("XDP: XML Diff And Patch");
//		stage.getIcons().add(new Image("iconN (1).png"));
		stage.getIcons().add(new Image(XDP_Launcher.class.getResourceAsStream("images/icon-main@3x.png")));
//		stage.getIcons().add(new Image("icon-main@2x.png"));
//		stage.getIcons().add(new Image("icon-main.png"));
//		stage.getIcons().add(new Image(Main.class.getResourceAsStream( "icon.ico" )));
		stage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}
