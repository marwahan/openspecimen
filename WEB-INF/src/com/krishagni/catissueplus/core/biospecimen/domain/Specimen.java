
package com.krishagni.catissueplus.core.biospecimen.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainerPosition;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenErrorCode;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.service.LabelGenerator;
import com.krishagni.catissueplus.core.common.util.AuthUtil;
import com.krishagni.catissueplus.core.common.util.Status;
import com.krishagni.catissueplus.core.common.util.Utility;

@Configurable
@Audited
public class Specimen extends BaseEntity {
	public static final String NEW = "New";
	
	public static final String ALIQUOT = "Aliquot";
	
	public static final String DERIVED = "Derived";
	
	public static final String COLLECTED = "Collected";
	
	public static final String MISSED_COLLECTION = "Missed Collection";
	
	public static final String PENDING = "Pending";
	
	public static final String ACCEPTABLE = "Acceptable";
	
	private static final String ENTITY_NAME = "specimen";

	private String tissueSite;

	private String tissueSide;

	private String pathologicalStatus;

	private String lineage;

	private Double initialQuantity;

	private String specimenClass;

	private String specimenType;

	private Double concentrationInMicrogramPerMicroliter;

	private String label;

	private String activityStatus;

	private Boolean isAvailable;

	private String barcode;

	private String comment;

	private Date createdOn;

	private Double availableQuantity;

	private String collectionStatus;
	
	private Set<String> biohazards = new HashSet<String>();

	private Visit visit;

	private SpecimenRequirement specimenRequirement;

	private StorageContainerPosition position;

	private Specimen parentSpecimen;

	private Set<Specimen> childCollection = new HashSet<Specimen>();

	private Set<ExternalIdentifier> externalIdentifierCollection = new HashSet<ExternalIdentifier>();
	
	private SpecimenCollectionEvent collectionEvent;
	
	private SpecimenReceivedEvent receivedEvent;
	
	private List<SpecimenTransferEvent> transferEvents;
	
	@Autowired
	@Qualifier("specimenLabelGenerator")
	private LabelGenerator labelGenerator;
	
	public static String getEntityName() {
		return ENTITY_NAME;
	}
	
	public String getTissueSite() {
		return tissueSite;
	}

	public void setTissueSite(String tissueSite) {
		if (StringUtils.isNotBlank(this.tissueSite) && !this.tissueSite.equals(tissueSite)) {
			for (Specimen child : getChildCollection()) {
				child.setTissueSite(tissueSite);
			}
		}
		
		this.tissueSite = tissueSite;
	}

	public String getTissueSide() {
		return tissueSide;
	}

	public void setTissueSide(String tissueSide) {
		if (StringUtils.isNotBlank(this.tissueSide) && !this.tissueSide.equals(tissueSide)) {
			for (Specimen child : getChildCollection()) {
				child.setTissueSide(tissueSide);
			}
		}
		
		this.tissueSide = tissueSide;
	}

	public String getPathologicalStatus() {
		return pathologicalStatus;
	}

	public void setPathologicalStatus(String pathologicalStatus) {
		if (StringUtils.isNotBlank(this.pathologicalStatus) && !this.pathologicalStatus.equals(pathologicalStatus)) {
			for (Specimen child : getChildCollection()) {
				child.setPathologicalStatus(pathologicalStatus);
			}
		}
				
		this.pathologicalStatus = pathologicalStatus;
	}

	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public Double getInitialQuantity() {
		return initialQuantity;
	}

	public void setInitialQuantity(Double initialQuantity) {
		this.initialQuantity = initialQuantity;
	}

	public String getSpecimenClass() {
		return specimenClass;
	}

	public void setSpecimenClass(String specimenClass) {
		if (StringUtils.isNotBlank(this.specimenClass) && !this.specimenClass.equals(specimenClass)) {
			for (Specimen child : getChildCollection()) {
				if (child.isAliquot()) {
					child.setSpecimenClass(specimenClass);
				}				
			}
		}
		
		this.specimenClass = specimenClass;
	}

