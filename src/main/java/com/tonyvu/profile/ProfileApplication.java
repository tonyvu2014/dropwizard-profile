package com.tonyvu.profile;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.security.Principal;
import javax.ws.rs.core.SecurityContext;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.SessionFactory;
import com.google.common.base.Function;
import com.tonyvu.profile.auth.AdminAuthenticator;
import com.tonyvu.profile.core.RecentVisit;
import com.tonyvu.profile.core.User;
import com.tonyvu.profile.core.UserProfile;
import com.tonyvu.profile.core.Visit;
import com.tonyvu.profile.db.RecentVisitDAO;
import com.tonyvu.profile.db.UserProfileDAO;
import com.tonyvu.profile.db.VisitDAO;
import com.tonyvu.profile.resources.UserProfileResource;
import com.xeiam.dropwizard.sundial.SundialBundle;
import com.xeiam.dropwizard.sundial.SundialConfiguration;
import com.xeiam.dropwizard.sundial.tasks.AddCronJobTriggerTask;
import com.xeiam.dropwizard.sundial.tasks.AddJobTask;
import com.xeiam.dropwizard.sundial.tasks.LockSundialSchedulerTask;
import com.xeiam.dropwizard.sundial.tasks.RemoveJobTask;
import com.xeiam.dropwizard.sundial.tasks.RemoveJobTriggerTask;
import com.xeiam.dropwizard.sundial.tasks.StartJobTask;
import com.xeiam.dropwizard.sundial.tasks.StopJobTask;
import com.xeiam.dropwizard.sundial.tasks.UnlockSundialSchedulerTask;

public class ProfileApplication extends Application<ProfileConfiguration> {

	public static void main(String[] args) throws Exception {
		new ProfileApplication().run(args);
	}

	private final HibernateBundle<ProfileConfiguration> hibernateBundle = new HibernateBundle<ProfileConfiguration>(
			UserProfile.class, Visit.class, RecentVisit.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(
				ProfileConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};
		

	@Override
	public String getName() {
		return "profile";
	}

	@Override
	public void initialize(Bootstrap<ProfileConfiguration> bootstrap) {
		// Enable variable substitution with environment variables
		bootstrap
				.setConfigurationSourceProvider(new SubstitutingSourceProvider(
						bootstrap.getConfigurationSourceProvider(),
						new EnvironmentVariableSubstitutor(false)));
		bootstrap.addBundle(new MigrationsBundle<ProfileConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(
					ProfileConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		bootstrap.addBundle(hibernateBundle);
	    bootstrap.addBundle(new SundialBundle<ProfileConfiguration>() {

		    @Override
		    public SundialConfiguration getSundialConfiguration(ProfileConfiguration configuration) {
		      return configuration.getSundialConfiguration();
		    }

			@Override
			public void initialize(Bootstrap bootstrap) {
				
			}

	    });
	}

	@Override
	public void run(ProfileConfiguration configuration, Environment environment)
			throws Exception {
		SessionFactory sessionFactory = hibernateBundle.getSessionFactory();
		final UserProfileDAO userProfileDAO = new UserProfileDAO(sessionFactory);
		final VisitDAO visitDAO = new VisitDAO(sessionFactory);
		final RecentVisitDAO recentVisitDAO = new RecentVisitDAO(sessionFactory);
		AdminAuthenticator adminAuthenticator = new AdminAuthenticator();
		environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User, AdminAuthenticator>()
							.setAuthenticator(adminAuthenticator)
							.setRealm("SECRET REALM")
							.buildAuthFilter()));
		environment.jersey().register(new AuthValueFactoryProvider.Binder<User>(User.class));
		environment.jersey().register(RolesAllowedDynamicFeature.class);
		environment.jersey().register(new UserProfileResource(userProfileDAO, visitDAO, recentVisitDAO));
		// Add object to ServletContext for accessing from Sundial Jobs
		environment.getApplicationContext().setAttribute("sessionFactory", sessionFactory);
		
		// Add sundial tasks which should be triggered by a POST, but don't need to respond
		environment.admin().addTask(new LockSundialSchedulerTask());
		environment.admin().addTask(new UnlockSundialSchedulerTask());
		environment.admin().addTask(new RemoveJobTriggerTask());
		environment.admin().addTask(new AddCronJobTriggerTask());
		environment.admin().addTask(new StartJobTask());
		environment.admin().addTask(new StopJobTask());
		environment.admin().addTask(new RemoveJobTask());
		environment.admin().addTask(new AddJobTask());
	}

}
