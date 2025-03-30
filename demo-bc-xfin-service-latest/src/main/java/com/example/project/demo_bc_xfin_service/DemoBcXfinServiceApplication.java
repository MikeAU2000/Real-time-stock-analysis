package com.example.project.demo_bc_xfin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoBcXfinServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoBcXfinServiceApplication.class, args);
	}

}
