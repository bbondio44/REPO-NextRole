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
class AssessmentHistoryApiContractTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_history_contract").withUsername("nextrole").withPassword("test_password");

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
    void reevaluationRecoversOneDraftAndDiscardIsAnOwnedNonDestructiveTransition() {
        assertEquals(HttpStatus.UNAUTHORIZED, rest.postForEntity(url("/api/v2/assessment-attempts/reevaluations"), null, Map.class).getStatusCode());
        HttpHeaders user = register("history-contract-" + UUID.randomUUID() + "@example.test");
        ResponseEntity<Map> first = rest.exchange(url("/api/v2/assessment-attempts/reevaluations"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.CREATED, first.getStatusCode());
        String attemptId = first.getBody().get("attemptId").toString();
        assertNull(first.getBody().get("sourceAttemptId"));

        ResponseEntity<Map> recovered = rest.exchange(url("/api/v2/assessment-attempts/reevaluations"), HttpMethod.POST,
                new HttpEntity<>(user), Map.class);
        assertEquals(HttpStatus.OK, recovered.getStatusCode());
        assertEquals(attemptId, recovered.getBody().get("attemptId"));

        ResponseEntity<Void> discarded = rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/discard"),
                HttpMethod.POST, new HttpEntity<>(user), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, discarded.getStatusCode());
        assertEquals("DISCARDED", rest.exchange(url("/api/v2/assessment-attempts/" + attemptId), HttpMethod.GET,
                new HttpEntity<>(user), Map.class).getBody().get("status"));
        assertEquals(HttpStatus.CONFLICT, rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/discard"),
                HttpMethod.POST, new HttpEntity<>(user), Map.class).getStatusCode());
        List<?> history = rest.exchange(url("/api/v2/assessment-history"), HttpMethod.GET,
                new HttpEntity<>(user), List.class).getBody();
        assertNotNull(history);
        assertTrue(history.isEmpty());
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
