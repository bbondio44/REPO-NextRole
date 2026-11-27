package com.nextrole.assessment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/roles")
public class RoleCatalogController {
    private final RoleCatalogService catalog;

    public RoleCatalogController(RoleCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/{roleId}")
    public RoleCatalogService.RoleDescription one(@PathVariable String roleId) {
        return catalog.one(roleId);
    }
}
