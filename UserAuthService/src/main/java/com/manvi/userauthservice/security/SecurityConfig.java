package com.manvi.userauthservice.security;

import com.manvi.userauthservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Inject JwtService through Lombok-generated constructor.
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF: JWT is explicitly sent in Authorization header, not cookies so CRSF attack is not appliabe in our app.
                .csrf(csrf -> csrf.disable())

                // Stateless: authentication is not stored/retrieved from HTTP sessions.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Run our JWT authentication filter before Spring's username/password filter position.
                // Valid JWT -> sets Authentication in SecurityContextHolder.
                // No JWT (login/signup) -> skips authentication; context remains unauthenticated.
                // Note: JwtFilter still runs for public endpoints unless explicitly skipped .
                // job of JwtFilter is to provide authentication in secturity context then authorization rules(next filter) will decicde who may ente
                .addFilterBefore(
                        new JwtFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class
                )

                // Configure endpoint authorization using Spring Security's AuthorizationFilter.
                .authorizeHttpRequests(auth -> auth

                        // Login/signup are public because users don't have JWTs yet.
                        // error is public so Spring Boot can dispatch errors without requiring authentication and potentially masking the original error.
                        /*
                         When something goes wrong inside Spring (a typo in the URL, bad JSON), Spring (Error Dispatcher DispatcherType.ERROR)sends the request on to its own /error page to build the error response.
                         If we lock that page, every mistake shows up as a 401 and original error will be masked (400/404/415).
                         Why /error is reached as "nobody"?
                          When the dispatcher hits a problem (no controller for the URL, JSON it cannot read), it does not build the error body itself. It sends the request on a second trip, to /error.
                          That second trip goes through the security filters again, but two things differ: the identity (authentictaion) from the first trip has already been cleared,
                          and OncePerRequestFilter(our JwtFilter) by default skips this kind of error trip(as only one trip is allowed), so JwtFilter does not run again. The /error request therefore arrives as "nobody"(without authentication).
                          If /error is not in permitAll, the rules refuse it and the client sees 401 (or 403 before our entry point) instead of the real 400, 404, or 415.
                                1. POST /products
                                   Authorization: Bearer <valid-jwt>
                                          |
                                          v
                                2. JwtFilter validates JWT
                                   → Sets Authentication in SecurityContextHolder
                                          |
                                          v
                                3. Controller argument parsing fails
                                   → Invalid JSON (400)
                                          |
                                          v
                                4. Servlet container performs ERROR dispatch to /error
                                          |
                                          v
                                5. Security filters may execute again
                                   → Original authentication may no longer be available
                                   → OncePerRequestFilter skips ERROR dispatch by default
                                          |
                                          v
                                6. /error requires authentication?
                                   YES → Security may return 401/403
                                   NO  → Original 400 error can be returned

                             One additional Spring Security detail: If you want to permit error dispatches regardless of their destination path, Spring Security also supports:
                             .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                             This permits requests based on their dispatch type, whereas requestMatchers("/error") permits requests based on their URL path.
                        * */
                        .requestMatchers("/auth/**", "/error").permitAll()

                        // All remaining endpoints require an authenticated user.
                        .anyRequest().authenticated()
                ).exceptionHandling(
                        ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))

                          //When the rules refuse a request, Spring Security looks at the identity in the box(SecurityContextHolder):
                        //No identity ("nobody"): it calls the authentication entry point. Without above line, the entry point it falls back to when no login method (no form login, no Basic login) is configured answers 403. That is the surprise in this section. Our HttpStatusEntryPoint(UNAUTHORIZED) changes it to 401.
                        //A known identity that is not allowed: it calls the access-denied handler, which answers 403 by default.
                );

        // Build the configured Spring Security filter chain.
        return http.build();
    }
}
