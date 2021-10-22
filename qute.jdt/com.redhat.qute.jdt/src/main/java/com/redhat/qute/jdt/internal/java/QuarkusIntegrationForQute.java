package com.redhat.qute.jdt.internal.java;

import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getASTRoot;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.hasQuteSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CodeLens;

import com.redhat.qute.jdt.utils.IJDTUtils;

public class QuarkusIntegrationForQute {

	public static List<? extends CodeLens> codeLens(ITypeRoot typeRoot, IJDTUtils utils, IProgressMonitor monitor) {
		if (typeRoot == null || !hasQuteSupport(typeRoot.getJavaProject())) {
			return Collections.emptyList();
		}		
		List<CodeLens> lenses = new ArrayList<>();
		CompilationUnit cu = getASTRoot(typeRoot);
		cu.accept(new QuteJavaCodeLensCollector(typeRoot, lenses, utils, monitor));
		return lenses;
	}
}
