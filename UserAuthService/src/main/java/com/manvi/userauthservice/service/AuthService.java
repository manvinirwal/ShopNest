package com.manvi.userauthservice.service;


import com.manvi.userauthservice.model.Role;
import com.manvi.userauthservice.model.User;
import com.manvi.userauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



    public User signUp(String email, String password){

        if(userRepository.existsByEmail(email)) {
            return null;
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        return userRepository.save(user);


    }


    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()
                || !passwordEncoder.matches(password, optionalUser.get().getPasswordHash())) {
            return null;
        }
        return jwtService.generateToken(optionalUser.get());
    }

    /*
    * in above login unknown email answered in about 2 ms, and a real user in about 60 ms.
    * The reason: for an unknown email, the || stops before matches, so no BCrypt work happens.
    *  The messages are identical(we show eamil or password are not correct (ambigous by design so hacker has to make more guess)),
    * but the time tells an attacker whether the account exists. Real systems run a dummy check to hide the difference.
    *
    * public String login(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        String hash = optionalUser.map(User::getPasswordHash).orElse(dummyHash);
        boolean passwordOk = passwordEncoder.matches(password, hash);
        if (optionalUser.isEmpty() || !passwordOk) {
            return null;
        }
        return jwtService.generateToken(optionalUser.get());
      }
      * Here dummyHash is a real BCrypt value with the same cost, made once (for example by encoding a random string when the service starts).
      * Now both paths do one full BCrypt check and take about the same time.

    *
    * */


}
