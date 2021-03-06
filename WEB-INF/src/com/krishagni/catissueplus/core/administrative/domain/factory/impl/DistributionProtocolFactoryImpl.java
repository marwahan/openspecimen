
package com.krishagni.catissueplus.core.administrative.domain.factory.impl;

import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocol;
import com.krishagni.catissueplus.core.administrative.domain.Institute;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.administrative.domain.factory.DistributionProtocolErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.DistributionProtocolFactory;
import com.krishagni.catissueplus.core.administrative.domain.factory.InstituteErrorCode;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.ActivityStatusErrorCode;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.util.Status;

public class DistributionProtocolFactoryImpl implements DistributionProtocolFactory {
	private DaoFactory daoFactory;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	public DistributionProtocol createDistributionProtocol(DistributionProtocolDetail detail) {
		DistributionProtocol distributionProtocol = new DistributionProtocol();
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		
		distributionProtocol.setId(detail.getId());
		setTitle(detail, distributionProtocol, ose);
		setShortTitle(detail, distributionProtocol, ose);
		setInstitute(detail, distributionProtocol, ose);
		setPrincipalInvestigator(detail, distributionProtocol, ose);
		setIrbId(detail, distributionProtocol, ose);
		setStartDate(detail, distributionProtocol);
		setEndDate(detail, distributionProtocol);
		setActivityStatus(detail, distributionProtocol, ose);
		
		ose.checkAndThrow();
		return distributionProtocol;
	}
	
	private void setTitle(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol, OpenSpecimenException ose) {
		if (StringUtils.isBlank(detail.getTitle())) {
			ose.addError(DistributionProtocolErrorCode.TITLE_REQUIRED);
			return;
		}
		distributionProtocol.setTitle(detail.getTitle());

	}

	private void setShortTitle(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol, OpenSpecimenException ose) {
		if (StringUtils.isBlank(detail.getShortTitle())) {
			ose.addError(DistributionProtocolErrorCode.SHORT_TITLE_REQUIRED);
			return;
		}
		distributionProtocol.setShortTitle(detail.getShortTitle());
	}
	
	private void setStartDate(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol) {
		distributionProtocol.setStartDate(detail.getStartDate());
	}
	
	private void setEndDate(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol) {
		distributionProtocol.setEndDate(detail.getEndDate());
	}

	private void setInstitute(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol, OpenSpecimenException ose) {
		String instituteName = detail.getInstituteName();
		if (StringUtils.isBlank(instituteName)) {
			ose.addError(DistributionProtocolErrorCode.INSTITUTE_REQUIRED);
			return;
		}
		
		Institute institute = daoFactory.getInstituteDao().getInstituteByName(instituteName);
		if (institute == null) {
			ose.addError(InstituteErrorCode.NOT_FOUND);
			return;
		}
		
		distributionProtocol.setInstitute(institute);
	}
	
	private void setPrincipalInvestigator(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol, OpenSpecimenException ose) {
		
		if (detail.getPrincipalInvestigator() == null || detail.getPrincipalInvestigator().getId() == null) {
			ose.addError(DistributionProtocolErrorCode.PI_REQUIRED);
			return;
		}
		
		Long piId = detail.getPrincipalInvestigator().getId();
		User pi = daoFactory.getUserDao().getById(piId);
		if (pi == null) {
			ose.addError(DistributionProtocolErrorCode.PI_NOT_FOUND);
			return;
		}
		
		if (!pi.getInstitute().equals(distributionProtocol.getInstitute())) {
			ose.addError(DistributionProtocolErrorCode.PI_DOES_NOT_BELONG_TO_INST);
			return;
		}
		
		distributionProtocol.setPrincipalInvestigator(pi);
	}
	
	private void setIrbId(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol, OpenSpecimenException ose) {
		distributionProtocol.setIrbId(detail.getIrbId());
	}

	private void setActivityStatus(DistributionProtocolDetail detail, DistributionProtocol distributionProtocol, OpenSpecimenException ose) {
		String activityStatus = detail.getActivityStatus();
		if (StringUtils.isBlank(activityStatus)) {
			activityStatus = Status.ACTIVITY_STATUS_ACTIVE.getStatus();
		} else if (!Status.isValidActivityStatus(activityStatus)) {
			ose.addError(ActivityStatusErrorCode.INVALID);
			return;
		}
		
		distributionProtocol.setActivityStatus(activityStatus);
	}
}
