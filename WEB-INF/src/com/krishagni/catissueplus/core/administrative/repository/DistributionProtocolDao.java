
package com.krishagni.catissueplus.core.administrative.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocol;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface DistributionProtocolDao extends Dao<DistributionProtocol> {

	public DistributionProtocol getByShortTitle(String shortTitle);

	public DistributionProtocol getDistributionProtocol(String title);

	public List<DistributionProtocol> getDistributionProtocols(DpListCriteria criteria);
	
	//
	// At present this is only returning count of specimens distributed by protocol
	// in future this would be extended to return other stats related to protocol
	//	
	public Map<Long, Integer> getSpecimensCountByDpIds(Collection<Long> dpIds);

}
