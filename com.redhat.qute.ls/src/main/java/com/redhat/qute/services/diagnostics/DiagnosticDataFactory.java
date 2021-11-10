package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_ITERABLE;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_NAME;

import com.google.gson.JsonObject;

public class DiagnosticDataFactory {

	public static JsonObject createUndefinedVariableData(String partName, boolean iterable) {
		JsonObject data = new JsonObject();
		data.addProperty(DIAGNOSTIC_DATA_NAME, partName);
		data.addProperty(DIAGNOSTIC_DATA_ITERABLE, iterable);
		return data;
	}
}
