package com.redhat.qute.jdt.internal.template;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.redhat.qute.commons.datamodel.ParameterDataModel;
import com.redhat.qute.commons.datamodel.TemplateDataModel;

public class TemplateDataCollector extends ASTVisitor {

	private final IMethod method;

	private final TemplateDataModel<ParameterDataModel> template;

	public TemplateDataCollector(IMethod method, TemplateDataModel<ParameterDataModel> template,
			IProgressMonitor monitor) {
		this.method = method;
		this.template = template;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getName().isSimpleName() && method.getElementName().equals(node.getName().getIdentifier())) {
			node.getBody();

		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// TODO Auto-generated method stub
		String className = node.getParent().toString(); // .getName().getIdentifier();
		String methodName = node.getName().getIdentifier();
		if (template.getSourceMethod().equals(methodName)) {
			ASTNode parent = node.getParent();
			while (parent != null && parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
				if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
					MethodInvocation invocation = (MethodInvocation) parent;
					if ("data".equals(invocation.getName().getIdentifier())) {
						// .data("book", book)
						List arguments = invocation.arguments();
						if (arguments.size() == 2) {

							String paramName = null;
							Object name = arguments.get(0);
							if (name instanceof StringLiteral) {
								paramName = ((StringLiteral) name).getLiteralValue();
							}

							String paramType = null;
							Object second = arguments.get(1);
							if (second instanceof SimpleName) {
								paramType = ((SimpleName) second).getIdentifier();
							}

							ParameterDataModel parameter = new ParameterDataModel();
							parameter.setKey(paramName);
							parameter.setSourceType("java.lang.String");
							template.getParameters().add(parameter);
						}
					}
					parent = parent.getParent();
				}
			}
		}
		return super.visit(node);
	}

}
