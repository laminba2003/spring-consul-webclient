package com.spring.training.controller;

import com.spring.training.domain.Person;
import com.spring.training.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("persons")
@AllArgsConstructor
public class PersonController {

    final PersonService service;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "getPersons", description = "return the list of persons")
    @ApiResponse(responseCode = "200", description = "persons found successfully")
    public Flux<Person> getPersons() {
        return service.getPersons();
    }

    @GetMapping("{id}")
    @Operation(summary = "getPerson", description = "return a person by its id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "person found successfully"),
            @ApiResponse(responseCode = "404", description = "person not found")})
    public Mono<Person> getPerson(@Parameter(description = "person id", required = true) @PathVariable("id") Long id) {
        return service.getPerson(id);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "createPerson", description = "create a person")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "person created successfully"),
            @ApiResponse(responseCode = "404", description = "country not found")})
    public Mono<Person> createPerson(@RequestBody Person person) {
        return service.createPerson(person);
    }

    @PutMapping("{id}")
    @Operation(summary = "updatePerson", description = "update a person by its id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "person updated successfully"),
            @ApiResponse(responseCode = "404", description = "person or country not found")})
    public Mono<Person> updatePerson(@Parameter(description = "person id", required = true) @PathVariable("id") Long id,
                                     @RequestBody Person person) {
        return service.updatePerson(id, person);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "deletePerson", description = "delete a person by its id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "person deleted successfully"),
            @ApiResponse(responseCode = "409", description = "cannot delete person")})
    public Mono<Void> deletePerson(@Parameter(description = "person id", required = true) @PathVariable("id") Long id) {
        return service.deletePerson(id);
    }

}