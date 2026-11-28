package com.nextrole.explainability;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.ExplainabilityDiagnosticStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/assessment-attempts/{attemptId}/diagnoses/{roleId}")
public class ExplainabilityController {
    private final ExplainabilityService service;

    public ExplainabilityController(ExplainabilityService service) {
        this.service = service;
    }

    @GetMapping
    public DiagnosisResponse status(@PathVariable UUID attemptId, @PathVariable String roleId) {
        return DiagnosisResponse.from(service.getStatus(CurrentUser.id(), attemptId, roleId));
    }

    @PostMapping("/generate")
    public ResponseEntity<DiagnosisResponse> generate(@PathVariable UUID attemptId, @PathVariable String roleId) {
        ExplainabilityService.GenerationOutcome outcome = service.generateInitial(CurrentUser.id(), attemptId, roleId);
        return operationResponse(outcome);
    }

    @PostMapping("/retry")
    public ResponseEntity<DiagnosisResponse> retry(@PathVariable UUID attemptId, @PathVariable String roleId) {
        ExplainabilityService.GenerationOutcome outcome = service.retryManual(CurrentUser.id(), attemptId, roleId);
        return operationResponse(outcome);
    }

    private static ResponseEntity<DiagnosisResponse> operationResponse(ExplainabilityService.GenerationOutcome outcome) {
        if (outcome.status() == ExplainabilityDiagnosticStatus.FAILED) throw generationProblem(outcome.failureCode());
        HttpStatus status = outcome.status() == ExplainabilityDiagnosticStatus.GENERATING
                ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity.status(status).body(DiagnosisResponse.from(outcome));
    }

    private static ApiProblem generationProblem(String code) {
        if ("GENERATION_INTERRUPTED".equals(code)) {
            return new ApiProblem(HttpStatus.CONFLICT, "DIAGNOSIS_RETRY_REQUIRED",
                    "El diagnóstico requiere un reintento manual");
        }
        if ("PROVIDER_UNAVAILABLE".equals(code)) {
            return new ApiProblem(HttpStatus.SERVICE_UNAVAILABLE, code,
                    "El servicio de diagnóstico no está disponible en este momento");
        }
        if ("PROVIDER_TIMEOUT".equals(code)) {
            return new ApiProblem(HttpStatus.GATEWAY_TIMEOUT, code,
                    "El servicio de diagnóstico demoró demasiado en responder");
        }
        return new ApiProblem(HttpStatus.BAD_GATEWAY,
                "INVALID_PROVIDER_RESPONSE".equals(code) ? code : "PROVIDER_ERROR",
                "No se pudo validar o generar el diagnóstico");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"attemptId", "roleId", "status", "roleName", "roleDescription", "diagnosis", "model",
            "promptVersion", "generatedAt", "retryAfterSeconds", "failureCode", "canRetry"})
    public record DiagnosisResponse(UUID attemptId, String roleId, String status, String roleName,
                                    String roleDescription, DiagnosisValidator.ValidatedDiagnosis diagnosis,
                                    String model, String promptVersion, Instant generatedAt,
                                    Integer retryAfterSeconds, String failureCode, Boolean canRetry) {
        static DiagnosisResponse from(ExplainabilityService.GenerationOutcome outcome) {
            return new DiagnosisResponse(outcome.attemptId(), outcome.roleId(), outcome.statusValue(),
                    outcome.roleName(), outcome.roleDescription(), outcome.diagnosis(), outcome.modelRequested(),
                    outcome.promptVersion(), outcome.generatedAt(), outcome.retryAfterSeconds(),
                    outcome.failureCode(), outcome.canRetry());
        }
    }
}
