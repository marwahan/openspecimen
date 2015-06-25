
package com.krishagni.catissueplus.core.biospecimen.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.biospecimen.ConfigParams;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Participant;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CollectionProtocolRegistrationFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpeErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CprErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitErrorCode;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolRegistrationDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ConsentFormDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantRegistrationsList;
import com.krishagni.catissueplus.core.biospecimen.events.RegistrationQueryCriteria;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.events.VisitSpecimensQueryCriteria;
import com.krishagni.catissueplus.core.biospecimen.events.VisitSummary;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.repository.VisitsListCriteria;
import com.krishagni.catissueplus.core.biospecimen.services.CollectionProtocolRegistrationService;
import com.krishagni.catissueplus.core.biospecimen.services.ParticipantService;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.service.impl.ConfigurationServiceImpl;
import com.krishagni.catissueplus.core.common.util.ConfigUtil;

public class CollectionProtocolRegistrationServiceImpl implements CollectionProtocolRegistrationService {
	private DaoFactory daoFactory;

	private CollectionProtocolRegistrationFactory cprFactory;
	
	private ParticipantService participantService;
	
	private ConfigurationServiceImpl cfgSvc;
	
	private static final Pattern digitsPtrn = Pattern.compile("%(\\d+)d");

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setCprFactory(CollectionProtocolRegistrationFactory cprFactory) {
		this.cprFactory = cprFactory;
	}
	
	public void setParticipantService(ParticipantService participantService) {
		this.participantService = participantService;
	}
	
