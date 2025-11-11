package dev.matheuslf.desafio.inscritos.controller;

import dev.matheuslf.desafio.inscritos.controller.dto.auth.AuthenticationRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.auth.AuthenticationResponse;
import dev.matheuslf.desafio.inscritos.controller.dto.auth.RegisterRequest;
import dev.matheuslf.desafio.inscritos.domain.user.User;
import dev.matheuslf.desafio.inscritos.repository.UserRepository;
import dev.matheuslf.desafio.inscritos.service.token.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // Extrai o username do objeto UserDetails (ou User, se for sua implementação)
        String username = ((UserDetails) auth.getPrincipal()).getUsername();
        // Busca o usuário persistido para obter a role
        var user = userRepository.findByUsername(username).orElse(null);
        String role = user != null ? (user.getRole() != null ? user.getRole().name() : null) : null;
        var token = tokenService.generateToken(username, role);
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        if (this.userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(request.role());
        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
