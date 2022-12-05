package com.spring.training.service;

import com.spring.training.client.PersonClient;
import com.spring.training.model.Person;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PersonService {

    final PersonClient client;

    public Flux<Person> getPersons() {
        return client.getPersons();
    }

    public Mono<Person> getPerson(Long id) {
        return client.getPerson(id);
    }

    public Mono<Person> createPerson(Person person) {
        return client.createPerson(person);
    }

    public Mono<Person> updatePerson(Long id, Person person) {
        return client.updatePerson(id, person);
    }

    public Mono<Void> deletePerson(Long id) {
        return client.deletePerson(id);
    }
}
