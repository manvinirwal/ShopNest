package com.manvi.userauthservice.security;

import com.manvi.userauthservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter  extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String header = request.getHeader("Authorization");

        //No header at all is not an error here. The request simply continues as "nobody", and the rules decide.

        if(header != null && header.startsWith("Bearer")){

           try{
               Claims claims = jwtService.parseToken(header.substring(("Bearer_").length()));

               List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)));

               //Authentication: Spring's word for "who this request is". It holds the email and the authorities (the roles).ROLE_ + the role name: in Spring Security, role authorities start with ROLE_
               Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities) ;
               //The three arguments of UsernamePasswordAuthenticationToken: who (the email from the verified token), the credentials (null: we never keep a password here), and the authorities.
               // Clarification: the three-argument constructor marks the result as authenticated.
               // The two-argument one (without authorities) does not, so a filter that used it would leave every request looking logged out.

               SecurityContextHolder.getContext().setAuthentication(authentication);
               //SecurityContextHolder keeps the user in a thread-local variable, so each request (each thread) has its own box.
               // That is how many users can be served at once without seeing each other's identity.



           }catch(JwtException j){
               // catch covers JwtException, which is what jjwt throws for a bad seal, an expired token, or a malformed string.
               // an empty token string (Authorization: Bearer  with nothing after it) is refused with an IllegalArgumentException instead, which this catch does not cover.
               // That request ends in an error response, not a quiet "nobody". It is rare, but it is worth catching both if you want every bad token to be treated the same way.

               SecurityContextHolder.clearContext();

           }

        }
        //Pass the request to the next gate." Never forget this line. Without it, the request never reaches the controller and every call hangs.
        filterChain.doFilter(request,response);

    }
}
