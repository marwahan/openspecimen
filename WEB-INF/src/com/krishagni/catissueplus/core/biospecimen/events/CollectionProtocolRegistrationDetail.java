
package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.ConsentTierResponse;

@JsonSerialize(include=Inclusion.NON_NULL)
public class CollectionProtocolRegistrationDetail {
	private Long id;
	
	private ParticipantDetail participant;

	private Long cpId;
	
	private String cpTitle;
	
	private String cpShortTitle;

	private String ppid;

	private String barcode;

	private String activityStatus;

	private Date registrationDate;

	private ConsentDetail consentDetails;
	
	/** For UI efficiency **/
	private String specimenLabelFmt;
	
	private String aliquotLabelFmt;
	
	private String derivativeLabelFmt;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ParticipantDetail getParticipant() {
		return participant;
	}

	public void setParticipant(ParticipantDetail participant) {
		this.participant = participant;
	}

	public Long getCpId() {
		return cpId;
	}

	public void setCpId(Long cpId) {
		this.cpId = cpId;
	}

	public String getCpTitle() {
		return cpTitle;
	}

	public void setCpTitle(String cpTitle) {
		this.cpTitle = cpTitle;
	}
	
	public String getCpShortTitle() {
		return cpShortTitle;
	}

	public void setCpShortTitle(String cpShortTitle) {
		this.cpShortTitle = cpShortTitle;
	}

	public String getPpid() {
		return ppid;
	}

	public void setPpid(String ppid) {
		this.ppid = ppid;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public ConsentDetail getConsentDetails() {
		return consentDetails;
	}

	public void setConsentDetails(ConsentDetail consentDetails) {
		this.consentDetails = consentDetails;
	}

	public String getSpecimenLabelFmt() {
		return specimenLabelFmt;
	}

	public void setSpecimenLabelFmt(String specimenLabelFmt) {
		this.specimenLabelFmt = specimenLabelFmt;
	}

	public String getAliquotLabelFmt() {
		return aliquotLabelFmt;
	}

	public void setAliquotLabelFmt(String aliquotLabelFmt) {
		this.aliquotLabelFmt = aliquotLabelFmt;
	}

	public String getDerivativeLabelFmt() {
		return derivativeLabelFmt;
	}

	public void setDerivativeLabelFmt(String derivativeLabelFmt) {
		this.derivativeLabelFmt = derivativeLabelFmt;
	}

	public static CollectionProtocolRegistrationDetail from(CollectionProtocolRegistration cpr, boolean excludePhi) {		 
		CollectionProtocolRegistrationDetail detail = new CollectionProtocolRegistrationDetail();
		
		detail.setParticipant(ParticipantDetail.from(cpr.getParticipant(), excludePhi));
		detail.setId(cpr.getId());		
		detail.setActivityStatus(cpr.getActivityStatus());
		detail.setBarcode(cpr.getBarcode());
		detail.setPpid(cpr.getPpid());
		detail.setRegistrationDate(cpr.getRegistrationDate());
		
		ConsentDetail consent = new ConsentDetail();
		consent.setConsentDocumentUrl(cpr.getSignedConsentDocumentUrl());
		consent.setConsentSignatureDate(cpr.getConsentSignDate());
		
		String fileName = cpr.getSignedConsentDocumentName();
		if (fileName != null) {
			fileName = fileName.split("_", 2)[1];
		}
		consent.setConsentDocumentName(fileName);
		if (cpr.getConsentWitness() != null) {
			consent.setWitnessName(cpr.getConsentWitness().getEmailAddress());
		}
		
		if (cpr.getConsentResponses() != null) {
			for (ConsentTierResponse response : cpr.getConsentResponses()) {
				ConsentTierResponseDetail stmt = new ConsentTierResponseDetail();
				if (response.getConsentTier() != null) {
					stmt.setConsentStatment(response.getConsentTier().getStatement());
					stmt.setParticipantResponse(response.getResponse());
					consent.getConsentTierResponses().add(stmt);
				}
				
			}
		}
		
		detail.setConsentDetails(consent);
		
		CollectionProtocol cp = cpr.getCollectionProtocol();
		detail.setCpId(cp.getId());
		detail.setCpTitle(cp.getTitle());
		detail.setCpShortTitle(cp.getShortTitle());
		detail.setSpecimenLabelFmt(cp.getSpecimenLabelFormat());
		detail.setAliquotLabelFmt(cp.getAliquotLabelFormat());
		detail.setDerivativeLabelFmt(cp.getDerivativeLabelFormat());
		
		return detail;
	}	
}
