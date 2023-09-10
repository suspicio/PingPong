package com.PingPong.lab1.component;

import com.PingPong.lab1.service.PingPongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class onStartRunner implements ApplicationRunner {

    @Autowired
    PingPongService pingPongService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        pingPongService.startPing();
    }
}
