package com.nextrole.integration;

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
class AssessmentWorkflowIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_workflow").withUsername("nextrole").withPassword("test_password");

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
    void catalogDraftCompletionReevaluationAndHistoryPreserveBothCompletedResults() {
        HttpHeaders user = register("workflow-" + UUID.randomUUID() + "@example.test");
        ResponseEntity<Map> catalogResponse = rest.exchange(url("/api/v2/assessment-catalog/current"), HttpMethod.GET,
                new HttpEntity<>(user), Map.class);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) catalogResponse.getBody().get("questions");
        assertEquals(31, questions.size());

        Map<String, Object> initial = rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class).getBody();
        String initialId = initial.get("attemptId").toString();
        for (Map<String, Object> question : questions) save(user, initialId, question, 2);
        Map<String, Object> completed = post(user, "/api/v2/assessment-attempts/" + initialId + "/complete");
        assertEquals("COMPLETED", completed.get("status"));
        List<?> initialResults = (List<?>) completed.get("results");
        assertEquals(6, initialResults.size());

        Map<String, Object> reevaluation = post(user, "/api/v2/assessment-attempts/reevaluations");
        String reevaluationId = reevaluation.get("attemptId").toString();
        assertNotEquals(initialId, reevaluationId);
        assertEquals(31, ((Map<String, Object>) reevaluation.get("answers")).size());
        save(user, reevaluationId, questions.get(0), 4);
        assertEquals(initialResults, get(user, "/api/v2/assessment-attempts/" + initialId).get("results"));
        assertEquals("2", ((Map<?, ?>) get(user, "/api/v2/assessment-attempts/" + initialId)
                .get("answers")).get(questions.get(0).get("competencyCode")).toString());

        Map<String, Object> currentAgain = post(user, "/api/v2/assessment-attempts/reevaluations");
        assertEquals(reevaluationId, currentAgain.get("attemptId"));
        assertEquals(31, ((Map<?, ?>) currentAgain.get("answers")).size());
        post(user, "/api/v2/assessment-attempts/" + reevaluationId + "/complete");

        List<Map<String, Object>> history = (List<Map<String, Object>>) rest.exchange(url("/api/v2/assessment-history"),
                HttpMethod.GET, new HttpEntity<>(user), List.class).getBody();
        assertEquals(2, history.size());
        assertEquals(reevaluationId, history.get(0).get("attemptId"));
        assertEquals(initialId, history.get(1).get("attemptId"));
        assertEquals(6, ((List<?>) history.get(1).get("results")).size());
    }

    private void save(HttpHeaders user, String attemptId, Map<String, Object> question, int level) {
        HttpHeaders headers = new HttpHeaders(user);
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/" + question.get("competencyCode")),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", level), headers), Map.class);
    }

    private Map<String, Object> post(HttpHeaders user, String path) {
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(user), Map.class).getBody();
    }

    private Map<String, Object> get(HttpHeaders user, String path) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(user), Map.class).getBody();
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
