package com.redhat.qute.services.commands;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ResolvedJavaClassInfo;
import com.redhat.qute.commons.datamodel.ParameterDataModel;

public class ResolvedParameterDataModel extends ParameterDataModel {
	private final CompletableFuture<ResolvedJavaClassInfo> future;

	public ResolvedParameterDataModel(ParameterDataModel parameter,
			CompletableFuture<ResolvedJavaClassInfo> resolvedJavaType) {
		super.setKey(parameter.getKey());
		super.setSourceType(parameter.getSourceType());
		this.future = resolvedJavaType;
	}

	public ResolvedJavaClassInfo getResolvedJavaType() {
		return future.getNow(null);
	}

}