	public String getSpecimenType() {
		return specimenType;
	}

	public void setSpecimenType(String specimenType) {
		if (StringUtils.isNotBlank(this.specimenType) && !this.specimenType.equals(specimenType)) {
			for (Specimen child : getChildCollection()) {
				if (child.isAliquot()) {
					child.setSpecimenType(specimenType);
				}				
			}
		}
				
		this.specimenType = specimenType;
	}

	public Double getConcentrationInMicrogramPerMicroliter() {
		return concentrationInMicrogramPerMicroliter;
	}

	public void setConcentrationInMicrogramPerMicroliter(Double concentrationInMicrogramPerMicroliter) {
		this.concentrationInMicrogramPerMicroliter = concentrationInMicrogramPerMicroliter;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		if (StringUtils.isBlank(activityStatus)) {
			activityStatus = Status.ACTIVITY_STATUS_ACTIVE.getStatus();
		}
		this.activityStatus = activityStatus;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Double getAvailableQuantity() {
		return availableQuantity;
	}

	public void setAvailableQuantity(Double availableQuantity) { 
		this.availableQuantity = availableQuantity;
	}

	public String getCollectionStatus() {
		return collectionStatus;
	}

	public void setCollectionStatus(String collectionStatus) {
		if (this.collectionStatus != null && !this.collectionStatus.equals(collectionStatus)) {
			if (isCollected()) { // COLLECTED -> PENDING
				// TODO: check whether this is distributed
				// Should initial quantity get added over here?				
				isAvailable = false;
				availableQuantity = 0.0d;
				
				if (position != null) {
					position.vacate();
				}				
				position = null;
				
				deleteEvents();
				
				for (Specimen child : getChildCollection()) {
					child.setCollectionStatus(collectionStatus);
				}				
			} else if (!getVisit().isCompleted()) { // PENDING -> COLLECTED
				throw OpenSpecimenException.userError(SpecimenErrorCode.VISIT_NOT_COMPLETED);
			} else { // PENDING -> COLLECTED
				decAliquotedQtyFromParent();
				addCollRecvEvents();
			}
		}
		
		this.collectionStatus = collectionStatus;		
	}

	public Set<String> getBiohazards() {
		return biohazards;
	}

	public void setBiohazards(Set<String> biohazards) {
		this.biohazards = biohazards;
	}
	
	public void updateBiohazards(Set<String> biohazards) {
		getBiohazards().addAll(biohazards);
		getBiohazards().retainAll(biohazards);
		
		for (Specimen child : getChildCollection()) {
			if (child.isAliquot()) {
				child.updateBiohazards(biohazards);
			}
		}
	}

	public Visit getVisit() {
		return visit;
	}

	public void setVisit(Visit visit) {
		this.visit = visit;
	}

	public SpecimenRequirement getSpecimenRequirement() {
		return specimenRequirement;
	}

	public void setSpecimenRequirement(SpecimenRequirement specimenRequirement) {
		this.specimenRequirement = specimenRequirement;
	}

	public StorageContainerPosition getPosition() {
		return position;
	}

	public void setPosition(StorageContainerPosition position) {
		this.position = position;
	}

	public Specimen getParentSpecimen() {
		return parentSpecimen;
	}

	public void setParentSpecimen(Specimen parentSpecimen) {
		this.parentSpecimen = parentSpecimen;
	}

	@NotAudited
	public Set<Specimen> getChildCollection() {
		return childCollection;
	}

	public void setChildCollection(Set<Specimen> childSpecimenCollection) {
		this.childCollection = childSpecimenCollection;
	}

	@NotAudited
	public Set<ExternalIdentifier> getExternalIdentifierCollection() {
		return externalIdentifierCollection;
	}

	public void setExternalIdentifierCollection(Set<ExternalIdentifier> externalIdentifierCollection) {
		this.externalIdentifierCollection = externalIdentifierCollection;
	}

	@NotAudited
	public SpecimenCollectionEvent getCollectionEvent() {
		if (isAliquot() || isDerivative()) {
			return null;
		}
				
		if (this.collectionEvent == null) {
			this.collectionEvent = SpecimenCollectionEvent.getFor(this); 
		}
		
		if (this.collectionEvent == null) {
			this.collectionEvent = SpecimenCollectionEvent.createFromSr(this);
		}
		
		return this.collectionEvent;
	}

	public void setCollectionEvent(SpecimenCollectionEvent collectionEvent) {
		this.collectionEvent = collectionEvent;
	}

	@NotAudited
	public SpecimenReceivedEvent getReceivedEvent() {
		if (isAliquot() || isDerivative()) {
			return null;
		}
		
		if (this.receivedEvent == null) {
			this.receivedEvent = SpecimenReceivedEvent.getFor(this); 			 
		}
		
		if (this.receivedEvent == null) {
			this.receivedEvent = SpecimenReceivedEvent.createFromSr(this);
		}
		
		return this.receivedEvent; 
	}

	public void setReceivedEvent(SpecimenReceivedEvent receivedEvent) {
		this.receivedEvent = receivedEvent;
	}
	
	@NotAudited
	public List<SpecimenTransferEvent> getTransferEvents() {
		if (this.transferEvents == null) {
			this.transferEvents = SpecimenTransferEvent.getFor(this);
		}		
		return this.transferEvents;
	}

	public LabelGenerator getLabelGenerator() {
		return labelGenerator;
	}

	public void setLabelGenerator(LabelGenerator labelGenerator) {
		this.labelGenerator = labelGenerator;
	}

	public boolean isActive() {
		return Status.ACTIVITY_STATUS_ACTIVE.getStatus().equals(getActivityStatus());
	}
	
	public boolean isClosed() {
		return Status.ACTIVITY_STATUS_CLOSED.getStatus().equals(getActivityStatus());
	}
	
	public boolean isActiveOrClosed() {
		return isActive() || isClosed();
	}
	
	public boolean isAliquot() {
		return ALIQUOT.equals(lineage);
	}
	
	public boolean isDerivative() {
		return DERIVED.equals(lineage);
	}
	
	public boolean isCollected() {
		return Status.SPECIMEN_COLLECTION_STATUS_COLLECTED.getStatus().equals(this.collectionStatus);
	}

	public void disable() {
		disable(true);
	}
	
	protected void disable(boolean checkChildSpecimens) {
		if (getActivityStatus().equals(Status.ACTIVITY_STATUS_DISABLED.getStatus())) {
			return;
		}

		if (checkChildSpecimens) {
			ensureChildSpecimensAreDisabled();
		}

		virtualize();
		setLabel(Utility.getDisabledValue(getLabel()));
		setBarcode(Utility.getDisabledValue(getBarcode()));
		setActivityStatus(Status.ACTIVITY_STATUS_DISABLED.getStatus());		
		
	}
	
	public void close(User user, Date time, String reason) {
		if (!getActivityStatus().equals(Status.ACTIVITY_STATUS_ACTIVE.getStatus())) {
			return;
		}
		
		setIsAvailable(false);
		virtualize();
		addDisposalEvent(user, time, reason);		
		setActivityStatus(Status.ACTIVITY_STATUS_CLOSED.getStatus());
	}
	
	public void activate() {
		if (getActivityStatus().equals(Status.ACTIVITY_STATUS_ACTIVE.getStatus())) {
			return;
		}
		
		setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.getStatus());
		if (getAvailableQuantity() > 0) {
			setIsAvailable(true);
		}
		
		// TODO: we need to add a reopen event here
	}
		
