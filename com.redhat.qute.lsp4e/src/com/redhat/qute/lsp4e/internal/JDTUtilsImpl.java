package com.redhat.qute.lsp4e.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
//import org.eclipse.lsp4mp.commons.DocumentFormat;

import com.redhat.qute.jdt.utils.IJDTUtils;

public class JDTUtilsImpl implements IJDTUtils {
	private static final IJDTUtils INSTANCE = new JDTUtilsImpl();

	public static IJDTUtils getInstance() {
		return INSTANCE;
	}

	private JDTUtilsImpl() {
	}

	@Override
	public ICompilationUnit resolveCompilationUnit(String uriString) {
		return JDTUtils.resolveCompilationUnit(uriString);
	}

	public IClassFile resolveClassFile(String uriString) {
		return JDTUtils.resolveClassFile(uriString);
	}

	@Override
	public boolean isHiddenGeneratedElement(IJavaElement element) {
		return JDTUtils.isHiddenGeneratedElement(element);
	}

	@Override
	public Range toRange(IOpenable openable, int offset, int length) throws JavaModelException {
		return JDTUtils.toRange(openable, offset, length);
	}

	@Override
	public String toClientUri(String uri) {
		return ResourceUtils.toClientUri(uri);
	}

	@Override
	public String toUri(ITypeRoot typeRoot) {
		return JDTUtils.toUri(typeRoot);
	}

	@Override
	public void waitForLifecycleJobs(IProgressMonitor monitor) {
		/*
		 * try {
		 * Job.getJobManager().join(DocumentLifeCycleHandler.DOCUMENT_LIFE_CYCLE_JOBS,
		 * monitor); } catch (OperationCanceledException ignorable) { // No need to
		 * pollute logs when query is cancelled } catch (Exception e) {
		 * QuarkusLSPPlugin.logException(e.getMessage(), e); }
		 */
		// TODO: verify we need to synchronise on jobs
	}

	@Override
	public int toOffset(IBuffer buffer, int line, int column) {
		return JsonRpcHelpers.toOffset(buffer, line, column);
	}

	@Override
	public IFile findFile(String uriString) {
		return JDTUtils.findFile(uriString);
	}

	/**
	 * Enumeration for determining the location of a Java element. Either returns
	 * with the name range only, or the extended source range around the name of the
	 * element.
	 */
	public enum LocationType {
		/**
		 * This is range encapsulating only the name of the Java element.
		 */
		NAME_RANGE {

			@Override
			ISourceRange getRange(IJavaElement element) throws JavaModelException {
				return getNameRange(element);
			}

		},
		/**
		 * The range enclosing this element not including leading/trailing whitespace
		 * but everything else like comments. This information is typically used to
		 * determine if the client's cursor is inside the element.
		 */
		FULL_RANGE {

			@Override
			ISourceRange getRange(IJavaElement element) throws JavaModelException {
				return getSourceRange(element);
			}

		};

		/* default */ abstract ISourceRange getRange(IJavaElement element) throws JavaModelException;
	}

	public Location toLocation(IJavaElement element) throws JavaModelException {
		return toLocation(element, LocationType.NAME_RANGE);
	}
	
	/**
	 * Creates a location for a given java element. Unlike {@link #toLocation} this
	 * method can be called to return with a range that contains surrounding
	 * comments (method body), not just the name of the Java element. Element can be
	 * a {@link ICompilationUnit} or {@link IClassFile}
	 *
	 * @param element
	 * @param type the range type. The {@link LocationType#NAME_RANGE name} or {@link LocationType#FULL_RANGE full} range.
	 * @return location or null
	 * @throws JavaModelException
	 */
	public Location toLocation(IJavaElement element, LocationType type) throws JavaModelException {
		ICompilationUnit unit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
		if (unit == null && cf == null) {
			return null;
		}
		if (element instanceof ISourceReference) {
			ISourceRange nameRange = type.getRange(element);
			if (SourceRange.isAvailable(nameRange)) {
				if (cf == null) {
					return toLocation(unit, nameRange.getOffset(), nameRange.getLength());
				} else {
					return toLocation(cf, nameRange.getOffset(), nameRange.getLength());
				}
			}
		}
		return null;
	}

	public static ISourceRange getNameRange(IJavaElement element) throws JavaModelException {
		ISourceRange nameRange = null;
		if (element instanceof IMember) {
			IMember member = (IMember) element;
			nameRange = member.getNameRange();
			if ((!SourceRange.isAvailable(nameRange))) {
				nameRange = member.getSourceRange();
			}
		} else if (element instanceof ITypeParameter || element instanceof ILocalVariable) {
			nameRange = ((ISourceReference) element).getNameRange();
		} else if (element instanceof ISourceReference) {
			nameRange = ((ISourceReference) element).getSourceRange();
		}
		if (!SourceRange.isAvailable(nameRange) && element.getParent() != null) {
			nameRange = getNameRange(element.getParent());
		}
		return nameRange;
	}

	private static ISourceRange getSourceRange(IJavaElement element) throws JavaModelException {
		ISourceRange sourceRange = null;
		if (element instanceof IMember) {
			IMember member = (IMember) element;
			sourceRange = member.getSourceRange();
		} else if (element instanceof ITypeParameter || element instanceof ILocalVariable) {
			sourceRange = ((ISourceReference) element).getSourceRange();
		} else if (element instanceof ISourceReference) {
			sourceRange = ((ISourceReference) element).getSourceRange();
		}
		if (!SourceRange.isAvailable(sourceRange) && element.getParent() != null) {
			sourceRange = getSourceRange(element.getParent());
		}
		return sourceRange;
	}
	
	/**
	 * Creates location to the given offset and length for the compilation unit
	 *
	 * @param unit
	 * @param offset
	 * @param length
	 * @return location or null
	 * @throws JavaModelException
	 */
	public Location toLocation(ICompilationUnit unit, int offset, int length) throws JavaModelException {
		return new Location(ResourceUtils.toClientUri(toUri(unit)), toRange(unit, offset, length));
	}
	
	/**
	 * Creates a default location for the class file.
	 *
	 * @param classFile
	 * @return location
	 * @throws JavaModelException
	 */
	public Location toLocation(IClassFile classFile) throws JavaModelException{
		return toLocation(classFile, 0, 0);
	}
	
	/**
	 * Creates location to the given offset and length for the class file.
	 *
	 * @param unit
	 * @param offset
	 * @param length
	 * @return location
	 * @throws JavaModelException
	 */
	public Location toLocation(IClassFile classFile, int offset, int length) throws JavaModelException {
		String uriString = toUri(classFile);
		if (uriString != null) {
			Range range = toRange(classFile, offset, length);
			return new Location(uriString, range);
		}
		return null;
	}


/*	@Override
	public String getJavadoc(IMember member, DocumentFormat documentFormat) throws JavaModelException {
		// TODO Auto-generated method stub
		return null;
	}
*/
}
