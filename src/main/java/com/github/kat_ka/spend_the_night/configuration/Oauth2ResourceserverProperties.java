package com.github.kat_ka.spend_the_night.configuration;

import java.util.Map;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "custom.auth.server")
@Data
public class Oauth2ResourceserverProperties {
	
	protected static final String SCOPE_PREFIX = "SCOPE_";

	private Map<String, String> scope;
}
