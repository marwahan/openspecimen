package com.krishagni.catissueplus.core.administrative.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.administrative.domain.DistributionOrder;
import com.krishagni.catissueplus.core.administrative.events.DistributionOrderListCriteria;
import com.krishagni.catissueplus.core.administrative.events.DistributionOrderSummary;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolDetail;
import com.krishagni.catissueplus.core.administrative.repository.DistributionOrderDao;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.util.Status;

public class DistributionOrderDaoImpl extends AbstractDao<DistributionOrder> implements DistributionOrderDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<DistributionOrderSummary> getOrders(DistributionOrderListCriteria listCrit) {
		List<Object[]> rows = getOrderList(listCrit);
		
		List<DistributionOrderSummary> result = new ArrayList<DistributionOrderSummary>();
		Map<Long, DistributionOrderSummary> doMap = new HashMap<Long, DistributionOrderSummary>();
		
		for (Object[] row : rows) {
			DistributionOrderSummary order = getDoSummary(row);
			result.add(order);
			
			if (listCrit.includeStat()) {
				doMap.put(order.getId(), order);
			}
		}
		
		if (listCrit.includeStat() && !doMap.isEmpty()) {
			rows = getSessionFactory().getCurrentSession()
				.getNamedQuery(GET_SPEC_CNT_BY_ORDER)
				.setParameterList("orderIds", doMap.keySet())
				.list();
			
			for (Object[] row : rows) {
				DistributionOrderSummary order = doMap.get((Long)row[0]);
				order.setSpecimenCnt((Long)row[1]);
			}
		}
		
		return result;		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public DistributionOrder getOrder(String name) {
		List<DistributionOrder> result = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_DIST_ORD_BY_NAME)
				.setString("name", name)
				.list();
				
		return result.isEmpty() ? null : result.iterator().next();
	}
	
	@Override
	public Class<DistributionOrder> getType() {
		return DistributionOrder.class;
	}
	
	@SuppressWarnings("unchecked")
	private List<Object[]> getOrderList(DistributionOrderListCriteria crit) {
		Criteria query = sessionFactory.getCurrentSession()
				.createCriteria(DistributionOrder.class)
				.createAlias("distributionProtocol", "dp")
				.createAlias("requester", "user")
				.createAlias("site", "site")
				.add(Restrictions.ne("activityStatus", Status.ACTIVITY_STATUS_DISABLED.getStatus()))
				.setFirstResult(crit.startAt() < 0 ? 0 : crit.startAt())
				.setMaxResults(crit.maxResults() < 0 || crit.maxResults() > 100 ? 100 : crit.maxResults())
				.addOrder(Order.desc("id"));
		
		//
		// Restrict by institutes 
		//
		if (CollectionUtils.isNotEmpty(crit.instituteIds())) {
			query.createAlias("dp.institute", "institute")
				.add(Restrictions.in("institute.id", crit.instituteIds()));
		}
		
		//
		// Restrict by search term
		//
		String searchTerm = crit.query();
		if (StringUtils.isNotBlank(searchTerm)) {
			query.add(Restrictions.ilike("name", searchTerm, MatchMode.ANYWHERE));
		}
		
		addProjections(query);
		return query.list();		
	}
	
	private void addProjections(Criteria query) {
		ProjectionList projs = Projections.projectionList();
		query.setProjection(projs);
		
		projs.add(Projections.property("id"));
		projs.add(Projections.property("name"));
		projs.add(Projections.property("creationDate"));
		projs.add(Projections.property("executionDate"));
		projs.add(Projections.property("status"));
		projs.add(Projections.property("dp.id"));
		projs.add(Projections.property("dp.shortTitle"));
		projs.add(Projections.property("site.id"));
		projs.add(Projections.property("site.name"));
		projs.add(Projections.property("user.id"));
		projs.add(Projections.property("user.firstName"));
		projs.add(Projections.property("user.lastName"));
		projs.add(Projections.property("user.emailAddress"));
	}
	
	private DistributionOrderSummary getDoSummary(Object[] row) {
		DistributionOrderSummary result = new DistributionOrderSummary();
		result.setId((Long)row[0]);
		result.setName((String)row[1]);
		result.setCreationDate((Date)row[2]);
		result.setExecutionDate((Date)row[3]);
		result.setStatus(((DistributionOrder.Status)row[4]).name());
		
		DistributionProtocolDetail dp = new DistributionProtocolDetail();
		dp.setId((Long)row[5]);
		dp.setShortTitle((String)row[6]);
		result.setDistributionProtocol(dp);
		
		result.setSiteId((Long)row[7]);
		result.setSiteName((String)row[8]);
		
		UserSummary requester = new UserSummary();
		requester.setId((Long)row[9]);
		requester.setFirstName((String)row[10]);
		requester.setLastName((String)row[11]);
		requester.setEmailAddress((String)row[12]);
		result.setRequester(requester);
		
		return result;
	}
	
	public static final String FQN  = DistributionOrder.class.getName();
	
	private static final String GET_DIST_ORD_BY_NAME = FQN + ".getOrderByName";
	
	private static final String GET_SPEC_CNT_BY_ORDER = FQN + ".getSpecimenCountByOrder";

}
