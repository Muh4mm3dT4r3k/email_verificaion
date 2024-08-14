package com.mohamed.emailVerification.auth;

import com.mohamed.emailVerification.email.EmailService;
import com.mohamed.emailVerification.role.Role;
import com.mohamed.emailVerification.role.RoleRepository;
import com.mohamed.emailVerification.security.JwtService;
import com.mohamed.emailVerification.user.CodeToken;
import com.mohamed.emailVerification.user.CodeTokenRepository;
import com.mohamed.emailVerification.user.User;
import com.mohamed.emailVerification.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;

import static com.mohamed.emailVerification.email.EmailTemplateName.ACTIVATION_CODE;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeTokenRepository codeTokenRepository;
    @Value("${application.mailing.frontend.confirmation-url}")
    private String confirmationUrl;


    public void register(RegisterRequest request) throws MessagingException {
        Role role = roleRepository
                .findRoleByName("USER").orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User
                .builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(false)
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();

        userRepository.save(user);
        sendEmail(user);
    }

    private void sendEmail(User user) throws MessagingException {
        String activationCode = generateAndSaveActivationCode(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getUsername(),
                ACTIVATION_CODE,
                confirmationUrl,
                activationCode,
                "Account Activation"
        );

    }

    private String generateAndSaveActivationCode(User user) {
        String code = generateActivationCode(6);
        CodeToken codeToken = CodeToken
                .builder()
                .codeToken(code)
                .createdAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .user(user)
                .build();
        codeTokenRepository.save(codeToken);
        return code;
    }

    private String generateActivationCode(int length) {
        String chars = "1234567890";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(chars.length());
            codeBuilder.append(chars.charAt(index));
        }
        return  codeBuilder.toString();
    }


    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
        );

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }


    @Transactional
    public void confirmationCode(String codeToken) throws MessagingException {
        CodeToken token = codeTokenRepository
                .findByCodeToken(codeToken)
                .orElseThrow();
        if (token.getExpireAt().isAfter(LocalDateTime.now())) {
            sendEmail(token.getUser());
            throw new RuntimeException("code expired, check your inbox");
        }

        User user = userRepository
                .findById(token.getUser().getId())
                .orElseThrow();

        user.setEnabled(true);
        userRepository.save(user);

        token.setValidatedAt(LocalDateTime.now());
        codeTokenRepository.save(token);
    }
}
