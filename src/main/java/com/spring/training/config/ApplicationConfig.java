package com.spring.training.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.FormHttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

@Configuration
@Slf4j
public class ApplicationConfig extends WebFluxConfigurationSupport {

    @Bean
    @Profile("!ssl")
    public WebClient webClient(ClientConfig config) {
        return buildWebClient(config, createHttpClient());
    }

    @Bean
    @Profile("ssl")
    public WebClient webSSLClient(ClientConfig config) {
        return buildWebClient(config, createHttpClient().secure(spec -> configureSSL(spec, config)));
    }

    private HttpClient createHttpClient() {
        return HttpClient.create().doOnConnected(connection -> connection
                .addHandlerLast(new ReadTimeoutHandler(10))
                .addHandlerLast(new WriteTimeoutHandler(10)));
    }

    private WebClient buildWebClient(ClientConfig config, HttpClient httpClient) {
        return webClientBuilder()
                .baseUrl(config.getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(new ServerBearerExchangeFilterFunction())
                .exchangeStrategies(ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true)).build())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @SneakyThrows
    private void configureSSL(SslProvider.SslContextSpec spec, ClientConfig config) {
        String type = config.getSsl().get("key-store-type");
        KeyStore keyStore = KeyStore.getInstance(type);
        DefaultResourceLoader loader = new DefaultResourceLoader();
        String file = config.getSsl().get("key-store");
        String password = config.getSsl().get("key-store-password");
        keyStore.load(loader.getResource(file).getInputStream(), password.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        spec.sslContext(SslContextBuilder.forClient()
                .keyManager(keyManagerFactory)
                .trustManager(trustManagerFactory)
                .build());
    }

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    @Bean
    @ConfigurationProperties(prefix = "remote.services")
    public ClientConfig clientConfig() {
        return new ClientConfig();
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer config) {
        FormHttpMessageReader reader = new FormHttpMessageReader();
        reader.setEnableLoggingRequestDetails(true);
        config.customCodecs().register(reader);
    }

}