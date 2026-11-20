package com.nextrole.auth;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Service
public class AuthService {
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwt;
    private final SecureRandom random=new SecureRandom();
    private final Map<String, Attempts> attempts=new ConcurrentHashMap<>();
    @Value("${nextrole.security.issuer}") private String issuer;
    @Value("${nextrole.security.access-minutes}") private long accessMinutes;
    @Value("${nextrole.security.refresh-days}") private long refreshDays;
    @Value("${nextrole.security.cookie-secure}") private boolean secureCookie;
    @Value("${nextrole.security.allowed-origins}") private String allowedOrigins;
    public AuthService(UserAccountRepository users,RefreshTokenRepository refreshTokens,PasswordEncoder passwords,JwtEncoder jwt){
        this.users=users;this.refreshTokens=refreshTokens;this.passwords=passwords;this.jwt=jwt;
    }
    public record AccountView(UUID id,String firstName,String lastName,String email,String role) {}
    public record AuthView(String accessToken,String tokenType,long expiresIn,AccountView user) {}
    public static AccountView view(UserAccount u){return new AccountView(u.id,u.firstName,u.lastName,u.email,u.role);}
    private String issueAccess(UserAccount user){
        Instant now=Instant.now();
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer(issuer).subject(user.id.toString()).issuedAt(now)
                .expiresAt(now.plus(accessMinutes,java.time.temporal.ChronoUnit.MINUTES)).claim("role",user.role).build();
        return jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
    }
    private String randomToken(){byte[] bytes=new byte[48];random.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
    private static String digest(String token){
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private void refreshCookie(HttpServletResponse response,String value,long seconds){
        ResponseCookie cookie=ResponseCookie.from("nr_refresh",value).httpOnly(true).secure(secureCookie)
              .sameSite("Strict").path("/api/v1/auth").maxAge(seconds).build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }
    private AuthView startSession(UserAccount u,HttpServletResponse response){
        String plain=randomToken();
        refreshTokens.save(new RefreshToken(UUID.randomUUID(),u.id,digest(plain),Instant.now().plus(refreshDays,java.time.temporal.ChronoUnit.DAYS),null));
        refreshCookie(response,plain,refreshDays*86400);
        return new AuthView(issueAccess(u),"Bearer",accessMinutes*60,view(u));
    }
    @Transactional
    public AuthView register(String first,String last,String email,String password,HttpServletResponse response){
        String normalized=email.trim().toLowerCase(Locale.ROOT);
        if(users.findByEmailIgnoreCase(normalized).isPresent())throw ApiProblem.conflict("El correo ya está registrado");
        UserAccount u=users.save(new UserAccount(UUID.randomUUID(),normalized,first.trim(),last.trim(),passwords.encode(password),"STUDENT",Instant.now()));
        return startSession(u,response);
    }
    private static class Attempts { AtomicInteger count=new AtomicInteger(); Instant since=Instant.now(); }
    private void throttle(String key){
        Attempts a=attempts.compute(key,(k,v)->v==null || v.since.isBefore(Instant.now().minusSeconds(900))?new Attempts():v);
        if(a.count.get()>=10)throw new ApiProblem(HttpStatus.TOO_MANY_REQUESTS,"TOO_MANY_ATTEMPTS","Demasiados intentos; probá más tarde");
    }
    @Transactional
    public AuthView login(String email,String password,String ip,HttpServletResponse response){
        String key=ip+":"+email.toLowerCase(Locale.ROOT);throttle(key);
        Optional<UserAccount> found=users.findByEmailIgnoreCase(email.trim());
        // Always do a hash comparison to reduce account enumeration timing differences.
        boolean valid=found.map(u->passwords.matches(password,u.passwordHash)).orElseGet(()->passwords.matches(password,"$2a$12$70nQbHUJsYxEXdTFRIg6Q.HlEO9WkLHB1N.OC9oW4yWzIBYw6rWRm"));
        if(!valid){attempts.get(key).count.incrementAndGet();throw new org.springframework.security.authentication.BadCredentialsException("Invalid login");}
        attempts.remove(key);
        return startSession(found.orElseThrow(),response);
    }
    public void requireTrustedOrigin(HttpServletRequest req){
        String origin=req.getHeader("Origin");
        if(origin==null || Arrays.stream(allowedOrigins.split(",")).map(String::trim).noneMatch(origin::equals))
          throw new ApiProblem(HttpStatus.FORBIDDEN,"INVALID_ORIGIN","Origen de solicitud no autorizado");
    }
    @Transactional
    public AuthView refresh(String cookie,HttpServletResponse response){
        if(cookie==null || cookie.isBlank())throw new ApiProblem(HttpStatus.UNAUTHORIZED,"SESSION_EXPIRED","Sesión no disponible");
        RefreshToken old=refreshTokens.lockActiveByHash(digest(cookie))
           .orElseThrow(()->new ApiProblem(HttpStatus.UNAUTHORIZED,"SESSION_EXPIRED","Sesión expirada"));
        if(old.expiresAt.isBefore(Instant.now())){old.revokedAt=Instant.now();refreshTokens.save(old);throw new ApiProblem(HttpStatus.UNAUTHORIZED,"SESSION_EXPIRED","Sesión expirada");}
        old.revokedAt=Instant.now();refreshTokens.save(old);
        UserAccount user=users.findById(old.userId).orElseThrow(()->ApiProblem.notFound("Usuario"));
        return startSession(user,response);
    }
    @Transactional
    public void logout(String cookie,HttpServletResponse response){
        if(cookie!=null&&!cookie.isBlank())refreshTokens.findByTokenHashAndRevokedAtIsNull(digest(cookie)).ifPresent(t->{t.revokedAt=Instant.now();refreshTokens.save(t);});
        refreshCookie(response,"",0);
    }
    @Transactional(readOnly=true)
    public AccountView me(UUID userId){return view(users.findById(userId).orElseThrow(()->ApiProblem.notFound("Usuario")));}
}
