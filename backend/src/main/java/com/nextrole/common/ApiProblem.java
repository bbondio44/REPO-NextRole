package com.nextrole.common;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;
public class ApiProblem extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final Map<String, Object> details;
    public ApiProblem(HttpStatus status, String code, String message) { this(status, code, message, Map.of()); }
    public ApiProblem(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message); this.status=status; this.code=code; this.details=Map.copyOf(details);
    }
    public HttpStatus status() { return status; }
    public String code() { return code; }
    public Map<String, Object> details() { return details; }
    public static ApiProblem notFound(String what) { return new ApiProblem(HttpStatus.NOT_FOUND,"NOT_FOUND",what+" no encontrado"); }
    public static ApiProblem invalid(String message) { return new ApiProblem(HttpStatus.BAD_REQUEST,"INVALID_INPUT",message); }
    public static ApiProblem conflict(String message) { return new ApiProblem(HttpStatus.CONFLICT,"CONFLICT",message); }
    public static ApiProblem incompleteAssessment(List<String> missingCompetencies) {
        return new ApiProblem(HttpStatus.CONFLICT, "INCOMPLETE_ASSESSMENT",
                "Respondé las 31 competencias antes de finalizar", Map.of("missingCompetencies", List.copyOf(missingCompetencies)));
    }
}
