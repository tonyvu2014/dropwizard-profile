package com.truecaller.profile.core;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.truecaller.profile.helper.JsonDateDeserializer;
import com.truecaller.profile.helper.JsonDateSerializer;

/**
 * 
 * Table to keep track of profile view history
 *
 */
@Entity
@Table(name="visit")
public class Visit {
	
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
