
package com.krishagni.catissueplus.core.biospecimen.domain.factory.impl;

import static com.krishagni.catissueplus.core.common.PvAttributes.CLINICAL_DIAG;
import static com.krishagni.catissueplus.core.common.PvAttributes.CLINICAL_STATUS;
import static com.krishagni.catissueplus.core.common.PvAttributes.VISIT_STATUS;
import static com.krishagni.catissueplus.core.common.service.PvValidator.isValid;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.domain.factory.SiteErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpeErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CprErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitFactory;
import com.krishagni.catissueplus.core.biospecimen.events.VisitDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.ActivityStatusErrorCode;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.util.Status;

public class VisitFactoryImpl implements VisitFactory {

	private DaoFactory daoFactory;
	
	private String defaultNameTmpl;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}
	
	public void setDefaultNameTmpl(String defNameTmpl) {
		this.defaultNameTmpl = defNameTmpl;
	}

	@Override
	public Visit createVisit(VisitDetail visitDetail) {
		Visit visit = new Visit();
		
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
				
		visit.setId(visitDetail.getId());
		visit.setName(visitDetail.getName());
		setCpe(visitDetail, visit, ose);		
		setCpr(visitDetail, visit, ose);
		validateCprAndCpe(visit, ose);
		
		setVisitDate(visitDetail, visit, ose);
		setVisitStatus(visitDetail, visit, ose);
		setClinicalDiagnosis(visitDetail, visit, ose);
		setClinicalStatus(visitDetail, visit, ose);
		setSite(visitDetail, visit, ose);
		setActivityStatus(visitDetail, visit, ose);
		
		visit.setComments(visitDetail.getComments());
		visit.setSurgicalPathologyNumber(visitDetail.getSurgicalPathologyNumber());
		visit.setDefNameTmpl(defaultNameTmpl);
		
		ose.checkAndThrow();
		return visit;
	}
	
	@Override
	public Visit createVisit(Visit existing, VisitDetail detail) {
		Visit visit = new Visit();
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		
		visit.setId(existing.getId());		
		if (detail.isAttrModified("name")) {
			visit.setName(detail.getName());
		} else {
			visit.setName(existing.getName());
		}
		
		setCpe(detail, existing, visit, ose);		
		setCpr(detail, existing, visit, ose);
		validateCprAndCpe(visit, ose);
		
		setVisitDate(detail, existing, visit, ose);
		setVisitStatus(detail, existing, visit, ose);
		setClinicalDiagnosis(detail, existing, visit, ose);
		setClinicalStatus(detail, existing, visit, ose);
		setSite(detail, existing, visit, ose);
		setActivityStatus(detail, existing, visit, ose);
		setComments(detail, existing, visit, ose);
		setSurgicalPathNo(detail, existing, visit, ose);
		visit.setDefNameTmpl(defaultNameTmpl);
		
		ose.checkAndThrow();
		return visit;
	}

	private void setCpe(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		CollectionProtocolEvent cpe = null;
		
		Long cpeId = visitDetail.getEventId();
		String cpTitle = visitDetail.getCpTitle(),
				cpShortTitle = visitDetail.getCpShortTitle(),
				eventLabel = visitDetail.getEventLabel();
		
		if (cpeId != null) {
			cpe = daoFactory.getCollectionProtocolDao().getCpe(cpeId);
		} else if (StringUtils.isNotBlank(cpTitle) && StringUtils.isNotBlank(eventLabel)) {			
			cpe = daoFactory.getCollectionProtocolDao().getCpeByEventLabel(cpTitle, eventLabel);			
		} else if (StringUtils.isNotBlank(cpShortTitle) && StringUtils.isNotBlank(eventLabel)) {
			cpe = daoFactory.getCollectionProtocolDao().getCpeByShortTitleAndEventLabel(cpShortTitle, eventLabel);
		}
		
		if (cpe == null) {
			ose.addError(CpeErrorCode.NOT_FOUND);
			return;
		}
					
		visit.setCpEvent(cpe);
	}
	
	private void setCpe(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("eventId")) {
			setCpe(detail, visit, ose);
		} else if (detail.isAttrModified("eventLabel") && (detail.isAttrModified("cpTitle") || detail.isAttrModified("cpShortTitle"))) {
			setCpe(detail, visit, ose);
		} else {
			visit.setCpEvent(existing.getCpEvent());
		}		
	}

	private void setCpr(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		CollectionProtocolRegistration cpr = null;

		Long cprId = visitDetail.getCprId();
		String cpTitle = visitDetail.getCpTitle(),
				cpShortTitle = visitDetail.getCpShortTitle(),
				ppid = visitDetail.getPpid();
		
		if (cprId != null) {
			cpr = daoFactory.getCprDao().getById(cprId);
		} else if (StringUtils.isNotBlank(cpTitle) && StringUtils.isNotBlank(ppid)) {			
			cpr = daoFactory.getCprDao().getCprByPpid(cpTitle, ppid);
		} else if (StringUtils.isNotBlank(cpShortTitle) && StringUtils.isNotBlank(ppid)) {
			cpr = daoFactory.getCprDao().getCprByCpShortTitleAndPpid(cpShortTitle, ppid);
		}

		if (cpr == null) {
			ose.addError(CprErrorCode.NOT_FOUND);
			return;
		}
		
		visit.setRegistration(cpr);
	}
	
	private void setCpr(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("cprId")) {
			setCpr(detail, visit, ose);
		} else if (detail.isAttrModified("ppid") && (detail.isAttrModified("cpTitle") || detail.isAttrModified("cpShortTitle"))) {
			setCpr(detail, visit, ose);
		} else {
			visit.setRegistration(existing.getRegistration());
		}
	}

	private void validateCprAndCpe(Visit visit, OpenSpecimenException ose) {
		CollectionProtocolRegistration cpr = visit.getRegistration();
		CollectionProtocolEvent cpe = visit.getCpEvent();
		
		if (cpr == null || cpe == null) {
			return;
		}
		
		if (!cpr.getCollectionProtocol().equals(cpe.getCollectionProtocol())) {
			ose.addError(CprErrorCode.INVALID_CPE);
		}
	}

	private void setVisitDate(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		CollectionProtocolRegistration cpr = visit.getRegistration();
		if (cpr == null) {
			return;
		}
		
		Date regDate = cpr.getRegistrationDate();
		Date visitDate = visitDetail.getVisitDate();
		if (visitDate == null) {
			visitDate = Calendar.getInstance().getTime();
		}
			
		if (visitDate.before(regDate)) {
			ose.addError(VisitErrorCode.INVALID_VISIT_DATE);
			return;
		}
		
		visit.setVisitDate(visitDate);
	}
	
	private void setVisitDate(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("visitDate")) {
			setVisitDate(detail, visit, ose);
		} else {
			visit.setVisitDate(existing.getVisitDate());
		}
	}
	
	private void setVisitStatus(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		String visitStatus = visitDetail.getStatus();
		if (!isValid(VISIT_STATUS, visitStatus)) {
			ose.addError(VisitErrorCode.INVALID_STATUS);			
			return;
		}
		
		visit.setStatus(visitStatus);		
	}
	
	private void setVisitStatus(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("status")) {
			setVisitStatus(detail, visit, ose);
		} else {
			visit.setStatus(existing.getStatus());
		}
	}

	private void setClinicalDiagnosis(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		String clinicalDiagnosis = visitDetail.getClinicalDiagnosis();
		if (!isValid(CLINICAL_DIAG, clinicalDiagnosis)) {
			ose.addError(VisitErrorCode.INVALID_CLINICAL_DIAGNOSIS);
			return;
		}
		
		visit.setClinicalDiagnosis(clinicalDiagnosis);
	}
	
	private void setClinicalDiagnosis(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("clinicalDiagnosis")) {
			setClinicalDiagnosis(detail, visit, ose);
		} else {
			visit.setClinicalDiagnosis(existing.getClinicalDiagnosis());
		}
	}
	
	private void setClinicalStatus(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		String clinicalStatus = visitDetail.getClinicalStatus();
		if (!isValid(CLINICAL_STATUS, clinicalStatus)) {
			ose.addError(VisitErrorCode.INVALID_CLINICAL_STATUS);
			return;			
		}
		
		visit.setClinicalStatus(clinicalStatus);
	}
	
	private void setClinicalStatus(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("clinicalStatus")) {
			setClinicalStatus(detail, visit, ose);
		} else {
			visit.setClinicalStatus(existing.getClinicalStatus());
		}
	}

	private void setSite(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		String visitSite = visitDetail.getSite();
		if (StringUtils.isBlank(visitSite)) {
			if (visit.isCompleted()) {
				ose.addError(VisitErrorCode.SITE_REQUIRED);
			}
		} else {
			Site site = daoFactory.getSiteDao().getSiteByName(visitSite);
			if (site == null) {
				ose.addError(SiteErrorCode.NOT_FOUND);
				return;
			}
			
			visit.setSite(site);			
		}
	}
	
	private void setSite(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("site")) {
			setSite(detail, visit, ose);
		} else {
			visit.setSite(existing.getSite());
		}
	}

	private void setActivityStatus(VisitDetail visitDetail, Visit visit, OpenSpecimenException ose) {
		String status = visitDetail.getActivityStatus();
		if (StringUtils.isBlank(status)) {
			visit.setActive();
		} else if (Status.isValidActivityStatus(status)) {
			visit.setActivityStatus(status);
		} else { 
			ose.addError(ActivityStatusErrorCode.INVALID);
		}
	}
	
	private void setActivityStatus(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("activityStatus")) {
			setActivityStatus(detail, visit, ose);
		} else {
			visit.setActivityStatus(existing.getActivityStatus());
		}
	}
	
	private void setComments(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("comments")) {
			visit.setComments(detail.getComments());
		} else {
			visit.setComments(existing.getComments());
		}		
	}
	
	private void setSurgicalPathNo(VisitDetail detail, Visit existing, Visit visit, OpenSpecimenException ose) {
		if (detail.isAttrModified("surgicalPathologyNumber")) {
			visit.setSurgicalPathologyNumber(detail.getSurgicalPathologyNumber());
		} else {
			visit.setSurgicalPathologyNumber(existing.getSurgicalPathologyNumber());
		}
	}
}