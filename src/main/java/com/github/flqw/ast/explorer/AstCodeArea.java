package com.github.flqw.ast.explorer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.StyleSpans;

import com.github.flqw.ast.parsers.AstNode;

public class AstCodeArea extends CodeArea {

	private static final String SELECTED = "selected";
	private static final String HIGHLIGHTED = "highlighted";

//	private static final String DEMO_CODE = "var test = \"Hello World!\"";
	private static final String DEMO_CODE = "public class Test {\n\tvoid test() {\n\t\tSystem.out.println(\"Test\");\n\t}\n}";

	private AstNode<?> currentlySelectedNode;
	private AstNode<?> currentlyHighlightedNode;
	private Controller controller;

	public void initialize(Controller controller) {
		this.controller = controller;
		addEventHandlers();
		addLineNumbers();
		replaceText(DEMO_CODE);
		setEditable(false);
	}

	private void addLineNumbers() {
		IntFunction<javafx.scene.Node> lineNumberFactory = LineNumberFactory.get(this);
        IntFunction<javafx.scene.Node> graphicFactory = line -> {
            HBox padding = new HBox();
            padding.setPrefWidth(10);
			HBox hbox = new HBox(lineNumberFactory.apply(line), padding);
            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };
        setParagraphGraphicFactory(graphicFactory);
	}

	private static final EventHandler<MouseOverTextEvent> mouseOverFilter = e -> e.consume();

	private void addEventHandlers() {

		setMouseOverTextDelay(Duration.ofMillis(1)); // 0 equals to disabled, as nothing does.
		addEventHandler(ScrollEvent.SCROLL_STARTED, e -> {
			addEventFilter(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, mouseOverFilter);
		});

		addEventHandler(ScrollEvent.SCROLL_FINISHED, e -> {
			removeEventFilter(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, mouseOverFilter);
		});

        addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
        	if (controller.editing || controller.rootNode == null) {
        		return;
        	}
            int index = e.getCharacterIndex();
            // Put the caret to the mouse position to avoid jumping
            positionCaret(index);


            AstNode<?> node = controller.rootNode.getNodeAt(index);
            if (node == null) {
            	return;
            }
            highlightNode(node);
        });

        setOnMouseExited(e -> {
        	removeNodeHighlight();
        });

		setOnMouseClicked(e -> {
			if (controller.editing || controller.rootNode == null) {
				return;
			}
			AstNode<?> node = controller.rootNode.getNodeAt(getCaretPosition());
			controller.tree.selectNode(node);
		});
	}

	public void jumpToNode(AstNode<?> node) {
		positionCaret(node.getStartOffset());
	}

	public void deselectNode() {
		if (currentlySelectedNode == null) {
			return;
		}
		removeClassFromNode(SELECTED, currentlySelectedNode);
		currentlySelectedNode = null;
	}

	public void selectNode(AstNode<?> node) {
		if (currentlySelectedNode == node) {
			return;
		}
		deselectNode();
		currentlySelectedNode = node;
		addClassToNode(SELECTED, node);
	}

	public void highlightNode(AstNode<?> node) {
		if (node == currentlyHighlightedNode) {
			return;
		}
		removeNodeHighlight();
        controller.highlightName.setText(node.getType());
		currentlyHighlightedNode = node;
		addClassToNode(HIGHLIGHTED, node);
	}

	public void removeNodeHighlight() {
		if (currentlyHighlightedNode == null) {
			return;
		}
        controller.highlightName.setText("");
        removeClassFromNode(HIGHLIGHTED, currentlyHighlightedNode);
        currentlyHighlightedNode = null;
	}

	private void addClassToNode(String styleClass, AstNode<?> node) {
		updateStylesForNode(node, styles -> {
			ArrayList<String> newClasses = new ArrayList<>(styles);
			newClasses.add(styleClass);
			return newClasses;
		});
	}

	private void removeClassFromNode(String styleClass, AstNode<?> node) {
		updateStylesForNode(node, styles -> {
			return styles.stream().filter(s -> !s.equals(styleClass)).collect(Collectors.toList());
		});
	}

	private void updateStylesForNode(AstNode<?> node, UnaryOperator<Collection<String>> mapper) {
		try {
			int from = node.getStartOffset();
			int to = node.getEndOffset();
			StyleSpans<Collection<String>> newStyles = getStyleSpans(from, to).mapStyles(mapper);
			setStyleSpans(from, newStyles);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
