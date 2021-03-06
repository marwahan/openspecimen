
package com.krishagni.catissueplus.core.biospecimen.domain.factory.impl;

import static com.krishagni.catissueplus.core.common.PvAttributes.BIOHAZARD;
import static com.krishagni.catissueplus.core.common.PvAttributes.COLL_PROC;
import static com.krishagni.catissueplus.core.common.PvAttributes.CONTAINER;
import static com.krishagni.catissueplus.core.common.PvAttributes.PATH_STATUS;
import static com.krishagni.catissueplus.core.common.PvAttributes.RECV_QUALITY;
import static com.krishagni.catissueplus.core.common.PvAttributes.SPECIMEN_ANATOMIC_SITE;
import static com.krishagni.catissueplus.core.common.PvAttributes.SPECIMEN_CLASS;
import static com.krishagni.catissueplus.core.common.PvAttributes.SPECIMEN_LATERALITY;
import static com.krishagni.catissueplus.core.common.service.PvValidator.areValid;
import static com.krishagni.catissueplus.core.common.service.PvValidator.isValid;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.administrative.domain.StorageContainerPosition;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.administrative.domain.factory.StorageContainerErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.UserErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenCollectionEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenReceivedEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SrErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitErrorCode;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionEventDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ReceivedEventDetail;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenEventDetail;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenInfo.StorageLocationSummary;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.ActivityStatusErrorCode;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.util.Status;

public class SpecimenFactoryImpl implements SpecimenFactory {

	private DaoFactory daoFactory;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	public Specimen createSpecimen(SpecimenDetail detail, Specimen parent) {
		return createSpecimen(null, detail, parent);
	}
	
	@Override
	public Specimen createSpecimen(Specimen existing, SpecimenDetail detail, Specimen parent) {
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);

		SpecimenRequirement sr = getSpecimenRequirement(detail, existing, ose);
		Visit visit = getVisit(detail, existing, ose);
		ose.checkAndThrow();
		
		if (sr != null && visit != null) {
			if (!sr.getCollectionProtocolEvent().getId().equals(visit.getCpEvent().getId())) {
				ose.addError(SpecimenErrorCode.INVALID_VISIT);
				throw ose;
			}			
		}
		
		Specimen specimen = null;
		if (sr != null) {
			specimen = sr.getSpecimen();
		} else {
			specimen = new Specimen();
		}
		
		specimen.setId(detail.getId());
		specimen.setVisit(visit);
		
		setCollectionStatus(detail, existing, specimen, ose);
		setLineage(detail, existing, specimen, ose);
		setParentSpecimen(detail, existing, parent, specimen, ose);
				
		setLabel(detail, existing, specimen, ose);
		setBarcode(detail, existing, specimen, ose);
		setActivityStatus(detail, existing, specimen, ose);
						
		setAnatomicSite(detail, existing, specimen, ose);
		setLaterality(detail, existing, specimen, ose);
		setPathologicalStatus(detail, existing, specimen, ose);
		setQuantity(detail, existing, specimen, ose);
		setSpecimenClass(detail, existing, specimen, ose);
		setSpecimenType(detail, existing, specimen, ose);
		setCreatedOn(detail, existing, specimen, ose);
		setBiohazards(detail, existing, specimen, ose);
				
		if (sr != null && 
				(!sr.getSpecimenClass().equals(specimen.getSpecimenClass()) ||
					!sr.getSpecimenType().equals(specimen.getSpecimenType()))) {
			specimen.setSpecimenRequirement(null);
		}
		
		setSpecimenPosition(detail, existing, specimen, ose);
		setCollectionDetail(detail, existing, specimen, ose);
		setReceiveDetail(detail, existing, specimen, ose);

