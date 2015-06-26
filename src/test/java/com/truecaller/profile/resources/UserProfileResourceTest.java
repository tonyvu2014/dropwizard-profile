package com.truecaller.profile.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import com.truecaller.profile.core.RecentVisit;
import com.truecaller.profile.core.User;
import com.truecaller.profile.core.UserProfile;
import com.truecaller.profile.core.Visit;
import com.truecaller.profile.db.RecentVisitDAO;
import com.truecaller.profile.db.UserProfileDAO;
import com.truecaller.profile.db.VisitDAO;
import com.truecaller.profile.helper.Constant;
import com.truecaller.profile.helper.DateUtil;

/**
 * Unit tests for {@link UserProfileResource}.
 */
public class UserProfileResourceTest {

	private static final UserProfileDAO userProfileDAO = mock(UserProfileDAO.class);
	private static final VisitDAO visitDAO = mock(VisitDAO.class);
	private static final RecentVisitDAO recentVisitDAO = mock(RecentVisitDAO.class);
	
	@ClassRule
	public static final ResourceTestRule resourceRule = ResourceTestRule.builder()
			.addResource(new UserProfileResource(userProfileDAO, visitDAO, recentVisitDAO))
			.setTestContainerFactory(new GrizzlyWebTestContainerFactory())
			.addProvider(new AuthValueFactoryProvider.Binder<User>(User.class))
			.build();
	private UserProfile userProfile1;
	private UserProfile userProfile2;
	private Visit visit;
	private List<RecentVisit> recentVisits;


	@Before
	public void setUp() {
		userProfile1 = new UserProfile();
		userProfile1.setId(1L);
		userProfile1.setName("Joe Hart");
		userProfile1.setUsername("joe");
		
		userProfile2 = new UserProfile();
		userProfile1.setId(2L);
		userProfile1.setName("Ryan Tan");
		userProfile1.setUsername("ryan");
		
		visit = new Visit();
		visit.setId(1L);
		visit.setVisitorId(1L);
		visit.setVisitTime(new Date());
		visit.setTargetId(2L);
		
		recentVisits = setUpRecentVisits();
	}
	
	//Add 10 recent visit records
	private List<RecentVisit> setUpRecentVisits() {
		List<RecentVisit> recentVisits = new ArrayList<RecentVisit>();
		
		for (int i=0;i<Constant.MAX_RECENT_VIEW_RECORD;i++) {
			RecentVisit recentVisit = new RecentVisit();
			recentVisit.setId(Long.valueOf(i));
			recentVisit.setVisitorId(1L);
			recentVisit.setTargetId(2L);
			recentVisit.setVisitTime(new Date());
			recentVisits.add(recentVisit);
		}
		
		return recentVisits;
	}

	@After
	public void tearDown() {
		reset(userProfileDAO);
		reset(visitDAO);
		reset(recentVisitDAO);
	}

	//Verify that the view profile API will return the correct target profile id
	@Test
	public void visitSuccessTest() {
		when(userProfileDAO.hasExist(1L)).thenReturn(true);
		when(userProfileDAO.hasExist(2L)).thenReturn(true);
		when(userProfileDAO.findById(1L)).thenReturn(userProfile1);
		when(userProfileDAO.findById(2L)).thenReturn(userProfile2);
		when(visitDAO.create(any(Visit.class))).thenReturn(visit);
		UserProfile found = resourceRule.getJerseyTest().target("/profile/1/visit/2").request().get(UserProfile.class);
		assertThat(found.getId()).isEqualTo(userProfile2.getId());
		verify(visitDAO).create(any(Visit.class));
	}
	
	//Verify that if visitor profile id is not found, WebApplicationException is returned for the view profile API 
	@Test(expected=WebApplicationException.class)
	public void visitVisitorNotFoundTest() {
		when(userProfileDAO.hasExist(3L)).thenReturn(false);
		when(userProfileDAO.hasExist(2L)).thenReturn(true);
		when(userProfileDAO.findById(2L)).thenReturn(userProfile2);
		resourceRule.getJerseyTest().target("/profile/3/visit/2").request().get(UserProfile.class);
	}
	
	//Verify that if target profile id is not found, WebApplicationException is returned for the view profile API 
	@Test(expected=WebApplicationException.class)
	public void visitTargetNotFoundTest() {
		when(userProfileDAO.hasExist(99L)).thenReturn(false);
		when(userProfileDAO.hasExist(1L)).thenReturn(true);
		when(userProfileDAO.findById(1L)).thenReturn(userProfile1);
	    resourceRule.getJerseyTest().target("/profile/1/visit/99").request().get(UserProfile.class);
	}
	
	//Verify that only maximum 10 recent visit records are returned
	@Test
	public void viewRecentVisitsMaxRecordsTest() {
		RecentVisit recentVisit = new RecentVisit();
		recentVisit.setId(Long.valueOf(Constant.MAX_RECENT_VIEW_RECORD));
		recentVisit.setVisitorId(1L);
		recentVisit.setTargetId(2L);
		recentVisit.setVisitTime(new Date());
		recentVisits.add(recentVisit);
		when(userProfileDAO.hasExist(1L)).thenReturn(true);
		when(userProfileDAO.hasExist(2L)).thenReturn(true);
		when(recentVisitDAO.findRecentVisitByTarget(2L)).thenReturn(recentVisits);
		List<RecentVisit> found = resourceRule.getJerseyTest().target("/profile/1/viewRecentVisits/2").request().get(new GenericType<List<RecentVisit>>() {});
		assertThat(found.size()).isEqualTo(Constant.MAX_RECENT_VIEW_RECORD);
	}
	
	//Verify that any record which older than 10 days will not be included in the result of view recent visits API
	@Test
	public void viewRecentVisitsMaxDaysTest() {
		recentVisits.remove(0);//Remove the first element to leave recentVisits with 1 less than maximum records
		RecentVisit recentVisit = new RecentVisit();
		recentVisit.setId(Long.valueOf(Constant.MAX_RECENT_VIEW_RECORD));
		recentVisit.setVisitorId(1L);
		recentVisit.setTargetId(2L);
		recentVisit.setVisitTime(DateUtil.getPastDate(new Date(), Constant.MAX_RECENT_VIEW_DAY));
		recentVisits.add(recentVisit);
		when(userProfileDAO.hasExist(1L)).thenReturn(true);
		when(userProfileDAO.hasExist(2L)).thenReturn(true);
		when(recentVisitDAO.findRecentVisitByTarget(2L)).thenReturn(recentVisits);
		List<RecentVisit> found = resourceRule.getJerseyTest().target("/profile/1/viewRecentVisits/2").request().get(new GenericType<List<RecentVisit>>() {});
		assertThat(found.size()).isEqualTo(Constant.MAX_RECENT_VIEW_RECORD-1);
	}

}
