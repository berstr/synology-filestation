package com.bstransky.synology.filestation;

import com.bstransky.synology.filestation.helper.Login;

import com.google.gson.JsonObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class FilestationApplication {

	static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		logger.info("Startup - main() ...");

		int sleep_time = 5;

		do {
			try {
				JsonObject result = Login.login();
				if (result.get("result").getAsString().equals("ok")) {
					logger.info("Startup - Login success - response: {}...", result.get("result").toString());
					break;
				} else {
					logger.error("Startup - Login failure - response: {}", result.get("result").toString());
				}
			} catch (IOException err) {
				logger.error("Startup - Login error: {}", err.toString());
			}
			try {
				sleep_time = sleep_time * 2;
				logger.info("Startup - sleep for {} seconds ...",sleep_time);
				TimeUnit.SECONDS.sleep(sleep_time);
			} catch (InterruptedException ie) {
				logger.info("Startup - INTERRUPTED - sleep for {} seconds ... excption: {}",sleep_time, ie.toString());
				Thread.currentThread().interrupt();
			}
		} while (false);

		SpringApplication app = new SpringApplication(FilestationApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", "37081"));
		app.run(args);

	}

}
