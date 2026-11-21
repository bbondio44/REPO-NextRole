package com.nextrole.assessment;

import com.nextrole.assessment.AssessmentDtos.*;
import com.nextrole.auth.CurrentUser;
import com.nextrole.data.AssessmentAttempt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class AssessmentController {
    private final AssessmentCatalogService catalog;
    private final AssessmentAttemptService attempts;
    private final AssessmentAnswerService answers;

    public AssessmentController(AssessmentCatalogService catalog, AssessmentAttemptService attempts,
                                AssessmentAnswerService answers) {
        this.catalog = catalog;
        this.attempts = attempts;
        this.answers = answers;
    }

    @GetMapping("/api/v2/assessment-catalog/current")
    public CatalogDto currentCatalog() {
        return catalog.current();
    }

    @PostMapping("/api/v2/assessment-attempts")
    public ResponseEntity<AttemptDto> startOrRecover() {
        var result = attempts.startOrRecover(CurrentUser.id());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(attempts.toDto(result.attempt()));
    }

    @GetMapping("/api/v2/assessment-attempts/current")
    public ResponseEntity<AttemptDto> currentAttempt() {
        AssessmentAttempt current = attempts.current(CurrentUser.id());
        return current == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(attempts.toDto(current));
    }

    @GetMapping("/api/v2/assessment-attempts/{attemptId}")
    public AttemptDto attempt(@PathVariable UUID attemptId) {
        return attempts.toDto(attempts.owned(CurrentUser.id(), attemptId));
    }

    @PutMapping("/api/v2/assessment-attempts/{attemptId}/answers/{competencyCode}")
    public AnswerSavedDto saveAnswer(@PathVariable UUID attemptId, @PathVariable String competencyCode,
                                     @Valid @RequestBody SaveAnswerRequest request) {
        return answers.save(CurrentUser.id(), attemptId, competencyCode, request.level());
    }
}
