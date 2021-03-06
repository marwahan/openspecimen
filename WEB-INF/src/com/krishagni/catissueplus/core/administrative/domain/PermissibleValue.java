
package com.krishagni.catissueplus.core.administrative.domain;

import com.krishagni.catissueplus.core.biospecimen.domain.BaseEntity;

public class PermissibleValue extends BaseEntity {
	private String value;

	private String attribute;

	private String conceptCode;

	private PermissibleValue parent;

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

	public PermissibleValue getParent() {
		return parent;
	}

	public void setParent(PermissibleValue parent) {
		this.parent = parent;
	}
	
	public void update(PermissibleValue other) {
		setConceptCode(other.getConceptCode());
		setAttribute(other.getAttribute());
		setParent(other.getParent());
		setValue(other.getValue());
	}
}
