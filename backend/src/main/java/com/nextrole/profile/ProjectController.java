package com.nextrole.profile;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController
@RequestMapping("/api/v1/profile/projects")
public class ProjectController {
    private final ProfileProjectRepository repository;
    public ProjectController(ProfileProjectRepository repository){this.repository=repository;}
    public record ProjectInput(@NotBlank String name,String description,@Pattern(regexp="^https://.*$",message="Usá una URL HTTPS") String url,
                               @NotBlank String projectType) {}
    public record ProjectView(UUID id,String name,String description,String url,String projectType,Instant createdAt) {}
    private ProjectView view(ProfileProject p){return new ProjectView(p.id,p.name,p.description,p.url,p.projectType,p.createdAt);}
    @GetMapping @Transactional(readOnly=true)
    public List<ProjectView> list(){return repository.findByUserId(CurrentUser.id()).stream().map(this::view).toList();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
    public ProjectView add(@Valid @RequestBody ProjectInput input){
        ProfileProject p=new ProfileProject(UUID.randomUUID(),CurrentUser.id(),input.name(),input.description(),input.url(),input.projectType(),Instant.now());
        return view(repository.save(p));
    }
    @DeleteMapping("/{projectId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void delete(@PathVariable UUID projectId){
        ProfileProject p=repository.findById(projectId).filter(v->v.userId.equals(CurrentUser.id())).orElseThrow(()->ApiProblem.notFound("Proyecto"));
        repository.delete(p);
    }
}
