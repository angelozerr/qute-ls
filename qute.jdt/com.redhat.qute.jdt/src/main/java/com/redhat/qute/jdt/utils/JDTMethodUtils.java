package com.redhat.qute.jdt.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;

public class JDTMethodUtils {

	private static final Logger LOGGER = Logger.getLogger(JDTMethodUtils.class.getName());

	private static final String RETURN_TYPE_SEPARATOR = " : ";

	private JDTMethodUtils() {

	}

	public static String getMethodSignature(IMethod method) throws JavaModelException {
		StringBuilder description = new StringBuilder();

		// method name
		description.append(method.getElementName());

		// parameters
		description.append('(');
		appendUnboundedParameterList(description, method);
		description.append(')');

		// return type
		if (!method.isConstructor()) {
			// TODO remove SignatureUtil.fix83600 call when bugs are fixed
			String returnType = String
					.valueOf(Signature.getReturnType(SignatureUtil.fix83600(method.getSignature().toCharArray())));
			try {
				returnType = createTypeDisplayName(SignatureUtil.getUpperBound(
						Signature.getReturnType(SignatureUtil.fix83600(method.getSignature().toCharArray()))));
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error while getting return type of '" + method.getElementName() + "'.");
			}
			description.append(RETURN_TYPE_SEPARATOR);
			description.append(returnType);
		}
		return description.toString(); // dummy
	}

	/**
	 * Appends the parameter list to <code>buffer</code>.
	 *
	 * @param buffer the buffer to append to
	 * @param method the method proposal
	 * @return the modified <code>buffer</code>
	 * @throws JavaModelException
	 */
	private static StringBuilder appendUnboundedParameterList(StringBuilder buffer, IMethod method)
			throws JavaModelException {
		// TODO remove once https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
		// gets fixed.
		String[] parameterNames = method.getParameterNames();
		String[] parameterTypes = method.getParameterTypes();

		for (int i = 0; i < parameterTypes.length; i++) {
			try {
				parameterTypes[i] = createTypeDisplayName(
						SignatureUtil.getLowerBound(SignatureUtil.fix83600(parameterTypes[i].toCharArray())));
			} catch (Exception e) {
				LOGGER.log(Level.WARNING,
						"Error while getting method parameter '" + i + "' of '" + method.getElementName() + "'.");
			}
		}

		/*
		 * if (Flags.isVarargs(methodProposal.getFlags())) { int index=
		 * parameterTypes.length - 1; parameterTypes[index]=
		 * convertToVararg(parameterTypes[index]); }
		 */
		return appendParameterSignature(buffer, parameterTypes, parameterNames);
	}

	/**
	 * Returns the display string for a java type signature.
	 *
	 * @param typeSignature the type signature to create a display name for
	 * @return the display name for <code>typeSignature</code>
	 * @throws IllegalArgumentException if <code>typeSignature</code> is not a valid
	 *                                  signature
	 * @see Signature#toCharArray(char[])
	 * @see Signature#getSimpleName(char[])
	 */
	private static String createTypeDisplayName(char[] typeSignature) throws IllegalArgumentException {
		try {
			char[] displayName = Signature.getSimpleName(Signature.toCharArray(typeSignature));

			// XXX see https://bugs.eclipse.org/bugs/show_bug.cgi?id=84675
			boolean useShortGenerics = false;
			if (useShortGenerics) {
				StringBuilder buf = new StringBuilder();
				buf.append(displayName);
				int pos;
				do {
					pos = buf.indexOf("? extends "); //$NON-NLS-1$
					if (pos >= 0) {
						buf.replace(pos, pos + 10, "+"); //$NON-NLS-1$
					} else {
						pos = buf.indexOf("? super "); //$NON-NLS-1$
						if (pos >= 0) {
							buf.replace(pos, pos + 8, "-"); //$NON-NLS-1$
						}
					}
				} while (pos >= 0);
				return buf.toString();
			}
			return String.valueOf(displayName);
		} catch (Exception e) {
			return String.valueOf(typeSignature);
		}
	}

	/**
	 * Creates a display string of a parameter list (without the parentheses) for
	 * the given parameter types and names.
	 *
	 * @param buffer         the string buffer
	 * @param parameterTypes the parameter types
	 * @param parameterNames the parameter names
	 * @return the display string of the parameter list defined by the passed
	 *         arguments
	 */
	private static StringBuilder appendParameterSignature(StringBuilder buffer, String[] parameterTypes,
			String[] parameterNames) {
		if (parameterTypes != null) {
			for (int i = 0; i < parameterTypes.length; i++) {
				if (i > 0) {
					buffer.append(',');
					buffer.append(' ');
				}
				buffer.append(parameterTypes[i]);
				if (parameterNames != null && parameterNames[i] != null) {
					buffer.append(' ');
					buffer.append(parameterNames[i]);
				}
			}
		}
		return buffer;
	}
}
