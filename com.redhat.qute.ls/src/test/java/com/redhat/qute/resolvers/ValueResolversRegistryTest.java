package com.redhat.qute.resolvers;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.services.datamodel.ValueResolversRegistry;

public class ValueResolversRegistryTest {

	@Test 
	public void match() {
		ValueResolversRegistry registry = new ValueResolversRegistry();
		ResolvedJavaClassInfo javaType = new ResolvedJavaClassInfo();
		javaType.setClassName("org.acme.Item");
		javaType.setExtendedTypes(Arrays.asList("java.lang.Object"));
		
		List<ValueResolver> resolvers = registry.getResolversFor(javaType);
		System.err.println(resolvers.get(0).getSignature());
	}
}
