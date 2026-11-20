package com.nextrole.common;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalErrorHandler {
    public record ErrorBody(String code, String message, Map<String,String> details, String requestId, Instant timestamp) {}
    private ResponseEntity<ErrorBody> error(HttpStatus status,String code,String msg,Map<String,String> details,HttpServletRequest req) {
        String requestId=Optional.ofNullable(req.getHeader("X-Request-ID")).filter(s->s.matches("[A-Za-z0-9_-]{1,80}")).orElse(UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(new ErrorBody(code,msg,details,requestId,Instant.now()));
    }
    @ExceptionHandler(ApiProblem.class)
    public ResponseEntity<ErrorBody> known(ApiProblem ex,HttpServletRequest req){ return error(ex.status(),ex.code(),ex.getMessage(),Map.of(),req); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException ex,HttpServletRequest req){
        Map<String,String> details=new LinkedHashMap<>();
        for (var issue: ex.getBindingResult().getFieldErrors()) details.putIfAbsent(issue.getField(),issue.getDefaultMessage());
        return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Revisá los datos ingresados",details,req);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorBody> constraint(DataIntegrityViolationException ex,HttpServletRequest req){ return error(HttpStatus.CONFLICT,"CONFLICT","La operación viola una restricción de datos",Map.of(),req); }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorBody> badCredentials(BadCredentialsException ex,HttpServletRequest req){ return error(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","Credenciales inválidas",Map.of(),req); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorBody> forbidden(AccessDeniedException ex,HttpServletRequest req){ return error(HttpStatus.FORBIDDEN,"FORBIDDEN","No autorizado",Map.of(),req); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> unexpected(Exception ex,HttpServletRequest req){ return error(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","Ocurrió un error inesperado",Map.of(),req); }
}
