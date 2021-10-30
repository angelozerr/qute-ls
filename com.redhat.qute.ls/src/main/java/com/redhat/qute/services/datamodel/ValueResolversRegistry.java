package com.redhat.qute.services.datamodel;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.ValueResolver;

public class ValueResolversRegistry {

	private List<ValueResolver> resolvers;

	public ValueResolversRegistry() {
		ValueResolverLoader loader = new Gson().fromJson(
				new InputStreamReader(ValueResolversRegistry.class.getResourceAsStream("qute-resolvers.json")),
				ValueResolverLoader.class);
		this.resolvers = loader.getResolvers();
	}

	public List<ValueResolver> getResolversFor(ResolvedJavaClassInfo javaType) {
		List<ValueResolver> matches = new ArrayList<>();
		for (ValueResolver resolver : resolvers) {
			if (resolver.match(javaType)) {
				matches.add(resolver);
			}
		}
		return matches;
	}

	private class ValueResolverLoader {
		private List<ValueResolver> resolvers;

		public List<ValueResolver> getResolvers() {
			return resolvers;
		}
	}
}
