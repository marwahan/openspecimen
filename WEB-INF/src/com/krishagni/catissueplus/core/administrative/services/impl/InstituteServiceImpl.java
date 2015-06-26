package com.krishagni.catissueplus.core.administrative.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.Department;
import com.krishagni.catissueplus.core.administrative.domain.Institute;
import com.krishagni.catissueplus.core.administrative.domain.factory.InstituteErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.InstituteFactory;
import com.krishagni.catissueplus.core.administrative.events.InstituteDetail;
import com.krishagni.catissueplus.core.administrative.events.InstituteQueryCriteria;
import com.krishagni.catissueplus.core.administrative.events.InstituteSummary;
import com.krishagni.catissueplus.core.administrative.repository.InstituteListCriteria;
import com.krishagni.catissueplus.core.administrative.services.InstituteService;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.DeleteEntityOp;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class InstituteServiceImpl implements InstituteService {
	private DaoFactory daoFactory;

	private InstituteFactory instituteFactory;
	
	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setInstituteFactory(InstituteFactory instituteFactory) {
		this.instituteFactory = instituteFactory;
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<InstituteSummary>> getInstitutes(RequestEvent<InstituteListCriteria> req) {
		try {
			InstituteListCriteria crit = req.getPayload();
			List<Institute> institutes = daoFactory.getInstituteDao().getInstitutes(crit);
			
			return ResponseEvent.response(InstituteSummary.from(institutes, crit.includeStat()));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
		
	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> getInstitute(RequestEvent<InstituteQueryCriteria> req) {
		try {
			InstituteQueryCriteria crit = req.getPayload();
			Institute institute = null;
			
			if (crit.getId() != null) {
				institute = daoFactory.getInstituteDao().getById(crit.getId());
			} else if (StringUtils.isNotBlank(crit.getName())) {
				institute = daoFactory.getInstituteDao().getInstituteByName(crit.getName());
			}
					
			if (institute == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND);
			}
			
			return ResponseEvent.response(InstituteDetail.from(institute));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> createInstitute(RequestEvent<InstituteDetail> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			
			Institute institute = instituteFactory.createInstitute(req.getPayload());
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueName(institute.getName(), ose);
			ose.checkAndThrow();

			daoFactory.getInstituteDao().saveOrUpdate(institute, true);
			return ResponseEvent.response(InstituteDetail.from(institute));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> updateInstitute(RequestEvent<InstituteDetail> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			
			InstituteDetail detail = req.getPayload();			
			Institute existing = null;
			
			if (detail.getId() != null) {
				existing = daoFactory.getInstituteDao().getById(detail.getId());
			} else if (StringUtils.isNotBlank(detail.getName())) {
				existing = daoFactory.getInstituteDao().getInstituteByName(detail.getName());
			}
			
			if (existing == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND);
			}
			
			Institute institute = instituteFactory.createInstitute(detail);
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);			
			if (!existing.getName().equals(institute.getName())) {
				ensureUniqueName(institute.getName(), ose);
			}
			
			checkRemovedDeptRefs(existing, institute, ose);
			ose.checkAndThrow();
			
			existing.update(institute);
			daoFactory.getInstituteDao().saveOrUpdate(existing);
			return ResponseEvent.response(InstituteDetail.from(existing));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<DependentEntityDetail>> getDependentEntities(RequestEvent<Long> req) {
		try {
			Institute existing = daoFactory.getInstituteDao().getById(req.getPayload());
			if (existing == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND);
			}
			
			return ResponseEvent.response(existing.getDependentEntities());
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> deleteInstitute(RequestEvent<DeleteEntityOp> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			
			DeleteEntityOp deleteOp = req.getPayload();
			Institute existing = daoFactory.getInstituteDao().getById(deleteOp.getId());
			if (existing == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND);
			}
			
			existing.delete(deleteOp.isClose());
			return ResponseEvent.response(InstituteDetail.from(existing));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	private void ensureUniqueName(String name, OpenSpecimenException ose) {
		Institute institute = daoFactory.getInstituteDao().getInstituteByName(name);
		if (institute != null) {
			ose.addError(InstituteErrorCode.DUP_NAME);
		}
	}
	
	private void checkRemovedDeptRefs(Institute existing, Institute institute, OpenSpecimenException ose) {
		Set<Department> removedDepartments = new HashSet<Department>(existing.getDepartments());
		removedDepartments.removeAll(institute.getDepartments());
		
		for (Department department : removedDepartments) {
			if (CollectionUtils.isNotEmpty(department.getUsers())) {
				ose.addError(InstituteErrorCode.DEPT_REF_ENTITY_FOUND);
			}
		}
	}	
}