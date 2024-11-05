package com.github.kat_ka.spend_the_night.configuration;

import com.github.kat_ka.spend_the_night.model.data.UserEntity;
import com.github.kat_ka.spend_the_night.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SystemConfiguration {

	@Autowired
	private UserRepository userRepository;

	@Bean
	protected ApplicationRunner initializer() {
		final List<UserEntity> users = UserData.getUserEntities();
		return args -> userRepository.saveAll(users);
	}

	private static final String USER_AGENT_VALUE = "STN-testing-user-agent";

	@Bean
	protected WebClient webClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT_VALUE).build();
	}
}
