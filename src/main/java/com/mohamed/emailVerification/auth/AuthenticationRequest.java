package com.mohamed.emailVerification.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotNull(message = "Firstname is required")
    @NotEmpty(message = "Firstname is required")
    private String firstName;

    @NotNull(message = "Lastname is required")
    @NotEmpty(message = "Lastname is required")
    private String lastName;


    @Email(message = "enter valid email")
    @NotNull(message = "email is required")
    @NotEmpty(message = "email is required")
    private String email;


    @Size(min = 6, message = "password must be more than 6 char")
    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    private String password;
}
