package com.redhat.qute.jdt.internal.resolver;

import org.eclipse.jdt.core.IClassFile;

public class ClassFileTypeResolver extends AbstractTypeResolver {

	public ClassFileTypeResolver(IClassFile classFile) {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String resolveSimpleType(String typeSignature) {
		return typeSignature;
	}

}
