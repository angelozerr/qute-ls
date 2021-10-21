package com.redhat.qute.jdt.internal.resolver;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class CompilationUnitTypeResolver extends AbstractTypeResolver {

	private final Map<String, String> packages;
	private final IType primaryType;

	public CompilationUnitTypeResolver(ICompilationUnit compilationUnit) {
		this.packages = new HashMap<>();
		this.primaryType = compilationUnit.findPrimaryType();
		try {
			IImportDeclaration[] imports = compilationUnit.getImports();
			if (imports != null) {
				for (int i = 0; i < imports.length; i++) {
					String name = imports[i].getElementName();
					int importedIndex = name.lastIndexOf('.');
					String importedClassName = name.substring(importedIndex + 1, name.length());
					packages.put(importedClassName, name);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();

		}
	}

	@Override
	protected String resolveSimpleType(String type) {
		String resolvedType = packages.get(type);
		if (resolvedType == null) {
			try {
				String[][] resolvedNames = primaryType.resolveType(type);
				if (resolvedNames != null && resolvedNames.length > 0) {
					return resolvedNames[0][0] + '.' + resolvedNames[0][1];
				}
			} catch (JavaModelException e) {
				e.printStackTrace();

			}
		}
		return resolvedType != null ? resolvedType : type;
	}
}
