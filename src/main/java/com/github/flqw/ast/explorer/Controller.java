package com.github.flqw.ast.explorer;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.github.flqw.ast.parsers.AstNode;
import com.github.flqw.ast.parsers.AstUtility;
import com.github.flqw.ast.parsers.ecmascript.EcmascriptAstUtility;
import com.github.flqw.ast.parsers.erb.ErbAstUtility;
import com.github.flqw.ast.parsers.java.JavaAstUtility;
import com.github.flqw.ast.parsers.jaxen.Attribute;
import com.github.flqw.ast.parsers.ruby.RubyAstUtility;

import org.jaxen.JaxenException;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class Controller implements Initializable {

	@FXML TextField xPathTextField;
	@FXML AstCodeArea code;
	@FXML AstTreeView tree;
	@FXML ComboBox<Object> xPathResultsChoiceBox;
	@FXML Button goButton;
	@FXML Label highlightName;
	@FXML AstNodeTableView nodeTable;
	@FXML SplitPane splitPane;
	@FXML Label xPathResultCount;
	@FXML ToggleButton modeSwitchButton;
	@FXML ComboBox<AstUtility<?>> languageComboBox;

	private static final EcmascriptAstUtility ECMASCRIPT = EcmascriptAstUtility.INSTANCE;
	private static final JavaAstUtility JAVA = JavaAstUtility.INSTANCE;
	private static final RubyAstUtility RUBY = RubyAstUtility.INSTANCE;
	private static final ErbAstUtility ERB = ErbAstUtility.INSTANCE;

	public boolean editing;
	public AstNode<?> rootNode;
	public AstUtility<?> astUtility = JAVA;
	private HBox upperBar;

	private ObservableList<Object> xPathResults = FXCollections.observableArrayList();

	private ChangeListener<? super Object> selectionChangeListener = (observable, oldValue, newValue) -> {
		if (editing) {
			return;
		}
		if (newValue instanceof AstNode) {
			AstNode<?> node = (AstNode<?>) newValue;
			code.selectNode(node);
			tree.selectNode(node);
		} else if (newValue instanceof Attribute) {
			Attribute<?> attribute = (Attribute<?>) newValue;
			AstNode<?> node = attribute.getParent();
			code.selectNode(node);
			tree.selectNode(node);
		} else {
			code.deselect();
		}
	};
	private double[] savedDividerPositions;

	// For easier access
	public static Controller INSTANCE;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		INSTANCE = this;

		xPathResultsChoiceBox.setItems(xPathResults);
		goButton.addEventHandler(MOUSE_CLICKED, e -> findXPath());
		xPathResultsChoiceBox.setCellFactory(param -> new NodeListCell());
		xPathResultsChoiceBox.setButtonCell(new NodeListCell());
		xPathResultsChoiceBox.valueProperty().addListener(selectionChangeListener);
		upperBar = (HBox) xPathResultsChoiceBox.getParent();
		hideXPathResultsBox();
		loadFonts();
		code.initialize(this);
		tree.initialize(this);
		nodeTable.initialize(this);
		splitPane.setDividerPositions(0.33, 0.66);
		initializeModeSwitchButton();
		loadAST();

		languageComboBox.getItems().addAll(ECMASCRIPT, JAVA, RUBY, ERB);
		languageComboBox.setValue(astUtility);
		languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> astUtility = newValue);
		languageComboBox.setButtonCell(new LanguageCell());
		languageComboBox.setCellFactory(param -> new LanguageCell());

		upperBar.getChildren().remove(languageComboBox);
	}

	private void loadFonts() {
		Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/SourceCodePro-Regular.ttf"), 13);
		Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/SourceCodePro-Bold.ttf"), 13);
	}

	private void initializeModeSwitchButton() {
		modeSwitchButton.setOnAction(e -> {
			if (editing) {
				if (!loadAST()) {
					modeSwitchButton.setSelected(true);
					return;
				}
				showTreeAndTable();
				hideLanguageBox();
				code.getStyleClass().remove("editing");
			} else {
				// Remove all styles
				code.removeNodeHighlight();
				code.deselectNode();
				hideXPathResultsBox();
				showLanguageBox();
				tree.clear();
				nodeTable.clear();
				xPathResultCount.setText("");
				code.getStyleClass().add("editing");
				hideTreeAndTable();
			}
			editing = !editing;
			code.setEditable(editing);
		});
	}

	private void hideLanguageBox() {
		upperBar.getChildren().remove(languageComboBox);
	}

	private void showLanguageBox() {
		upperBar.getChildren().add(languageComboBox);
	}

	private void showTreeAndTable() {
		splitPane.getItems().add(tree);
		splitPane.getItems().add(nodeTable);
		splitPane.setDividerPositions(savedDividerPositions);
	}

	private void hideTreeAndTable() {
		savedDividerPositions = splitPane.getDividerPositions();
		splitPane.getItems().remove(tree);
		splitPane.getItems().remove(nodeTable);
	}

	private void findXPath() {
		String xPath = xPathTextField.getText();
		List<Object> results;
		try {
			results = astUtility.findXPath(rootNode, xPath);
			xPathResults.clear();
			xPathResults.addAll(results);
			if (results.size() == 1) {
				xPathResultCount.setText("1 Result");
			} else {
				xPathResultCount.setText(results.size() + " Results");
			}
			if (!results.isEmpty()) {
				showXPathResultsBox(results);
			} else {
				hideXPathResultsBox();
			}
		} catch (JaxenException e) {
			hideXPathResultsBox();
			xPathResultCount.setText("");
			ExceptionAlert.show("invalid XPath", e);
		}
	}

	private boolean hideXPathResultsBox() {
		return upperBar.getChildren().remove(xPathResultsChoiceBox);
	}

	private void showXPathResultsBox(List<Object> results) {
		xPathResultsChoiceBox.setValue(results.get(0));
		if (!upperBar.getChildren().contains(xPathResultsChoiceBox)) {
			upperBar.getChildren().add(xPathResultsChoiceBox);
		}
	}

	public boolean loadAST() {
		String sourceCode = code.getText();
		try {
			rootNode = astUtility.parse(sourceCode);
			tree.build(rootNode);
			return true;
		} catch (Exception e) {
			ExceptionAlert.show("syntax Error", e);
			return false;
		}
	}


	private final class LanguageCell extends ListCell<AstUtility<?>> {
		@Override
		protected void updateItem(AstUtility<?> item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText("");
				return;
			}
			setText(item.getLanguage());
		}
	}


	public static class NodeListCell extends ListCell<Object> {
		@Override
		protected void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText("");
				return;
			}
			if (item instanceof AstNode) {
				setText(item.toString());
			} else if (item instanceof Attribute) {
				Attribute<?> attribute = (Attribute<?>) item;
				setText("@" + attribute.getName() + " = " + attribute.getValue() + " (" + attribute.getParent() + ")");
			} else {
				setText(item.getClass().getSimpleName() + ": " + item.toString());
			}
		}
	}



}