	public CollectionProtocolRegistration getRegistration() {
		return getVisit().getRegistration();
	}

	public void update(Specimen specimen) {		
		String previousStatus = getCollectionStatus();
		Double previousQty = getInitialQuantity();
		
		updateStatus(specimen.getActivityStatus(), null);
		if (!isActive()) {
			return;
		}
		
		setLabel(specimen.getLabel());
		setBarcode(specimen.getBarcode());
		
		updateEvent(getCollectionEvent(), specimen.getCollectionEvent());
		updateEvent(getReceivedEvent(), specimen.getReceivedEvent());
		setCollectionStatus(specimen.getCollectionStatus());

		// TODO: Specimen class/type should not be allowed to change 
		if (isAliquot()) {
			setSpecimenClass(parentSpecimen.getSpecimenClass());
			setSpecimenType(parentSpecimen.getSpecimenType());
			updateBiohazards(parentSpecimen.getBiohazards());
		} else {
			setSpecimenClass(specimen.getSpecimenClass());
			setSpecimenType(specimen.getSpecimenType());
			updateBiohazards(specimen.getBiohazards());
		}
		
		if (parentSpecimen == null) {
			setTissueSite(specimen.getTissueSite());
			setTissueSide(specimen.getTissueSide());
			setPathologicalStatus(specimen.getPathologicalStatus());			
		} else {
			setTissueSite(parentSpecimen.getTissueSite());
			setTissueSide(parentSpecimen.getTissueSide());
			setPathologicalStatus(parentSpecimen.getPathologicalStatus());			
		}
		
		setInitialQuantity(specimen.getInitialQuantity());		
		setAvailableQuantity(specimen.getAvailableQuantity());
		setIsAvailable(specimen.getIsAvailable());
				
		setComment(specimen.getComment());		
		updatePosition(specimen.getPosition());
		
		if (isAliquot()) {
			if (isCollected()) {
				if (!collectionStatus.equals(previousStatus)) { // *** -> COLLECTED
					decAliquotedQtyFromParent();
				} else if (!initialQuantity.equals(previousQty)) {
					adjustParentSpecimenQty(initialQuantity - previousQty);
				}
			} else if (COLLECTED.equals(previousStatus)) { // COLLECTED -> ***
				adjustParentSpecimenQty(-previousQty); // give back the quantity to parent
			}
		}
		
		checkQtyConstraints();
	}
	
