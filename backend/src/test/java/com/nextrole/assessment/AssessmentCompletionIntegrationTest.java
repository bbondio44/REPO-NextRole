package com.nextrole.assessment;

import com.nextrole.data.AssessmentAnswerRepository;
import com.nextrole.data.AssessmentAttemptRepository;
import com.nextrole.data.AssessmentResultSetRepository;
import com.nextrole.data.AssessmentRoleResultRepository;
import com.nextrole.data.AssessmentAttemptStatus;
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
class AssessmentCompletionIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_completion_integration").withUsername("nextrole").withPassword("test_password");

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
    @Autowired AssessmentResultSetRepository resultSets;
    @Autowired AssessmentRoleResultRepository roleResults;

    @Test
    @SuppressWarnings("unchecked")
    void completionIsAtomicIdempotentAndDoesNotReplaceTheLatestCompletedAttemptWhenANewDraftIsEdited() {
        HttpHeaders user = register("atomic-" + UUID.randomUUID() + "@example.test");
        Map<String, Object> first = start(user);
        String firstId = first.get("attemptId").toString();
        List<Map<String, Object>> questions = catalog(user);

        for (int i = 0; i < 30; i++) save(user, firstId, questions.get(i), 0);
        ResponseEntity<Map> incomplete = complete(user, firstId);
        assertEquals(HttpStatus.CONFLICT, incomplete.getStatusCode());
        assertEquals(0, resultSets.count());
        assertEquals(0, roleResults.count());
        assertEquals(AssessmentAttemptStatus.DRAFT, attempts.findById(UUID.fromString(firstId)).orElseThrow().status);
        assertEquals(30, answers.countByIdAttemptId(UUID.fromString(firstId)));

        save(user, firstId, questions.get(30), 0);
        ResponseEntity<Map> completed = complete(user, firstId);
        assertEquals(HttpStatus.OK, completed.getStatusCode());
        Map<String, Object> completedBody = completed.getBody();
        List<Map<String, Object>> originalResults = (List<Map<String, Object>>) completedBody.get("results");
        assertEquals(6, originalResults.size());
        assertEquals(6, roleResults.count());
        assertEquals(AssessmentAttemptStatus.COMPLETED, attempts.findById(UUID.fromString(firstId)).orElseThrow().status);

        ResponseEntity<Map> retry = complete(user, firstId);
        assertEquals(originalResults, retry.getBody().get("results"));
        assertEquals(1, resultSets.count());
        assertEquals(6, roleResults.count());

        Map<String, Object> second = start(user);
        String secondId = second.get("attemptId").toString();
        save(user, secondId, questions.get(0), 4);
        Map<String, Object> originalAfterEdit = detail(user, firstId);
        assertEquals("COMPLETED", originalAfterEdit.get("status"));
        assertEquals(originalResults, originalAfterEdit.get("results"));
        assertEquals("DRAFT", detail(user, secondId).get("status"));
        assertEquals(1, resultSets.count());
    }

    private Map<String, Object> start(HttpHeaders user) {
        return rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> catalog(HttpHeaders user) {
        Map<String, Object> body = rest.exchange(url("/api/v2/assessment-catalog/current"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
        return (List<Map<String, Object>>) body.get("questions");
    }

    private void save(HttpHeaders user, String attemptId, Map<String, Object> question, int level) {
        HttpHeaders headers = new HttpHeaders(user);
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/" + question.get("competencyCode")),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", level), headers), Map.class);
    }

    private ResponseEntity<Map> complete(HttpHeaders user, String attemptId) {
        return rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/complete"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class);
    }

    private Map<String, Object> detail(HttpHeaders user, String attemptId) {
        return rest.exchange(url("/api/v2/assessment-attempts/" + attemptId), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
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
