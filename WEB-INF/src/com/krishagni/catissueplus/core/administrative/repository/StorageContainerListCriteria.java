package com.krishagni.catissueplus.core.administrative.repository;

import java.util.Set;

import com.krishagni.catissueplus.core.common.events.AbstractListCriteria;

public class StorageContainerListCriteria extends AbstractListCriteria<StorageContainerListCriteria> {

	private boolean onlyFreeContainers;
	
	private Long parentContainerId;
	
	private String siteName;
	
	private boolean includeChildren;
	
	private boolean topLevelContainers;
	
	private Boolean storeSpecimensEnabled;
	
	private String specimenClass;
	
	private String specimenType;
	
	private boolean hierarchical;
	
	private Long cpId;
	
	private Set<Long> siteIds;
	
	@Override
	public StorageContainerListCriteria self() {
		return this;
	}
	
	public boolean onlyFreeContainers() {
		return onlyFreeContainers;
	}
	
	public StorageContainerListCriteria onlyFreeContainers(boolean onlyFreeContainers) {
		this.onlyFreeContainers = onlyFreeContainers;
		return self();
	}
	
	public Long parentContainerId() {
		return parentContainerId;
	}
	
	public StorageContainerListCriteria parentContainerId(Long parentContainerId) {
		this.parentContainerId = parentContainerId;
		return self();
	}
	
	public String siteName() {
		return siteName;
	}
	
	public StorageContainerListCriteria siteName(String siteName) {
		this.siteName = siteName;
		return self();
	}
	
	public boolean includeChildren() {
		return includeChildren;
	}
	
	public StorageContainerListCriteria includeChildren(boolean includeChildren) {
		this.includeChildren = includeChildren;
		return self();
	}
	
	public boolean topLevelContainers() {
		return topLevelContainers;
	}
	
	public StorageContainerListCriteria topLevelContainers(boolean topLevelContainers) {
		this.topLevelContainers = topLevelContainers;
		return self();
	}
	
	public Boolean storeSpecimensEnabled() {
		return storeSpecimensEnabled;
	}
	
	public StorageContainerListCriteria storeSpecimensEnabled(Boolean storeSpecimensEnabled) {
		this.storeSpecimensEnabled = storeSpecimensEnabled;
		return self();
	}
	
	public String specimenClass() {
		return specimenClass;
	}
	
	public StorageContainerListCriteria specimenClass(String specimenClass) {
		this.specimenClass = specimenClass;
		return self();
	}
	
	public String specimenType() {
		return specimenType;
	}
	
	public StorageContainerListCriteria specimenType(String specimenType) {
		this.specimenType = specimenType;
		return self();
	}
	
	public boolean hierarchical() {
		return hierarchical;
	}
	
	public StorageContainerListCriteria hierarchical(boolean hierarchical) {
		this.hierarchical = hierarchical;
		return self();
	}
	
	public Long cpId() {
		return cpId;
	}
	
	public StorageContainerListCriteria cpId(Long cpId) {
		this.cpId = cpId;
		return self();
	}	
	
	public Set<Long> siteIds() {
		return siteIds;
	}
	
	public void siteIds(Set<Long> siteIds) {
		this.siteIds = siteIds;
	}
}
