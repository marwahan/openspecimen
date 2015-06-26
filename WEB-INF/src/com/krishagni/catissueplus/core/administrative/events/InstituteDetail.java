
package com.krishagni.catissueplus.core.administrative.events;

import java.util.List;

import com.krishagni.catissueplus.core.administrative.domain.Institute;

public class InstituteDetail extends InstituteSummary {

	private List<DepartmentDetail> departments;

	public List<DepartmentDetail> getDepartments(){
		return departments;
	}
	
	public void setDepartments(List<DepartmentDetail> departments){		
		this.departments = departments;
	}

	public static InstituteDetail from(Institute institute) {
  		return from(institute, false);
	}

	public static InstituteDetail from(Institute institute, boolean includeStats) {
		InstituteDetail detail = new InstituteDetail();
		InstituteDetail.transform(institute, detail, includeStats);

		detail.setDepartments(DepartmentDetail.from(institute.getDepartments()));
		return detail;
	}
}