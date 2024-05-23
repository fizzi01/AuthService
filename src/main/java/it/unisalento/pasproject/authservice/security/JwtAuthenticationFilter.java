package it.unisalento.pasproject.authservice.security;

import it.unisalento.pasproject.authservice.exceptions.AccessDeniedException;
import it.unisalento.pasproject.authservice.exceptions.UserNotAuthorizedException;
import it.unisalento.pasproject.authservice.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilities jwtUtilities ;

    @Autowired
    private CustomUserDetailsService customerUserDetailsService ;

    private final List<AntPathRequestMatcher> excludedMatchers;

    public JwtAuthenticationFilter(List<String> excludedMatchers) {
        this.excludedMatchers = excludedMatchers.stream()
                .map(AntPathRequestMatcher::new)
                .toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {



        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String role = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtilities.extractUsername(jwt);
                role = jwtUtilities.extractRole(jwt);
            }else {
                throw new AccessDeniedException("Missing token");
            }
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid token: " + e.getMessage());
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.customerUserDetailsService.loadUserByUsername(username);

            if (jwtUtilities.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                throw new UserNotAuthorizedException("User not authorized");
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return excludedMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
    }

}