package com.spring.training.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    Long id;
    @NotNull
    String firstName;
    @NotNull
    String lastName;
    @NotNull
    Country country;
}
