package finance.freedom.finance_freedom_backend.filter;

import finance.freedom.finance_freedom_backend.service.jwt.JWTServiceImpl;
import finance.freedom.finance_freedom_backend.service.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTServiceImpl jwtService;

    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
             String authorizationHeader = request.getHeader("Authorization");
             String token = null;
             String username = null;


             if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                 token = authorizationHeader.substring(7);
                 username = jwtService.extractUserName(token);
             }

             if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                 UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                if(jwtService.validateToken(token, userDetails)){
                    UsernamePasswordAuthenticationToken tokenAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    tokenAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(tokenAuthentication);
                }
             }
        filterChain.doFilter(request, response);
    }
}