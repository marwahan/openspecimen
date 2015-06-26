
package com.krishagni.catissueplus.core.common.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.krishagni.catissueplus.core.administrative.domain.User;

@JsonFilter("withoutId")
public class UserSummary {

	private Long id;

	private String firstName;

	private String lastName;

	private String loginName;
	
	private String domain;
	
	private String emailAddress;

	private Boolean admin;

	private int cpCount;

	private int since;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public int getCpCount() {
		return cpCount;
	}

	public void setCpCount(int cpCount) {
		this.cpCount = cpCount;
	}

	public int getSince() {
		return since;
	}

	public void setSince(int since) {
		this.since = since;
	}

	public static UserSummary from(User user) {
		UserSummary userSummary = new UserSummary();
		userSummary.setId(user.getId());
		userSummary.setFirstName(user.getFirstName());
		userSummary.setLastName(user.getLastName());
		userSummary.setLoginName(user.getLoginName());
		userSummary.setDomain(user.getAuthDomain().getName());
		userSummary.setEmailAddress(user.getEmailAddress());
		userSummary.setAdmin(user.isAdmin());

		return userSummary;
	}
	
	public static List<UserSummary> from(Collection<User> users) {
		List<UserSummary> result = new ArrayList<UserSummary>();
		if (users == null) {
			return result;
		}	

		for (User user : users) {
			result.add(UserSummary.from(user));
		}
		
		return result;
	}
}
