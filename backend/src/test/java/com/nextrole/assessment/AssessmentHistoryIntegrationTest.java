package com.nextrole.assessment;

import com.nextrole.data.AssessmentAnswerRepository;
import com.nextrole.data.AssessmentAttempt;
import com.nextrole.data.AssessmentAttemptRepository;
import com.nextrole.data.AssessmentAttemptStatus;
import com.nextrole.data.AssessmentMatrixVersion;
import com.nextrole.data.AssessmentMatrixVersionRepository;
import com.nextrole.data.AssessmentQuestionnaireVersion;
import com.nextrole.data.AssessmentQuestionnaireVersionRepository;
import com.nextrole.data.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssessmentHistoryIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_history_integration").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", postgres::getJdbcUrl);
        props.add("spring.datasource.username", postgres::getUsername);
        props.add("spring.datasource.password", postgres::getPassword);
        props.add("nextrole.security.jwt-secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired AssessmentAttemptRepository attempts;
    @Autowired AssessmentAnswerRepository answers;
    @Autowired AssessmentQuestionnaireVersionRepository questionnaireVersions;
    @Autowired AssessmentMatrixVersionRepository matrixVersions;
    @Autowired UserAccountRepository users;

    @Test
    @SuppressWarnings("unchecked")
    void reevaluationStartsEmptyRecoversItsOwnAnswersAndKeepsSourceAndHistoryUnchanged() {
        HttpHeaders user = register("history-flow-" + UUID.randomUUID() + "@example.test");
        List<Map<String, Object>> questions = catalog(user);
        String originalId = createReevaluation(user).get("attemptId").toString();
        for (int i = 0; i < questions.size(); i++) save(user, originalId, questions.get(i), i % 5);
        complete(user, originalId);
        Map<String, Object> sourceBefore = detail(user, originalId);

        Map<String, Object> reevaluation = createReevaluation(user);
        String draftId = reevaluation.get("attemptId").toString();
        assertNotEquals(originalId, draftId);
        assertEquals(originalId, attempts.findById(UUID.fromString(draftId)).orElseThrow().sourceAttemptId.toString());
        assertEquals(0, answers.countByIdAttemptId(UUID.fromString(draftId)));
        assertTrue(((Map<String, Object>) reevaluation.get("answers")).isEmpty());
        assertEquals(0, ((Number) reevaluation.get("answeredCount")).intValue());
        assertEquals(draftId, rest.exchange(url("/api/v2/assessment-attempts/current"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody().get("attemptId"));

        save(user, draftId, questions.get(0), 4);
        Map<String, Object> recovered = rest.exchange(url("/api/v2/assessment-attempts/reevaluations"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();
        assertEquals(draftId, recovered.get("attemptId"));
        assertEquals(1, ((Map<String, Object>) recovered.get("answers")).size());
        assertEquals(1, ((Number) recovered.get("answeredCount")).intValue());
        assertEquals(sourceBefore.get("answers"), detail(user, originalId).get("answers"));
        assertEquals(sourceBefore.get("results"), detail(user, originalId).get("results"));
        List<?> historyWithDraft = history(user);
        assertEquals(1, historyWithDraft.size());

        rest.exchange(url("/api/v2/assessment-attempts/" + draftId + "/discard"), HttpMethod.POST,
                new HttpEntity<>(user), Void.class);
        assertEquals(AssessmentAttemptStatus.DISCARDED, attempts.findById(UUID.fromString(draftId)).orElseThrow().status);
        Map<String, Object> next = createReevaluation(user);
        String finalId = next.get("attemptId").toString();
        assertEquals(originalId, attempts.findById(UUID.fromString(finalId)).orElseThrow().sourceAttemptId.toString());
        assertEquals(0, answers.countByIdAttemptId(UUID.fromString(finalId)));
        assertTrue(((Map<String, Object>) next.get("answers")).isEmpty());
        for (int i = 0; i < questions.size(); i++) save(user, finalId, questions.get(i), (i + 1) % 5);
        complete(user, finalId);

        List<Map<String, Object>> historyAfterRestart = (List<Map<String, Object>>) history(user);
        assertEquals(2, historyAfterRestart.size());
        assertEquals(finalId, historyAfterRestart.get(0).get("attemptId"));
        assertEquals(originalId, historyAfterRestart.get(1).get("attemptId"));
        Map<String, Object> historicalDetail = detail(user, originalId);
        assertEquals("M1", historicalDetail.get("matrixVersion"));
        assertEquals(31, ((Map<String, Object>) historicalDetail.get("answers")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void q1DraftCanContinueAndCompleteAfterQ2BecomesCurrent() {
        String email = "history-q1-draft-" + UUID.randomUUID() + "@example.test";
        HttpHeaders user = register(email);
        UUID userId = users.findByEmailIgnoreCase(email).orElseThrow().id;
        UUID q1Id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID m1Id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID draftId = UUID.randomUUID();
        attempts.save(new AssessmentAttempt(draftId, userId, AssessmentAttemptStatus.DRAFT,
                q1Id, m1Id, java.time.Instant.now()));

        Map<String, Object> recovered = createReevaluation(user);
        assertEquals(draftId.toString(), recovered.get("attemptId"));
        assertEquals("Q1", recovered.get("questionnaireVersion"));
        assertEquals("M1", recovered.get("matrixVersion"));
        List<Map<String, Object>> q1Questions = (List<Map<String, Object>>) recovered.get("questions");
        assertEquals(31, q1Questions.size());
        for (int i = 0; i < q1Questions.size(); i++) save(user, draftId.toString(), q1Questions.get(i), i % 5);
        complete(user, draftId.toString());

        Map<String, Object> historical = detail(user, draftId.toString());
        assertEquals("Q1", historical.get("questionnaireVersion"));
        assertEquals("M1", historical.get("matrixVersion"));
        assertEquals(31, ((Map<String, Object>) historical.get("answers")).size());
        assertEquals(6, ((List<?>) historical.get("results")).size());

        Map<String, Object> newAttempt = createReevaluation(user);
        assertEquals("Q2", newAttempt.get("questionnaireVersion"));
        assertEquals("M1", newAttempt.get("matrixVersion"));
        String q2AttemptId = newAttempt.get("attemptId").toString();
        List<Map<String, Object>> q2Questions = (List<Map<String, Object>>) newAttempt.get("questions");
        for (int i = 0; i < q2Questions.size(); i++) save(user, q2AttemptId, q2Questions.get(i), i % 5);
        complete(user, q2AttemptId);

        Map<String, Object> q2Completed = detail(user, q2AttemptId);
        assertEquals(scoreSnapshot(historical), scoreSnapshot(q2Completed));
        assertEquals(skillGaps(user, draftId.toString()).get("competencies"),
                skillGaps(user, q2AttemptId).get("competencies"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void oneDraftIsPersistedAndKeepsItsPinnedVersionsWhenNewVersionsBecomeCurrent() {
        HttpHeaders user = register("history-version-pin-" + UUID.randomUUID() + "@example.test");
        Map<String, Object> draft = createReevaluation(user);
        UUID draftId = UUID.fromString(draft.get("attemptId").toString());
        AssessmentAttempt pinned = attempts.findById(draftId).orElseThrow();
        AssessmentQuestionnaireVersion pinnedQuestionnaire = questionnaireVersions
                .findById(pinned.questionnaireVersionId).orElseThrow();
        AssessmentMatrixVersion pinnedMatrix = matrixVersions.findById(pinned.matrixVersionId).orElseThrow();
        UUID newerQuestionnaireId = UUID.randomUUID();
        UUID newerMatrixId = UUID.randomUUID();
        AssessmentQuestionnaireVersion newerQuestionnaire = new AssessmentQuestionnaireVersion(newerQuestionnaireId,
                "Q-T025-" + UUID.randomUUID().toString().substring(0, 8), java.time.Instant.now(), true);
        AssessmentMatrixVersion newerMatrix = new AssessmentMatrixVersion(newerMatrixId,
                "M-T025-" + UUID.randomUUID().toString().substring(0, 8), java.time.Instant.now(), true);

        try {
            pinnedQuestionnaire.current = false;
            pinnedMatrix.current = false;
            questionnaireVersions.save(pinnedQuestionnaire);
            matrixVersions.save(pinnedMatrix);
            questionnaireVersions.save(newerQuestionnaire);
            matrixVersions.save(newerMatrix);

            Map<String, Object> recovered = createReevaluation(user);
            assertEquals(draftId.toString(), recovered.get("attemptId").toString());
            assertEquals(pinnedQuestionnaire.versionCode, recovered.get("questionnaireVersion"));
            assertEquals(pinnedMatrix.versionCode, recovered.get("matrixVersion"));
            assertEquals(1, attempts.findAll().stream()
                    .filter(attempt -> attempt.userId.equals(pinned.userId)
                            && attempt.status == AssessmentAttemptStatus.DRAFT)
                    .count());
            assertEquals(pinned.questionnaireVersionId, attempts.findById(draftId).orElseThrow().questionnaireVersionId);
            assertEquals(pinned.matrixVersionId, attempts.findById(draftId).orElseThrow().matrixVersionId);
        } finally {
            newerQuestionnaire.current = false;
            newerMatrix.current = false;
            questionnaireVersions.save(newerQuestionnaire);
            matrixVersions.save(newerMatrix);
            pinnedQuestionnaire.current = true;
            pinnedMatrix.current = true;
            questionnaireVersions.save(pinnedQuestionnaire);
            matrixVersions.save(pinnedMatrix);
            questionnaireVersions.deleteById(newerQuestionnaireId);
            matrixVersions.deleteById(newerMatrixId);
        }
    }

    private Map<String, Object> createReevaluation(HttpHeaders user) {
        return rest.exchange(url("/api/v2/assessment-attempts/reevaluations"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> catalog(HttpHeaders user) {
        return (List<Map<String, Object>>) rest.exchange(url("/api/v2/assessment-catalog/current"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody().get("questions");
    }

    private void save(HttpHeaders user, String attemptId, Map<String, Object> question, int level) {
        HttpHeaders headers = new HttpHeaders(user);
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/" + question.get("competencyCode")),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", level), headers), Map.class);
    }

    private void complete(HttpHeaders user, String attemptId) {
        ResponseEntity<Map> response = rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/complete"),
                HttpMethod.POST, new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private Map<String, Object> detail(HttpHeaders user, String attemptId) {
        return rest.exchange(url("/api/v2/assessment-attempts/" + attemptId), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
    }

    @SuppressWarnings("unchecked")
    private List<String> scoreSnapshot(Map<String, Object> detail) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) detail.get("results");
        return results.stream()
                .map(result -> result.get("roleId") + "|" + result.get("scoreNumerator") + "|"
                        + result.get("scoreDenominator") + "|" + result.get("displayPercentage"))
                .sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> skillGaps(HttpHeaders user, String attemptId) {
        return rest.exchange(url("/api/v2/assessment-attempts/" + attemptId
                        + "/skill-gaps/data-analyst-jr"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
    }

    private List<?> history(HttpHeaders user) {
        return rest.exchange(url("/api/v2/assessment-history"), HttpMethod.GET,
                new HttpEntity<>(user), List.class).getBody();
    }

    private HttpHeaders register(String email) {
        Map<String, Object> payload = Map.of("firstName", "Estudiante", "lastName", "Prueba", "email", email,
                "password", "Segura-12345!", "confirmPassword", "Segura-12345!", "acceptTerms", true);
        ResponseEntity<Map> response = rest.postForEntity(url("/api/v1/auth/register"), payload, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) response.getBody().get("accessToken"));
        return headers;
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}
