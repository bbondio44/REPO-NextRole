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
class ApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("nextrole_test").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props){
        props.add("spring.datasource.url",postgres::getJdbcUrl);
        props.add("spring.datasource.username",postgres::getUsername);
        props.add("spring.datasource.password",postgres::getPassword);
        props.add("nextrole.security.jwt-secret",()->Base64.getEncoder().encodeToString(new byte[48]));
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    String url(String path){return "http://localhost:"+port+"/api/v1"+path;}

    @SuppressWarnings("unchecked")
    @Test void userCanRegisterEvaluateGetRecommendationsAndCreateLearningPlan(){
        Map<String,Object> input=Map.of(
          "firstName","Ana","lastName","Prueba","email",UUID.randomUUID()+"@example.test",
          "password","Segura-12345!","confirmPassword","Segura-12345!","acceptTerms",true);
        ResponseEntity<Map> created=rest.postForEntity(url("/auth/register"),input,Map.class);
        assertEquals(HttpStatus.CREATED,created.getStatusCode());
        assertNotNull(created.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        String token=(String)created.getBody().get("accessToken");
        HttpHeaders headers=new HttpHeaders();headers.setBearerAuth(token);

        ResponseEntity<Map> profile=rest.exchange(url("/profile"),HttpMethod.PUT,
           new HttpEntity<>(Map.of("career","Ingeniería en Informática","stage","Intermedio","objective","Primera experiencia IT"),headers),Map.class);
        assertEquals(HttpStatus.OK,profile.getStatusCode());

        ResponseEntity<Map> catalog=rest.exchange(url("/skills"),HttpMethod.GET,new HttpEntity<>(headers),Map.class);
        assertEquals(HttpStatus.OK,catalog.getStatusCode());
        assertFalse(((List<?>)catalog.getBody().get("skills")).isEmpty());

        ResponseEntity<Map> saved=rest.exchange(url("/skills/react/level"),HttpMethod.PUT,
           new HttpEntity<>(Map.of("level",2),headers),Map.class);
        assertEquals(HttpStatus.OK,saved.getStatusCode());

        ResponseEntity<List> scores=rest.exchange(url("/matching/analyze"),HttpMethod.POST,new HttpEntity<>(headers),List.class);
        assertEquals(HttpStatus.OK,scores.getStatusCode());
        assertEquals(8,scores.getBody().size());

        ResponseEntity<Map> selected=rest.exchange(url("/profile/target-role"),HttpMethod.PUT,
           new HttpEntity<>(Map.of("roleId","frontend-dev-jr"),headers),Map.class);
        assertEquals(HttpStatus.OK,selected.getStatusCode());

        ResponseEntity<Map> gap=rest.exchange(url("/skill-gap/analyze"),HttpMethod.POST,
           new HttpEntity<>(Map.of("roleId","frontend-dev-jr"),headers),Map.class);
        assertEquals(HttpStatus.OK,gap.getStatusCode());
        assertFalse(((List<?>)gap.getBody().get("gaps")).isEmpty());

        ResponseEntity<Map> plan=rest.exchange(url("/learning/plans"),HttpMethod.POST,
           new HttpEntity<>(Map.of("roleId","frontend-dev-jr"),headers),Map.class);
        assertEquals(HttpStatus.OK,plan.getStatusCode());
        assertEquals("frontend-dev-jr",plan.getBody().get("roleId"));
    }
}
