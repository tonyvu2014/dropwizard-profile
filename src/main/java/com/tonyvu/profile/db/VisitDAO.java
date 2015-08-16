package com.tonyvu.profile.db;

import java.util.List;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import com.truecaller.profile.core.Visit;

public class VisitDAO extends AbstractDAO<Visit> {

	public VisitDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
    public Visit findById(Long id) {
        return get(id);
    }

    public Visit create(Visit visit) {
        return persist(visit);
    }
    
    public List<Visit> findVisitByTarget(long targetId) {
    	return list(namedQuery("com.tonyvu.profile.core.Visit.findVisitByTarget").setParameter("targetId", targetId));
    }

}
