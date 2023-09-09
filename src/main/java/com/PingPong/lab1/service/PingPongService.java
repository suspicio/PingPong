package com.PingPong.lab1.service;

import com.PingPong.lab1.utils.SingletonInstance;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PingPongService {

    private final WebClient webClient;

    private final String apiUrl = "http://localhost:8081";

    @Autowired
    public PingPongService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.create("extendedPool", 1500)).responseTimeout(Duration.ofSeconds(2)))).exchangeStrategies(ExchangeStrategies.builder()
                .build()).codecs(configurer -> configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder())).build();
    }

    public void Ping() {
        Instant start = Instant.now();

        String message = "a".repeat(128);

        Mono<String> responseMono = webClient.post().uri(apiUrl + "/pong").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).
                bodyValue(message).accept(MediaType.APPLICATION_JSON).exchangeToMono(response -> {
            Mono<String> bodyMono = response.bodyToMono(String.class);
            return bodyMono.map(body -> body);
        }).onErrorResume(Mono::error);

        responseMono.subscribe(tuple -> {
            System.out.println(tuple);
            Duration duration = Duration.between(start, Instant.now());
            SingletonInstance.timeSpans.add(duration);
        });
    }
}