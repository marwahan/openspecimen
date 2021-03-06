package com.krishagni.catissueplus.core.importer.services;

import java.util.Map;

import com.krishagni.catissueplus.core.importer.domain.ObjectSchema;

public interface ObjectSchemaFactory {
	public ObjectSchema getSchema(String name);
	
	public ObjectSchema getSchema(String name, Map<String, Object> params);
	
	public void registerSchema(String name, ObjectSchema schema);
}
