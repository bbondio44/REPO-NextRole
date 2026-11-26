package com.nextrole.skillgap;

import com.nextrole.data.*;
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
import java.util.stream.Collectors;

import static com.nextrole.data.AssessmentRequirementImportance.NOT_RELEVANT;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SkillGapV2ApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("skill_gap_v2_integration").withUsername("nextrole").withPassword("test_password");

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
    @Autowired AssessmentQuestionRepository questions;
    @Autowired AssessmentRoleRequirementRepository requirements;
    @Autowired AssessmentQuestionnaireVersionRepository questionnaireVersions;
    @Autowired AssessmentMatrixVersionRepository matrixVersions;
    @Autowired GapRunRepository gapRuns;

    @Test
    @SuppressWarnings("unchecked")
    void endpointUsesOwnedCompletedAttemptAndItsPinnedVersionsWithoutPersistingGapRuns() {
        HttpHeaders user = register("skill-gap-v2-" + UUID.randomUUID() + "@example.test");
        HttpHeaders other = register("skill-gap-v2-other-" + UUID.randomUUID() + "@example.test");
        Map<String, Object> draft = start(user);
        String attemptId = draft.get("attemptId").toString();
        UUID attemptUuid = UUID.fromString(attemptId);
        AssessmentAttempt attempt = attempts.findById(attemptUuid).orElseThrow();
        List<Map<String, Object>> catalogQuestions = catalog(user);
        saveAllAnswers(user, attemptId, catalogQuestions, 0);
        complete(user, attemptId);
        assertTrue(gapRuns.findFirstByUserIdOrderByCreatedAtDesc(userId(attemptUuid)).isEmpty());

        String roleId = "backend-dev-jr";
        String endpoint = skillGapPath(attemptId, roleId);
        ResponseEntity<Map> response = rest.exchange(url(endpoint), HttpMethod.GET, new HttpEntity<>(user), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(attemptId, body.get("attemptId").toString());
        assertEquals(roleId, body.get("roleId"));
        assertEquals(questionnaireVersions.findById(attempt.questionnaireVersionId).orElseThrow().versionCode,
                body.get("questionnaireVersion"));
        assertEquals(matrixVersions.findById(attempt.matrixVersionId).orElseThrow().versionCode,
                body.get("matrixVersion"));

        List<AssessmentRoleRequirement> relevant = requirements.findByIdMatrixVersionId(attempt.matrixVersionId).stream()
                .filter(row -> row.id.roleId.equals(roleId) && row.importance != NOT_RELEVANT)
                .toList();
        List<Map<String, Object>> actual = (List<Map<String, Object>>) body.get("competencies");
        assertEquals(relevant.size(), actual.size());
        Map<String, Map<String, Object>> actualByCode = actual.stream().collect(Collectors.toMap(
                row -> row.get("competencyCode").toString(), row -> row));
        Map<String, Integer> declaredByCode = answers.findByIdAttemptId(attemptUuid).stream().collect(Collectors.toMap(
                answer -> answer.id.competencyCode, answer -> (int) answer.level));
        Map<String, AssessmentQuestion> questionByCode = questions
                .findByIdQuestionnaireVersionIdOrderByPosition(attempt.questionnaireVersionId).stream()
                .collect(Collectors.toMap(question -> question.id.competencyCode, question -> question));
        for (AssessmentRoleRequirement requirement : relevant) {
            String code = requirement.id.competencyCode;
            Map<String, Object> item = actualByCode.get(code);
            int declared = declaredByCode.get(code);
            int expected = requirement.expectedLevel;
            assertNotNull(item);
            assertEquals(questionByCode.get(code).title, item.get("competencyName"));
            assertEquals(declared, ((Number) item.get("declaredLevel")).intValue());
            assertEquals(expected, ((Number) item.get("requiredLevel")).intValue());
            assertEquals(requirement.importance.name(), item.get("importance"));
            assertEquals(GapPolicy.amount(declared, expected), ((Number) item.get("gap")).intValue());
            assertFalse(item.containsKey("description"));
            assertFalse(item.containsKey("priority"));
            assertFalse(item.containsKey("question"));
        }
        assertTrue(gapRuns.findFirstByUserIdOrderByCreatedAtDesc(userId(attemptUuid)).isEmpty());

        ResponseEntity<Map> foreign = rest.exchange(url(endpoint), HttpMethod.GET, new HttpEntity<>(other), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, foreign.getStatusCode());

        ResponseEntity<Map> unknownRole = rest.exchange(url(skillGapPath(attemptId, "not-a-v2-role")),
                HttpMethod.GET, new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, unknownRole.getStatusCode());

        String draftId = start(user).get("attemptId").toString();
        ResponseEntity<Map> draftResponse = rest.exchange(url(skillGapPath(draftId, roleId)), HttpMethod.GET,
                new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.CONFLICT, draftResponse.getStatusCode());
        assertEquals("ATTEMPT_NOT_COMPLETED", draftResponse.getBody().get("code"));
    }

    @Test
    void historicalAttemptUsesItsPinnedMatrixAfterANewerMatrixAndAttemptExist() {
        HttpHeaders user = register("skill-gap-v2-history-" + UUID.randomUUID() + "@example.test");
        List<Map<String, Object>> catalog = catalog(user);
        String oldAttemptId = start(user).get("attemptId").toString();
        saveAllAnswers(user, oldAttemptId, catalog, 0);
        complete(user, oldAttemptId);

        AssessmentAttempt oldAttempt = attempts.findById(UUID.fromString(oldAttemptId)).orElseThrow();
        AssessmentMatrixVersion oldMatrix = matrixVersions.findById(oldAttempt.matrixVersionId).orElseThrow();
        String roleId = "backend-dev-jr";
        AssessmentRoleRequirement target = requirements.findByIdMatrixVersionId(oldAttempt.matrixVersionId).stream()
                .filter(row -> row.id.roleId.equals(roleId) && row.importance != NOT_RELEVANT)
                .findFirst().orElseThrow();
        short changedLevel = target.expectedLevel == 1 ? (short) 4 : (short) 1;
        String newVersionCode = "SG" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        AssessmentMatrixVersion newMatrix = new AssessmentMatrixVersion(UUID.randomUUID(), newVersionCode,
                java.time.Instant.now(), true);
        oldMatrix.current = false;
        matrixVersions.save(oldMatrix);
        matrixVersions.save(newMatrix);

        List<AssessmentRoleRequirement> copiedRows = requirements.findByIdMatrixVersionId(oldMatrix.id).stream()
                .map(row -> new AssessmentRoleRequirement(
                        new AssessmentRoleRequirementId(newMatrix.id, row.id.roleId, row.id.competencyCode),
                        row.importance,
                        row.id.roleId.equals(roleId) && row.id.competencyCode.equals(target.id.competencyCode)
                                ? Short.valueOf(changedLevel) : row.expectedLevel))
                .toList();
        requirements.saveAll(copiedRows);

        String newAttemptId = rest.exchange(url("/api/v2/assessment-attempts/reevaluations"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody().get("attemptId").toString();
        assertEquals(newMatrix.id, attempts.findById(UUID.fromString(newAttemptId)).orElseThrow().matrixVersionId);
        saveAllAnswers(user, newAttemptId, catalog, 4);
        complete(user, newAttemptId);

        Map<String, Object> oldDiagnostic = diagnostic(user, oldAttemptId, roleId);
        Map<String, Object> newDiagnostic = diagnostic(user, newAttemptId, roleId);
        assertEquals(oldMatrix.versionCode, oldDiagnostic.get("matrixVersion"));
        assertEquals(newVersionCode, newDiagnostic.get("matrixVersion"));
        Map<String, Object> oldItem = competency(oldDiagnostic, target.id.competencyCode);
        Map<String, Object> newItem = competency(newDiagnostic, target.id.competencyCode);
        assertEquals(target.expectedLevel.intValue(), ((Number) oldItem.get("requiredLevel")).intValue());
        assertEquals((int) changedLevel, ((Number) newItem.get("requiredLevel")).intValue());
        assertEquals(0, ((Number) newItem.get("gap")).intValue());
    }

    @Test
    void rejectsUnauthenticatedRequests() {
        assertEquals(HttpStatus.UNAUTHORIZED, rest.getForEntity(url(skillGapPath(UUID.randomUUID().toString(), "backend-dev-jr")), Map.class).getStatusCode());
    }

    @Test
    void rejectsDiscardedAndMissingAttemptsWithoutFallingBackToAnotherAttempt() {
        HttpHeaders user = register("skill-gap-v2-discard-" + UUID.randomUUID() + "@example.test");
        String draftId = start(user).get("attemptId").toString();

        ResponseEntity<Void> discarded = rest.postForEntity(
                url("/api/v2/assessment-attempts/" + draftId + "/discard"), new HttpEntity<>(user), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, discarded.getStatusCode());

        ResponseEntity<Map> discardedResponse = rest.exchange(
                url(skillGapPath(draftId, "backend-dev-jr")), HttpMethod.GET, new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.CONFLICT, discardedResponse.getStatusCode());
        assertEquals("ATTEMPT_NOT_COMPLETED", discardedResponse.getBody().get("code"));

        ResponseEntity<Map> missingResponse = rest.exchange(
                url(skillGapPath(UUID.randomUUID().toString(), "backend-dev-jr")), HttpMethod.GET,
                new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> diagnostic(HttpHeaders user, String attemptId, String roleId) {
        return rest.exchange(url(skillGapPath(attemptId, roleId)), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> competency(Map<String, Object> diagnostic, String code) {
        return ((List<Map<String, Object>>) diagnostic.get("competencies")).stream()
                .filter(item -> item.get("competencyCode").equals(code)).findFirst().orElseThrow();
    }

    private UUID userId(UUID attemptId) {
        return attempts.findById(attemptId).orElseThrow().userId;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> catalog(HttpHeaders user) {
        Map<String, Object> result = rest.exchange(url("/api/v2/assessment-catalog/current"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
        return (List<Map<String, Object>>) result.get("questions");
    }

    private Map<String, Object> start(HttpHeaders user) {
        return rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();
    }

    private void saveAllAnswers(HttpHeaders user, String attemptId, List<Map<String, Object>> catalog,
                                int defaultLevel) {
        for (Map<String, Object> question : catalog) {
            String code = question.get("competencyCode").toString();
            HttpHeaders headers = new HttpHeaders(user);
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/" + code), HttpMethod.PUT,
                    new HttpEntity<>(Map.of("level", defaultLevel), headers), Map.class);
        }
    }

    private void complete(HttpHeaders user, String attemptId) {
        ResponseEntity<Map> response = rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/complete"),
                HttpMethod.POST, new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
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

    private static String skillGapPath(String attemptId, String roleId) {
        return "/api/v2/assessment-attempts/" + attemptId + "/skill-gaps/" + roleId;
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}
