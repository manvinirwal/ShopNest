package com.manvi.userauthservice.controller;

import com.manvi.userauthservice.dto.AuthRequest;
import com.manvi.userauthservice.dto.LoginResponse;
import com.manvi.userauthservice.dto.UserDto;
import com.manvi.userauthservice.model.Role;
import com.manvi.userauthservice.model.User;
import com.manvi.userauthservice.service.AuthService;
import com.manvi.userauthservice.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody AuthRequest authRequest) {
        User user = authService.signUp(authRequest.getEmail(),
                authRequest.getPassword());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setId(user.getId());
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        if (token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validateToken")
    public ResponseEntity<UserDto> validateToken(@RequestHeader("Authorization") String header) {
        if (!header.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = jwtService.parseToken(header.substring("Bearer ".length()));
            UserDto dto = new UserDto();
            dto.setEmail(claims.getSubject());
            dto.setRole(Role.valueOf(claims.get("role", String.class)));
            return ResponseEntity.ok(dto);
        } catch (JwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }




}