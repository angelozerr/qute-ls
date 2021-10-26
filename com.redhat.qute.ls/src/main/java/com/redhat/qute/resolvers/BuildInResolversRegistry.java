package com.redhat.qute.resolvers;

import java.util.Collections;
import java.util.List;

import com.redhat.qute.commons.ResolvedJavaClassInfo;

public class BuildInResolversRegistry {

	public List<BuildInResolver> getResolvers(ResolvedJavaClassInfo resolvedType) {
		if (resolvedType.isIterable()) {
			
		}
		return Collections.emptyList();
	}
}
