package com.truecaller.profile.auth;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import com.google.common.base.Optional;
import com.truecaller.profile.core.User;

/**
 * Basic authentication for APIs which require security 
 *
 */
public class AdminAuthenticator implements
		Authenticator<BasicCredentials, User> {
	@Override
	public Optional<User> authenticate(BasicCredentials credentials) {
		if ("m0r1ng@".equals(credentials.getPassword()) && "admin".equals(credentials.getUsername())) {
			return Optional.of(new User(credentials.getUsername()));
		} 
		
		return Optional.absent();
	}
}