package com.krishagni.catissueplus.core.biospecimen.repository;

import java.util.List;

import com.krishagni.catissueplus.core.common.Pair;
import com.krishagni.catissueplus.core.common.events.AbstractListCriteria;

public class SpecimenListCriteria extends AbstractListCriteria<SpecimenListCriteria> {

	private List<Pair<Long, Long>> siteCps;
	
	private List<String> labels;
	
	@Override
	public SpecimenListCriteria self() {		
		return this;
	} 
	
	public List<Pair<Long, Long>> siteCps() {
		return siteCps;
	}
	
	public SpecimenListCriteria siteCps(List<Pair<Long, Long>> siteCps) {
		this.siteCps = siteCps;
		return self();
	}
	
	public List<String> labels() {
		return labels;
	}
	
	public SpecimenListCriteria labels(List<String> labels) {
		this.labels = labels;
		return self();
	}
}