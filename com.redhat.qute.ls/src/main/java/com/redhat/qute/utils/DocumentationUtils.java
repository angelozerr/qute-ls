package com.redhat.qute.utils;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMemberInfo.JavaMemberKind;
import com.redhat.qute.commons.ResolvedJavaClassInfo;

/**
 * Utility for documentation.
 *
 */
public class DocumentationUtils {

	private DocumentationUtils() {

	}

	public static MarkupContent getDocumentation(ResolvedJavaClassInfo resolvedType, boolean markdown) {

		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(resolvedType.getClassName());
		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}
		return createMarkupContent(documentation, markdown);
	}

	private static MarkupContent createMarkupContent(StringBuilder documentation, boolean markdown) {
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	public static MarkupContent getDocumentation(JavaMemberInfo member, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(member.getMemberSimpleType());
		documentation.append(" ");
		if (member.getResolvedClass() != null) {
			documentation.append(member.getResolvedClass().getClassName());
			documentation.append(".");
		}
		documentation.append(member.getName());

		if (member.getKind() == JavaMemberKind.METHOD) {
			documentation.append('(');
			documentation.append(')');
		}

		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}

		if (member.getDescription() != null) {
			documentation.append(System.lineSeparator());
			documentation.append(member.getDescription());
		}

		return createMarkupContent(documentation, markdown);
	}
}
