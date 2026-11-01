package com.nextrole.common;
import org.springframework.http.HttpStatus;
public class ApiProblem extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public ApiProblem(HttpStatus status, String code, String message) { super(message); this.status=status; this.code=code; }
    public HttpStatus status() { return status; }
    public String code() { return code; }
    public static ApiProblem notFound(String what) { return new ApiProblem(HttpStatus.NOT_FOUND,"NOT_FOUND",what+" no encontrado"); }
    public static ApiProblem invalid(String message) { return new ApiProblem(HttpStatus.BAD_REQUEST,"INVALID_INPUT",message); }
    public static ApiProblem conflict(String message) { return new ApiProblem(HttpStatus.CONFLICT,"CONFLICT",message); }
}
