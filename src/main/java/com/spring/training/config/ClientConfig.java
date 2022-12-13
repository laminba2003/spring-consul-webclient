package com.spring.training.config;

import lombok.Data;

import java.util.Map;

@Data
public class ClientConfig {
    String url;
    Map<String, String> ssl;
}
