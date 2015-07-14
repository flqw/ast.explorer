package com.github.flqw.ast.explorer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.flqw.ast.parsers.AstNode;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AstNodeTableView extends TableView<NodeMethod> {

	private Controller controller;

	public void initialize(Controller controller) {
		this.controller = controller;
		initializeColumns();
	}

	@SuppressWarnings("unchecked")
	private void initializeColumns() {
		TableColumn<NodeMethod, String> propertyColumn = new TableColumn<>("Property");
		TableColumn<NodeMethod, String> valueColumn = new TableColumn<>("Value");
		getColumns().addAll(propertyColumn, valueColumn);

		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		propertyColumn.setCellValueFactory(param -> {
			NodeMethod nodeMethod = param.getValue();
			String name = nodeMethod.method.getName();
			if (name.startsWith("is")) {
				name = name.substring(2);
			} else if (name.startsWith("get")) {
				name = name.substring(3);
			}
			return new SimpleStringProperty(name);
		});
		valueColumn.setCellValueFactory(param -> {
			NodeMethod nodeMethod = param.getValue();
			Object value = nodeMethod.value;

			return new SimpleStringProperty(value == null ? null : value.toString());
		});

		getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				controller.code.removeNodeHighlight();
				return;
			}
			Object value = newValue.value;
			if (value instanceof AstNode) {
				controller.code.highlightNode((AstNode<?>) value);
			} else {
				controller.code.removeNodeHighlight();
			}
		});
	}

	public void clear() {
		setItems(null);
	}

	public void fillForNode(AstNode<?> node) {
		List<NodeMethod> methods = Stream.of(node.getUnderlyingNode().getClass().getMethods())
//			.filter(m -> AstNode.class.isAssignableFrom(m.getDeclaringClass())) // No more generic methods
			.filter(m -> m.getName().startsWith("is") || m.getName().startsWith("get")) // Only getters
			.filter(m -> m.getParameterCount() == 0) // With 0 arguments
			.map(m -> new NodeMethod(node, m))
			.collect(Collectors.toList());

		setItems(FXCollections.observableArrayList(methods));
	}

}
