package com.nextrole.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.nextrole.data.AssessmentRole;
import com.nextrole.data.AssessmentRoleRepository;
import org.junit.jupiter.api.Test;

class RoleCatalogServiceTest {
    private final AssessmentRoleRepository repository = mock(AssessmentRoleRepository.class);
    private final RoleCatalogService service = new RoleCatalogService(repository);

    @Test
    void returnsProfessionalRoleNameAndDescriptionByStableId() {
        AssessmentRole role = role("qa-jr", "QA", "Descripción oficial.");
        when(repository.findById("qa-jr")).thenReturn(Optional.of(role));

        assertEquals(new RoleCatalogService.RoleDescription("qa-jr", "QA", "Descripción oficial."),
                service.one("qa-jr"));
    }

    @Test
    void returnsStableSortedCatalog() {
        when(repository.findAll()).thenReturn(List.of(
                role("qa-jr", "QA", "QA."), role("backend-dev-jr", "Backend Developer", "Backend.")));

        assertEquals(List.of("backend-dev-jr", "qa-jr"),
                service.all().stream().map(RoleCatalogService.RoleDescription::roleId).toList());
    }

    private static AssessmentRole role(String id, String name, String description) {
        return new AssessmentRole(id, name, description);
    }
}
