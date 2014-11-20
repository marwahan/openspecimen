
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

import com.krishagni.catissueplus.core.biospecimen.events.AllCollectionProtocolsEvent;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolDetail;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolRegistrationDetail;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolRespEvent;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolSummary;
import com.krishagni.catissueplus.core.biospecimen.events.CprSummary;
import com.krishagni.catissueplus.core.biospecimen.events.CreateRegistrationEvent;
import com.krishagni.catissueplus.core.biospecimen.events.RegisteredParticipantsEvent;
import com.krishagni.catissueplus.core.biospecimen.events.RegistrationCreatedEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ReqAllCollectionProtocolsEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ReqCollectionProtocolEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ReqRegisteredParticipantsEvent;
import com.krishagni.catissueplus.core.biospecimen.services.CollectionProtocolRegistrationService;
import com.krishagni.catissueplus.core.biospecimen.services.CollectionProtocolService;

import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.SessionDataBean;

@Controller
@RequestMapping("/collection-protocols")
public class CollectionProtocolController {

	@Autowired
	private CollectionProtocolService cpSvc;

	@Autowired
	private CollectionProtocolRegistrationService cprSvc;

	@Autowired
	private HttpServletRequest httpServletRequest;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<CollectionProtocolSummary> getCollectionProtocolList(
			@RequestParam(value = "chkPrivilege", required = false, defaultValue = "true")  boolean chkPrivlege,
			@RequestParam(value = "detailedList", required = false, defaultValue = "false") boolean detailedList) {
		
		ReqAllCollectionProtocolsEvent req = new ReqAllCollectionProtocolsEvent();
		req.setSessionDataBean(getSession());
		req.setChkPrivileges(chkPrivlege);
		req.setIncludePi(detailedList);
		req.setIncludeStats(detailedList);

		AllCollectionProtocolsEvent resp = cpSvc.getAllProtocols(req);
		if (!resp.isSuccess()) {
			resp.raiseException();
		}
		return resp.getCpList();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public  CollectionProtocolDetail getCollectionProtocol(@PathVariable("id") Long cpId) {
		ReqCollectionProtocolEvent req = new ReqCollectionProtocolEvent();
		req.setCpId(cpId);
		req.setSessionDataBean(getSession());
		
		CollectionProtocolRespEvent resp = cpSvc.getCollectionProtocol(req);
		if (!resp.isSuccess()) {
			resp.raiseException();
		}
				
		return resp.getCp();
	}	
		
	@RequestMapping(method = RequestMethod.GET, value = "/{id}/registered-participants")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<CprSummary> getRegisteredParticipants(
			@PathVariable("id") Long cpId,
			@RequestParam(value = "query", required = false, defaultValue = "") String searchStr,
			@RequestParam(value = "startAt", required = false, defaultValue = "0") int startAt,
			@RequestParam(value = "maxRecs", required = false, defaultValue = "100") int maxRecs,
			@RequestParam(value = "includeStats", required = false, defaultValue = "false") boolean includeStats) {
		
		ReqRegisteredParticipantsEvent req = new ReqRegisteredParticipantsEvent();
		req.setCpId(cpId);
		req.setSearchString(searchStr);
		req.setSessionDataBean(getSession());
		req.setStartAt(startAt);
		req.setMaxResults(maxRecs);
		req.setIncludeStats(includeStats);
		
		RegisteredParticipantsEvent resp = cpSvc.getRegisteredParticipants(req);		
		if (!resp.isSuccess()) {
			resp.raiseException();
		}
		
		return resp.getParticipants();
	}

//	@RequestMapping(method = RequestMethod.GET, value = "/{id}/participant")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	public ParticipantInfo getParticipant(@PathVariable("id") Long cpId,
//			@RequestParam(value = "pId", required = true, defaultValue = "") String participantId) {
//		ReqParticipantSummaryEvent event = new ReqParticipantSummaryEvent();
//		event.setCpId(cpId);
//		event.setParticipantId(StringUtils.isBlank(participantId) ? null : Long.valueOf(participantId));
//		event.setSessionDataBean(getSession());
//		ParticipantSummaryEvent resp = cpSvc.getParticipant(event);
//		if (!resp.isSuccess()) {
//			resp.raiseException();
//		}
//		return resp.getParticipantInfo();
//	}
//
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/registered-participants")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public CollectionProtocolRegistrationDetail register(
			@PathVariable("id") Long cpId,
			@RequestBody CollectionProtocolRegistrationDetail cprDetails) {
		
		cprDetails.setCpId(cpId);
		
		CreateRegistrationEvent req = new CreateRegistrationEvent();
		req.setCpId(cpId);		
		req.setCprDetail(cprDetails);
		req.setSessionDataBean(getSession());
		
		RegistrationCreatedEvent resp = cprSvc.createRegistration(req);
		if (!resp.isSuccess()) {
			resp.raiseException();
		}
		
		return resp.getCprDetail();
	}

	private SessionDataBean getSession() {
		return (SessionDataBean) httpServletRequest.getSession().getAttribute(Constants.SESSION_DATA);
	}
	
	
//	@RequestMapping(method = RequestMethod.GET, value="/{id}/childProtocols")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	public List<CollectionProtocolSummary> getChildProtocolsList(@PathVariable("id") Long id) {
//		ReqChildProtocolEvent req = new ReqChildProtocolEvent();
//		req.setSessionDataBean(getSession()); 
//		req.setCpId(id);
//
//		ChildCollectionProtocolsEvent result = cpSvc.getChildProtocols(req);
//		return result.getChildProtocols();
//	}

}
