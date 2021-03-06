package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.krishagni.catissueplus.core.biospecimen.domain.AliquotSpecimensRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.DerivedSpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;
import com.krishagni.catissueplus.core.common.events.UserSummary;

@JsonFilter("withoutId")
public class SpecimenRequirementDetail implements Comparable<SpecimenRequirementDetail> {
	private Long id;
	
	private String name;
	
	private String lineage;
	
	private String specimenClass;
	
	private String type;
	
	private String anatomicSite;
	
	private String laterality;
	
	private String pathology;
	
	private String storageType;
	
	private Double initialQty;
	
	private Double concentration;
	
	private UserSummary collector;
	
	private String collectionProcedure;
	
	private String collectionContainer;
	
	private UserSummary receiver;
	
	private String labelFmt;
	
	private Long eventId;
	
	private List<SpecimenRequirementDetail> children;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public String getSpecimenClass() {
		return specimenClass;
	}

	public void setSpecimenClass(String specimenClass) {
		this.specimenClass = specimenClass;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAnatomicSite() {
		return anatomicSite;
	}

	public void setAnatomicSite(String anatomicSite) {
		this.anatomicSite = anatomicSite;
	}

	public String getLaterality() {
		return laterality;
	}

	public void setLaterality(String laterality) {
		this.laterality = laterality;
	}

	public String getPathology() {
		return pathology;
	}

	public void setPathology(String pathology) {
		this.pathology = pathology;
	}

	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public Double getInitialQty() {
		return initialQty;
	}

	public void setInitialQty(Double initialQty) {
		this.initialQty = initialQty;
	}

	public Double getConcentration() {
		return concentration;
	}

	public void setConcentration(Double concentration) {
		this.concentration = concentration;
	}

	public UserSummary getCollector() {
		return collector;
	}

	public void setCollector(UserSummary collector) {
		this.collector = collector;
	}

	public String getCollectionProcedure() {
		return collectionProcedure;
	}

	public void setCollectionProcedure(String collectionProcedure) {
		this.collectionProcedure = collectionProcedure;
	}

	public String getCollectionContainer() {
		return collectionContainer;
	}

	public void setCollectionContainer(String collectionContainer) {
		this.collectionContainer = collectionContainer;
	}

	public UserSummary getReceiver() {
		return receiver;
	}

	public void setReceiver(UserSummary receiver) {
		this.receiver = receiver;
	}

	public String getLabelFmt() {
		return labelFmt;
	}

	public void setLabelFmt(String labelFmt) {
		this.labelFmt = labelFmt;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public List<SpecimenRequirementDetail> getChildren() {
		return children;
	}

	public void setChildren(List<SpecimenRequirementDetail> children) {
		this.children = children;
	}

	@Override
	public int compareTo(SpecimenRequirementDetail other) {
		if (id != null && other.id != null) {
			return id.compareTo(other.id);
		} else if (id != null) {
			return -1;
		} else if(other.id != null) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public AliquotSpecimensRequirement toAliquotRequirement(Long parentSrId, int noOfAliquots) {
		AliquotSpecimensRequirement req = new AliquotSpecimensRequirement();
		req.setNoOfAliquots(noOfAliquots);
		req.setLabelFmt(getLabelFmt());
		req.setParentSrId(parentSrId);
		req.setQtyPerAliquot(getInitialQty());
		req.setStorageType(getStorageType());
		
		return req;		
	}
	
	public DerivedSpecimenRequirement toDerivedRequirement(Long parentSrId) {
		DerivedSpecimenRequirement req = new DerivedSpecimenRequirement();
		req.setConcentration(getConcentration());
		req.setLabelFmt(getLabelFmt());
		req.setName(getName());
		req.setParentSrId(parentSrId);
		req.setQuantity(getInitialQty());
		req.setSpecimenClass(getSpecimenClass());
		req.setStorageType(getStorageType());
		req.setType(getType());
		
		return req;
	}
	
	public static SpecimenRequirementDetail from(SpecimenRequirement sr) {
		SpecimenRequirementDetail detail = new SpecimenRequirementDetail();
		detail.setId(sr.getId());
		detail.setName(sr.getName());
		detail.setLineage(sr.getLineage());
		detail.setSpecimenClass(sr.getSpecimenClass());
		detail.setType(sr.getSpecimenType());
		detail.setAnatomicSite(sr.getAnatomicSite());
		detail.setLaterality(sr.getLaterality());
		detail.setPathology(sr.getPathologyStatus());
		detail.setStorageType(sr.getStorageType());
		detail.setInitialQty(sr.getInitialQuantity());
		detail.setConcentration(sr.getConcentration());
		detail.setCollector(sr.getCollector() == null ? null : UserSummary.from(sr.getCollector()));
		detail.setCollectionProcedure(sr.getCollectionProcedure());
		detail.setCollectionContainer(sr.getCollectionContainer());
		detail.setReceiver(sr.getReceiver() == null ? null : UserSummary.from(sr.getReceiver()));
		detail.setLabelFmt(sr.getLabelFormat());
		detail.setEventId(sr.getCollectionProtocolEvent().getId());
		detail.setChildren(from(sr.getChildSpecimenRequirements()));
		
		return detail;
	}
	
	public static List<SpecimenRequirementDetail> from(Collection<SpecimenRequirement> srs) {
		List<SpecimenRequirementDetail> result = new ArrayList<SpecimenRequirementDetail>();
		if (srs == null) {
			return result;
		}
		
		for (SpecimenRequirement sr : srs) {
			result.add(from(sr));
		}
		
		Collections.sort(result);
		return result;
	}	
}
