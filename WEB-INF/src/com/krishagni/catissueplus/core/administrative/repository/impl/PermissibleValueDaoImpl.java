package com.krishagni.catissueplus.core.administrative.repository.impl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import com.krishagni.catissueplus.core.administrative.domain.PermissibleValue;
import com.krishagni.catissueplus.core.administrative.events.ListPvCriteria;
import com.krishagni.catissueplus.core.administrative.repository.PermissibleValueDao;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;


public class PermissibleValueDaoImpl extends AbstractDao<PermissibleValue> implements PermissibleValueDao {

	@Override
	public PermissibleValue getById(Long id) {
		return (PermissibleValue) sessionFactory.getCurrentSession().get(PermissibleValue.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PermissibleValue> getPvs(ListPvCriteria crit) {
		Criteria query = null;
		if (StringUtils.isNotBlank(crit.attribute()) && crit.attribute().equals(ANATOMIC_SITE)) {
			query = getAnatomicSiteQuery(crit);
		} else {
			query = getPvQuery(crit);
		}
		
		if (StringUtils.isNotBlank(crit.query())) {
			query.add(Restrictions.ilike("value", crit.query(), MatchMode.ANYWHERE));
		}
		
		int maxResults = crit.maxResults() < 0 ? 100 : crit.maxResults();
		return query.setMaxResults(maxResults)
			.addOrder(Order.asc("value"))
			.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getSpecimenClasses() {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class)
				.add(Restrictions.eq("attribute", SPECIMEN_CLASS));
		return query.setProjection(Projections.property("value")).list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getSpecimenTypes(Collection<String> specimenClasses) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class)
				.createAlias("parent", "ppv")
				.add(Restrictions.eq("ppv.attribute", SPECIMEN_CLASS));
		
		if (CollectionUtils.isNotEmpty(specimenClasses)) {
			query.add(Restrictions.in("ppv.value", specimenClasses));
		}
		
		return query.setProjection(Projections.property("value")).list();
	}	
	
	
	public boolean exists(String attribute, Collection<String> values) {
		Number count = (Number)sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class)
				.add(Restrictions.eq("attribute", attribute))
				.add(Restrictions.in("value", values))
				.setProjection(Projections.count("id"))
				.uniqueResult();
		return count.intValue() == values.size();
	}
	
	public boolean exists(String attribute, String parentValue, Collection<String> values) {
		Number count = (Number)sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class)
				.createAlias("parent", "ppv")
				.add(Restrictions.eq("ppv.attribute", attribute))
				.add(Restrictions.eq("ppv.value", parentValue))
				.add(Restrictions.in("value", values))
				.setProjection(Projections.count("id"))
				.uniqueResult();
		return count.intValue() == values.size();
	}

	public boolean exists(String attribute, int depth, Collection<String> values) {
		return exists(attribute, depth, values, false);
	}
		
	public boolean exists(String attribute, int depth, Collection<String> values, boolean anyLevel) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class)
				.add(Restrictions.in("value", values))
				.setProjection(Projections.count("id"));
		
		for (int i = 1; i <= depth; ++i) {			
			if (i == 1) {
				query.createAlias("parent", "pv" + i, anyLevel ? JoinType.LEFT_OUTER_JOIN : JoinType.INNER_JOIN);
			} else {
				query.createAlias("pv" + (i - 1) + ".parent", "pv" + i, anyLevel ? JoinType.LEFT_OUTER_JOIN : JoinType.INNER_JOIN);
			}			
		}
		
		Disjunction attrCond = Restrictions.disjunction();
		attrCond.add(Restrictions.eq("pv" + depth + ".attribute", attribute));
		if (anyLevel) {
			for (int i = depth - 1; i >= 1; i--) {
				attrCond.add(Restrictions.eq("pv" + i + ".attribute", attribute));
			}
			
			attrCond.add(Restrictions.eq("attribute", attribute));
		}
		
		Number count = (Number)query.add(attrCond).uniqueResult();
		return count.intValue() == values.size();
	}
	
	private Criteria getPvQuery(ListPvCriteria crit) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class);
		if (StringUtils.isNotBlank(crit.parentAttribute()) || StringUtils.isNotBlank(crit.parentValue())) {
			query.createAlias("parent", "p");
		}
				
		if (StringUtils.isNotBlank(crit.attribute())) {
			query.add(Restrictions.eq("attribute", crit.attribute()));
		} else if (StringUtils.isNotBlank(crit.parentAttribute())) {
			query.add(Restrictions.eq("p.attribute", crit.parentAttribute()));
		}
		
		if (StringUtils.isNotBlank(crit.parentValue())) {
			query.add(Restrictions.eq("p.value", crit.parentValue()));
		}
		
		return query;
	}
	
	private Criteria getAnatomicSiteQuery(ListPvCriteria crit) {
		return sessionFactory.getCurrentSession().createCriteria(PermissibleValue.class)
				.createAlias("parent", "mpv")
				.createAlias("mpv.parent", "tpv", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.disjunction()
						.add(Restrictions.eq("tpv.attribute", ANATOMIC_SITE))
						.add(Restrictions.eq("mpv.attribute", ANATOMIC_SITE))
					);
	}
	
	private static final String ANATOMIC_SITE = "Tissue_Site_PID";
	
	private static final String SPECIMEN_CLASS = "2003991";
}
