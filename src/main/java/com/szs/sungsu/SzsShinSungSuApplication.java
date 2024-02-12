package com.szs.sungsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class SzsShinSungSuApplication {

	public static void main(String[] args) {
		SpringApplication.run(SzsShinSungSuApplication.class, args);
	}

}
