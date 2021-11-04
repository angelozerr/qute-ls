package com.redhat.qute.jdt.internal.resolver;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public abstract class AbstractTypeResolver implements ITypeResolver {

	@Override
	public String resolveFieldType(IField field) {
		try {
			return resolveTypeSignature(field.getTypeSignature());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String resolveMethodSignature(IMethod method) {
		StringBuilder signature = new StringBuilder(method.getElementName());
		signature.append('(');
		try {
			ILocalVariable[] parameters = method.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					signature.append(", ");
				}
				ILocalVariable parameter = parameters[i];
				signature.append(parameter.getElementName());
				signature.append(" : ");
				signature.append(resolveTypeSignature(parameter.getTypeSignature()));
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		signature.append(')');
		try {
			String returnType = resolveTypeSignature(method.getReturnType());
			signature.append(" : ");
			signature.append(returnType);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return signature.toString();
	}

	private String resolveTypeSignature(String typeSignature) {
		int arrayCount = Signature.getArrayCount(typeSignature);
		char type = typeSignature.charAt(arrayCount);
		if (type == Signature.C_UNRESOLVED) {
			String name = ""; //$NON-NLS-1$
			String genericName = null;
			int bracket = typeSignature.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
			if (bracket > 0) {
				name = typeSignature.substring(arrayCount + 1, bracket);
				int endBracket = typeSignature.indexOf(Signature.C_GENERIC_END, bracket);
				genericName = typeSignature.substring(bracket + 2, endBracket -1);
			} else {
				int semi = typeSignature.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
				if (semi == -1) {
					throw new IllegalArgumentException();
				}
				name = typeSignature.substring(arrayCount + 1, semi);
			}

			if (genericName != null) {
				genericName = resolveSimpleType(genericName);
			}

			String resolved = resolveSimpleType(name);
			if (resolved != null) {
				if (genericName != null) {
					return resolved + '<' + genericName + '>';
				}
				return resolved;
			}

			if (genericName != null) {
				return name + '<' + genericName + '>';
			}
			return name;
			/*
			 * String[][] resolvedNames= declaringType.resolveType(name); if (resolvedNames
			 * != null && resolvedNames.length > 0) { return
			 * concatenateName(resolvedNames[0][0], resolvedNames[0][1].replace('.',
			 * enclosingTypeSeparator)); }
			 */
			// return null;
		} else {
			return Signature.toString(typeSignature.substring(arrayCount));
		}
	}

	protected abstract String resolveSimpleType(String name);
}
