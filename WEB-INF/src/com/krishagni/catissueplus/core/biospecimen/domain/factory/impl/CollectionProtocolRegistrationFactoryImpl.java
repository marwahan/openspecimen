
package com.krishagni.catissueplus.core.biospecimen.domain.factory.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.ConsentTier;
import com.krishagni.catissueplus.core.biospecimen.domain.ConsentTierResponse;
import com.krishagni.catissueplus.core.biospecimen.domain.Participant;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CollectionProtocolRegistrationFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CprErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.ParticipantErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.ParticipantFactory;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolRegistrationDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentTierResponseDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.ActivityStatusErrorCode;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.util.Status;


public class CollectionProtocolRegistrationFactoryImpl implements CollectionProtocolRegistrationFactory {
	private DaoFactory daoFactory;

	private final String CONSENT_RESP_NOT_SPECIFIED = "Not Specified";

	private ParticipantFactory participantFactory;

	public void setParticipantFactory(ParticipantFactory participantFactory) {
		this.participantFactory = participantFactory;
	}

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	public CollectionProtocolRegistration createCpr(CollectionProtocolRegistrationDetail detail) {
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		
		CollectionProtocolRegistration cpr = new CollectionProtocolRegistration();
		cpr.setBarcode(detail.getBarcode());
		setRegDate(detail, cpr, ose);
		setActivityStatus(detail, cpr, ose);
		setCollectionProtocol(detail, cpr, ose);
		setConsents(detail, cpr, ose);
		setPpid(detail, cpr, ose);
		setParticipant(detail, cpr, ose);
		
		ose.checkAndThrow();
		return cpr;
	}
	
	private void setRegDate(CollectionProtocolRegistrationDetail detail, CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		if (detail.getRegistrationDate() == null) {
			ose.addError(CprErrorCode.REG_DATE_REQUIRED);
			return;
		}
		
		cpr.setRegistrationDate(detail.getRegistrationDate());
	}

