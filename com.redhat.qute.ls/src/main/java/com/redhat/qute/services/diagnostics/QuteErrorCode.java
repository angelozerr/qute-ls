package com.redhat.qute.services.diagnostics;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.ls.commons.CodeActionFactory;

public enum QuteErrorCode implements IQuteErrorCode {
	
	// Error code for resolving Java type
	ResolvingJavaType("Resolving Java type `{0}`."), //

	// Error code for namespaces
	UndefinedNamespace("No namespace resolver found for: `{0}`"), //

	// Error code for object, property, method parts
	UndefinedVariable("`{0}` cannot be resolved to a variable."), //
	UnkwownType("`{0}` cannot be resolved to a type."), //
	UnkwownMethod("`{0}` cannot be resolved or is not a method for `{1}` Java type."), //
	UnkwownProperty("`{0}` cannot be resolved or is not a field for `{1}` Java type."), //

	// Error code for #for / #each section
	NotInstanceOfIterable("`{0}` is not an instance of `java.lang.Iterable`."),

	// Error code for #include section
	TemplateNotFound("Template not found: `{0}`."), //
	TemplateNotDefined("Template id must be defined as parameter.");

	private final String unformatedMessage;

	QuteErrorCode(String unformatedMessage) {
		this.unformatedMessage = unformatedMessage;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public String getUnformatedMessage() {
		return unformatedMessage;
	}

	public boolean isQuteErrorCode(Either<String, Integer> code) {
		return CodeActionFactory.isDiagnosticCode(code, name());
	}
}