	public void updateStatus(String activityStatus, String reason) {
		if (this.activityStatus != null && this.activityStatus.equals(activityStatus)) {
			return;
		}
		
		if (Status.ACTIVITY_STATUS_DISABLED.getStatus().equals(activityStatus)) {
			disable();
		} else if (Status.ACTIVITY_STATUS_CLOSED.getStatus().equals(activityStatus)) {
			close(AuthUtil.getCurrentUser(), Calendar.getInstance().getTime(), reason);
		} else if (Status.ACTIVITY_STATUS_ACTIVE.getStatus().equals(activityStatus)) {
			activate();
		}
	}
		
	public void distribute(User distributor, Date time, Double quantity, boolean closeAfterDistribution) {
		if (!getIsAvailable() || !isCollected() || getAvailableQuantity() <= 0) {
			throw OpenSpecimenException.userError(SpecimenErrorCode.NOT_AVAILABLE_FOR_DIST, getLabel());
		}
		
		if (getAvailableQuantity() < quantity) {
			throw OpenSpecimenException.userError(SpecimenErrorCode.INSUFFICIENT_QTY);
		}
		
		setAvailableQuantity(getAvailableQuantity() - quantity);
		addDistributionEvent(distributor, time, quantity);
		if (availableQuantity == 0 || closeAfterDistribution) {
			close(distributor, time, "Distributed");
		}
	}
	
	private void addDistributionEvent(User user, Date time, Double quantity) {
		SpecimenDistributionEvent event = new SpecimenDistributionEvent(this);
		event.setQuantity(quantity);
		event.setUser(user);
		event.setTime(time);
		event.saveOrUpdate();
	}
	
	private void addDisposalEvent(User user, Date time, String reason) {
		SpecimenDisposalEvent event = new SpecimenDisposalEvent(this);
		event.setReason(reason);
		event.setUser(user);
		event.setTime(time);
		event.saveOrUpdate();
	}
	
	private void virtualize() {
		transferTo(null);
	}
	