	public void setCfgSvc(ConfigurationServiceImpl cfgSvc) {
		this.cfgSvc = cfgSvc;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> getRegistration(RequestEvent<RegistrationQueryCriteria> req) {				
		try {			
			RegistrationQueryCriteria crit = req.getPayload();
			CollectionProtocolRegistration cpr = getCpr(crit.getCprId(), crit.getCpId(), crit.getPpid());
			boolean allowPhiAccess = AccessCtrlMgr.getInstance().ensureReadCprRights(cpr);
			return ResponseEvent.response(CollectionProtocolRegistrationDetail.from(cpr, !allowPhiAccess));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> createRegistration(RequestEvent<CollectionProtocolRegistrationDetail> req) {
		try {
			return ResponseEvent.response(createRegistration(req.getPayload(), true));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> updateRegistration(RequestEvent<CollectionProtocolRegistrationDetail> req) {
		try {
			CollectionProtocolRegistrationDetail detail = req.getPayload();
			
			CollectionProtocolRegistration existing = getCpr(detail.getId(), detail.getCpId(), detail.getPpid());
			AccessCtrlMgr.getInstance().ensureUpdateCprRights(existing);

			//
			// Note: PPID edit is not allowed; therefore PPID validity is
			// not checked
			//
						
			CollectionProtocolRegistration cpr = cprFactory.createCpr(detail);			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueBarcode(existing, cpr, ose);
			ose.checkAndThrow();
			
			saveParticipant(existing, cpr);
			existing.update(cpr);
			
			daoFactory.getCprDao().saveOrUpdate(existing);
			return ResponseEvent.response(CollectionProtocolRegistrationDetail.from(existing, false));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<DependentEntityDetail>> getDependentEntities(RequestEvent<RegistrationQueryCriteria> req) {
		try {
			RegistrationQueryCriteria crit = req.getPayload();
			CollectionProtocolRegistration cpr = getCpr(crit.getCprId(), crit.getCpId(), crit.getPpid());
			AccessCtrlMgr.getInstance().ensureReadCprRights(cpr);
			return ResponseEvent.response(cpr.getDependentEntities());
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> deleteRegistration(RequestEvent<RegistrationQueryCriteria> req) {
		try {
			RegistrationQueryCriteria crit = req.getPayload();
			CollectionProtocolRegistration cpr = getCpr(crit.getCprId(), crit.getCpId(), crit.getPpid());
			AccessCtrlMgr.getInstance().ensureDeleteCprRights(cpr);
			cpr.delete();
			return ResponseEvent.response(CollectionProtocolRegistrationDetail.from(cpr, false));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}	
	
	@Override
	@PlusTransactional
	public ResponseEvent<File> getConsentForm(RequestEvent<RegistrationQueryCriteria> req) {
		try {
			Long cprId = req.getPayload().getCprId();
			CollectionProtocolRegistration existing = daoFactory.getCprDao().getById(cprId);
			if (existing == null) {
				return ResponseEvent.userError(CprErrorCode.NOT_FOUND);
			}
			
			AccessCtrlMgr.getInstance().ensureReadCprRights(existing);
			
			String fileName = existing.getSignedConsentDocumentName();
			if (StringUtils.isBlank(fileName)) {
				return ResponseEvent.userError(CprErrorCode.CONSENT_FORM_NOT_FOUND);
			}
			
			File file = new File(getConsentDirPath() + fileName);
			if (!file.exists()) {
				return ResponseEvent.userError(CprErrorCode.CONSENT_FORM_NOT_FOUND);
			}
			
			return ResponseEvent.response(file);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<String> uploadConsentForm(RequestEvent<ConsentFormDetail> req) {
		OutputStream outputStream = null;
		try {
			ConsentFormDetail detail = req.getPayload();
			CollectionProtocolRegistration existing = daoFactory.getCprDao().getById(detail.getCprId());
			if (existing == null) {
				return ResponseEvent.userError(CprErrorCode.NOT_FOUND);
			}
			
			AccessCtrlMgr.getInstance().ensureUpdateCprRights(existing);
			
			String newFileName = UUID.randomUUID() + "_" + detail.getFileName(); 
			File newFile = new File(getConsentDirPath() + newFileName);
			
			outputStream = new FileOutputStream(newFile);
			IOUtils.copy(detail.getInputStream(), outputStream);
			
			String oldFileName = existing.getSignedConsentDocumentName();
			if (StringUtils.isNotBlank(oldFileName)) {
				File oldFile = new File(getConsentDirPath() + oldFileName);
				if (oldFile.exists()) {
					oldFile.delete();
				}
 			}
			existing.setSignedConsentDocumentName(newFileName);
			return ResponseEvent.response(detail.getFileName());
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<ConsentDetail> getConsents(RequestEvent<RegistrationQueryCriteria> req) {
		try {
			RegistrationQueryCriteria crit = req.getPayload();
			AccessCtrlMgr.getInstance().ensureReadCprRights(crit.getCprId());
			CollectionProtocolRegistration cpr = getCpr(crit.getCprId(), crit.getCpId(), crit.getPpid());
			return ResponseEvent.response(ConsentDetail.fromCpr(cpr));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<ConsentDetail> saveConsents(RequestEvent<ConsentDetail> req) {
		try {
			ConsentDetail consentDetail = req.getPayload();
			CollectionProtocolRegistration existing = getCpr(consentDetail.getCprId(), null, null);
			AccessCtrlMgr.getInstance().ensureUpdateCprRights(existing);
			
			CollectionProtocolRegistration cpr = cprFactory.updateConsents(existing, consentDetail);
			existing.updateConsents(cpr);
			return ResponseEvent.response(ConsentDetail.fromCpr(existing));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<Boolean> deleteConsentForm(RequestEvent<RegistrationQueryCriteria> req) {
		try {
			Long cprId = req.getPayload().getCprId();
			CollectionProtocolRegistration cpr = daoFactory.getCprDao().getById(cprId);
			if (cpr == null) {
				return ResponseEvent.userError(CprErrorCode.NOT_FOUND);
			}
			
			AccessCtrlMgr.getInstance().ensureUpdateCprRights(cpr);

			String fileName = cpr.getSignedConsentDocumentName();
			if (StringUtils.isBlank(fileName)) {
				return ResponseEvent.userError(CprErrorCode.CONSENT_FORM_NOT_FOUND);
			}
			
			File file = new File(getConsentDirPath() + fileName);
			if (!file.exists()) {
				return ResponseEvent.userError(CprErrorCode.CONSENT_FORM_NOT_FOUND);
			}
			
			boolean isFileDeleted = file.delete();
			if (isFileDeleted) {
				cpr.setSignedConsentDocumentName(null);
			} 

			return ResponseEvent.response(isFileDeleted);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}	

	@Override
	@PlusTransactional
	public ResponseEvent<List<VisitSummary>> getVisits(RequestEvent<VisitsListCriteria> req) {
		try {
			CollectionProtocolRegistration cpr = getCpr(req.getPayload().cprId(), null, null);
			AccessCtrlMgr.getInstance().ensureReadVisitRights(cpr);
			return ResponseEvent.response(daoFactory.getVisitsDao().getVisits(req.getPayload()));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<SpecimenDetail>> getSpecimens(RequestEvent<VisitSpecimensQueryCriteria> req) {
		VisitSpecimensQueryCriteria crit = req.getPayload();
		
		try {
			CollectionProtocolRegistration cpr = getCpr(req.getPayload().getCprId(), null, null);
			AccessCtrlMgr.getInstance().ensureReadSpecimenRights(cpr);

			List<SpecimenDetail> specimens = Collections.emptyList();			
			if (crit.getVisitId() != null) {
				specimens = getSpecimensByVisit(crit.getCprId(), crit.getVisitId());
			} else if (crit.getEventId() != null) {
				specimens = getAnticipatedSpecimens(crit.getCprId(), crit.getEventId());
			}
			
			return ResponseEvent.response(specimens);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<ParticipantRegistrationsList> createRegistrations(RequestEvent<ParticipantRegistrationsList> req) {
		try {
			ParticipantRegistrationsList input = req.getPayload();
						
			//
			// Step 1: Save or update participant
			//
			ParticipantDetail participantDetail = participantService.saveOrUpdateParticipant(input.getParticipant());
			ParticipantDetail p = new ParticipantDetail();
			p.setId(participantDetail.getId());
			
			//
			// Step 2: Run through each registration
			//
			List<CollectionProtocolRegistrationDetail> registrations = new ArrayList<CollectionProtocolRegistrationDetail>();
			for (CollectionProtocolRegistrationDetail cprDetail : input.getRegistrations()) {
				cprDetail.setParticipant(p);
				cprDetail = createRegistration(cprDetail, false);
				cprDetail.setParticipant(null);
				registrations.add(cprDetail);
			}
			
			ParticipantRegistrationsList result = new ParticipantRegistrationsList();
			result.setParticipant(participantDetail);
			result.setRegistrations(registrations);			
			return ResponseEvent.response(result);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	private CollectionProtocolRegistrationDetail createRegistration(CollectionProtocolRegistrationDetail input, boolean saveParticipant) {
		CollectionProtocolRegistration cpr = cprFactory.createCpr(input);
		AccessCtrlMgr.getInstance().ensureCreateCprRights(cpr);
		
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		
		ensureValidAndUniquePpid(cpr, ose);
		ensureUniqueParticipantReg(cpr, ose);		
		ensureUniqueBarcode(null, cpr, ose);
		
		ose.checkAndThrow();
		
		if (saveParticipant) {
			saveParticipant(null, cpr);
		}
		
		cpr.setPpidIfEmpty();
		daoFactory.getCprDao().saveOrUpdate(cpr);
		return CollectionProtocolRegistrationDetail.from(cpr, false);		
	}
	
	private void saveParticipant(CollectionProtocolRegistration existing, CollectionProtocolRegistration cpr) {		
		Participant existingParticipant = null;
		Participant participant = cpr.getParticipant();
		
		if (existing == null) { 
			// new registration
			if (participant.getId() != null) { 
				// existing participant
				existingParticipant = daoFactory.getParticipantDao().getById(participant.getId());
			} else {
				// new participant
			}
		} else { 
			// existing reg, therefore it has to be existing participant
			existingParticipant = existing.getParticipant();
		}
		
		if (existingParticipant != null) {
			participantService.updateParticipant(existingParticipant, participant);
			cpr.setParticipant(existingParticipant);
		} else {
			participantService.createParticipant(participant);
			cpr.setParticipant(participant);
		}
	}
		
	//
	// Checks whether same participant is registered for same protocol already
	//
	private void ensureUniqueParticipantReg(CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		if (cpr.getParticipant() == null || cpr.getParticipant().getId() == null) {
			return ;
		}
		
		Long participantId = cpr.getParticipant().getId();
		Long cpId = cpr.getCollectionProtocol().getId();
		
		if (daoFactory.getCprDao().getCprByParticipantId(cpId, participantId) != null) {
			ose.addError(CprErrorCode.DUP_REGISTRATION);
		}
	}

	private void ensureValidAndUniquePpid(CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		CollectionProtocol cp = cpr.getCollectionProtocol();
		boolean ppidReq = cp.isManualPpidEnabled() || StringUtils.isBlank(cp.getPpidFormat());
		
		String ppid = cpr.getPpid();
		if (StringUtils.isBlank(ppid)) {
			if (ppidReq) {
				ose.addError(CprErrorCode.PPID_REQUIRED);
			}
			
			return;
		}
		
		
		if (StringUtils.isNotBlank(cp.getPpidFormat())) {
			//
			// PPID format is specified
			//
			
			if (!cp.isManualPpidEnabled()) {
				ose.addError(CprErrorCode.MANUAL_PPID_NOT_ALLOWED);
				return;
			}
			
			
			if (!isValidPpid(cp.getPpidFormat(), ppid)) {
				ose.addError(CprErrorCode.INVALID_PPID, ppid);
				return;
			}
		}
		
		if (daoFactory.getCprDao().getCprByPpid(cp.getId(), ppid) != null) {
			ose.addError(CprErrorCode.DUP_PPID, ppid);
		}
	}

	private void ensureUniqueBarcode(CollectionProtocolRegistration existing, CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		if (existing != null && 
			StringUtils.isNotBlank(existing.getBarcode()) && 
			existing.getBarcode().equals(cpr.getBarcode())) { // barcode has not changed
			return;
		}
		
		if (StringUtils.isBlank(cpr.getBarcode())) {
			return;
		}
		
		if (daoFactory.getCprDao().getCprByBarcode(cpr.getBarcode()) != null) {
			ose.addError(CprErrorCode.DUP_BARCODE);
		}
	}
		
	private List<SpecimenDetail> getSpecimensByVisit(Long cprId, Long visitId) {
		Visit visit = daoFactory.getVisitsDao().getById(visitId);
		if (visit == null) {
			throw OpenSpecimenException.userError(VisitErrorCode.NOT_FOUND);
		}
		
		Set<SpecimenRequirement> anticipatedSpecimens = visit.getCpEvent().getTopLevelAnticipatedSpecimens();
		Set<Specimen> specimens = visit.getTopLevelSpecimens();

		return SpecimenDetail.getSpecimens(anticipatedSpecimens, specimens);
	}
	
	private List<SpecimenDetail> getAnticipatedSpecimens(Long cprId, Long eventId) {
		CollectionProtocolEvent cpe = daoFactory.getCollectionProtocolDao().getCpe(eventId);
		if (cpe == null) {
			throw OpenSpecimenException.userError(CpeErrorCode.NOT_FOUND);
		}
		
		Set<SpecimenRequirement> anticipatedSpecimens = cpe.getTopLevelAnticipatedSpecimens();
		return SpecimenDetail.getSpecimens(anticipatedSpecimens, Collections.<Specimen>emptySet());		
	}
	
	private CollectionProtocolRegistration getCpr(Long cprId, Long cpId, String ppid) {
		CollectionProtocolRegistration cpr = null;
		if (cprId != null) {
			cpr = daoFactory.getCprDao().getById(cprId);
		} else if (cpId != null && StringUtils.isNotBlank(ppid)) {
			cpr = daoFactory.getCprDao().getCprByPpid(cpId, ppid);
		}
		
		if (cpr == null) {
			throw OpenSpecimenException.userError(CprErrorCode.NOT_FOUND);
		}
		
		return cpr;
	}	
	
	private String getConsentDirPath() {
		String path = cfgSvc.getStrSetting(ConfigParams.MODULE, "participant_consent_dir");
		if (path == null) {
			path = ConfigUtil.getInstance().getDataDir() + File.separator + "participant-consents";
		}
		
		return path + File.separator;
	}
	
	private boolean isValidPpid(String format, String ppid) {
		Matcher matcher = digitsPtrn.matcher(format);
		if (!matcher.find()) {
			return format.equals(ppid);
		}
		
		int matchStartIdx = format.indexOf(matcher.group(0));
		String beforeDigits = format.substring(0, matchStartIdx);
		String afterDigits = format.substring(matchStartIdx + matcher.group(0).length());
		
		String regex = beforeDigits + "\\d{" + matcher.group(1) + "}" + afterDigits;
		return Pattern.matches(regex, ppid);		
	}
}
