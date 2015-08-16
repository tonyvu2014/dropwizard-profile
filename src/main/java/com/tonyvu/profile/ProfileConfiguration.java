package com.tonyvu.profile;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.dropwizard.sundial.SundialConfiguration;

public class ProfileConfiguration extends Configuration {

	@Valid
	@NotNull
	private DataSourceFactory database = new DataSourceFactory();

	@Valid
	@NotNull
	public SundialConfiguration sundialConfiguration = new SundialConfiguration();

	@JsonProperty("database")
	public DataSourceFactory getDataSourceFactory() {
		return database;
	}

	@JsonProperty("database")
	public void setDataSourceFactory(DataSourceFactory database) {
		this.database = database;
	}

	@JsonProperty("sundial")
	public SundialConfiguration getSundialConfiguration() {
		return sundialConfiguration;
	}
	
	@JsonProperty("sundial")
	public void setSundialConfiguration(
			SundialConfiguration sundialConfiguration) {
		this.sundialConfiguration = sundialConfiguration;
	}

}
