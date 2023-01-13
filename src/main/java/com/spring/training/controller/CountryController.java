package com.spring.training.controller;

import com.spring.training.annotation.IsAdmin;
import com.spring.training.domain.Country;
import com.spring.training.service.CountryService;
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
@RequestMapping("countries")
@AllArgsConstructor
public class CountryController {

    final CountryService service;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "getCountries", description = "return the list of countries")
    @ApiResponse(responseCode = "200", description = "countries found successfully")
    public Flux<Country> getCountries() {
        return service.getCountries();
    }

    @GetMapping("{name}")
    @Operation(summary = "getCountry", description = "return a country by its name")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "country found successfully"),
            @ApiResponse(responseCode = "404", description = "country not found")})
    public Mono<Country> getCountry(@Parameter(description = "country name", required = true) @PathVariable("name") String name) {
        return service.getCountry(name);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "createCountry", description = "create a country")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "country created successfully"),
            @ApiResponse(responseCode = "409", description = "country already created")})
    @IsAdmin
    public Mono<Country> createCountry(@RequestBody Country country) {
        return service.createCountry(country);
    }

    @PutMapping("{name}")
    @Operation(summary = "updateCountry", description = "update a country by its name")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "country updated successfully"),
            @ApiResponse(responseCode = "404", description = "country not found")})
    @IsAdmin
    public Mono<Country> updateCountry(@Parameter(description = "country name", required = true) @PathVariable("name") String name,
                                       @RequestBody Country user) {
        return service.updateCountry(name, user);
    }

    @DeleteMapping("{name}")
    @Operation(summary = "deleteCountry", description = "delete a country by its name")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "country deleted successfully"),
            @ApiResponse(responseCode = "409", description = "cannot delete country")})
    @IsAdmin
    public Mono<Void> deleteCountry(@Parameter(description = "country name", required = true) @PathVariable("name") String name) {
        return service.deleteCountry(name);
    }

}