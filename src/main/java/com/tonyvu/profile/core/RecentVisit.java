package com.tonyvu.profile.core;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tonyvu.profile.helper.JsonDateDeserializer;
import com.tonyvu.profile.helper.JsonDateSerializer;

/**
 * 
 * Table to keep only most recent profile views 
 * 
 * For each target profile id, there will be no more than 10 records at any point of time
 *
 */
@Entity
@Table(name="recentVisit")
@NamedQueries({
@NamedQuery(
	name = "com.tonyvu.profile.core.RecentVisit.findRecentVisitByTarget",
	query = "SELECT rv FROM RecentVisit rv WHERE targetId = :targetId ORDER BY visitTime DESC"
	),
@NamedQuery(
	name = "com.tonyvu.profile.core.RecentVisit.removeExpiredRecentVisit",
	query = "DELETE FROM RecentVisit rv WHERE rv.visitTime < :cutoffTime"
	)
})
public class RecentVisit {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private long id;
	
	//Time of the profile view
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	@JsonSerialize(using=JsonDateSerializer.class)
	@JsonDeserialize(using=JsonDateDeserializer.class)
	private Date visitTime=new Date();
	
	//Profile id of the visitor
	@Column(nullable = false)
	private long visitorId;
	
	//Profile id of the view target
	@Column(nullable = false)
	@JsonIgnore
	private long targetId;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(Date visitTime) {
		this.visitTime = visitTime;
	}

	public long getVisitorId() {
		return visitorId;
	}

	public void setVisitorId(long visitorId) {
		this.visitorId = visitorId;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

}
