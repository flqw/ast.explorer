package com.github.flqw.ast.explorer;

import com.github.flqw.ast.parsers.AstNode;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class AstTreeView extends TreeView<AstNode<?>> {

	private Controller controller;

	public void initialize(Controller controller) {
		this.controller = controller;
		setCellFactory(param -> new NodeTreeCell());
		addEventHandlers();
	}

	public void selectNode(AstNode<?> node) {
		selectTreeItemForNode(node, getRoot());
	}

	public void clear() {
		setRoot(null);
	}

	// public void build(Node rootNode) {
	public void build(AstNode<?> rootNode) {
		TreeItem<AstNode<?>> treeRoot = buildTreeViewRecursive(rootNode);
		setRoot(treeRoot);
		treeRoot.setExpanded(true); // Expand the root node for starters
	}

	private void selectTreeItemForNode(AstNode<?> toFind, TreeItem<AstNode<?>> current) {

		// Collapse everything on the way.
		current.setExpanded(false);

		if (current.getValue().equals(toFind)) {
			selectionModelProperty().get().select(current);
		}

		for (TreeItem<AstNode<?>> child : current.getChildren()) {
			selectTreeItemForNode(toFind, child);
		}
	}

	private void addEventHandlers() {
		getSelectionModel().selectedItemProperty().addListener(
				(ChangeListener<TreeItem<AstNode<?>>>) (observable, oldValue, newValue) -> {
					if (controller.editing) {
						return;
					}
					if (newValue == null) {
						controller.code.deselectNode();
						return;
					}
					AstNode<?> node = newValue.getValue();
					controller.code.selectNode(node);
					controller.code.jumpToNode(node);
					controller.nodeTable.fillForNode(node);
				});
	}

	private TreeItem<AstNode<?>> buildTreeViewRecursive(AstNode<?> node) {

		TreeItem<AstNode<?>> item = new TreeItem<>(node);

		for (AstNode<?> recursive : node.getChildren()) {
			item.getChildren().add(buildTreeViewRecursive(recursive));
		}

		return item;
	}

	private static class NodeTreeCell extends TreeCell<AstNode<?>> {
		@Override
		protected void updateItem(AstNode<?> item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText("");
				return;
			}
			setText(item.toString());
		}
	}

}
