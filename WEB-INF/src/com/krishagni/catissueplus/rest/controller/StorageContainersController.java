package com.krishagni.catissueplus.rest.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.krishagni.catissueplus.core.administrative.events.AssignPositionsOp;
import com.krishagni.catissueplus.core.administrative.events.ContainerMapExportDetail;
import com.krishagni.catissueplus.core.administrative.events.ContainerQueryCriteria;
import com.krishagni.catissueplus.core.administrative.events.PositionTenantDetail;
import com.krishagni.catissueplus.core.administrative.events.StorageContainerDetail;
import com.krishagni.catissueplus.core.administrative.events.StorageContainerPositionDetail;
import com.krishagni.catissueplus.core.administrative.events.StorageContainerSummary;
import com.krishagni.catissueplus.core.administrative.repository.StorageContainerListCriteria;
import com.krishagni.catissueplus.core.administrative.services.StorageContainerService;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

import edu.common.dynamicextensions.nutility.IoUtil;

@Controller
@RequestMapping("/storage-containers")
public class StorageContainersController {
	
	@Autowired
	private StorageContainerService storageContainerSvc;
	
	@Autowired
	private HttpServletRequest httpReq;	
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<StorageContainerSummary> getStorageContainers(
			@RequestParam(value = "name", required = false) 
			String name,
			
			@RequestParam(value = "site", required = false)
			String siteName,
			
			@RequestParam(value = "onlyFreeContainers", required = false, defaultValue = "false")
			boolean onlyFreeContainers,
			
			@RequestParam(value = "startAt", required = false, defaultValue = "0")
			int startAt,
			
			@RequestParam(value = "maxRecords", required = false, defaultValue = "100")
			int maxRecords,
			
			@RequestParam(value = "parentContainerId", required = false)
			Long parentContainerId,
			
			@RequestParam(value = "includeChildren", required = false, defaultValue = "false")
			boolean includeChildren,
			
			@RequestParam(value = "topLevelContainers", required = false, defaultValue = "false")
			boolean topLevelContainers,
			
			@RequestParam(value = "specimenClass", required = false)
			String specimenClass,
			
			@RequestParam(value = "specimenType", required = false)
			String specimenType,
			
			@RequestParam(value = "cpId", required = false)
			Long cpId,
			
			@RequestParam(value = "storeSpecimensEnabled", required = false)
			Boolean storeSpecimensEnabled,
			
			@RequestParam(value = "hierarchical", required = false, defaultValue = "false")
			boolean hierarchical
			) {
		
		StorageContainerListCriteria crit = new StorageContainerListCriteria()
			.query(name)
			.siteName(siteName)
			.onlyFreeContainers(onlyFreeContainers)
			.startAt(startAt)
			.maxResults(maxRecords)
			.parentContainerId(parentContainerId)
			.includeChildren(includeChildren)
			.topLevelContainers(topLevelContainers)
			.specimenClass(specimenClass)
			.specimenType(specimenType)
			.cpId(cpId)
			.storeSpecimensEnabled(storeSpecimensEnabled)
			.hierarchical(hierarchical);
					
		RequestEvent<StorageContainerListCriteria> req = new RequestEvent<StorageContainerListCriteria>(crit);
		ResponseEvent<List<StorageContainerSummary>> resp = storageContainerSvc.getStorageContainers(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.HEAD, value="{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody	
	public Boolean isTenantAllowed(
			@PathVariable("id") 
			Long containerId,
			
			@RequestParam(value = "cpId", required = true) 
			Long cpId,
			
			@RequestParam(value = "specimenType", required = true)
			String specimenType,
			
			@RequestParam(value = "specimenClass", required = true)
			String specimenClass) {
		
		PositionTenantDetail detail = new PositionTenantDetail();
		detail.setContainerId(containerId);
		detail.setCpId(cpId);
		detail.setSpecimenClass(specimenClass);
		detail.setSpecimenType(specimenType);
		
		RequestEvent<PositionTenantDetail> req = new RequestEvent<PositionTenantDetail>(detail);
		ResponseEvent<Boolean> resp = storageContainerSvc.isAllowed(req);
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload();
	}
			
	@RequestMapping(method = RequestMethod.GET, value="{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public StorageContainerDetail getStorageContainer(@PathVariable("id") Long containerId) {
		return getContainer(new ContainerQueryCriteria(containerId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/byname/{name}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public StorageContainerDetail getStorageContainer(@PathVariable("name") String name) {
		return getContainer(new ContainerQueryCriteria(name));
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public StorageContainerDetail createStorageContainer(@RequestBody StorageContainerDetail detail) {
		RequestEvent<StorageContainerDetail> req = new RequestEvent<StorageContainerDetail>(detail);
		ResponseEvent<StorageContainerDetail> resp = storageContainerSvc.createStorageContainer(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.PUT, value="{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public StorageContainerDetail updateStorageContainer(
			@PathVariable("id") 
			Long containerId,
			
			@RequestBody 
			StorageContainerDetail detail) {
		
		detail.setId(containerId);
		
		RequestEvent<StorageContainerDetail> req = new RequestEvent<StorageContainerDetail>(detail);
		ResponseEvent<StorageContainerDetail> resp = storageContainerSvc.updateStorageContainer(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.PATCH, value="{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public StorageContainerDetail patchStorageContainer(
			@PathVariable("id") 
			Long containerId,
			
			@RequestBody 
			StorageContainerDetail detail) {
		
		detail.setId(containerId);
		
		RequestEvent<StorageContainerDetail> req = new RequestEvent<StorageContainerDetail>(detail);
		ResponseEvent<StorageContainerDetail> resp = storageContainerSvc.patchStorageContainer(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="{id}/occupied-positions")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<StorageContainerPositionDetail> getStorageContainerOccupiedPositions(@PathVariable("id") Long containerId) {
		RequestEvent<Long> req = new RequestEvent<Long>(containerId);
		ResponseEvent<List<StorageContainerPositionDetail>> resp = storageContainerSvc.getOccupiedPositions(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}

	@RequestMapping(method = RequestMethod.GET, value="{id}/export-map")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody	
	public void exportContainerMap(@PathVariable("id") Long id, HttpServletResponse response) {
		ContainerQueryCriteria crit = new ContainerQueryCriteria(id);
		RequestEvent<ContainerQueryCriteria> req = new RequestEvent<ContainerQueryCriteria>(crit);
		ResponseEvent<ContainerMapExportDetail> resp = storageContainerSvc.exportMap(req);
		resp.throwErrorIfUnsuccessful();
		
		
		ContainerMapExportDetail detail = resp.getPayload();		
		response.setContentType("application/csv");
		response.setHeader("Content-Disposition", "attachment;filename=" + detail.getName() + ".csv");
			
		InputStream in = null;
		try {
			in = new FileInputStream(detail.getFile());
			IoUtil.copy(in, response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException("Error sending file", e);
		} finally {
			IoUtil.close(in);
			detail.getFile().delete();
		}				
	}
	
	@RequestMapping(method = RequestMethod.POST, value="{id}/occupied-positions")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<StorageContainerPositionDetail> assignPositions(
			@PathVariable("id")
			Long containerId,
			
			@RequestBody
			List<StorageContainerPositionDetail> positions) {
		
		AssignPositionsOp detail = new AssignPositionsOp();
		detail.setContainerId(containerId);
		detail.setPositions(positions);
		
		RequestEvent<AssignPositionsOp> req = new RequestEvent<AssignPositionsOp>(detail);
		ResponseEvent<List<StorageContainerPositionDetail>> resp = storageContainerSvc.assignPositions(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();		
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/{id}/dependent-entities")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<DependentEntityDetail> getDependentEntities(@PathVariable Long id) {
		RequestEvent<Long> req = new RequestEvent<Long>(id);
		ResponseEvent<List<DependentEntityDetail>> resp = storageContainerSvc.getDependentEntities(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value="/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public StorageContainerDetail deleteStorageContainer(@PathVariable Long id) {
		RequestEvent<Long> req = new RequestEvent<Long>(id);
		ResponseEvent<StorageContainerDetail> resp = storageContainerSvc.deleteStorageContainer(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();
	}
	
	private StorageContainerDetail getContainer(ContainerQueryCriteria crit) {
		RequestEvent<ContainerQueryCriteria> req = new RequestEvent<ContainerQueryCriteria>(crit);
		ResponseEvent<StorageContainerDetail> resp = storageContainerSvc.getStorageContainer(req);
		resp.throwErrorIfUnsuccessful();
		
		return resp.getPayload();		
	}	
}
