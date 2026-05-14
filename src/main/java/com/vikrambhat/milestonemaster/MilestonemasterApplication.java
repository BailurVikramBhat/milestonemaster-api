package com.vikrambhat.milestonemaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MilestonemasterApplication {

	static void main(String[] args) {
		SpringApplication.run(MilestonemasterApplication.class, args);
	}

}
