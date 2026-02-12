package com.app.todoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point
 * This class bootstraps the Spring Boot application.
 * @SpringBootApplication enables auto-configuration, component scanning, and property support.
 */
@SpringBootApplication
public class TodoappApplication {

	/**
	 * Main method to start the application.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		SpringApplication.run(TodoappApplication.class, args);
	}

}
