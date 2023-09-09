package com.PingPong.lab1.controller;

import com.PingPong.lab1.service.PingPongService;
import com.PingPong.lab1.utils.SingletonInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

@RestController
public class TestingController {

    @Autowired
    private PingPongService pingPongService;

    @GetMapping("/start")
    public ResponseEntity<String> startTesting() {
        for (int i = 0; i < 10000; i++) {
            pingPongService.Ping();
        }

        try {
            // Create a File object
            String filePath = "data.txt";
            File file = new File(filePath);

            // Create a FileWriter object with append mode (true) or overwrite mode (false)
            FileWriter fileWriter = new FileWriter(file, false); // Set to true for append mode

            // Create a BufferedWriter for efficient writing
            BufferedWriter writer = new BufferedWriter(fileWriter);

            StringBuilder stringBuilder = new StringBuilder();

            // Iterate through the ArrayList and convert each Duration to a string
            for (Duration duration : SingletonInstance.timeSpans) {
                // Convert the duration to a string in the format "PT1H30M" (for example)
                String durationString = duration.toString();

                // Append the duration string followed by a newline character
                stringBuilder.append(durationString).append("\n");
            }

            // Write data to the file
            writer.write(stringBuilder.toString());

            // Close the BufferedWriter to flush and close the file
            writer.close();

            System.out.println("Data has been written to the file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("started", HttpStatus.OK);
    }

    @PostMapping("/pong")
    public ResponseEntity<String> ping(@RequestBody String data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
