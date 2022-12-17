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

The WebClient is part of spring-webflux module, and we will add it as required dependency for Spring Reactive support.

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
        prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health

remote:
  services:
    url: http://spring-consul
```

## Creating WebClient

An inter-service communication is realized by the WebClient from Spring WebFlux project. The same as for RestTemplate you should annotate it with @LoadBalanced. It enables integration with service discovery and load balancing using Spring Cloud Load Balancer. So, the first step is to declare a client builder bean with **@LoadBalanced** annotation.

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

@Bean
@ConfigurationProperties(prefix = "remote.services")
public ClientConfig clientConfig() {
    return new ClientConfig();
}

```
Load balancing is the process of distributing traffic among different instances of the same application.

To create a fault-tolerant system, it's common to run multiple instances of each application. Thus, whenever one service needs to communicate with another, it needs to pick a particular instance to send its request.

There are many algorithms when it comes to load balancing:

- Random selection: Choosing an instance randomly
- Round-robin: Choosing an instance in the same order each time
- Least connections: Choosing the instance with the fewest current connections
- Weighted metric: Using a weighted metric to choose the best instance (for example, CPU or memory usage)
- IP hash: Using the hash of the client IP to map to an instance


You can as well use the discovery client in your application to lookup for your services instances.

```java
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    DiscoveryClient discoveryClient;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        final String serviceId = "spring-consul";
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        System.out.println(serviceId);
        if (instances.size() > 0) {
            instances.forEach(instance -> System.out.println(instance.getUri()));
        } else {
            System.out.println("no registered instance found");
        }
    }
}

```

## Configuring TLS with WebClient

There is some work to do to get WebClient to communicate with TLS.

```yaml
remote:
  services:
    url: https://spring-consul
    ssl:
      key-store: classpath:server.jks
      key-store-password: changeit
      key-store-type: JKS
```

```java
@Bean
@Profile("ssl")
public WebClient webSSLClient(ClientConfig config) {
    HttpClient httpClient = HttpClient.create()
            .doOnConnected(connection -> connection
                    .addHandlerLast(new ReadTimeoutHandler(10))
                    .addHandlerLast(new WriteTimeoutHandler(10)))
            .secure(spec -> {
                try {
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
                } catch (Exception e) {
                    log.error("Unable to set SSL Context", e);
                }
            });

    ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    return webClientBuilder()
            .baseUrl(config.getUrl())
            .clientConnector(connector)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
}
```

## Bearer Token Propagation

It might be handy to pass a bearer token to downstream services. This is quite simple with **ServerBearerExchangeFilterFunction**, which you can see in the following example:

```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
            .filter(new ServerBearerExchangeFilterFunction())
            .build();
}
```

When the WebClient is used to perform requests, Spring Security will look up the current Authentication and extract any AbstractOAuth2Token credential. Then, it will propagate that token in the Authorization header.


## Sending Request

We can use retrieve() and then bodyToFlux() and bodyToMono() method in case we are only interested in the API response. 
We can use the exchange() method in case we need more details from response.

```java
return client.get().uri("/persons/{id}", id)
                   .retrieve()
                   .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("person not found with id : " + id)))
                   .bodyToMono(Person.class);
```

### REST endpoints

| HTTP verb | Resource  | Description
|----|---|---|
|  GET  | /persons  | retrieve list and information of persons  
|  GET |  /persons/{id} | retrieve information of a person specified by {id}
|  POST | /persons  | create a new person with payload  
|  PUT   |  /persons/{id} | update a person with payload   
|  DELETE   | /persons/{id}  |  delete a person specified by {id} 
|  GET  | /countries  | retrieve list and information of countries  
|  GET |  /countries/{name} | retrieve information of a country specified by {name} 
|  POST | /countries  | create a new country with payload  
|  PUT   |  /countries/{name} | update a country with payload   
|  DELETE   | /countries/{name}  |  delete a country specified by {name} 
