package com.manvi.userauthservice.controller;


import com.manvi.userauthservice.dto.UserDto;
import com.manvi.userauthservice.model.User;
import com.manvi.userauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;




    @GetMapping("/users/me")
    public ResponseEntity<UserDto> me(Authentication authentication){
        //Authentication authentication: Spring hands us "who is this request", the box our filter filled.
        // getName() returns the email we put there.We do not read the email from the request body or a parameter. It comes from the verified token, so a client cannot lie about it.
        //If the token is good but the user was deleted from the database, we answer 404. A valid token proves who someone was when it was issued, not that they still exist.

        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());

        if(optionalUser.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }

        UserDto dto = new UserDto();
        dto.setId(optionalUser.get().getId());
        dto.setRole(optionalUser.get().getRole());
        dto.setEmail(optionalUser.get().getEmail());


        return ResponseEntity.ok(dto);

    }


}
