
package com.krishagni.catissueplus.rest.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolDetail;
import com.krishagni.catissueplus.core.administrative.repository.DpListCriteria;
import com.krishagni.catissueplus.core.administrative.services.DistributionProtocolService;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

@Controller
@RequestMapping("/distribution-protocols")
public class DistributionProtocolController {
	@Autowired
	private DistributionProtocolService dpSvc;

	@Autowired
	private HttpServletRequest httpServletRequest;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<DistributionProtocolDetail> getAllDistributionProtocols(
			@RequestParam(value = "query", required = false, defaultValue = "") 
			String searchStr,
			
			@RequestParam(value = "title", required = false)
			String title,
			
			@RequestParam(value = "piId", required = false)
			Long piId,
			
			@RequestParam(value = "startAt", required = false, defaultValue = "0") 
			int startAt,
			
			@RequestParam(value = "maxResults", required = false, defaultValue = "100")
			int maxResults,

			@RequestParam(value = "includeStats", required = false, defaultValue = "false")
			boolean includeStats) {
		
		DpListCriteria criteria = new DpListCriteria()
			.startAt(startAt)
			.maxResults(maxResults)
			.query(searchStr)
			.title(title)
			.piId(piId)
			.includeStat(includeStats);
		
		
		ResponseEvent<List<DistributionProtocolDetail>> resp = dpSvc.getDistributionProtocols(getRequest(criteria));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DistributionProtocolDetail getDistributionProtocol(@PathVariable Long id) {
		ResponseEvent<DistributionProtocolDetail> resp = dpSvc.getDistributionProtocol(getRequest(id));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DistributionProtocolDetail createDistributionProtocol(
			@RequestBody DistributionProtocolDetail detail) {
		ResponseEvent<DistributionProtocolDetail> resp = dpSvc.createDistributionProtocol(getRequest(detail));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public DistributionProtocolDetail updateDistributionProtocol(@PathVariable Long id,
			@RequestBody DistributionProtocolDetail detail) {
		detail.setId(id);
		ResponseEvent<DistributionProtocolDetail> resp = dpSvc.updateDistributionProtocol(getRequest(detail));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}/dependent-entities")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<DependentEntityDetail> getDependentEntities(@PathVariable Long id) {
		RequestEvent<Long> req = new RequestEvent<Long>(id);
		ResponseEvent<List<DependentEntityDetail>> resp = dpSvc.getDependentEntities(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public DistributionProtocolDetail deleteDistributionProtocol(@PathVariable Long id) {
		ResponseEvent<DistributionProtocolDetail> resp  = dpSvc.deleteDistributionProtocol(getRequest(id));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	private <T> RequestEvent<T> getRequest(T payload) {
		return new RequestEvent<T>(payload);
	}
}