		ose.checkAndThrow();
		return specimen;
	}

	private void setBarcode(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if(StringUtils.isBlank(detail.getBarcode())) {
			return;
		}

		specimen.setBarcode(detail.getBarcode());
	}
	
	private void setBarcode(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("barcode")) {
			setBarcode(detail, specimen, ose);
		} else {
			specimen.setBarcode(existing.getBarcode());
		}
	}

	private void setActivityStatus(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		String status = detail.getActivityStatus();
		if (StringUtils.isBlank(status)) {
			specimen.setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.getStatus());
		} else if (Status.isValidActivityStatus(status)) {
			specimen.setActivityStatus(status);
		} else {
			ose.addError(ActivityStatusErrorCode.INVALID);
		}
	}
	
	private void setActivityStatus(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("activityStatus")) {
			setActivityStatus(detail, specimen, ose);
		} else {
			specimen.setActivityStatus(existing.getActivityStatus());
		}
	}
	
	private void setLabel(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (StringUtils.isNotBlank(detail.getLabel())) {
			specimen.setLabel(detail.getLabel());
			return;
		} 
		
		if (!specimen.isCollected() || specimen.isAliquot() || specimen.isDerivative()) {
			return;
		}
		
		String labelTmpl = specimen.getLabelTmpl();
		if (StringUtils.isBlank(labelTmpl)) {
			ose.addError(SpecimenErrorCode.LABEL_REQUIRED);
		}		
	}
	
	private void setLabel(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("label")) {
			setLabel(detail, specimen, ose);
		} else {
			specimen.setLabel(existing.getLabel());
		}
	}

	private Visit getVisit(SpecimenDetail detail, Specimen existing, OpenSpecimenException ose) {
		Long visitId = detail.getVisitId();
		String visitName = detail.getVisitName();
		
		Visit visit = null;
		if (visitId != null) {
			visit = daoFactory.getVisitsDao().getById(visitId);
		} else if (StringUtils.isNotBlank(visitName)) {
			visit = daoFactory.getVisitsDao().getByName(visitName);
		} else if (existing != null) {
			visit = existing.getVisit();
		} else {
			ose.addError(SpecimenErrorCode.VISIT_REQUIRED);
			return null;
		}
		
		if (visit == null) {
			ose.addError(VisitErrorCode.NOT_FOUND);
			return null;
		}
		
		return visit;
	}
	
	private void setLineage(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		String lineage = detail.getLineage();
		if (lineage == null) {
			if (specimen.getSpecimenRequirement() == null) {
				lineage = Specimen.NEW;
			}
			
			return;
		}
		
		if (!lineage.equals(Specimen.NEW) && 
		    !lineage.equals(Specimen.ALIQUOT) && 
		    !lineage.equals(Specimen.DERIVED)) {
			ose.addError(SpecimenErrorCode.INVALID_LINEAGE);
			return;
		}
		
		specimen.setLineage(lineage);
	}
	
	private void setLineage(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("lineage")) {
			setLineage(detail, specimen, ose);
		} else {
			specimen.setLineage(existing.getLineage());
		}
	}
	
	private void setParentSpecimen(SpecimenDetail detail, Specimen parent, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.getLineage().equals(Specimen.NEW)) {
			return;
		}
		
		if (parent != null) {
			specimen.setParentSpecimen(parent);
			return;
		}
		
		Long parentId = detail.getParentId();
		String parentLabel = detail.getParentLabel();
		
		Object key = null;
		if (StringUtils.isNotBlank(parentLabel)) {
			key = parentLabel;
			parent = daoFactory.getSpecimenDao().getByLabel(parentLabel);
		} else if (parentId != null) {
			key = parentId;
			parent = daoFactory.getSpecimenDao().getById(parentId);
		} else if (specimen.getVisit() != null && specimen.getSpecimenRequirement() != null) {			
			Long visitId = specimen.getVisit().getId();
			Long srId = specimen.getSpecimenRequirement().getId();
			key = visitId + ":" + srId;
			parent = daoFactory.getSpecimenDao().getParentSpecimenByVisitAndSr(visitId, srId);
		}
		
		if (parent == null) {
			ose.addError(key != null ? SpecimenErrorCode.NOT_FOUND : SpecimenErrorCode.PARENT_REQUIRED, key);
		}
		
		specimen.setParentSpecimen(parent);
	}
	
	private void setParentSpecimen(SpecimenDetail detail, Specimen existing, Specimen parent, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("parentLabel")) {
			setParentSpecimen(detail, parent, specimen, ose);
		} else {
			specimen.setParentSpecimen(existing.getParentSpecimen());
		}
	}
	
	private SpecimenRequirement getSpecimenRequirement(SpecimenDetail detail, Specimen existing, OpenSpecimenException ose) {
		Long reqId = detail.getReqId();
		if (reqId == null) {
			return existing != null ? existing.getSpecimenRequirement() : null;
		}
		
		SpecimenRequirement sr = daoFactory.getSpecimenRequirementDao().getById(reqId);
		if (sr == null) {
			ose.addError(SrErrorCode.NOT_FOUND);
			return null;
		}
		
		return sr;
	}
	
	private void setCollectionStatus(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		String status = detail.getStatus();
		if (StringUtils.isBlank(status)) {
			status = Specimen.COLLECTED;
		}
		
		if (!status.equals(Specimen.COLLECTED) && 
			!status.equals(Specimen.PENDING) && 
			!status.equals(Specimen.MISSED_COLLECTION)) {
			ose.addError(SpecimenErrorCode.INVALID_COLL_STATUS);
			return;
		}

		specimen.setCollectionStatus(status);
	}
	
	private void setCollectionStatus(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("status")) {
			setCollectionStatus(detail, specimen, ose);
		} else {
			specimen.setCollectionStatus(existing.getCollectionStatus());
		}
	}
	
	private void setAnatomicSite(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.getParentSpecimen() != null) {
			specimen.setTissueSite(specimen.getParentSpecimen().getTissueSite());
			return;
		}
		
		if (specimen.isAliquot() || specimen.isDerivative()) {
			return; // invalid parent scenario
		}
		
		String anatomicSite = detail.getAnatomicSite();
		if (StringUtils.isBlank(anatomicSite)) {
			if (specimen.getSpecimenRequirement() == null) {
				ose.addError(SpecimenErrorCode.ANATOMIC_SITE_REQUIRED);
			}
			
			return;				
		}
		
		if (!isValid(SPECIMEN_ANATOMIC_SITE, 2, anatomicSite, true)) {
			ose.addError(SpecimenErrorCode.INVALID_ANATOMIC_SITE);
			return;
		}
		
		specimen.setTissueSite(anatomicSite);		
	}
	
	private void setAnatomicSite(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("anatomicSite")) {
			setAnatomicSite(detail, specimen, ose);
		} else {
			specimen.setTissueSite(existing.getTissueSite());
		}
	}

	private void setLaterality(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.getParentSpecimen() != null) {
			specimen.setTissueSide(specimen.getParentSpecimen().getTissueSide());
			return;
		}
		
		if (specimen.isAliquot() || specimen.isDerivative()) {
			return; // invalid parent scenario
		}
		
		String laterality = detail.getLaterality();
		if (StringUtils.isBlank(laterality)) {
			if (specimen.getSpecimenRequirement() == null) {
				ose.addError(SpecimenErrorCode.LATERALITY_REQUIRED);
			}
			
			return;
		}
		
		if (!isValid(SPECIMEN_LATERALITY, laterality)) {
			ose.addError(SpecimenErrorCode.INVALID_LATERALITY);
			return;
		}
		
		specimen.setTissueSide(laterality);
	}
	
	private void setLaterality(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("laterality")) {
			setLaterality(detail, specimen, ose);
		} else {
			specimen.setTissueSide(existing.getTissueSide());
		}
	}
	
	private void setPathologicalStatus(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.getParentSpecimen() != null) {
			specimen.setPathologicalStatus(specimen.getParentSpecimen().getPathologicalStatus());
			return;
		}
		
		if (specimen.isAliquot() || specimen.isDerivative()) {
			return; // invalid parent specimen scenario
		}
		
		String pathology = detail.getPathology();
		if (StringUtils.isBlank(pathology)) {
			if (specimen.getSpecimenRequirement() == null) {
				ose.addError(SpecimenErrorCode.PATHOLOGY_STATUS_REQUIRED);
			}
			
			return;
		}

		if (!isValid(PATH_STATUS, pathology)) {
			ose.addError(SpecimenErrorCode.INVALID_PATHOLOGY_STATUS);
			return;
		}
		
		specimen.setPathologicalStatus(detail.getPathology());
	}
	
	private void setPathologicalStatus(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("pathology")) {
			setPathologicalStatus(detail, specimen, ose);
		} else {
			specimen.setPathologicalStatus(existing.getPathologicalStatus());
		}
	}
	
	private void setQuantity(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("initialQty")) {
			setInitialQty(detail, specimen, ose);
		} else {
			specimen.setInitialQuantity(existing.getInitialQuantity());
		}
		
		if (existing == null || detail.isAttrModified("availableQty")) {
			setAvailableQty(detail, specimen, ose);
		} else {
			specimen.setAvailableQuantity(existing.getAvailableQuantity());
		}
	}
	
	private void setInitialQty(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		Double qty = detail.getInitialQty();
		if (qty == null) {
			SpecimenRequirement sr = specimen.getSpecimenRequirement();			
			if (sr != null) {
				qty = sr.getInitialQuantity();
			}
		}
				
		if (qty == null || qty <= 0) {
			ose.addError(SpecimenErrorCode.INVALID_QTY);
			return;
		}
				
		specimen.setInitialQuantity(qty);		
	}
	
	private void setAvailableQty(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		Double availableQty = detail.getAvailableQty();
		if (availableQty == null) {
			availableQty = specimen.getInitialQuantity();
		}
		
		if (availableQty > specimen.getInitialQuantity() || availableQty < 0) {
			ose.addError(SpecimenErrorCode.INVALID_QTY);
			return;
		}
		
		specimen.setAvailableQuantity(availableQty);
		
		if (detail.getAvailable() == null) {
			specimen.setIsAvailable(availableQty > 0);
		} else {
			specimen.setIsAvailable(detail.getAvailable());
		}		
	}
	
	private void setSpecimenClass(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.getParentSpecimen() != null && specimen.isAliquot()) {
			specimen.setSpecimenClass(specimen.getParentSpecimen().getSpecimenClass());
			return;
		}
		
		if (specimen.isAliquot()) {
			return; // parent not specified case
		}
				
		String specimenClass = detail.getSpecimenClass();
		if (StringUtils.isBlank(specimenClass)) {
			if (specimen.getSpecimenRequirement() == null) {
				ose.addError(SpecimenErrorCode.SPECIMEN_CLASS_REQUIRED);
			}
			
			return;
		}
		
		if (!isValid(SPECIMEN_CLASS, specimenClass)) {
			ose.addError(SpecimenErrorCode.INVALID_SPECIMEN_CLASS);
			return;
		}
		
		specimen.setSpecimenClass(specimenClass);		
	}
	
	private void setSpecimenClass(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("specimenClass")) {
			setSpecimenClass(detail, specimen, ose);
		} else {
			specimen.setSpecimenClass(existing.getSpecimenClass());
		}
	}
	
	private void setSpecimenType(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.getParentSpecimen() != null && specimen.isAliquot()) {
			specimen.setSpecimenType(specimen.getParentSpecimen().getSpecimenType());
			return;
		}
		
		if (specimen.isAliquot()) {
			return; // parent not specified case
		}
		
		String type = detail.getType();
		if (StringUtils.isBlank(type)) {
			if (specimen.getSpecimenRequirement() == null) {
				ose.addError(SpecimenErrorCode.SPECIMEN_TYPE_REQUIRED);
			}
			
			return;
		}
		
		if (!isValid(SPECIMEN_CLASS, detail.getSpecimenClass(), type)) {
			ose.addError(SpecimenErrorCode.INVALID_SPECIMEN_TYPE);
			return;
		}
		
		specimen.setSpecimenType(type);		
	}
	
	private void setSpecimenType(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("type")) {
			setSpecimenType(detail, specimen, ose);
		} else {
			specimen.setSpecimenType(existing.getSpecimenType());
		}
	}
	
	private void setCreatedOn(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (detail.getCreatedOn() == null) {
			specimen.setCreatedOn(Calendar.getInstance().getTime());
		} else {
			specimen.setCreatedOn(detail.getCreatedOn());
		}
	}
	
	private void setCreatedOn(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("createdOn")) {
			setCreatedOn(detail, specimen, ose); 
		} else {
			specimen.setCreatedOn(existing.getCreatedOn());
		}
	}
	
	private void setBiohazards(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		Specimen parentSpecimen = specimen.getParentSpecimen();
		
		if (specimen.isAliquot()) {
			if (parentSpecimen != null) {
				specimen.setBiohazards(new HashSet<String>(parentSpecimen.getBiohazards()));
			}
			
			return;
		}
		
		Set<String> biohazards = detail.getBiohazards();
		if (CollectionUtils.isEmpty(biohazards)) {
			return;
		}
		
		if (!areValid(BIOHAZARD, biohazards)) {
			ose.addError(SpecimenErrorCode.INVALID_BIOHAZARDS);
			return;
		}
		
		specimen.setBiohazards(detail.getBiohazards());
	}
	
	private void setBiohazards(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("biohazards")) {
			setBiohazards(detail, specimen, ose);
		} else {
			specimen.setBiohazards(existing.getBiohazards());
		}
	}
	
	private void setSpecimenPosition(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		StorageContainer container = null;

		StorageLocationSummary location = detail.getStorageLocation();
		if (isVirtual(location) || !specimen.isCollected()) {
			//
			// When specimen location is virtual or specimen is 
			// not collected - pending / missed collection
			//
			return;
		}
		
		if (location.id != null && location.id != -1) {
			container = daoFactory.getStorageContainerDao().getById(location.id);			
		} else {
			container = daoFactory.getStorageContainerDao().getByName(location.name);
		} 
		
		if (container == null) {
			ose.addError(StorageContainerErrorCode.NOT_FOUND);
			return;
		}
		
		if (!container.canContain(specimen)) {
			ose.addError(StorageContainerErrorCode.CANNOT_HOLD_SPECIMEN, container.getName(), specimen.getLabelOrDesc());
			return;
		}
		
		StorageContainerPosition position = null;
		String posOne = location.positionX, posTwo = location.positionY;
		if (StringUtils.isNotBlank(posOne) && StringUtils.isNotBlank(posTwo)) {
			if (container.canSpecimenOccupyPosition(specimen.getId(), posOne, posTwo)) {
				position = container.createPosition(posOne, posTwo);
			} else {
				ose.addError(StorageContainerErrorCode.NO_FREE_SPACE);
			}
		} else {
			position = container.nextAvailablePosition();
			if (position == null) {
				ose.addError(StorageContainerErrorCode.NO_FREE_SPACE);
			} 
		} 
		
		if (position != null) {
			position.setOccupyingSpecimen(specimen);
			specimen.setPosition(position);
		}
	}
	
	private void setSpecimenPosition(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("storageLocation")) {
			setSpecimenPosition(detail, specimen, ose);
		} else {
			specimen.setPosition(existing.getPosition());
		}
	}
	
	private boolean isVirtual(StorageLocationSummary location) {
		if (location == null) {
			return true;
		}
		
		if (location.id != null && location.id != -1) {
			return false;
		}
		
		if (StringUtils.isNotBlank(location.name)) {
			return false;			
		}
		
		return true;
	}
	
	private void setCollectionDetail(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.isAliquot() || specimen.isDerivative()) {
			return;
		}
				
		CollectionEventDetail collDetail = detail.getCollectionEvent();
		if (collDetail == null) {
			return;
		}
		
		SpecimenCollectionEvent event = SpecimenCollectionEvent.createFromSr(specimen);
		setEventAttrs(collDetail, event, ose);

		String collCont = collDetail.getContainer();
		if (StringUtils.isNotBlank(collCont)) {
			if (isValid(CONTAINER, collCont)) {
				event.setContainer(collCont);				
			} else {
				ose.addError(SpecimenErrorCode.INVALID_COLL_CONTAINER);
			}
		}
			
		String proc = collDetail.getProcedure();
		if (StringUtils.isNotBlank(proc)) {
			if (isValid(COLL_PROC, proc)) {
				event.setProcedure(proc);
			} else {
				ose.addError(SpecimenErrorCode.INVALID_COLL_PROC);
			}
		}
		
		specimen.setCollectionEvent(event);
	}
	
	private void setCollectionDetail(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("collectionEvent")) {
			setCollectionDetail(detail, specimen, ose);
		} else {
			specimen.setCollectionEvent(existing.getCollectionEvent());
		}
	}
	
	private void setReceiveDetail(SpecimenDetail detail, Specimen specimen, OpenSpecimenException ose) {
		if (specimen.isAliquot() || specimen.isDerivative()) {
			return;			
		}
		
		ReceivedEventDetail recvDetail = detail.getReceivedEvent();
		if (recvDetail == null) {
			return;
		}
		
		SpecimenReceivedEvent event = SpecimenReceivedEvent.createFromSr(specimen);
		setEventAttrs(recvDetail, event, ose);
		
		String recvQuality = recvDetail.getReceivedQuality();
		if (StringUtils.isNotBlank(recvQuality)) {
			if (isValid(RECV_QUALITY, recvQuality)) {
				event.setQuality(recvQuality);
			} else {
				ose.addError(SpecimenErrorCode.INVALID_RECV_QUALITY);
			}
		}
		
		specimen.setReceivedEvent(event);
	}
	
	private void setReceiveDetail(SpecimenDetail detail, Specimen existing, Specimen specimen, OpenSpecimenException ose) {
		if (existing == null || detail.isAttrModified("receivedEvent")) {
			setReceiveDetail(detail, specimen, ose);
		} else {
			specimen.setReceivedEvent(existing.getReceivedEvent());
		}
	}

	private void setEventAttrs(SpecimenEventDetail detail, SpecimenEvent event, OpenSpecimenException ose) {
		User user = getUser(detail, ose);
		if (user != null) {
			event.setUser(user);
		}
		
		if (detail.getTime() != null) {
			event.setTime(detail.getTime());
		}
		
		if (StringUtils.isNotBlank(detail.getComments())) {
			event.setComments(detail.getComments());
		}		
	}
	
	private User getUser(SpecimenEventDetail detail, OpenSpecimenException ose) {
		if (detail.getUser() == null) {
			return null;			
		}
		
		Long userId = detail.getUser().getId();		
		String emailAddress = detail.getUser().getEmailAddress();
		
		User user = null;
		if (userId != null) {
			user = daoFactory.getUserDao().getById(userId);
		} else if (StringUtils.isNotBlank(emailAddress)) {
			user = daoFactory.getUserDao().getUserByEmailAddress(emailAddress);
		}
				
		if (user == null) {
			ose.addError(UserErrorCode.NOT_FOUND);			
		}
		
		return user;
	}
}