	private void transferTo(StorageContainerPosition newPosition) {
		StorageContainerPosition oldPosition = getPosition();
		if (same(oldPosition, newPosition)) {
			return;
		}
		
		SpecimenTransferEvent transferEvent = new SpecimenTransferEvent(this);
		transferEvent.setUser(AuthUtil.getCurrentUser());
		transferEvent.setTime(Calendar.getInstance().getTime());
		
		if (oldPosition != null && newPosition != null) {
			transferEvent.setFromPosition(oldPosition);
			transferEvent.setToPosition(newPosition);
			
			oldPosition.update(newPosition);			
		} else if (oldPosition != null) {
			transferEvent.setFromPosition(oldPosition);
			
			oldPosition.vacate();
			setPosition(null);
		} else if (oldPosition == null && newPosition != null) {
			transferEvent.setToPosition(newPosition);
			
			newPosition.setOccupyingSpecimen(this);
			newPosition.occupy();
			setPosition(newPosition);
		}
		
		transferEvent.saveOrUpdate();		
	}
	
	public void addSpecimen(Specimen specimen) {
		specimen.setParentSpecimen(this);				
		if (specimen.isAliquot()) {
			specimen.decAliquotedQtyFromParent();		
			specimen.checkQtyConstraints();		
		}
		
		specimen.occupyPosition();		
		getChildCollection().add(specimen);
	}
	
	public CollectionProtocol getCollectionProtocol() {
		return getVisit().getCollectionProtocol();
	}
	
	public void setPending() {
		setCollectionStatus(PENDING);
	}
	
	public void checkQtyConstraints() {
		if (!isCollected()) { // No checks on un-collected specimens
			return;
		}
		
		ensureAliquotQtyOk(
				SpecimenErrorCode.INIT_QTY_LT_ALIQUOT_QTY, 
				SpecimenErrorCode.AVBL_QTY_GT_ACTUAL);
		
		if (isAliquot()) {
			//
			// Ensure initial quantity is less than parent specimen quantity
			//
			if (initialQuantity > parentSpecimen.getInitialQuantity()) {
				throw OpenSpecimenException.userError(SpecimenErrorCode.ALIQUOT_QTY_GT_PARENT_QTY);
			}
			
			parentSpecimen.ensureAliquotQtyOk(
					SpecimenErrorCode.PARENT_INIT_QTY_LT_ALIQUOT_QTY, 
					SpecimenErrorCode.PARENT_AVBL_QTY_GT_ACTUAL);
		}
 
	}
	
	public void decAliquotedQtyFromParent() {
		if (isCollected() && isAliquot()) {
			adjustParentSpecimenQty(initialQuantity);
		}		
	}
	
	public void occupyPosition() {
		if (position == null) {
			return;
		}
		
		if (!isCollected()) { 
			// Un-collected (pending/missed collection) specimens can't occupy space
			position = null;
			return;
		}
				
		position.occupy();
	}
	
	public void addCollRecvEvents() {
		addCollectionEvent();
		addReceivedEvent();		
	}
	
	public void setLabelIfEmpty() {
		if (StringUtils.isNotBlank(label) || !isCollected()) {
			return;
		}
		
		String labelTmpl = getLabelTmpl();				
		String label = null;
		if (StringUtils.isNotBlank(labelTmpl)) {
			label = labelGenerator.generateLabel(labelTmpl, this);
		} else if (isAliquot() || isDerivative()) {
			Specimen parentSpecimen = getParentSpecimen();
			int count = parentSpecimen.getChildCollection().size();
			label = parentSpecimen.getLabel() + "_" + (count + 1);
		}
		
		if (StringUtils.isBlank(label)) {
			throw OpenSpecimenException.userError(SpecimenErrorCode.LABEL_REQUIRED);
		}
		
		setLabel(label);
	}

	private double getAliquotQuantity() {
		double aliquotQty = 0.0;
		for (Specimen child : getChildCollection()) {
			if (child.isAliquot() && child.isCollected()) {
				aliquotQty += child.getInitialQuantity();
			}
		}
		
		return aliquotQty;		
	}
	
