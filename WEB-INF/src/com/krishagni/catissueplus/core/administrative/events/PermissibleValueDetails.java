
package com.krishagni.catissueplus.core.administrative.events;

import com.krishagni.catissueplus.core.administrative.domain.PermissibleValue;

public class PermissibleValueDetails {

	private Long id;

	private Long parentId;

	private String value;

	private String attribute;

	private String conceptCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getConceptCode() {
		return conceptCode;
	}

	public void setConceptCode(String conceptCode) {
		this.conceptCode = conceptCode;
	}

	public static PermissibleValueDetails fromDomain(PermissibleValue permissibleValue) {
		PermissibleValueDetails details = new PermissibleValueDetails();
		details.setConceptCode(permissibleValue.getConceptCode());
		details.setId(permissibleValue.getId());
		details.setAttribute(permissibleValue.getAttribute());
		details.setValue(permissibleValue.getValue());
		if (permissibleValue.getParent() != null) {
			details.setParentId(permissibleValue.getParent().getId());
		}
		return details;
	}
}
