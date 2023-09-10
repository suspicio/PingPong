package com.PingPong.lab1.service;

import com.PingPong.lab1.utils.SingletonInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PingPongService {

    private final WebClient webClient;

    private final String apiUrl = "http://4.236.182.54:8080";

    @Autowired
    public PingPongService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).clientConnector(new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.create("extendedPool", 1500))
                .responseTimeout(Duration.ofSeconds(2)))).exchangeStrategies(ExchangeStrategies.builder()
                .build()).codecs(configurer -> configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder())).build();
    }

    public void startPing() {
        ping(10000);
    }

    public void writeData() throws IOException {

        // Create a File object
        String filePath = "data.txt";
        File file = new File(filePath);

        // Create a FileWriter object with append mode (true) or overwrite mode (false)
        FileWriter fileWriter = new FileWriter(file, false); // Set to true for append mode

        // Create a BufferedWriter for efficient writing
        BufferedWriter writer = new BufferedWriter(fileWriter);

        StringBuilder stringBuilder = new StringBuilder();

        // Iterate through the ArrayList and convert each Duration to a string
        for (Long duration : SingletonInstance.timeSpans) {
            // Convert the duration to a string in the format "PT1H30M" (for example)
            String durationString = Long.toString(duration);

            // Append the duration string followed by a newline character
            stringBuilder.append(durationString).append("\n");
        }

        // Sort the latency data in ascending order
        Collections.sort(SingletonInstance.timeSpans);

        // Compute average latency
        double average = SingletonInstance.timeSpans.stream().mapToLong(Long::longValue).average().orElse(0.0);

        // Compute median latency
        long median;
        int dataSize = SingletonInstance.timeSpans.size();
        if (dataSize % 2 == 0) {
            median = (SingletonInstance.timeSpans.get(dataSize / 2 - 1) + SingletonInstance.timeSpans.get(dataSize / 2)) / 2;
        } else {
            median = SingletonInstance.timeSpans.get(dataSize / 2);
        }

        // Compute 99th percentile latency
        int percentile99Index = (int) Math.ceil(0.99 * dataSize) - 1;
        long percentile99 = SingletonInstance.timeSpans.get(percentile99Index);

        // Compute 99.9th percentile latency
        int percentile999Index = (int) Math.ceil(0.999 * dataSize) - 1;
        long percentile999 = SingletonInstance.timeSpans.get(percentile999Index);

        System.out.println("Average Latency: " + average);
        System.out.println("Median Latency: " + median);
        System.out.println("99th Percentile Latency: " + percentile99);
        System.out.println("99.9th Percentile Latency: " + percentile999);


        stringBuilder.append("Average Latency: ").append(average).append("\n");
        stringBuilder.append("Median Latency: ").append(median).append("\n");
        stringBuilder.append("99th Percentile Latency: ").append(percentile99).append("\n");
        stringBuilder.append("99.9th Percentile Latency: ").append(percentile999).append("\n");


        // Write data to the file
        writer.write(stringBuilder.toString());

        // Close the BufferedWriter to flush and close the file
        writer.close();

        SingletonInstance.timeSpans.clear();

        System.out.println("Data has been written to the file successfully.");

        startPing();
    }

    public void ping(int left) {
        String message = "a".repeat(128);

        Instant start = Instant.now();

        Mono<String> responseMono = webClient.post().uri("/pong").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).
                bodyValue(message).accept(MediaType.APPLICATION_JSON).exchangeToMono(response -> {
                    Mono<String> bodyMono = response.bodyToMono(String.class);
                    return bodyMono.map(body -> body);
                }).onErrorResume(Mono::error);

        responseMono.subscribe(tuple -> {
            Duration duration = Duration.between(start, Instant.now());
            SingletonInstance.timeSpans.add(duration.toMillis());
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // Call your function here
                if (left > 0) {
                    ping(left - 1);
                } else {
                    try {
                        writeData();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }
}