package de.hbrs.seka.wirschaffendas.configuration.api;

import de.hbrs.seka.wirschaffendas.configuration.application.ConfigurationApplicationService;
import de.hbrs.seka.wirschaffendas.configuration.domain.EngineConfiguration;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/configurations")
public class ConfigurationController {

    private final ConfigurationApplicationService service;

    public ConfigurationController(ConfigurationApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EngineConfigurationResponse> create(
            @Valid @RequestBody CreateConfigurationRequest request) {
        EngineConfiguration created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/configurations/" + created.getConfigurationId()))
                .body(EngineConfigurationResponse.from(created));
    }

    @GetMapping("/{configurationId}")
    public EngineConfigurationResponse get(@PathVariable String configurationId) {
        return EngineConfigurationResponse.from(service.get(configurationId));
    }
}
