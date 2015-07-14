package com.github.flqw.ast.explorer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionAlert {

	private static final String[] EXCEPTION_PREFIXES = { "Yikes", "Uh oh", "Oh no", "Oh my", "Duoh", "Oh", "Uh", "Whoops" };
	private static final Random RANDOM = new Random();

	public static void show(String headerText, Throwable ex) {

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		String prefix = EXCEPTION_PREFIXES[RANDOM.nextInt(EXCEPTION_PREFIXES.length)];
		alert.setHeaderText(prefix + ", " + headerText + ".");
		alert.setContentText(ex.getLocalizedMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}

}
