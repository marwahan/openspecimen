
package com.krishagni.catissueplus.core.administrative.services;

import com.krishagni.catissueplus.core.administrative.events.CreateEquipmentEvent;
import com.krishagni.catissueplus.core.administrative.events.DeleteEquipmentEvent;
import com.krishagni.catissueplus.core.administrative.events.EquipmentCreatedEvent;
import com.krishagni.catissueplus.core.administrative.events.EquipmentDeletedEvent;
import com.krishagni.catissueplus.core.administrative.events.EquipmentUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.PatchEquipmentEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdateEquipmentEvent;

public interface EquipmentService {

	public EquipmentCreatedEvent createEquipment(CreateEquipmentEvent reqEvent);

	public EquipmentUpdatedEvent updateEquipment(UpdateEquipmentEvent reqEvent);

	public EquipmentUpdatedEvent patchEquipment(PatchEquipmentEvent reqEvent);

	public EquipmentDeletedEvent deleteEquipment(DeleteEquipmentEvent reqEvent);

}
