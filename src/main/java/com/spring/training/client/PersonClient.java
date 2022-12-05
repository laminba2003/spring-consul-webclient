package com.spring.training.client;

import com.spring.training.exception.EntityNotFoundException;
import com.spring.training.model.Person;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class PersonClient {
    
    final WebClient client;

    public Flux<Person> getPersons() {
        return client.get().uri("/persons")
                .retrieve()
                .bodyToFlux(Person.class);
    }

    public Mono<Person> getPerson(Long id) {
        return client.get().uri("/persons/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("person not found with id : " + id)))
                .bodyToMono(Person.class);
    }

    public Mono<Person> createPerson(Person person) {
        return client.post().uri("/persons")
                .body(Mono.just(person), Person.class)
                .retrieve()
                .bodyToMono(Person.class);
    }

    public Mono<Person> updatePerson(Long id, Person person) {
        return client.put().uri("/persons/{id}", id)
                .body(Mono.just(person), Person.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("person not found with id : " + id)))
                .bodyToMono(Person.class);
    }

    public Mono<Void> deletePerson(Long id) {
        return client.delete().uri("/persons/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