	private void setActivityStatus(CollectionProtocolRegistrationDetail detail, CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		String activityStatus = detail.getActivityStatus();
		
		if (StringUtils.isBlank(activityStatus)) {
			cpr.setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.getStatus());
		} else if (Status.isValidActivityStatus(activityStatus)) {
			cpr.setActivityStatus(activityStatus);
		} else {
			ose.addError(ActivityStatusErrorCode.INVALID);
		}
	}

	private void setCollectionProtocol(
			CollectionProtocolRegistrationDetail detail,
			CollectionProtocolRegistration cpr, 			 
			OpenSpecimenException ose) {
				
		Long cpId = detail.getCpId();
		String title = detail.getCpTitle();
		String shortTitle = detail.getCpShortTitle();
		
		CollectionProtocol protocol = null;
		if (cpId != null) {
			protocol = daoFactory.getCollectionProtocolDao().getById(detail.getCpId());
		} else if (StringUtils.isNotBlank(title)) {
			protocol = daoFactory.getCollectionProtocolDao().getCollectionProtocol(title);
		} else if (StringUtils.isNotBlank(shortTitle)) {
			protocol = daoFactory.getCollectionProtocolDao().getCpByShortTitle(shortTitle);
		} else {
			ose.addError(CprErrorCode.CP_REQUIRED);
			return;
		} 
		
		if (protocol == null) {
			ose.addError(CpErrorCode.NOT_FOUND);
			return;
		}
		
		if (!Status.ACTIVITY_STATUS_ACTIVE.getStatus().equals(protocol.getActivityStatus())) {
			ose.addError(CpErrorCode.NOT_FOUND);
			return;
		}
		
		cpr.setCollectionProtocol(protocol);
	}

	private void setPpid(
			CollectionProtocolRegistrationDetail detail,
			CollectionProtocolRegistration cpr, 
			OpenSpecimenException ose) {
		
		if (cpr.getCollectionProtocol() == null) {
			return;
		}
		
		cpr.setPpid(detail.getPpid());
		
//		String ppidFormat = cpr.getCollectionProtocol().getPpidFormat();
//		String ppid = detail.getPpid();
//		
//		if (StringUtils.isBlank(ppid) && StringUtils.isBlank(ppidFormat)) {
//			ose.addError(CprErrorCode.PPID_REQUIRED);
//		} else {
//			cpr.setPpid(ppid);
//		}
	}

	private void setConsents(
			CollectionProtocolRegistrationDetail detail,
			CollectionProtocolRegistration cpr, 
			OpenSpecimenException ose) {
		if (cpr.getCollectionProtocol() == null) {
			return;
		}
		
		Collection<ConsentTier> consents = cpr.getCollectionProtocol().getConsentTier();
		if (consents == null || consents.isEmpty()) {
			return;
		}

		ConsentDetail consentDetail = detail.getConsentDetails();
		if (consentDetail == null) {
			return;
		}
				
		setConsentSignDate(cpr, consentDetail);
		setConsentWitness(cpr, consentDetail, ose);
		setConsentResponses(cpr, consentDetail);
		setConsentDocumentUrl(cpr, consentDetail.getConsentDocumentUrl());
	}
	
	private void setConsentDocumentUrl(CollectionProtocolRegistration cpr, String consentDocumentUrl) {
		cpr.setSignedConsentDocumentUrl(consentDocumentUrl);
	}

	private void setConsentSignDate(CollectionProtocolRegistration cpr, ConsentDetail consentDetail) {
		if (consentDetail.getConsentSignatureDate() != null) {
			cpr.setConsentSignDate(consentDetail.getConsentSignatureDate());
		}				
	}
	
	private void setConsentWitness(CollectionProtocolRegistration cpr, ConsentDetail consentDetail, OpenSpecimenException ose) {
		String witnessEmailId = consentDetail.getWitnessName();
		if (StringUtils.isBlank(witnessEmailId)) {
			return;
		}
		
		User witness = daoFactory.getUserDao().getUserByEmailAddress(witnessEmailId);
		if (witness == null) {
			ose.addError(CprErrorCode.CONSENT_WITNESS_NOT_FOUND);
		}
		
		cpr.setConsentWitness(witness);
	}
	
	private void setConsentResponses(CollectionProtocolRegistration cpr, ConsentDetail consentDetail) {
		Set<ConsentTierResponse> consentResponses = new HashSet<ConsentTierResponse>();
		
		for (ConsentTier consent : cpr.getCollectionProtocol().getConsentTier()) {
			ConsentTierResponse response = new ConsentTierResponse();
			response.setResponse(CONSENT_RESP_NOT_SPECIFIED);
			response.setConsentTier(consent);
			response.setCpr(cpr);
			
			for (ConsentTierResponseDetail userResp : consentDetail.getConsentTierResponses()) {
				if (consent.getStatement().equals(userResp.getConsentStatment())) {
					response.setResponse(userResp.getParticipantResponse());
					break;
				}
			}
			
			consentResponses.add(response);
		}
		
		cpr.setConsentResponses(consentResponses);		
	}

	private void setParticipant(
			CollectionProtocolRegistrationDetail detail,
			CollectionProtocolRegistration cpr,
			OpenSpecimenException ose) {
		
		ParticipantDetail participantDetail = detail.getParticipant();
		if (participantDetail == null) {
			ose.addError(CprErrorCode.PARTICIPANT_DETAIL_REQUIRED);
			return;
		}
		
		Long participantId = participantDetail.getId();
		Participant participant;
		if (participantId == null) {
			participant = participantFactory.createParticipant(participantDetail);			
			if (participant == null) {
				ose.addError(CprErrorCode.PARTICIPANT_DETAIL_REQUIRED);
			}
		} else {
			participant = daoFactory.getParticipantDao().getById(participantId);
			if (participant == null) {
				ose.addError(ParticipantErrorCode.NOT_FOUND);
			} else {
				participant = participantFactory.createParticipant(participant, participantDetail);
			}			
		}
		
		if (participant == null) {
			return;
		}
		
		cpr.setParticipant(participant);
	}
}