package com.nextrole.interests;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController
@RequestMapping("/api/v1/interests")
public class InterestsController {
    private final InterestAnswerRepository repository;
    public InterestsController(InterestAnswerRepository repository){this.repository=repository;}
    public static final Map<String,String> DIMENSIONS=Map.of(
       "frontend","Diseñar interfaces y experiencias web",
       "backend","Construir servicios y lógica de negocio",
       "quality","Probar y mejorar la calidad del software",
       "data","Analizar, transformar e interpretar datos",
       "cloud","Automatizar infraestructura y sistemas",
       "security","Investigar y prevenir riesgos de seguridad");
    public record InterestQuestion(String id,String label,int min,int max) {}
    public record Answer(@NotBlank String dimension,@NotNull @Min(0) @Max(4) Integer score){}
    public record AnswersRequest(@NotEmpty List<@Valid Answer> answers){}
    @GetMapping("/questions") public List<InterestQuestion> questions(){return DIMENSIONS.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(x->new InterestQuestion(x.getKey(),x.getValue(),0,4)).toList();}
    @GetMapping("/results") @Transactional(readOnly=true)
    public Map<String,Integer> results(){Map<String,Integer> data=new LinkedHashMap<>();for(InterestAnswer a:repository.findByUserId(CurrentUser.id()))data.put(a.dimension,a.score);return data;}
    @PostMapping("/answers") @Transactional
    public Map<String,Integer> save(@Valid @RequestBody AnswersRequest input){
        UUID uid=CurrentUser.id();Set<String> unique=new HashSet<>();
        for(Answer a:input.answers()){
            if(!DIMENSIONS.containsKey(a.dimension())||!unique.add(a.dimension()))throw ApiProblem.invalid("Interés duplicado o desconocido");
            InterestAnswer entity=repository.findByUserIdAndDimension(uid,a.dimension()).orElseGet(()->new InterestAnswer(UUID.randomUUID(),uid,a.dimension(),a.score(),Instant.now()));
            entity.score=a.score();entity.updatedAt=Instant.now();repository.save(entity);
        }
        return results();
    }
}
