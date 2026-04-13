package com.devops.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication tells Spring Boot:
// "This is the starting point, scan everything from here"
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // This line starts the embedded web server (Tomcat)
        // and makes your app ready to receive requests
        SpringApplication.run(Application.class, args);
    }
}