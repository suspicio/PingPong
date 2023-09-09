package com.PingPong.lab1.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.concurrent.TimeUnit;

@Configuration
public class NoCacheResponseConfiguration {

    @ModelAttribute
    public void setNoCacheHeaders(ResponseEntity<?> response) {
        CacheControl cacheControl = CacheControl.noCache().mustRevalidate().cachePrivate().maxAge(0, TimeUnit.SECONDS);
        response.getHeaders().setCacheControl(cacheControl);
    }
}
