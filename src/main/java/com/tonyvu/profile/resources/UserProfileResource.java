package com.tonyvu.profile.resources;

import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.annotation.Timed;
import com.tonyvu.profile.core.RecentVisit;
import com.tonyvu.profile.core.User;
import com.tonyvu.profile.core.UserProfile;
import com.tonyvu.profile.core.Visit;
import com.tonyvu.profile.db.RecentVisitDAO;
import com.tonyvu.profile.db.UserProfileDAO;
import com.tonyvu.profile.db.VisitDAO;
import com.tonyvu.profile.helper.Constant;
import com.tonyvu.profile.helper.DateUtil;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON)
public class UserProfileResource {
	
	private final Logger logger = LoggerFactory.getLogger(UserProfileResource.class);
	
	private final UserProfileDAO userProfileDAO;
	private final VisitDAO visitDAO;
	private final RecentVisitDAO recentVisitDAO;
	
	public UserProfileResource(UserProfileDAO userProfileDAO, VisitDAO visitDAO, RecentVisitDAO recentVisitDAO) {
		this.userProfileDAO = userProfileDAO;
		this.visitDAO = visitDAO;
		this.recentVisitDAO = recentVisitDAO;
	}
	
	/**
	 * Creates an user profile
	 * <p>This API is protected with username and password</p>
	 * 
	 * @param userProfile data of user profile to be created
	 * @return the profile created
	 */
	@POST
	@UnitOfWork
	@RolesAllowed("ADMIN")
	@Path("/create")
	public UserProfile createProfile(@Auth User user, UserProfile userProfile) {
		return userProfileDAO.create(userProfile);
	}
	
	@GET
	@UnitOfWork
	@Path("/list")
	public List<UserProfile> listProfile() {
		return userProfileDAO.findAll();
	}
	
	/**
	 * Views another user's profile
	 * 
	 * @param visitorId profile id of the visitor user
	 * @param targetId profile id of the target user
	 * @return information of the target user profile 
	 */
	@GET
	@UnitOfWork
	@Timed
	@Path("/{visitorId}/visit/{targetId}")
	public UserProfile visitProfile(@PathParam("visitorId") LongParam visitorId, @PathParam("targetId") LongParam targetId) {
		final Long vId = visitorId.get();
		final Long tId = targetId.get();
		logger.info("User " + vId + " visits user " + tId + " profile");
		
		//Throws exception if visitor id or target id cannot be found
		if (!userProfileDAO.hasExist(vId) || !userProfileDAO.hasExist(tId)) throw new WebApplicationException(Response.Status.NOT_FOUND);
		
		//Creates a visit record
		createVisit(vId, tId);
		
		//Maintains the recentVisit table
	    updateRecentVisitTable(vId, tId);
		
		UserProfile userProfile = userProfileDAO.findById(tId);
		return userProfile;
		
	}
	
	
	/**
	 * Views the recent view history of a certain user
	 * 
	 * <p>The history is limited to:</p>
	 * <ul>
	 * <li>No more than 10 items</li>
	 * <li>Views which are not older than 10 days</li>
	 * </ul>
	 *  
	 * @param visitorId profile id of the visitor user
	 * @param targetId profile id of the target user
	 * @return a list of recent profile views 
	 */
	@GET
	@UnitOfWork
	@Timed
	@Path("/{visitorId}/viewRecentVisits/{targetId}")
	public List<RecentVisit> viewRecentVisits(@PathParam("visitorId") LongParam visitorId, @PathParam("targetId") LongParam targetId) {
		Long vId = visitorId.get();
		Long tId = targetId.get();
		logger.info("User " + vId + " views visit history of user " + tId);
		
		//Throws exception if visitor id or target id cannot be found
		if (!userProfileDAO.hasExist(vId) || !userProfileDAO.hasExist(tId)) throw new WebApplicationException(Response.Status.NOT_FOUND);

		//Gets list of target profile's recent visits
		List<RecentVisit> recentVisits = recentVisitDAO.findRecentVisitByTarget(tId);
		List<RecentVisit> filteredVisits = new ArrayList<RecentVisit>();
	   
		Date cutoffDate = DateUtil.getPastDate(new Date(), Constant.MAX_RECENT_VIEW_DAY);
	    int count = 0;
        for (RecentVisit recentVisit : recentVisits) {
        	//Only add views which are not more than 10 days old and if there are less than 10 records
	    	if (cutoffDate.compareTo(recentVisit.getVisitTime()) <= 0 && count<Constant.MAX_RECENT_VIEW_RECORD) {
	    		filteredVisits.add(recentVisit);
	    		count++;
	    	} else {
	    		break;
	    	}
        }
		    
		return filteredVisits;
	}
	
	private void createVisit(Long visitorId, Long targetId) {
		logger.info("Creating a visit record");
		Visit visit = new Visit();
		visit.setVisitorId(visitorId);
		visit.setTargetId(targetId);
		visitDAO.create(visit);
	}
	
	private void createRecentVisit(Long visitorId, Long targetId) {
		logger.info("Creating a recent visit record");
		RecentVisit recentVisit = new RecentVisit();
		recentVisit.setVisitorId(visitorId);
		recentVisit.setTargetId(targetId);
		recentVisitDAO.create(recentVisit);
	}
	
	
	//Remove oldest recent visit record to a target profile if there are more than 10 of them 
	private void updateRecentVisitTable(Long visitorId, Long targetId) {
		logger.info("Updating RecentVisit table");
		List<RecentVisit> recentVisits = recentVisitDAO.findRecentVisitByTarget(targetId);
    
		int total = recentVisits.size(); 
	    if (total >= Constant.MAX_RECENT_VIEW_RECORD) {
	    	RecentVisit oldestVisit = recentVisits.get(total-1);
	    	if (oldestVisit != null) {
	    		recentVisitDAO.remove(oldestVisit);
	    	}
	    }
	    
	    //Add the current visit to recentVisit table
	    createRecentVisit(visitorId, targetId);
	}
}
