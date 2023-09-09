package com.PingPong.lab1.controller;

import com.PingPong.lab1.service.PingPongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestingController {

    @Autowired
    private PingPongService pingPongService;

    @GetMapping("/start")
    public ResponseEntity<String> startTesting() {
        pingPongService.startPing();

        return new ResponseEntity<>("started", HttpStatus.OK);
    }

    @PostMapping("/pong")
    public ResponseEntity<String> ping(@RequestBody String data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
