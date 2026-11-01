package com.nextrole.matching;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {
    private final MatchingService service;
    public MatchingController(MatchingService service){this.service=service;}
    @PostMapping("/analyze") public List<MatchingService.SuggestedRole> analyze(){return service.analyze();}
    @GetMapping("/results/latest") public List<MatchingService.SuggestedRole> latest(){return service.latest();}
}
