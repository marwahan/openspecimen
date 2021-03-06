package com.krishagni.catissueplus.core.importer.services;

import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.importer.events.ImportObjectDetail;

public interface ObjectImporter<T> {
	public ResponseEvent<T> importObject(RequestEvent<ImportObjectDetail<T>> req);
}
