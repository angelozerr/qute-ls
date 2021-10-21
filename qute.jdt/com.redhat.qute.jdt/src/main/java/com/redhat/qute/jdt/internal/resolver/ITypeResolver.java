package com.redhat.qute.jdt.internal.resolver;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;

public interface ITypeResolver {


	String resolveFieldType(IField field);
	
	String resolveMethodSignature(IMethod method);


}
