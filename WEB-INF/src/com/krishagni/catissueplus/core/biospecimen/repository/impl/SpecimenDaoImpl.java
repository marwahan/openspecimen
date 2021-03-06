
package com.krishagni.catissueplus.core.biospecimen.repository.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenDao;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenListCriteria;
import com.krishagni.catissueplus.core.common.Pair;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;

public class SpecimenDaoImpl extends AbstractDao<Specimen> implements SpecimenDao {	
	public Class<?> getType() {
		return Specimen.class;
	}

	@SuppressWarnings("unchecked")
	public List<Specimen> getSpecimens(SpecimenListCriteria crit) {
		Criteria query = getSessionFactory().getCurrentSession().createCriteria(Specimen.class)
				.add(Restrictions.eq("activityStatus", "Active"))
				.addOrder(Order.asc("id"))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		List<String> labels = crit.labels();
		if (CollectionUtils.isNotEmpty(labels)) {
			addLabelsCond(query, labels);
		} else {
			query.setFirstResult(crit.startAt() < 0 ? 0 : crit.startAt())
				.setMaxResults(crit.maxResults() <= 0 ? 100 : crit.maxResults());
		}
		
		if (CollectionUtils.isNotEmpty(crit.siteCps())) {
			addSiteCpsCond(query, crit.siteCps());
		}
				
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Specimen getByLabel(String label) {
		List<Specimen> specimens = sessionFactory.getCurrentSession()
			.getNamedQuery(GET_BY_LABEL)
			.setString("label", label)
			.list();
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Specimen getParentSpecimenByVisitAndSr(Long visitId, Long srId) {
		List<Specimen> specimens = sessionFactory.getCurrentSession()
			.getNamedQuery(GET_PARENT_BY_VISIT_AND_SR)
			.setLong("visitId", visitId)
			.setLong("srId", srId)
			.list();
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Specimen getByBarcode(String barcode) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Specimen.class);
		query.add(Restrictions.eq("barcode", barcode));
		List<Specimen> specimens = query.list();
		
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByIds(List<Long> specimenIds) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_BY_IDS)
				.setParameterList("specimenIds", specimenIds)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByVisitId(Long visitId) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_BY_VISIT_ID)
				.setLong("visitId", visitId)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByVisitName(String visitName) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_BY_VISIT_NAME)
				.setString("visitName", visitName)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Long> getCprAndVisitIds(Long specimenId) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_CPR_AND_VISIT_IDS)
				.setLong("specimenId", specimenId)
				.list();
		
		if (CollectionUtils.isEmpty(rows)) {
			return null;
		}
		
		Map<String, Long> result = new HashMap<String, Long>();
		Object[] row = rows.iterator().next();
		result.put("cpId", (Long)row[0]);
		result.put("cprId", (Long)row[1]);
		result.put("visitId", (Long)row[2]);
		result.put("specimenId", specimenId);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Long> getSpecimenInstitutes(Set<Long> specimenIds) {		
		List<Object[]> rows = getSessionFactory().getCurrentSession()
				.getNamedQuery(GET_SPECIMEN_INSTITUTE_IDS)
				.setParameterList("specimenIds", specimenIds)
				.list();
		
		Map<String, Long> result = new HashMap<String, Long>();
		for (Object[] row : rows) {
			result.put((String)row[0], (Long)row[1]);
		}
		
		return result;
	}
	
	private void addLabelsCond(Criteria query, List<String> labels) {
		int numLabels = labels.size();		
		Disjunction labelIn = Restrictions.disjunction();
		
		for (int i = 0; i < numLabels; i += 500) {
			List<String> params = labels.subList(i, i + 500 > numLabels ? numLabels : i + 500);
			labelIn.add(Restrictions.in("label", params));
			i += 500;
		}
		
		query.add(labelIn);		
	}
	
	private void addSiteCpsCond(Criteria query, List<Pair<Long, Long>> siteCps) {
		query.createAlias("visit", "visit")
			.createAlias("visit.registration", "cpr")
			.createAlias("cpr.collectionProtocol", "cp")
			.createAlias("cp.repositories", "cpSite")
			.createAlias("cpr.participant", "participant")
			.createAlias("participant.pmis", "pmi", JoinType.LEFT_OUTER_JOIN)
			.createAlias("pmi.site", "mrnSite", JoinType.LEFT_OUTER_JOIN);
		
		Disjunction cpSitesCond = Restrictions.disjunction();
		for (Pair<Long, Long> siteCp : siteCps) {
			Junction cond = Restrictions.conjunction();
			
			Long siteId = siteCp.first();
			Long cpId = siteCp.second();
		
			cond.add(
				/** mrn site = siteId or cp site = siteId **/
				Restrictions.disjunction()
					.add(Restrictions.eq("mrnSite.id", siteId))
					.add(Restrictions.eq("cpSite.id", siteId))
			);
								
			if (cpId != null) {
				cond.add(Restrictions.eq("cp.id", cpId));
			}
		
			cpSitesCond.add(cond);
		}
		
		query.add(cpSitesCond);
	}
			
	private static final String FQN = Specimen.class.getName();
	
	private static final String GET_BY_LABEL = FQN + ".getByLabel";
	
	private static final String GET_PARENT_BY_VISIT_AND_SR = FQN + ".getParentByVisitAndReq";
	
	private static final String GET_BY_IDS = FQN + ".getByIds";
	
	private static final String GET_BY_VISIT_ID = FQN + ".getByVisitId";
	
	private static final String GET_BY_VISIT_NAME = FQN + ".getByVisitName";
	
	private static final String GET_CPR_AND_VISIT_IDS = FQN + ".getCprAndVisitIds";
	
	private static final String GET_SPECIMEN_INSTITUTE_IDS = FQN + ".getSpecimenInstituteIds";
}
