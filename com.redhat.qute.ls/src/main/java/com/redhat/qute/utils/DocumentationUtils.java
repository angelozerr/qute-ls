package com.redhat.qute.utils;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMemberInfo.JavaMemberKind;
import com.redhat.qute.commons.JavaMethodInfo;
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
			documentation.append("**");
		}
		documentation.append(resolvedType.getClassName());
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(System.lineSeparator());

		// Class
		addParameter("Class", resolvedType.getClassName(), documentation, markdown);
		if (resolvedType.isIterable()) {
			addParameter("Iterable of", resolvedType.getIterableOf(), documentation, markdown);
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
			documentation.append("**");
		}
		documentation.append(member.getName());
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(System.lineSeparator());

		// Class
		addParameter("Class", member.getResolvedClass().getClassName(), documentation, markdown);
		if (member.getKind() == JavaMemberKind.FIELD) {
			// Field
			addParameter("Field name", member.getName(), documentation, markdown);
			addParameter("Field type", member.getMemberType(), documentation, markdown);
		} else {
			// Method/ Field
			addParameter("Method name", member.getName(), documentation, markdown);
			addParameter("Method signature", ((JavaMethodInfo) member).getSignature(), documentation, markdown);
		}

		return createMarkupContent(documentation, markdown);
	}

	private static void addParameter(String name, String value, StringBuilder documentation, boolean markdown) {
		if (value != null) {
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append(" * ");
			}
			documentation.append(name);
			documentation.append(": ");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(value);
			if (markdown) {
				documentation.append("`");
			}
		}
	}
}
