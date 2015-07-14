package com.github.flqw.ast.explorer;

import java.lang.reflect.Method;

import com.github.flqw.ast.parsers.AstNode;
import com.github.flqw.ast.parsers.AstUtility;

public class NodeMethod {

	public NodeMethod(AstNode<?> node, Method method) {
		this.node = node;
		this.method = method;
		Object temp = null;
		try {
			temp = method.invoke(node.getUnderlyingNode());
		} catch (Exception e) {
			// We don't care.
		}

		temp = processValue(temp);

		value = temp;

	}

	private Object processValue(Object o) {
		if (o == null) {
			return o;
		}

		Controller controller = Controller.INSTANCE;
		AstUtility<?> utility = controller.astUtility;

		if (utility.getNodeSuperClass().isAssignableFrom(o.getClass())) {

			AstNode<?> node = controller.rootNode.getNodeWithUnderlyingNode(o);

			if (node != null) {
				return node;
			}
		}
		return o;
	}

	final AstNode<?> node;
	final Method method;
	final Object value;

}