package com.github.flqw.ast.explorer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AstExplorer extends Application {

	public static void main(String[] args) {
		AstExplorer.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("AST Explorer");

		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
			ExceptionAlert.show("uncaught Exception", throwable);
        });

		Parent root = FXMLLoader.load(ClassLoader.getSystemResource("AstExplorer.fxml"));
		primaryStage.setScene(new Scene(root, 1000, 500));

		primaryStage.show();
	}

}
