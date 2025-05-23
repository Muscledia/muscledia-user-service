package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.user.dto.RegistrationRequest;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.exception.UsernameAlreadyExistsException;
import com.muscledia.user_service.user.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "JWT")
public class UserController {
    private final IUserService userService;

    public UserController(@Qualifier("userServiceImpl") IUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID", security = {
            @SecurityRequirement(name = "JWT") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserId(
            @Parameter(description = "ID of the user to retrieve") @PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Operation(summary = "Register new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (userService.existsByUsername(registrationRequest.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists: " + registrationRequest.getUsername());
        }

        if (userService.existsByEmail(registrationRequest.getEmail())) {
            throw new UsernameAlreadyExistsException(
                    "Email already exists: " + registrationRequest.getEmail());
        }

        User newUser = new User();
        mapping(registrationRequest, newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(newUser));
    }

    @Operation(summary = "Update user", description = "Updates an existing user's information", security = {
            @SecurityRequirement(name = "JWT") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable("id") Long id,
            @Valid @RequestBody RegistrationRequest updateRequest) {
        User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if username is being changed and if it's already taken
        if (!existingUser.getUsername().equals(updateRequest.getUsername())
                && userService.existsByUsername(updateRequest.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists: " + updateRequest.getUsername());
        }

        // Check if email is being changed and if it's already taken
        if (!existingUser.getEmail().equals(updateRequest.getEmail())
                && userService.existsByEmail(updateRequest.getEmail())) {
            throw new UsernameAlreadyExistsException(
                    "Email already exists: " + updateRequest.getEmail());
        }

        mapping(updateRequest, existingUser);
        User updatedUser = userService.saveUser(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID", security = {
            @SecurityRequirement(name = "JWT") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable("id") Long id) {
        if (!userService.getUserById(id).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private void mapping(RegistrationRequest updateRequest, User existingUser) {
        existingUser.setUsername(updateRequest.getUsername());
        existingUser.setEmail(updateRequest.getEmail());
        existingUser.setPasswordHash(updateRequest.getPassword()); // Will be encoded in service
        existingUser.setBirthDate(updateRequest.getBirthDate());
        existingUser.setGender(updateRequest.getGender());
        existingUser.setHeight(updateRequest.getHeight());
        existingUser.setInitialWeight(updateRequest.getInitialWeight());
        existingUser.setGoalType(updateRequest.getGoalType());

        if (updateRequest.getInitialAvatarType() != null) {
            Avatar avatar = new Avatar();
            avatar.setAvatarType(updateRequest.getInitialAvatarType());
            avatar.setUser(existingUser); // Set the user reference in Avatar
            existingUser.setAvatar(avatar); // Set the avatar reference in User
        }
    }
}
