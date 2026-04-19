package com.devops.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController means: this class handles HTTP requests
// and returns data directly (not a webpage)
@RestController
public class HelloController {

    // @GetMapping("/hello") means:
    // when someone does GET http://your-ip:8080/hello
    // run this method and return the result
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot - DevOps Pipeline v4!";
    }

    // A health check endpoint - useful to verify app is running
    @GetMapping("/health")
    public String health() {
        return "App is UP and Running!";
    }
}
