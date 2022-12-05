# Spring WebClient with Consul

Spring WebClient is a non-blocking reactive client which helps to perform HTTP request. They introduced this as part of Spring 5. WebClient API’s are introduced as part of replacing existent Spring RestTemplate 
with these features:

- Supports both synchronous and asynchronous.
- Supports streaming up and down.
- Works with HTTP/1.1
- Supports highly concurrent, reactive, non-blocking with less resource intensive framework.
- Supports both traditional and Spring reactive module.
- Provides a functional API that takes advantage of Java 8 lambdas.

## Setup

The WebClient is part of spring-webflux module and we will add it as required dependency for Spring Reactive support.

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```yaml
server:
  port: 9092
spring:
  application:
    name: spring-consul-webclient
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        instance-id: ${spring.application.name}:${random.value}
        service-name: ${spring.application.name}
        health-check-critical-timeout: "1m"
        health-check-path: /actuator/health
        health-check-interval: 10s
management:
  endpoints:
    web:
      exposure:
        include: health,info

remote:
  services:
    url: http://spring-consul
```

## Creating WebClient

An inter-service communication is realized by the WebClient from Spring WebFlux project. The same as for RestTemplate you should annotate it with Spring Cloud Commons @LoadBalanced . It enables integration with service discovery and load balancing using Netflix OSS Ribbon client. So, the first step is to declare a client builder bean with @LoadBalanced annotation.

```java
@Bean
    public WebClient webClient(ClientConfig config) {
        HttpClient httpClient = HttpClient.create()
                .doOnConnected(connection ->  connection
                .addHandlerLast(new ReadTimeoutHandler(10))
                .addHandlerLast(new WriteTimeoutHandler(10)));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return webClientBuilder()
                .baseUrl(config.getUrl())
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

```

You must as well enable the client-side service discovery

```java
@SpringBootApplication
@EnableDiscoveryClient
public class Application {

	@Autowired
	DiscoveryClient discoveryClient;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
```

## Sending Request

We can use retrieve() and then bodyToFlux() and bodyToMono() method in case we are only interested in the API response. 
We can use the exhange() method in case we need more details from response.

```java
return client.get().uri("/persons/{id}", id)
                   .retrieve()
                   .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("person not found with id : " + id)))
                   .bodyToMono(Person.class);
```