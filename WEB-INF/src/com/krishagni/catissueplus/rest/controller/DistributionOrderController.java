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

import com.krishagni.catissueplus.core.administrative.events.DistributionOrderDetail;
import com.krishagni.catissueplus.core.administrative.events.DistributionOrderListCriteria;
import com.krishagni.catissueplus.core.administrative.events.DistributionOrderSummary;
import com.krishagni.catissueplus.core.administrative.services.DistributionOrderService;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

@Controller
@RequestMapping("/distribution-orders")
public class DistributionOrderController {
	@Autowired
	private HttpServletRequest httpServletRequest;
	
	@Autowired
	private DistributionOrderService distributionService;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<DistributionOrderSummary> getDistributionOrders(			
		@RequestParam(value = "query", required = false, defaultValue = "") 
		String searchTerm,
			
		@RequestParam(value = "startAt", required = false, defaultValue = "0") 
		int startAt,
			
		@RequestParam(value = "maxResults", required = false, defaultValue = "50") 
		int maxResults,

		@RequestParam(value = "includeStats", required = false, defaultValue = "false") 
		boolean includeStats) {
		
		
		DistributionOrderListCriteria listCrit = new DistributionOrderListCriteria()
			.query(searchTerm)
			.startAt(startAt)
			.maxResults(maxResults)
			.includeStat(includeStats);
			
		ResponseEvent<List<DistributionOrderSummary>> resp = distributionService.getOrders(getRequest(listCrit));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DistributionOrderDetail getDistribution(@PathVariable("id") Long id) {
		ResponseEvent<DistributionOrderDetail> resp = distributionService.getOrder(getRequest(id));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DistributionOrderDetail createDistribution(@RequestBody DistributionOrderDetail order) {
		order.setId(null);
		ResponseEvent<DistributionOrderDetail> resp = distributionService.createOrder(getRequest(order));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.PUT, value="/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DistributionOrderDetail updateDistribution(
		@PathVariable("id") 
		Long distributionId,
		
		@RequestBody 
		DistributionOrderDetail order) {
		
		order.setId(distributionId);
		ResponseEvent<DistributionOrderDetail> resp = distributionService.updateOrder(getRequest(order));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
	
	private <T> RequestEvent<T> getRequest(T payload) {
		return new RequestEvent<T>(payload);
	}
}