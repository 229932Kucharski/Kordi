package pl.kucharski.Kordi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final Algorithm tokenAlgorithm;

    public CustomAuthorizationFilter(Algorithm tokenAlgorithm) {
        this.tokenAlgorithm = tokenAlgorithm;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getServletPath().equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                        .commaSeparatedStringToAuthorityList("ROLE_USER");
                String token = authorizationHeader.substring("Bearer ".length());
                JWTVerifier verifier = JWT.require(tokenAlgorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                String username = decodedJWT.getSubject();
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch(Exception ex) {
                response.setHeader("error", ex.getMessage());
                response.setStatus(UNAUTHORIZED.value());
                Map<String, String> error = new HashMap<>();
                error.put("error", ex.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
