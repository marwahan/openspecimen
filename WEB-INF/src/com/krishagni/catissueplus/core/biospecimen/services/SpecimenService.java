
package com.krishagni.catissueplus.core.biospecimen.services;

import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.core.biospecimen.events.PrintSpecimenLabelDetail;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenInfo;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenLabelPrintJobSummary;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenStatusDetail;
import com.krishagni.catissueplus.core.common.events.EntityQueryCriteria;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public interface SpecimenService {
	public ResponseEvent<SpecimenDetail> getSpecimen(RequestEvent<EntityQueryCriteria> req);
	
	public ResponseEvent<List<SpecimenInfo>> getSpecimens(RequestEvent<List<String>> req);
	
	public ResponseEvent<SpecimenDetail> createSpecimen(RequestEvent<SpecimenDetail> req);
	
	public ResponseEvent<SpecimenDetail> updateSpecimen(RequestEvent<SpecimenDetail> req);
	
	public ResponseEvent<SpecimenDetail> patchSpecimen(RequestEvent<SpecimenDetail> req);
	
	public ResponseEvent<SpecimenDetail> updateSpecimenStatus(RequestEvent<SpecimenStatusDetail> req);
	
	public ResponseEvent<List<SpecimenDetail>> collectSpecimens(RequestEvent<List<SpecimenDetail>> req);
	
	public ResponseEvent<Boolean> doesSpecimenExists(RequestEvent<String> label);
	
	public ResponseEvent<SpecimenLabelPrintJobSummary> printSpecimenLabels(RequestEvent<PrintSpecimenLabelDetail> req);
	
	/** Mostly present for UI **/
	public ResponseEvent<Map<String, Long>> getCprAndVisitIds(RequestEvent<Long> req);
}
