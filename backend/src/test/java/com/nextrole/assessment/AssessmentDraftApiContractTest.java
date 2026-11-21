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
class AssessmentDraftApiContractTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_draft_test").withUsername("nextrole").withPassword("test_password");

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
    void authenticatedUserCanRecoverDraftAndSaveLevelZeroWithoutCrossUserAccess() {
        assertEquals(HttpStatus.UNAUTHORIZED, rest.getForEntity(url("/api/v2/assessment-catalog/current"), Map.class).getStatusCode());

        HttpHeaders firstUser = register("first-" + UUID.randomUUID() + "@example.test");
        HttpHeaders secondUser = register("second-" + UUID.randomUUID() + "@example.test");

        ResponseEntity<Map> catalogResponse = rest.exchange(url("/api/v2/assessment-catalog/current"), HttpMethod.GET,
                new HttpEntity<>(firstUser), Map.class);
        assertEquals(HttpStatus.OK, catalogResponse.getStatusCode());
        List<Map<String, Object>> questions = (List<Map<String, Object>>) catalogResponse.getBody().get("questions");
        assertEquals(31, questions.size());
        questions.forEach(question -> assertEquals(5, ((List<?>) question.get("options")).size()));

        assertEquals(HttpStatus.NO_CONTENT, rest.exchange(url("/api/v2/assessment-attempts/current"), HttpMethod.GET,
                new HttpEntity<>(secondUser), Void.class).getStatusCode());

        ResponseEntity<Map> created = rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(firstUser), Map.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals("DRAFT", created.getBody().get("status"));
        assertEquals("Q1", created.getBody().get("questionnaireVersion"));
        assertEquals("M1", created.getBody().get("matrixVersion"));
        String attemptId = created.getBody().get("attemptId").toString();

        ResponseEntity<Map> recovered = rest.exchange(url("/api/v2/assessment-attempts/current"), HttpMethod.GET,
                new HttpEntity<>(firstUser), Map.class);
        assertEquals(HttpStatus.OK, recovered.getStatusCode());
        assertEquals(attemptId, recovered.getBody().get("attemptId"));

        ResponseEntity<Map> duplicateStart = rest.exchange(url("/api/v2/assessment-attempts"), HttpMethod.POST,
                new HttpEntity<>(firstUser), Map.class);
        assertEquals(HttpStatus.OK, duplicateStart.getStatusCode());
        assertEquals(attemptId, duplicateStart.getBody().get("attemptId"));

        HttpHeaders saveHeaders = new HttpHeaders(firstUser);
        saveHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> saved = rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/programming-fundamentals"),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", 0), saveHeaders), Map.class);
        assertEquals(HttpStatus.OK, saved.getStatusCode());
        assertEquals(1, ((Number) saved.getBody().get("answeredCount")).intValue());

        ResponseEntity<Map> detail = rest.exchange(url("/api/v2/assessment-attempts/" + attemptId), HttpMethod.GET,
                new HttpEntity<>(firstUser), Map.class);
        Map<String, Object> answers = (Map<String, Object>) detail.getBody().get("answers");
        assertTrue(answers.containsKey("programming-fundamentals"));
        assertEquals(0, ((Number) answers.get("programming-fundamentals")).intValue());
        assertFalse(answers.containsKey("algorithms"));

        assertEquals(HttpStatus.NOT_FOUND, rest.exchange(url("/api/v2/assessment-attempts/" + attemptId), HttpMethod.GET,
                new HttpEntity<>(secondUser), Map.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/algorithms"),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", 2), saveHeaders(secondUser)), Map.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/algorithms"),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", 5), saveHeaders(firstUser)), Map.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rest.exchange(url("/api/v2/assessment-attempts/" + attemptId + "/answers/not-a-competency"),
                HttpMethod.PUT, new HttpEntity<>(Map.of("level", 2), saveHeaders(firstUser)), Map.class).getStatusCode());
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

    private static HttpHeaders saveHeaders(HttpHeaders original) {
        HttpHeaders headers = new HttpHeaders(original);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}
