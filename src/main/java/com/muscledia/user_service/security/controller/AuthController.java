package com.muscledia.user_service.security.controller;

import com.muscledia.user_service.security.JwtTokenProvider;
import com.muscledia.user_service.security.dto.AuthenticationRequest;
import com.muscledia.user_service.security.dto.AuthenticationResponse;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUserService userService;

    public AuthController(AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            IUserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Operation(summary = "Authenticate user", description = "Authenticates a user with username and password and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.createToken(request.getUsername());
        User user = userService.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthenticationResponse response = new AuthenticationResponse(token, user.getUsername(), user.getUserId());
        return ResponseEntity.ok().body(response);
    }
}