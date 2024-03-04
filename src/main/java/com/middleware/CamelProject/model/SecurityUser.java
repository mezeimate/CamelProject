package com.middleware.CamelProject.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
