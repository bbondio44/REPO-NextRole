package com.nextrole.assessment;

import com.nextrole.assessment.AssessmentDtos.*;
import com.nextrole.auth.CurrentUser;
import com.nextrole.data.AssessmentAttempt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
public class AssessmentController {
    private final AssessmentCatalogService catalog;
    private final AssessmentAttemptService attempts;
    private final AssessmentAnswerService answers;
    private final AssessmentCompletionService completion;

    public AssessmentController(AssessmentCatalogService catalog, AssessmentAttemptService attempts,
                                AssessmentAnswerService answers, AssessmentCompletionService completion) {
        this.catalog = catalog;
        this.attempts = attempts;
        this.answers = answers;
        this.completion = completion;
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

    @PostMapping("/api/v2/assessment-attempts/reevaluations")
    public ResponseEntity<AttemptDto> reevaluate() {
        var result = attempts.reevaluate(CurrentUser.id());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(attempts.toDto(result.attempt()));
    }

    @PostMapping("/api/v2/assessment-attempts/{attemptId}/discard")
    public ResponseEntity<Void> discard(@PathVariable UUID attemptId) {
        attempts.discard(CurrentUser.id(), attemptId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v2/assessment-attempts/current")
    public ResponseEntity<AttemptDto> currentAttempt() {
        AssessmentAttempt current = attempts.current(CurrentUser.id());
        return current == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(attempts.toDto(current));
    }

    @GetMapping("/api/v2/assessment-history")
    public List<HistoryItemDto> history() {
        return attempts.history(CurrentUser.id());
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

    @PostMapping("/api/v2/assessment-attempts/{attemptId}/complete")
    public AttemptDto complete(@PathVariable UUID attemptId) {
        return completion.complete(CurrentUser.id(), attemptId);
    }
}
