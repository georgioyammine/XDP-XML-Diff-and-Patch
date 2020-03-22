package application;

import java.util.Stack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	static Stack<String> classes = new Stack<>();
	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("mainScene.fxml"));
		Scene scene = new Scene(root, 800, 450);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Application");
		stage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}
