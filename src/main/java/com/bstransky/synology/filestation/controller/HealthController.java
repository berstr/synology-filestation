package com.bstransky.synology.filestation.controller;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HealthController {

    private static final Logger logger = LogManager.getLogger();

    @GetMapping("/health")
    public String login() throws IOException {

        logger.info("GET /health");

        JsonObject attributes = new JsonObject();
        attributes.addProperty("result1", "ok");
        attributes.addProperty("message", "GET /health - response: OK");
        attributes.addProperty("filename", "HealthController.java");
        attributes.addProperty("package", "com.bstransky.synology.filestation.controller");
        logger.info("{}", attributes);
        logger.info("{\"result2\":\"ok\",\"filename1\":\"HealthController.java\",\"package\":\"com.bstransky.synology.filestation.controller\"}");

        JsonObject result = new JsonObject();

        result.addProperty("result", "ok");


        logger.info("GET /health - response: {}", result.toString());

        return result.toString();
    }

}
