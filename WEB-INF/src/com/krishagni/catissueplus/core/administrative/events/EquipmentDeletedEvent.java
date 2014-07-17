
package com.krishagni.catissueplus.core.administrative.events;

import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class EquipmentDeletedEvent extends ResponseEvent {

	private static final String SUCCESS = "success";

	private Long id;

	private EquipmentDetails details;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public EquipmentDetails getDetails() {
		return details;
	}

	public void setDetails(EquipmentDetails details) {
		this.details = details;
	}

	public static EquipmentDeletedEvent notFound(Long siteId) {
		EquipmentDeletedEvent response = new EquipmentDeletedEvent();
		response.setId(siteId);
		response.setStatus(EventStatus.NOT_FOUND);
		return response;
	}

	public static EquipmentDeletedEvent ok() {
		EquipmentDeletedEvent response = new EquipmentDeletedEvent();
		response.setStatus(EventStatus.OK);
		response.setMessage(SUCCESS);
		return response;
	}

	public static EquipmentDeletedEvent serverError(Throwable... t) {
		Throwable t1 = t != null && t.length > 0 ? t[0] : null;
		EquipmentDeletedEvent resp = new EquipmentDeletedEvent();
		resp.setStatus(EventStatus.INTERNAL_SERVER_ERROR);
		resp.setException(t1);
		resp.setMessage(t1 != null ? t1.getMessage() : null);
		return resp;
	}
}
