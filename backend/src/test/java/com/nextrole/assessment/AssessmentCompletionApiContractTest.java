package com.nextrole.assessment;

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
class AssessmentCompletionApiContractTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_completion_contract").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", postgres::getJdbcUrl);
        props.add("spring.datasource.username", postgres::getUsername);
        props.add("spring.datasource.password", postgres::getPassword);
        props.add("nextrole.security.jwt-secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    @SuppressWarnings("unchecked")
    void completionRejectsMissingAnswersThenReturnsSixPersistedRankedResults() {
        HttpHeaders user = register("completion-" + UUID.randomUUID() + "@example.test");
        Map<String, Object> catalog = rest.exchange(url("/api/v2/assessment-catalog/current"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
        List<Map<String, Object>> questions = (List<Map<String, Object>>) catalog.get("questions");
        Map<String, Object> attempt = rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();
        String attemptId = attempt.get("attemptId").toString();

        for (int i = 0; i < 30; i++) save(user, attemptId, questions.get(i), i % 5);
        ResponseEntity<Map> incomplete = rest.exchange(url(completePath(attemptId)), HttpMethod.POST,
                new HttpEntity<>(user), Map.class);

        assertEquals(HttpStatus.CONFLICT, incomplete.getStatusCode());
        assertEquals("INCOMPLETE_ASSESSMENT", incomplete.getBody().get("code"));
        Map<String, Object> details = (Map<String, Object>) incomplete.getBody().get("details");
        assertEquals(List.of(questions.get(30).get("competencyCode")), details.get("missingCompetencies"));
        assertEquals("DRAFT", detail(user, attemptId).get("status"));

        save(user, attemptId, questions.get(30), 0);
        ResponseEntity<Map> completed = rest.exchange(url(completePath(attemptId)), HttpMethod.POST,
                new HttpEntity<>(user), Map.class);

        assertEquals(HttpStatus.OK, completed.getStatusCode());
        assertEquals("COMPLETED", completed.getBody().get("status"));
        List<Map<String, Object>> results = (List<Map<String, Object>>) completed.getBody().get("results");
        assertEquals(6, results.size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), results.stream()
                .map(result -> ((Number) result.get("rankPosition")).intValue()).toList());
        for (Map<String, Object> result : results) {
            assertNotNull(result.get("roleId"));
            assertTrue(result.get("scoreNumerator") instanceof String);
            assertTrue(result.get("scoreDenominator") instanceof String);
            assertTrue(result.get("displayPercentage") instanceof String);
        }

        ResponseEntity<Map> retried = rest.exchange(url(completePath(attemptId)), HttpMethod.POST,
                new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.OK, retried.getStatusCode());
        assertEquals(results, retried.getBody().get("results"));
    }

    @Test
    void completionRequiresAuthenticationAndHidesAttemptsOwnedByAnotherUser() {
        assertEquals(HttpStatus.UNAUTHORIZED, rest.postForEntity(url(completePath(UUID.randomUUID().toString())), null, Map.class).getStatusCode());
        HttpHeaders user = register("owner-" + UUID.randomUUID() + "@example.test");
        HttpHeaders other = register("other-" + UUID.randomUUID() + "@example.test");
        Map<?, ?> attempt = rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();

        assertEquals(HttpStatus.NOT_FOUND, rest.exchange(url(completePath(attempt.get("attemptId").toString())), HttpMethod.POST,
                new HttpEntity<>(other), Map.class).getStatusCode());
    }

    private Map<String, Object> detail(HttpHeaders user, String attemptId) {
        return rest.exchange(url("/api/v2/assessment-attempts/" + attemptId), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody();
    }

    private void save(HttpHeaders user, String attemptId, Map<String, Object> question, int level) {
        HttpHeaders headers = new HttpHeaders(user);
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/" + question.get("competencyCode")),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", level), headers), Map.class);
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

    private static String completePath(String attemptId) {
        return "/api/v2/assessment-attempts/" + attemptId + "/complete";
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}
