package com.PingPong.lab1.service;

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

    private final String apiUrl = "http://localhost:8080";

    @Autowired
    public PingPongService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.create("extendedPool", 1500)).responseTimeout(Duration.ofSeconds(2)))).exchangeStrategies(ExchangeStrategies.builder()
                /*.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))*/.build()).codecs(configurer -> configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder())).build();
    }

    public void Ping() {
        Instant start = Instant.now();

        Mono<Tuple2<ClientResponse, UUID>> responseMono = webClient.post().uri(apiUrl + "/ping").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).bodyValue("").accept(MediaType.APPLICATION_JSON).exchangeToMono(response -> {
            Mono<UUID> bodyMono = response.bodyToMono(UUID.class);
            return bodyMono.map(body -> Tuples.of(response, body));
        }).onErrorResume(Mono::error);

        responseMono.subscribe(tuple -> {
            ClientResponse response = tuple.getT1();
            UUID uuid = tuple.getT2();
            HttpStatusCode statusCode = response.statusCode();
            Duration duration = Duration.between(start, Instant.now());
        });
    }

    public void Pong() {
        Instant start = Instant.now();

        Mono<Tuple2<ClientResponse, UUID>> responseMono = webClient.post().uri(apiUrl + "/pong").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).bodyValue("").accept(MediaType.APPLICATION_JSON).exchangeToMono(response -> {
            Mono<UUID> bodyMono = response.bodyToMono(UUID.class);
            return bodyMono.map(body -> Tuples.of(response, body));
        }).onErrorResume(Mono::error);

        responseMono.subscribe(tuple -> {
            ClientResponse response = tuple.getT1();
            UUID uuid = tuple.getT2();
            HttpStatusCode statusCode = response.statusCode();
            Duration duration = Duration.between(start, Instant.now());
        });
    }
}