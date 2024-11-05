package com.github.kat_ka.spend_the_night;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({ "com.github.kat_ka.spend_the_night" })
@ConfigurationPropertiesScan("com.github.kat_ka.spend_the_night.configuration")
@EntityScan("com.github.kat_ka.spend_the_night.model.data")
@EnableJpaRepositories("com.github.kat_ka.spend_the_night.repository")
public class SpendTheNightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpendTheNightApplication.class, args);
	}
}
