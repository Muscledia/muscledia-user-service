package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.user.dto.RegistrationRequest;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;

    public UserController(@Qualifier("userServiceImpl") IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserId(@PathVariable("id") Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (userService.existsByUsername(registrationRequest.getUsername())) {
            return ResponseEntity.badRequest().body(null); // or a custom error DTO
        }

        User newUser = new User();
        mapping(registrationRequest, newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(newUser));

    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @Valid @RequestBody RegistrationRequest updateRequest) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = optionalUser.get();
        mapping(updateRequest, existingUser);

        User updatedUser = userService.saveUser(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
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
            existingUser.setAvatar(avatar);
        }
    }


}
