package com.PingPong.lab1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestingController {
    @PostMapping("/pong")
    public ResponseEntity<String> ping(@RequestBody String data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
