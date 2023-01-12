package com.spring.training.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Autowired(required = false)
    BuildProperties buildProperties;
    @Autowired(required = false)
    GitProperties gitProperties;

    @Bean
    public OpenAPI openAPI(ConfigurableEnvironment environment) {
        String version = buildProperties != null ? buildProperties.getVersion() : "1.0";
        return new OpenAPI().info(new Info().title(environment.getProperty("info.application.name"))
                .description(environment.getProperty("info.application.description"))
                .version(version))
                .components(new Components().addSecuritySchemes("OAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .scheme("bearer")
                                .bearerFormat("jwt")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .flows(new OAuthFlows().authorizationCode(createOAuthFlow()))))
                .addSecurityItem(new SecurityRequirement().addList("OAuth"))
                .tags(createTags());
    }

    @Bean
    @ConfigurationProperties(prefix = "springdoc.swagger-ui.oauth")
    public OauthFlowConfig oauthFlowConfig() {
        return new OauthFlowConfig();
    }

    private OAuthFlow createOAuthFlow() {
        OAuthFlow oAuthFlow = new OAuthFlow();
        OauthFlowConfig oauthFlowConfig = oauthFlowConfig();
        oAuthFlow.authorizationUrl(oauthFlowConfig.getAuthorizationUrl());
        oAuthFlow.setTokenUrl(oauthFlowConfig.getTokenUrl());
        oAuthFlow.setRefreshUrl(oauthFlowConfig.getTokenUrl());
        return oAuthFlow;
    }

    private List<Tag> createTags() {
        List<Tag> tags = new ArrayList<>();
        if (gitProperties != null) {
            tags.add(new Tag().name("commit").description(gitProperties.getShortCommitId()));
            tags.add(new Tag().name("branch").description(gitProperties.getBranch()));
        }
        return tags;
    }

}
