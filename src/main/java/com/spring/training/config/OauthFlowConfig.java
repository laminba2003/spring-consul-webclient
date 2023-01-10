package com.spring.training.config;

import lombok.Data;

@Data
public class OauthFlowConfig {
    String authorizationUrl;
    String tokenUrl;
}
