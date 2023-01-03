package com.bstransky.synology.filestation.controller;

import com.bstransky.synology.filestation.helper.Login;

import com.google.gson.JsonObject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@RestController
public class LoginController {

    private static final Logger logger = LogManager.getLogger();

    @GetMapping("/login")
    public String login() throws IOException {
        logger.info("GET /login");

        JsonObject result = null;

        result = Login.login(2);

        logger.info("GET /login -- filestation response - {}", result.toString());

        return result.toString();
    }

}
