package com.tonyvu.profile.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;

import com.tonyvu.profile.core.RecentVisit;

public class RecentVisitDAO extends AbstractDAO<RecentVisit> {

	public RecentVisitDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public RecentVisit findById(Long id) {
	    return get(id);
	}

	public RecentVisit create(RecentVisit recentVisit) {
	    return persist(recentVisit);
	}
	    
	public List<RecentVisit> findRecentVisitByTarget(long targetId) {
	    return list(namedQuery("com.tonyvu.profile.core.RecentVisit.findRecentVisitByTarget").setParameter("targetId", targetId));
	}
	
	public void remove(RecentVisit recentVisit) {
		currentSession().delete(recentVisit);
	}
	
	public void removeExpiredRecentVisit(Date cutoffTime) {
	    namedQuery("com.tonyvu.profile.core.RecentVisit.removeExpiredRecentVisit").setTimestamp("cutoffTime", cutoffTime).executeUpdate();
	}
}
