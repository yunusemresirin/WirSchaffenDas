package de.hbrs.seka.wirschaffendas.configuration.infrastructure;

import de.hbrs.seka.wirschaffendas.configuration.domain.EngineConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineConfigurationRepository extends JpaRepository<EngineConfiguration, String> {
}
