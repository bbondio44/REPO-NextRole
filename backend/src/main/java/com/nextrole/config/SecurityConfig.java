package com.nextrole.config;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nextrole.common.ApiProblem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import java.util.*;
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
    @Value("${nextrole.security.jwt-secret}") private String base64Secret;
    @Value("${nextrole.security.allowed-origins}") private String origins;
    @Bean SecretKey tokenKey() {
        if (base64Secret==null || base64Secret.isBlank()) throw new IllegalStateException("JWT_SECRET must be configured");
        byte[] bytes;
        try {bytes=Base64.getDecoder().decode(base64Secret);} catch(IllegalArgumentException ex) {throw new IllegalStateException("JWT_SECRET must be base64",ex);}
        if(bytes.length<32) throw new IllegalStateException("JWT_SECRET requires 32+ decoded bytes");
        return new SecretKeySpec(bytes,"HmacSHA256");
    }
    @Bean JwtEncoder jwtEncoder(SecretKey key){ return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
    @Bean JwtDecoder jwtDecoder(SecretKey key,@Value("${nextrole.security.issuer}") String issuer){ NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));return decoder; }
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
    @Bean CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration c=new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isEmpty()).toList());
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-CSRF-Token"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src=new UrlBasedCorsConfigurationSource();src.registerCorsConfiguration("/**",c);return src;
    }
    @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(c -> {}).csrf(csrf -> csrf.disable()) // Cookies only used by refresh/logout; strict Origin validation there.
        .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a
          .requestMatchers("/api/v1/auth/register","/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/logout").permitAll()
          .requestMatchers("/actuator/health","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
          .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
          .anyRequest().authenticated())
        .oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(jwt ->
          new JwtAuthenticationToken(jwt,List.of(new SimpleGrantedAuthority("ROLE_"+jwt.getClaimAsString("role"))),jwt.getSubject()))));
        return http.build();
    }
}