	public String getLabelTmpl() {
		String labelTmpl = null;
		
		SpecimenRequirement sr = getSpecimenRequirement();
		if (sr != null) { // anticipated specimen
			labelTmpl = sr.getLabelFormat();
		}
				
		if (StringUtils.isNotBlank(labelTmpl)) {
			return labelTmpl;
		}
		
		CollectionProtocol cp = getVisit().getCollectionProtocol();
		if (isAliquot()) {
			labelTmpl = cp.getAliquotLabelFormat();
		} else if (isDerivative()) {
			labelTmpl = cp.getDerivativeLabelFormat();
		} else {
			labelTmpl = cp.getSpecimenLabelFormat();
		}			
		
		return labelTmpl;		
	}
	
	public void updatePosition(StorageContainerPosition newPosition) {
		transferTo(newPosition);
	}
	
	public String getLabelOrDesc() {
		if (StringUtils.isNotBlank(label)) {
			return label;
		}
		
		return getDesc(specimenClass, specimenType);
	}
	
	public static String getDesc(String specimenClass, String type) {
		StringBuilder desc = new StringBuilder();
		if (StringUtils.isNotBlank(specimenClass)) {
			desc.append(specimenClass);
		}
		
		if (StringUtils.isNotBlank(type)) {
			if (desc.length() > 0) {
				desc.append("-");
			}
			
			desc.append(type);
		}
			
		return desc.toString();		
	}

	private void ensureChildSpecimensAreDisabled() {
		for (Specimen specimen : getChildCollection()) {
			if (!specimen.getActivityStatus().equals(Status.ACTIVITY_STATUS_DISABLED.getStatus())) {
				throw OpenSpecimenException.userError(SpecimenErrorCode.REF_ENTITY_FOUND);
			}
		}
	}
	
	/**
	 * Ensures following constraints are adhered
	 * 1. Specimen initial quantity is greater than or equals to sum of
	 *    all immediate aliquot child specimens
	 * 2. Specimen available quantity is less than 
	 *    initial - sum of all immediate aliquot child specimens initial quantity
	 */
	private void ensureAliquotQtyOk(SpecimenErrorCode initGtAliquotQty, SpecimenErrorCode avblQtyGtAct) {		
		double initialQty = getInitialQuantity();
		double aliquotQty = getAliquotQuantity();
		
		if (initialQty < aliquotQty) {
			throw OpenSpecimenException.userError(initGtAliquotQty);
		}
		
		double actAvailableQty = initialQty - aliquotQty;
		if (getAvailableQuantity() > actAvailableQty) {
			throw OpenSpecimenException.userError(avblQtyGtAct);
		}
	}
			
	private void addCollectionEvent() {
		if (isAliquot() || isDerivative()) {
			return;
		}
		
		getCollectionEvent().saveOrUpdate();		
	}
	
	private void addReceivedEvent() {
		if (isAliquot() || isDerivative()) {
			return;
		}
		
		getReceivedEvent().saveOrUpdate();
	}
	
	private void deleteEvents() {
		if (!isAliquot() && !isDerivative()) {
			getCollectionEvent().delete();
			getReceivedEvent().delete();
		}
		
		for (SpecimenTransferEvent te : getTransferEvents()) {
			te.delete();
		}
	}
	
	private boolean same(StorageContainerPosition p1, StorageContainerPosition p2) {
		if (p1 == null && p2 == null) {
			return true;
		}
		
		if (p1 == null || p2 == null) {
			return false;
		}
		
		if (!p1.getContainer().equals(p2.getContainer())) {
			return false;
		}
		
		return p1.getPosOneOrdinal() == p2.getPosOneOrdinal() && 
				p1.getPosTwoOrdinal() == p2.getPosTwoOrdinal();
	}
	
	private void adjustParentSpecimenQty(double qty) {
		parentSpecimen.setAvailableQuantity(parentSpecimen.getAvailableQuantity() - qty); 
	}
	
	private void updateEvent(SpecimenEvent thisEvent, SpecimenEvent otherEvent) {
		if (isAliquot() || isDerivative()) {
			return;
		}
		
		thisEvent.update(otherEvent);
	}	
}
