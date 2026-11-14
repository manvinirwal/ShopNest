package com.manvi.productcatalogservice.security;

import com.manvi.productcatalogservice.dto.AuthUserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenCheckFilter extends OncePerRequestFilter {
    //@Component: Spring Boot registers it as a filter for every URL
    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public TokenCheckFilter(RestTemplateBuilder restTemplateBuilder,
                            @Value("${auth.service.url}") String authServiceUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.authServiceUrl = authServiceUrl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //A read goes straight through
        if(request.getMethod().equals("GET")){
            filterChain.doFilter(request, response);
            return;
        }
        // forward the same header. The auth service answers with the email and the role.
        String header = request.getHeader("Authorization");
        // A change with no pass is turned away: 401.
        if(header == null ){

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        AuthUserDto user;

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", header);
            ResponseEntity<AuthUserDto> answer = restTemplate.exchange(authServiceUrl+ "/auth/validateToken" , HttpMethod.GET, new HttpEntity<>(headers), AuthUserDto.class);
            user = answer.getBody();
        }catch(HttpClientErrorException.Unauthorized e){
            //401 from the auth service: the token is bad or expired. We answer 401.
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }catch(RestClientException e){
            //Any other problem (the auth service is down): 503 Service Unavailable. This means "we cannot check right now", not "you are not allowed".
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return;
        }
        //The order of the two catch blocks matters (clarification). HttpClientErrorException.Unauthorized is a special kind of RestClientException.

        if(request.getMethod().equals("DELETE") && !"ADMIN".equals(user.getRole())){
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;

        }

        filterChain.doFilter(request,response);


    }
}
