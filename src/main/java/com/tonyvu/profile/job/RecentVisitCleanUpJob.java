package com.tonyvu.profile.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tonyvu.profile.db.RecentVisitDAO;
import com.tonyvu.profile.helper.Constant;
import com.tonyvu.profile.helper.DateUtil;
import com.xeiam.sundial.Job;
import com.xeiam.sundial.SundialJobScheduler;
import com.xeiam.sundial.annotations.CronTrigger;
import com.xeiam.sundial.exceptions.JobInterruptException;

/**
 * Daily job at 23:59:59 to remove any expired record from RecentVisit table
 *
 */
@CronTrigger(cron = "59 59 23 * * ?")
public class RecentVisitCleanUpJob extends Job {
	private final Logger logger = LoggerFactory
			.getLogger(RecentVisitCleanUpJob.class);

	@Override
	public void doRun() throws JobInterruptException {
		SimpleDateFormat formatter = new SimpleDateFormat(Constant.DISPLAY_DATE_FORMAT);
		Date now = new Date();
		logger.info("Running the recent visit clean up job at: " + formatter.format(now));
		
	    SessionFactory sessionFactory = (SessionFactory) SundialJobScheduler.getServletContext().getAttribute("sessionFactory");
	    RecentVisitDAO recentVisitDAO = new RecentVisitDAO(sessionFactory);
		Session session = sessionFactory.openSession();
		try {
			ManagedSessionContext.bind(session);
			Transaction transaction = session.beginTransaction();
			try {
				Date cutoffTime = DateUtil.getPastDate(now,
						Constant.MAX_RECENT_VIEW_DAY);
				recentVisitDAO.removeExpiredRecentVisit(cutoffTime);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new RuntimeException(e);
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
		}
	}

}
