package com.redhat.qute.parser.template;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.services.datamodel.ExtendedParameterDataModel;

public class Template extends Node {

	private String projectUri;

	private final TextDocument textDocument;

	private CancelChecker cancelChecker;

	private TemplateDataModelProvider dataModelProvider;

	Template(TextDocument textDocument) {
		super(0, textDocument.getText().length());
		this.textDocument = textDocument;
		super.setClosed(true);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Template;
	}

	public String getNodeName() {
		return "#template";
	}

	@Override
	public Template getOwnerTemplate() {
		return this;
	}

	public void setCancelChecker(CancelChecker cancelChecker) {
		this.cancelChecker = cancelChecker;
	}

	public CancelChecker getCancelChecker() {
		return cancelChecker;
	}

	public Position positionAt(int offset) throws BadLocationException {
		checkCanceled();
		return textDocument.positionAt(offset);
	}

	public int offsetAt(Position position) throws BadLocationException {
		checkCanceled();
		return textDocument.offsetAt(position);
	}

	public String lineText(int lineNumber) throws BadLocationException {
		checkCanceled();
		return textDocument.lineText(lineNumber);
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		checkCanceled();
		return textDocument.lineDelimiter(lineNumber);
	}

	private void checkCanceled() {
		if (cancelChecker != null) {
			cancelChecker.checkCanceled();
		}
	}

	public String getUri() {
		return textDocument.getUri();
	}

	public String getText() {
		return textDocument.getText();
	}

	public String getText(RangeOffset range) {
		return getText(range.getStart(), range.getEnd());
	}

	public String getText(int start, int end) {
		String text = getText();
		return text.substring(start, end);
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setDataModelProvider(TemplateDataModelProvider dataModelProvider) {
		this.dataModelProvider = dataModelProvider;
	}

	/**
	 * Try to find the class name
	 * <ul>
	 * <li>- from parameter declaration.</li>
	 * <li>- from @CheckedTemplate.</li>
	 * </ul>
	 * 
	 * @param partName
	 * @return
	 */
	public JavaTypeInfoProvider findInInitialDataModel(String partName) {
		// Try to find the class name from parameter declaration
		JavaTypeInfoProvider parameter = findParameterByAlias(partName);
		if (parameter != null) {
			return parameter;
		}
		// Try to find the class name from @CheckedTemplate
		return findParameterDataModel(partName).getNow(null);
	}

	private ParameterDeclaration findParameterByAlias(String alias) {
		Optional<ParameterDeclaration> result = super.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.filter(parameter -> alias.equals(((ParameterDeclaration) parameter).getAlias())) //
				.map(n -> ((ParameterDeclaration) n)) //
				.findFirst();
		if (result.isPresent()) {
			return result.get();
		}
		return null;
	}
	
	private CompletableFuture<ExtendedParameterDataModel> findParameterDataModel(String parameterName) {
		if (dataModelProvider == null) {
			return CompletableFuture.completedFuture(null);
		}
		return dataModelProvider.getTemplateDataModel(this). //
				thenApply(dataModel -> {
					return dataModel != null ? dataModel.getParameter(parameterName) : null;
				});
	}
}
