package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.user.dto.RegistrationRequest;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.exception.UsernameAlreadyExistsException;
import com.muscledia.user_service.user.services.IUserService;
import com.muscledia.user_service.user.services.UserServiceImpl;
import com.muscledia.user_service.security.annotation.IsAdmin;
import com.muscledia.user_service.security.annotation.IsUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "JWT")
public class UserController {
        private final IUserService userService;
        private final UserServiceImpl userServiceImpl;

        public UserController(@Qualifier("userServiceImpl") IUserService userService,
                        UserServiceImpl userServiceImpl) {
                this.userService = userService;
                this.userServiceImpl = userServiceImpl;
        }

        @IsUser
        @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
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

        @IsUser
        @Operation(summary = "Update own user details", description = "Allows a user to update their own information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User details successfully updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Can only update own profile"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "Username or email already exists")
        })
        @PutMapping("/me")
        public ResponseEntity<User> updateOwnDetails(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody RegistrationRequest updateRequest) {
                User existingUser = userService.getUserByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!existingUser.getUsername().equals(updateRequest.getUsername())
                                && userService.existsByUsername(updateRequest.getUsername())) {
                        throw new UsernameAlreadyExistsException(
                                        "Username already exists: " + updateRequest.getUsername());
                }

                if (!existingUser.getEmail().equals(updateRequest.getEmail())
                                && userService.existsByEmail(updateRequest.getEmail())) {
                        throw new UsernameAlreadyExistsException(
                                        "Email already exists: " + updateRequest.getEmail());
                }

                mapping(updateRequest, existingUser);
                User updatedUser = userService.saveUser(existingUser);
                return ResponseEntity.ok(updatedUser);
        }

        @IsAdmin
        @Operation(summary = "Update user", description = "Updates an existing user's information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User successfully updated"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "Username or email already exists"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PutMapping("/{id}")
        public ResponseEntity<User> updateUser(
                        @Parameter(description = "ID of the user to update") @PathVariable("id") Long id,
                        @Valid @RequestBody RegistrationRequest updateRequest) {
                User existingUser = userService.getUserById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                if (!existingUser.getUsername().equals(updateRequest.getUsername())
                                && userService.existsByUsername(updateRequest.getUsername())) {
                        throw new UsernameAlreadyExistsException(
                                        "Username already exists: " + updateRequest.getUsername());
                }

                if (!existingUser.getEmail().equals(updateRequest.getEmail())
                                && userService.existsByEmail(updateRequest.getEmail())) {
                        throw new UsernameAlreadyExistsException(
                                        "Email already exists: " + updateRequest.getEmail());
                }

                mapping(updateRequest, existingUser);
                User updatedUser = userService.saveUser(existingUser);
                return ResponseEntity.ok(updatedUser);
        }

        @IsAdmin
        @Operation(summary = "Delete user", description = "Deletes a user by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "User successfully deleted"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
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

        @IsAdmin
        @Operation(summary = "Promote user to admin", description = "Grants ADMIN role to a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User successfully promoted to admin"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping("/{id}/promote")
        public ResponseEntity<User> promoteToAdmin(
                        @Parameter(description = "ID of the user to promote") @PathVariable("id") Long id) {
                return ResponseEntity.ok(userServiceImpl.promoteToAdmin(id));
        }

        @IsAdmin
        @Operation(summary = "Demote user from admin", description = "Removes ADMIN role from a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User successfully demoted from admin"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping("/{id}/demote")
        public ResponseEntity<User> demoteFromAdmin(
                        @Parameter(description = "ID of the user to demote") @PathVariable("id") Long id) {
                return ResponseEntity.ok(userServiceImpl.demoteFromAdmin(id));
        }

        private void mapping(RegistrationRequest updateRequest, User existingUser) {
                existingUser.setUsername(updateRequest.getUsername());
                existingUser.setEmail(updateRequest.getEmail());
                existingUser.setPasswordHash(updateRequest.getPassword());
                existingUser.setBirthDate(updateRequest.getBirthDate());
                existingUser.setGender(updateRequest.getGender());
                existingUser.setHeight(updateRequest.getHeight());
                existingUser.setInitialWeight(updateRequest.getInitialWeight());
                existingUser.setGoalType(updateRequest.getGoalType());

                if (updateRequest.getInitialAvatarType() != null) {
                        Avatar avatar = new Avatar();
                        avatar.setAvatarType(updateRequest.getInitialAvatarType());
                        avatar.setUser(existingUser);
                        existingUser.setAvatar(avatar);
                }
        }
}
