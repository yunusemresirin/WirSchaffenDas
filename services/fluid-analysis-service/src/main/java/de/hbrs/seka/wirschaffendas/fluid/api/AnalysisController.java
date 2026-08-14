package de.hbrs.seka.wirschaffendas.fluid.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/analyses")
public class AnalysisController {

    @PostMapping
    public ResponseEntity<Void> start(@RequestBody AnalysisCommand command) {
        // TODO next step: run FLUID asynchronously, report status/result,
        // and hand over to the next service in the choreography.
        return ResponseEntity.accepted().build();
    }
}
