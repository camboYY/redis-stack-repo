package com.crypto.ranking.crypto.ranking.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public class HttpConfig {

    private static String apiHost = "coinranking1.p.rapidapi.com";
    private static String apiKey = "13f8350dd0msh14a3e7764624108p1ae23cjsn18b5a7b62fb6";

    public static HttpEntity<String> getHttpEntity () {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-RapidAPI-Key",apiKey);
        headers.set("X-RapidAPI-Host",apiHost);
        return new HttpEntity<>(null, headers);
    }
}
