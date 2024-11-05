package com.github.kat_ka.spend_the_night.configuration;

import static com.github.kat_ka.spend_the_night.configuration.Oauth2ResourceserverProperties.SCOPE_PREFIX;

import com.github.kat_ka.spend_the_night.exception.RestAccessDeniedHandler;
import com.github.kat_ka.spend_the_night.exception.RestAuthenticationEntryPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfiguration {

	@Autowired
	private Oauth2ResourceserverProperties oauth2Server;

	@Bean
	protected RestAccessDeniedHandler accessDeniedHandler() {
		return new RestAccessDeniedHandler();
	}

	@Bean
	protected RestAuthenticationEntryPoint authenticationEntryPoint() {
		return new RestAuthenticationEntryPoint();
	}

	@Bean
	protected MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
		return new MvcRequestMatcher.Builder(introspector);
	}

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvcMatcherBuilder)
			throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize
					.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
					.permitAll() // H2 Web Console
					.requestMatchers(mvcMatcherBuilder.pattern("/v3/api-docs/**"),
							         mvcMatcherBuilder.pattern("/swagger-ui.html"),
							         mvcMatcherBuilder.pattern("/swagger-ui/**"))
					.permitAll() // Swagger
					.requestMatchers(mvcMatcherBuilder.pattern("/actuator/**"))
					.permitAll() // Spring Actuator
					.requestMatchers(mvcMatcherBuilder.pattern("/place_offers/**"))
					.hasAuthority(SCOPE_PREFIX + oauth2Server.getScope().get("publish"))
					.requestMatchers(mvcMatcherBuilder.pattern("/place_search/**"))
					.hasAuthority(SCOPE_PREFIX + oauth2Server.getScope().get("view"))
					.anyRequest().authenticated()
			)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.oauth2ResourceServer(oauth2 -> oauth2
					.jwt(Customizer.withDefaults())
					.authenticationEntryPoint(authenticationEntryPoint())
					.accessDeniedHandler(accessDeniedHandler())
			)
			.headers(headers -> headers.frameOptions(FrameOptionsConfig::disable)); // for H2 Web Console
		return http.build();
	}
}
