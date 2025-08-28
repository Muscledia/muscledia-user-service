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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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


        // ============= EXTRACTED VALIDATION METHODS =============
        // These replace the duplicated validation code in your methods

        /**
         * EXTRACTED: Check if username/email changes conflict with existing users
         * Used in: updateOwnDetails, updateOwnDetailsByUuid, updateUser, updateUserByUuid
         */
        private void validateUniqueFieldChanges(User existingUser, RegistrationRequest updateRequest) {
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
        }

        /**
         * EXTRACTED: Check if new user registration conflicts with existing users
         * Used in: registerUser
         */
        private void validateNewUserUniqueness(RegistrationRequest registrationRequest) {
                if (userService.existsByUsername(registrationRequest.getUsername())) {
                        throw new UsernameAlreadyExistsException(
                                "Username already exists: " + registrationRequest.getUsername());
                }

                if (userService.existsByEmail(registrationRequest.getEmail())) {
                        throw new UsernameAlreadyExistsException(
                                "Email already exists: " + registrationRequest.getEmail());
                }
        }

        /**
         * EXTRACTED: Handle UUID format errors consistently
         * Used in: getUserByUuid, updateOwnDetailsByUuid, updateUserByUuid, deleteUserByUuid, convertUuidToId
         */
        private ResponseEntity<?> handleUuidError(IllegalArgumentException e, String uuid) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid UUID format: " + uuid));
        }

        @IsUser
        @Operation(summary = "Get user by ID", description = "Retrieves a user by their Long ID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User found"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        @GetMapping("/{id}")
        public ResponseEntity<User> getUserById(
                @Parameter(description = "Long ID of the user to retrieve") @PathVariable("id") Long id) {
                return ResponseEntity.ok(userService.getUserById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
        }

        @IsUser
        @Operation(summary = "Get user by UUID", description = "Retrieves a user by their UUID string")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User found"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        @GetMapping("/uuid/{uuid}")
        public ResponseEntity<?> getUserByUuid(
                @Parameter(description = "UUID string of the user to retrieve") @PathVariable("uuid") String uuid) {
                try {
                        return ResponseEntity.ok(userServiceImpl.getUserByUuid(uuid)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid)));
                } catch (IllegalArgumentException e) {
                        return handleUuidError(e, uuid); // USING EXTRACTED METHOD
                }
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

                // USING EXTRACTED VALIDATION METHOD
                validateUniqueFieldChanges(existingUser, updateRequest);

                mapping(updateRequest, existingUser);
                User updatedUser = userServiceImpl.updateUser(existingUser);
                return ResponseEntity.ok(updatedUser);
        }

        @IsUser
        @Operation(summary = "Update own user details by UUID", description = "Allows a user to update their own information using UUID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User details successfully updated"),
                @ApiResponse(responseCode = "400", description = "Invalid input or UUID format"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Can only update own profile"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "409", description = "Username or email already exists")
        })
        @PutMapping("/me/uuid/{uuid}")
        public ResponseEntity<?> updateOwnDetailsByUuid(
                @AuthenticationPrincipal UserDetails userDetails,
                @Parameter(description = "UUID string of your own profile") @PathVariable("uuid") String uuid,
                @Valid @RequestBody RegistrationRequest updateRequest) {
                try {
                        User existingUser = userServiceImpl.getUserByUuid(uuid)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));

                        // Security check: ensure the UUID belongs to the authenticated user
                        if (!existingUser.getUsername().equals(userDetails.getUsername())) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("error", "You can only update your own profile"));
                        }

                        // USING EXTRACTED VALIDATION METHOD
                        validateUniqueFieldChanges(existingUser, updateRequest);

                        mapping(updateRequest, existingUser);
                        User updatedUser = userServiceImpl.updateUser(existingUser);
                        return ResponseEntity.ok(updatedUser);

                } catch (IllegalArgumentException e) {
                        return handleUuidError(e, uuid); // USING EXTRACTED METHOD
                }
        }

        @IsAdmin
        @Operation(summary = "Search user by ID or UUID", description = "Flexible search by either Long ID or UUID string (Admin only)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User found"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "400", description = "Invalid identifier format"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @GetMapping("/search/{identifier}")
        public ResponseEntity<?> searchUser(
                @Parameter(description = "Either Long ID or UUID string") @PathVariable("identifier") String identifier) {
                try {
                        return ResponseEntity.ok(userServiceImpl.findUserByIdOrUuid(identifier)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier)));
                } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Invalid identifier format: " + identifier));
                }
        }

        @Operation(summary = "Register new user", description = "Creates a new user account with UUID-based Long ID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "User successfully created"),
                @ApiResponse(responseCode = "400", description = "Invalid input"),
                @ApiResponse(responseCode = "409", description = "Username or email already exists"),
                @ApiResponse(responseCode = "503", description = "Service unavailable - ID generation issue")
        })
        @PostMapping("/register")
        public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
                try {
                        // USING EXTRACTED VALIDATION METHOD
                        validateNewUserUniqueness(registrationRequest);

                        User newUser = new User();
                        mapping(registrationRequest, newUser);

                        User createdUser = userService.saveUser(newUser);
                        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

                } catch (RuntimeException e) {
                        if (e.getMessage().contains("Unable to generate unique")) {
                                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                        .body(Map.of("error", "System capacity issue. Please try again later."));
                        }
                        throw e;
                }
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
                @Parameter(description = "Long ID of the user to update") @PathVariable("id") Long id,
                @Valid @RequestBody RegistrationRequest updateRequest) {

                User existingUser = userService.getUserById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                // USING EXTRACTED VALIDATION METHOD
                validateUniqueFieldChanges(existingUser, updateRequest);

                mapping(updateRequest, existingUser);
                User updatedUser = userServiceImpl.updateUser(existingUser);
                return ResponseEntity.ok(updatedUser);
        }

        @IsAdmin
        @Operation(summary = "Update user by UUID", description = "Updates an existing user's information using UUID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User successfully updated"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
                @ApiResponse(responseCode = "409", description = "Username or email already exists"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PutMapping("/uuid/{uuid}")
        public ResponseEntity<?> updateUserByUuid(
                @Parameter(description = "UUID string of the user to update") @PathVariable("uuid") String uuid,
                @Valid @RequestBody RegistrationRequest updateRequest) {
                try {
                        User existingUser = userServiceImpl.getUserByUuid(uuid)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));

                        // USING EXTRACTED VALIDATION METHOD
                        validateUniqueFieldChanges(existingUser, updateRequest);

                        mapping(updateRequest, existingUser);
                        User updatedUser = userServiceImpl.updateUser(existingUser);
                        return ResponseEntity.ok(updatedUser);
                } catch (IllegalArgumentException e) {
                        return handleUuidError(e, uuid); // USING EXTRACTED METHOD
                }
        }

        @IsAdmin
        @Operation(summary = "Delete user", description = "Deletes a user by their Long ID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "User successfully deleted"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(
                @Parameter(description = "Long ID of the user to delete") @PathVariable("id") Long id) {
                if (userService.getUserById(id).isEmpty()) {
                        throw new ResourceNotFoundException("User not found with id: " + id);
                }
                userService.deleteUser(id);
                return ResponseEntity.noContent().build();
        }

        @IsAdmin
        @Operation(summary = "Delete user by UUID", description = "Deletes a user by their UUID string")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "User successfully deleted"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @DeleteMapping("/uuid/{uuid}")
        public ResponseEntity<?> deleteUserByUuid(
                @Parameter(description = "UUID string of the user to delete") @PathVariable("uuid") String uuid) {
                try {
                        if (!userServiceImpl.existsByUuid(uuid)) {
                                throw new ResourceNotFoundException("User not found with UUID: " + uuid);
                        }
                        userServiceImpl.deleteUserByUuid(uuid);
                        return ResponseEntity.noContent().build();
                } catch (IllegalArgumentException e) {
                        return handleUuidError(e, uuid); // USING EXTRACTED METHOD
                }
        }

        @IsAdmin
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Promote user to admin", description = "Grants ADMIN role to a user")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User successfully promoted to admin"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping("/{id}/promote")
        public ResponseEntity<User> promoteToAdmin(
                @Parameter(description = "Long ID of the user to promote") @PathVariable("id") Long id,
                Authentication authentication) {

                String currentUsername = authentication.getName();
                User targetUser = userService.getUserById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                if (currentUsername.equals(targetUser.getUsername())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((User) Map.of("error", "You cannot promote yourself to admin"));
                }

                User promotedUser = userServiceImpl.promoteToAdmin(id);
                return ResponseEntity.ok(promotedUser);
        }

        @IsAdmin
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Demote user from admin", description = "Removes ADMIN role from a user")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "User successfully demoted from admin"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping("/{id}/demote")
        public ResponseEntity<User> demoteFromAdmin(
                @Parameter(description = "Long ID of the user to demote") @PathVariable("id") Long id,
                Authentication authentication) {

                String currentUsername = authentication.getName();
                User targetUser = userService.getUserById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

                if (currentUsername.equals(targetUser.getUsername())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((User) Map.of("error", "You cannot demote yourself from admin"));
                }

                User demotedUser = userServiceImpl.demoteFromAdmin(id);
                return ResponseEntity.ok(demotedUser);
        }

        @IsAdmin
        @Operation(summary = "Convert UUID to Long ID", description = "Utility endpoint for UUID to ID conversion")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Conversion successful"),
                @ApiResponse(responseCode = "404", description = "User not found with given UUID"),
                @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @GetMapping("/convert/uuid-to-id/{uuid}")
        public ResponseEntity<?> convertUuidToId(
                @Parameter(description = "UUID string to convert") @PathVariable("uuid") String uuid) {
                try {
                        Long userId = userServiceImpl.convertUuidToLongId(uuid);
                        return ResponseEntity.ok(Map.of(
                                "uuid", uuid,
                                "userId", userId,
                                "status", "conversion_successful"
                        ));
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.notFound().build();
                } catch (IllegalArgumentException e) {
                        return handleUuidError(e, uuid); // USING EXTRACTED METHOD
                }
        }

        @IsAdmin
        @Operation(summary = "Convert Long ID to UUID", description = "Utility endpoint for ID to UUID conversion")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Conversion successful"),
                @ApiResponse(responseCode = "404", description = "User not found with given ID"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @GetMapping("/convert/id-to-uuid/{id}")
        public ResponseEntity<?> convertIdToUuid(
                @Parameter(description = "Long ID to convert") @PathVariable("id") Long id) {
                try {
                        String uuid = userServiceImpl.convertLongIdToUuid(id);
                        return ResponseEntity.ok(Map.of(
                                "userId", id,
                                "uuid", uuid,
                                "status", "conversion_successful"
                        ));
                } catch (ResourceNotFoundException e) {
                        return ResponseEntity.notFound().build();
                }
        }

        @IsAdmin
        @Operation(summary = "Get system statistics", description = "Get user system statistics including total users")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @GetMapping("/admin/stats")
        public ResponseEntity<UserServiceImpl.UserStatistics> getSystemStats() {
                UserServiceImpl.UserStatistics stats = userServiceImpl.getSystemStatistics();
                return ResponseEntity.ok(stats);
        }

        @IsAdmin
        @GetMapping("/admin/all")
        @Operation(summary = "Get all users (Admin only)")
        public ResponseEntity<?> getAllUsers() {
                try {
                        return ResponseEntity.ok(userServiceImpl.getAllUsers());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to retrieve users"));
                }
        }


        // Helper method - enhanced to not overwrite UUID fields
        private void mapping(RegistrationRequest updateRequest, User existingUser) {
                existingUser.setUsername(updateRequest.getUsername());
                existingUser.setEmail(updateRequest.getEmail());
                existingUser.setPasswordHash(updateRequest.getPassword());
                existingUser.setBirthDate(updateRequest.getBirthDate());
                existingUser.setGender(updateRequest.getGender());
                existingUser.setHeight(updateRequest.getHeight());
                existingUser.setInitialWeight(updateRequest.getInitialWeight());
                existingUser.setGoalType(updateRequest.getGoalType());

                // Don't modify userId or uuidString - these should remain constant
                // Only handle avatar if specified
                if (updateRequest.getInitialAvatarType() != null) {
                        Avatar avatar = new Avatar();
                        avatar.setAvatarType(updateRequest.getInitialAvatarType());
                        avatar.setUser(existingUser);
                        existingUser.setAvatar(avatar);
                }
        }
}
