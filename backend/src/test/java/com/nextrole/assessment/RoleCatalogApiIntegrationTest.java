package com.nextrole.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.nextrole.explainability.ExplainabilityProperties;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleCatalogApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("explainability_role_catalog").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        props.add("spring.datasource.username", POSTGRES::getUsername);
        props.add("spring.datasource.password", POSTGRES::getPassword);
        props.add("nextrole.security.jwt-secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
        props.add("nextrole.explainability.openrouter-api-key", () -> "");
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired ExplainabilityProperties explainabilityProperties;

    @Test
    @SuppressWarnings("unchecked")
    void roleCatalogEndpointRequiresAuthenticationAndReturnsApprovedV2CatalogText() {
        assertEquals("", explainabilityProperties.getOpenrouterApiKey());
        assertEquals("baidu/ernie-5.5-flash-preview:free", explainabilityProperties.getModel());
        assertEquals(HttpStatus.UNAUTHORIZED,
                rest.getForEntity(url("/api/v2/roles/qa-jr"), Map.class).getStatusCode());
        HttpHeaders headers = register("role-catalog-" + UUID.randomUUID() + "@example.test");
        String[][] roles = {
                {"backend-dev-jr", "Backend Developer", "Se encarga de desarrollar y mantener la lógica interna de las aplicaciones. Sus responsabilidades incluyen implementar reglas de negocio, desarrollar APIs, gestionar bases de datos e integrar servicios externos. Contribuye a que los sistemas procesen información de manera segura, eficiente y confiable."},
                {"frontend-dev-jr", "Frontend Developer", "Se encarga de desarrollar las interfaces visuales e interactivas de las aplicaciones. Sus responsabilidades incluyen construir componentes, integrar APIs y gestionar el comportamiento de la interfaz. Busca garantizar una experiencia de usuario intuitiva, accesible, eficiente y adaptable a diferentes dispositivos."},
                {"fullstack-dev-jr", "Full Stack Developer", "Participa en el desarrollo integral de aplicaciones, trabajando tanto en sus interfaces como en su lógica interna. Sus responsabilidades incluyen construir funcionalidades, desarrollar e integrar APIs, gestionar bases de datos y conectar los distintos componentes del sistema. Su visión integral le permite comprender cómo interactúan las diferentes capas de una aplicación."},
                {"qa-jr", "QA", "Se encarga de contribuir a la calidad del software mediante la planificación, el diseño y la ejecución de pruebas. Sus responsabilidades incluyen verificar funcionalidades, identificar y documentar defectos, validar correcciones y comprobar que las aplicaciones cumplan sus requisitos. Colabora con los equipos de desarrollo para mejorar la confiabilidad del producto y prevenir errores."},
                {"data-analyst-jr", "Data Analyst", "Se encarga de transformar datos en información útil para la toma de decisiones. Sus responsabilidades incluyen obtener, limpiar, procesar y analizar datos, identificar patrones e interpretar indicadores de negocio. Utiliza técnicas estadísticas y herramientas de visualización para comunicar resultados, fundamentar conclusiones y contribuir a la resolución de problemas."},
                {"cloud-jr", "Cloud", "Se encarga de implementar, administrar y mantener infraestructura y servicios en la nube. Sus responsabilidades incluyen configurar recursos de procesamiento, almacenamiento y redes, automatizar tareas operativas y monitorear el funcionamiento de los sistemas. Contribuye a que las soluciones sean seguras, confiables, escalables y eficientes en el uso de recursos."}
        };
        for (String[] expected : roles) {
            ResponseEntity<Map> response = rest.exchange(url("/api/v2/roles/" + expected[0]), HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(Map.of("roleId", expected[0], "name", expected[1], "description", expected[2]), response.getBody());
        }
        ResponseEntity<Map> missing = rest.exchange(url("/api/v2/roles/not-a-role"), HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
    }

    private HttpHeaders register(String email) {
        Map<String, Object> payload = Map.of("firstName", "Test", "lastName", "User", "email", email,
                "password", "Segura-12345!", "confirmPassword", "Segura-12345!", "acceptTerms", true);
        ResponseEntity<Map> response = rest.postForEntity(url("/api/v1/auth/register"), payload, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) response.getBody().get("accessToken"));
        return headers;
    }

    private String url(String path) { return "http://localhost:" + port + path; }
}
