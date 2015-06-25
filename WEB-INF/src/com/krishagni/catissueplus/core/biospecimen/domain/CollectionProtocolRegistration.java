
package com.krishagni.catissueplus.core.biospecimen.domain;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.ParticipantErrorCode;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.CollectionUpdater;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.util.Status;
import com.krishagni.catissueplus.core.common.util.Utility;

@Configurable
@Audited
public class CollectionProtocolRegistration {
	private static final String ENTITY_NAME = "collection_protocol_registration";
	
	private Long id;

	private String ppid;

	private Date registrationDate;

	private Participant participant;

	private CollectionProtocol collectionProtocol;

	private Collection<Visit> visits = new HashSet<Visit>();

	private String activityStatus;

	private String signedConsentDocumentUrl;

	private Date consentSignDate;

	private User consentWitness;
	
	private String signedConsentDocumentName;

	private Set<ConsentTierResponse> consentResponses = new HashSet<ConsentTierResponse>();

	private String barcode;
	
	@Autowired
	private DaoFactory daoFactory;

	public static String getEntityName() {
		return ENTITY_NAME;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPpid() {
		return ppid;
	}

	public void setPpid(String ppid) {
		this.ppid = ppid;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public CollectionProtocol getCollectionProtocol() {
		return collectionProtocol;
	}

	public void setCollectionProtocol(CollectionProtocol collectionProtocol) {
		this.collectionProtocol = collectionProtocol;
	}

	@NotAudited
	public Collection<Visit> getVisits() {
		return visits;
	}

	public void setVisits(Collection<Visit> visits) {
		this.visits = visits;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		if (StringUtils.isBlank(activityStatus)) {
			activityStatus = Status.ACTIVITY_STATUS_ACTIVE.getStatus();
		}
		
		this.activityStatus = activityStatus;
	}

	public String getSignedConsentDocumentUrl() {
		return signedConsentDocumentUrl;
	}

	public void setSignedConsentDocumentUrl(String signedConsentDocumentUrl) {
		this.signedConsentDocumentUrl = signedConsentDocumentUrl;
	}

	public Date getConsentSignDate() {
		return consentSignDate;
	}

	public void setConsentSignDate(Date consentSignDate) {
		this.consentSignDate = consentSignDate;
	}

	public User getConsentWitness() {
		return consentWitness;
	}

	public void setConsentWitness(User consentWitness) {
		this.consentWitness = consentWitness;
	}

	public String getSignedConsentDocumentName() {
		return signedConsentDocumentName;
	}

	public void setSignedConsentDocumentName(String signedConsentDocumentName) {
		this.signedConsentDocumentName = signedConsentDocumentName;
	}

	public Set<ConsentTierResponse> getConsentResponses() {
		return consentResponses;
	}

	public void setConsentResponses(Set<ConsentTierResponse> consentResponses) {
		this.consentResponses = consentResponses;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public boolean isActive() {
		return Status.ACTIVITY_STATUS_ACTIVE.getStatus().equals(this.getActivityStatus());
	}

	public void setActive() {
		setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.getStatus());
	}

	public List<DependentEntityDetail> getDependentEntities() {
		return DependentEntityDetail.singletonList(Visit.getEntityName(), getActiveVisits()); 
	}
	
	public void updateActivityStatus(String activityStatus) {
		if (this.activityStatus != null && this.activityStatus.equals(activityStatus)) {
			return;
		}
		
		if (Status.ACTIVITY_STATUS_DISABLED.getStatus().equals(activityStatus)) {
			delete();
		}
	}
	
	public void delete() {
		ensureNoActiveChildObjects();
		for (Visit visit : getVisits()) {
			visit.delete();
		}
		
		setBarcode(Utility.getDisabledValue(getBarcode()));
		setPpid(Utility.getDisabledValue(getPpid()));
		setActivityStatus(Status.ACTIVITY_STATUS_DISABLED.getStatus());
	}

	public void update(CollectionProtocolRegistration cpr) {
		updateActivityStatus(cpr.getActivityStatus());
		if (!isActive()) {
			return;
		}
		
		setRegistrationDate(cpr.getRegistrationDate());		
		setBarcode(cpr.getBarcode());
		setParticipant(cpr.getParticipant());
		updateConsents(cpr);
	}
	
	public void updateConsents(CollectionProtocolRegistration cpr) {
		setConsentSignDate(cpr.getConsentSignDate());
		setConsentWitness(cpr.getConsentWitness());
		setConsentresponses(cpr.getConsentResponses());
	}
	
	public void setPpidIfEmpty() {
		if (StringUtils.isNotBlank(ppid)) {
			return;
		}
		
		CollectionProtocol cp = getCollectionProtocol();
		String ppidFmt = cp.getPpidFormat();
		if (StringUtils.isNotBlank(ppidFmt)) {
			Long uniqueId = daoFactory.getUniqueIdGenerator().getUniqueId("PPID", cp.getShortTitle());
			setPpid(String.format(ppidFmt, uniqueId.intValue()));
		} else {
			setPpid(cp.getId() + "_" + participant.getId());
		}		
	}

	private void setConsentresponses(Set<ConsentTierResponse> consentResponses) {
		CollectionUpdater.update(getConsentResponses(), consentResponses);
		for (ConsentTierResponse resp : getConsentResponses()) {
			resp.setCpr(this);
		}
	}

	private void ensureNoActiveChildObjects() {
		for (Visit visit : getVisits()) {
			if (visit.isActive() && visit.isCompleted()) {
				throw OpenSpecimenException.userError(ParticipantErrorCode.REF_ENTITY_FOUND);
			}			
		}
	}	
	
	private int getActiveVisits() {
		int count = 0;
		for (Visit visit : getVisits()) {
			if (visit.isActive() && visit.isCompleted()) {
				++count;
			}
		}
		
		return count;
	}


}
