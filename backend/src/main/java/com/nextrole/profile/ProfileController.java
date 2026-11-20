package com.nextrole.profile;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final ProfileRepository profiles;
    private final RoleRepository roles;
    public ProfileController(ProfileRepository profiles,RoleRepository roles){this.profiles=profiles;this.roles=roles;}
    public record ProfileInput(@NotBlank String career,@NotBlank String stage,@NotBlank String objective,String university,Integer graduationYear) {}
    public record ProfileView(String career,String stage,String objective,String university,Integer graduationYear,String selectedRoleId) {}
    public record TargetRoleRequest(@NotBlank String roleId) {}
    private ProfileView view(Profile p){return new ProfileView(p.career,p.stage,p.objective,p.university,p.graduationYear,p.selectedRoleId);}
    @GetMapping
    @Transactional(readOnly=true)
    public ResponseEntity<ProfileView> get(){return profiles.findById(CurrentUser.id()).map(p->ResponseEntity.ok(view(p))).orElseGet(()->ResponseEntity.noContent().build());}
    @PutMapping
    @Transactional
    public ProfileView save(@Valid @RequestBody ProfileInput r){
        UUID userId=CurrentUser.id();
        Profile p=profiles.findById(userId).orElseGet(()->new Profile(userId,null,null,null,null,null,null,Instant.now()));
        p.career=r.career().trim();p.stage=r.stage().trim();p.objective=r.objective().trim();
        p.university=r.university();p.graduationYear=r.graduationYear();p.updatedAt=Instant.now();
        return view(profiles.save(p));
    }
    @PutMapping("/target-role")
    @Transactional
    public Map<String,String> setRole(@Valid @RequestBody TargetRoleRequest r){
        Role role=roles.findById(r.roleId()).filter(x->Boolean.TRUE.equals(x.active)).orElseThrow(()->ApiProblem.notFound("Rol"));
        Profile p=profiles.findById(CurrentUser.id()).orElseThrow(()->ApiProblem.invalid("Primero completá tu perfil"));
        p.selectedRoleId=role.id;p.updatedAt=Instant.now();profiles.save(p);
        return Map.of("roleId",role.id);
    }
    @GetMapping("/target-role")
    @Transactional(readOnly=true)
    public Map<String,String> selected(){return profiles.findById(CurrentUser.id()).map(p->p.selectedRoleId==null?Map.<String,String>of():Map.of("roleId",p.selectedRoleId)).orElse(Map.of());}
}
