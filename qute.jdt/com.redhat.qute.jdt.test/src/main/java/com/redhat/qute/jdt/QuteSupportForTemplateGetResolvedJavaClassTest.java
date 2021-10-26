package com.redhat.qute.jdt;

import static com.redhat.qute.jdt.internal.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.internal.QuteProjectTest.loadMavenProject;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.commons.QuteResolvedJavaClassParams;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.jdt.internal.QuteProjectTest.QuteMavenProjectName;

public class QuteSupportForTemplateGetResolvedJavaClassTest {

	@Test
	public void iterable() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaClassParams params = new QuteResolvedJavaClassParams("java.lang.Iterable",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaClassInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaClassParams("Iterable", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("Iterable", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaClassParams("Iterable<String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("Iterable<String>", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("Iterable", result.getIterableType());
		Assert.assertEquals("String", result.getIterableOf());

		params = new QuteResolvedJavaClassParams("Iterable<java.lang.String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("Iterable<java.lang.String>", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());

		params = new QuteResolvedJavaClassParams("java.lang.Iterable<java.lang.String>",
				QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<java.lang.String>", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.lang.Iterable", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());
	}

	@Test
	public void list() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaClassParams params = new QuteResolvedJavaClassParams("java.util.List",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaClassInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.List", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.util.List", result.getIterableType());
		Assert.assertEquals("java.lang.Object", result.getIterableOf());

		params = new QuteResolvedJavaClassParams("List", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);

		params = new QuteResolvedJavaClassParams("List<String>", QuteMavenProjectName.qute_quickstart);
		Assert.assertNull(result);

		params = new QuteResolvedJavaClassParams("List<java.lang.String>", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);

		params = new QuteResolvedJavaClassParams("java.util.List<java.lang.String>",
				QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.List", result.getClassName());
		Assert.assertTrue(result.isIterable());
		Assert.assertEquals("java.util.List", result.getIterableType());
		Assert.assertEquals("java.lang.String", result.getIterableOf());
	}

	@Test
	public void someInterface() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaClassParams params = new QuteResolvedJavaClassParams("org.acme.qute.SomeInterface",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaClassInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaClass(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("org.acme.qute.SomeInterface", result.getClassName());
		Assert.assertFalse(result.isIterable());
		
		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getName() : java.lang.String", result.getMethods().get(0).getSignature());
	}
}
