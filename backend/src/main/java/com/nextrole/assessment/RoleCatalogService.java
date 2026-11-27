package com.nextrole.assessment;

import java.util.Comparator;
import java.util.List;

import com.nextrole.common.ApiProblem;
import com.nextrole.data.AssessmentRole;
import com.nextrole.data.AssessmentRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleCatalogService {
    private final AssessmentRoleRepository roles;

    public RoleCatalogService(AssessmentRoleRepository roles) {
        this.roles = roles;
    }

    @Transactional(readOnly = true)
    public RoleDescription one(String roleId) {
        AssessmentRole role = roles.findById(roleId).orElseThrow(() -> ApiProblem.notFound("Rol"));
        return new RoleDescription(role.id, role.name, role.description);
    }

    @Transactional(readOnly = true)
    public List<RoleDescription> all() {
        return roles.findAll().stream().sorted(Comparator.comparing(role -> role.id))
                .map(role -> new RoleDescription(role.id, role.name, role.description)).toList();
    }

    public record RoleDescription(String roleId, String name, String description) {}
}
