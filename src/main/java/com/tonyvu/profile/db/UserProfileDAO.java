package com.tonyvu.profile.db;

import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import org.hibernate.SessionFactory;
import com.tonyvu.profile.core.UserProfile;

public class UserProfileDAO extends AbstractDAO<UserProfile> {
	
    public UserProfileDAO(SessionFactory factory) {
        super(factory);
    }

    public UserProfile findById(Long id) {
        return get(id);
    }

    public UserProfile create(UserProfile userProfile) {
        return persist(userProfile);
    }
    
    public List<UserProfile> findAll() {
    	return list(namedQuery("com.tonyvu.profile.core.UserProfile.findAll"));
    }
    
    public boolean hasExist(Long id) {
    	return (findById(id) != null);
    }
}