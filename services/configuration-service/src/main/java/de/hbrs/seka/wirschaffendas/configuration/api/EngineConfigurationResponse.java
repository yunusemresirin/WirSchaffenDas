package de.hbrs.seka.wirschaffendas.configuration.api;

import de.hbrs.seka.wirschaffendas.configuration.domain.EngineConfiguration;

public record EngineConfigurationResponse(
        String configurationId,
        String oilSystem,
        String fuelSystem,
        String coolingSystem,
        String electricalSystem,
        String engineManagementSystem) {

    public static EngineConfigurationResponse from(EngineConfiguration configuration) {
        return new EngineConfigurationResponse(
                configuration.getConfigurationId(),
                configuration.getOilSystem(),
                configuration.getFuelSystem(),
                configuration.getCoolingSystem(),
                configuration.getElectricalSystem(),
                configuration.getEngineManagementSystem());
    }
}
