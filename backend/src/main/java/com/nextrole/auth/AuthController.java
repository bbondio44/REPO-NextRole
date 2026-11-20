package com.nextrole.auth;
import com.nextrole.common.ApiProblem;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){this.auth=auth;}
    public record RegisterRequest(@NotBlank String firstName,@NotBlank String lastName,@NotBlank @Email String email,
      @NotBlank @Size(min=8,max=128) String password,@NotBlank String confirmPassword,@NotNull @AssertTrue Boolean acceptTerms) {}
    public record LoginRequest(@NotBlank @Email String email,@NotBlank String password) {}
    @PostMapping("/register") public ResponseEntity<AuthService.AuthView> register(@Valid @RequestBody RegisterRequest r,HttpServletResponse response){
        if(!r.password().equals(r.confirmPassword()))throw ApiProblem.invalid("Las contraseñas no coinciden");
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r.firstName(),r.lastName(),r.email(),r.password(),response));
    }
    @PostMapping("/login") public AuthService.AuthView login(@Valid @RequestBody LoginRequest r,HttpServletRequest req,HttpServletResponse response){
        return auth.login(r.email(),r.password(),req.getRemoteAddr(),response);
    }
    @PostMapping("/refresh") public AuthService.AuthView refresh(@CookieValue(value="nr_refresh",required=false) String cookie,HttpServletRequest req,HttpServletResponse response){
        auth.requireTrustedOrigin(req);return auth.refresh(cookie,response);
    }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(value="nr_refresh",required=false) String cookie,HttpServletRequest req,HttpServletResponse response){auth.requireTrustedOrigin(req);auth.logout(cookie,response);}
    @GetMapping("/me") public AuthService.AccountView me(){return auth.me(CurrentUser.id());}
}
