package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.common.events.UserSummary;

@JsonFilter("withoutId")
@JsonInclude(Include.NON_NULL)
public class CollectionProtocolDetail extends CollectionProtocolSummary {
	private List<String> repositoryNames;
	
	private List<UserSummary> coordinators;

	private Boolean consentsWaived;

	private String irbId;

	private Long anticipatedParticipantsCount;

	private String descriptionUrl;

	private String specimenLabelFmt;

	private String derivativeLabelFmt;

	private String aliquotLabelFmt;

	private Boolean aliquotsInSameContainer;

	private String activityStatus;
	
	//
	// mostly used for export and import of CP
	// 
	private List<ConsentTierDetail> consents;
	
	private List<CollectionProtocolEventDetail> events;

	public List<String> getRepositoryNames() {
		return repositoryNames;
	}

	public void setRepositoryNames(List<String> repositoryNames) {
		this.repositoryNames = repositoryNames;
	}

	public List<UserSummary> getCoordinators() {
		return coordinators;
	}

	public void setCoordinators(List<UserSummary> coordinators) {
		this.coordinators = coordinators;
	}

	public Boolean getConsentsWaived() {
		return consentsWaived;
	}

	public void setConsentsWaived(Boolean consentsWaived) {
		this.consentsWaived = consentsWaived;
	}

	public String getIrbId() {
		return irbId;
	}

	public void setIrbId(String irbId) {
		this.irbId = irbId;
	}

	public Long getAnticipatedParticipantsCount() {
		return anticipatedParticipantsCount;
	}

	public void setAnticipatedParticipantsCount(Long anticipatedParticipantsCount) {
		this.anticipatedParticipantsCount = anticipatedParticipantsCount;
	}

	public String getDescriptionUrl() {
		return descriptionUrl;
	}

	public void setDescriptionUrl(String descriptionUrl) {
		this.descriptionUrl = descriptionUrl;
	}

	public String getSpecimenLabelFmt() {
		return specimenLabelFmt;
	}

	public void setSpecimenLabelFmt(String specimenLabelFmt) {
		this.specimenLabelFmt = specimenLabelFmt;
	}

	public String getDerivativeLabelFmt() {
		return derivativeLabelFmt;
	}

	public void setDerivativeLabelFmt(String derivativeLabelFmt) {
		this.derivativeLabelFmt = derivativeLabelFmt;
	}

	public String getAliquotLabelFmt() {
		return aliquotLabelFmt;
	}

	public void setAliquotLabelFmt(String aliquotLabelFmt) {
		this.aliquotLabelFmt = aliquotLabelFmt;
	}

	public Boolean getAliquotsInSameContainer() {
		return aliquotsInSameContainer;
	}

	public void setAliquotsInSameContainer(Boolean aliquotsInSameContainer) {
		this.aliquotsInSameContainer = aliquotsInSameContainer;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public List<ConsentTierDetail> getConsents() {
		return consents;
	}

	public void setConsents(List<ConsentTierDetail> consents) {
		this.consents = consents;
	}

	public List<CollectionProtocolEventDetail> getEvents() {
		return events;
	}

	public void setEvents(List<CollectionProtocolEventDetail> events) {
		this.events = events;
	}

	public static CollectionProtocolDetail from(CollectionProtocol cp) {
		return from(cp, false);
	}
	
	public static CollectionProtocolDetail from(CollectionProtocol cp, boolean fullObject) {
		CollectionProtocolDetail result = new CollectionProtocolDetail();
		CollectionProtocolSummary.copy(cp, result);
		result.setCoordinators(UserSummary.from(cp.getCoordinators()));

		result.setIrbId(cp.getIrbIdentifier());
		result.setAnticipatedParticipantsCount(cp.getEnrollment());
		result.setDescriptionUrl(cp.getDescriptionURL());
		result.setSpecimenLabelFmt(cp.getSpecimenLabelFormat());
		result.setDerivativeLabelFmt(cp.getDerivativeLabelFormat());
		result.setAliquotLabelFmt(cp.getAliquotLabelFormat());
		result.setActivityStatus(cp.getActivityStatus());
		result.setRepositoryNames(getRepositoryNames(cp.getRepositories()));
		
		if (fullObject) {
			result.setConsents(ConsentTierDetail.from(cp.getConsentTier()));
			result.setEvents(CollectionProtocolEventDetail.from(cp.getCollectionProtocolEvents(), true));
		}
		
		return result;
	}

	private static List<String> getRepositoryNames(Collection<Site> repositories) {
		List<String> repositoryNames = new ArrayList<String>();
		for (Site site: repositories) {
			repositoryNames.add(site.getName());
		}
		
		return repositoryNames;
	}
}
