
package com.krishagni.catissueplus.core.administrative.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.krishagni.catissueplus.core.administrative.domain.Department;
import com.krishagni.catissueplus.core.administrative.domain.Institute;

public class InstituteSummary {

	private Long id;

	private String name;

	private String activityStatus;

	private int departmentCount;

	private int userCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public int getDepartmentCount() {
		return departmentCount;
	}

	public void setDepartmentCount(int departmentCount) {
		this.departmentCount = departmentCount;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	protected static void transform(Institute institute, InstituteSummary summary, boolean includeStat) {
		summary.setId(institute.getId());
		summary.setName(institute.getName());
		summary.setActivityStatus(institute.getActivityStatus());

		if (includeStat) {
			summary.setDepartmentCount(institute.getDepartments().size());

			int userCount = 0;
			for (Department dept : institute.getDepartments()) {
				userCount += dept.getUsers().size();
			}
			summary.setUserCount(userCount);
		}
	}

	public static InstituteSummary from(Institute institute, boolean includeStats) {
		InstituteSummary summary = new InstituteSummary();
		transform(institute, summary, includeStats);
		return summary;
	}

	public static List<InstituteSummary> from(List<Institute> institutes, boolean includeStats) {
		List<InstituteSummary> result = new ArrayList<InstituteSummary>();

		if (CollectionUtils.isEmpty(institutes)) {
			return result;
		}

		for (Institute institute : institutes) {
			result.add(from(institute, includeStats));
		}

		return result;
	}

}