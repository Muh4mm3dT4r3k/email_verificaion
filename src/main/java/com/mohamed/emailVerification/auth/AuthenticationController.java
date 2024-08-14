package com.mohamed.emailVerification.auth;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) throws MessagingException {
        authenticationService.register(request);
        return ResponseEntity
                .accepted()
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/activate-account")
    public void confirm(@RequestParam String code) throws MessagingException {
        authenticationService.confirmationCode(code);
    }

}
