package de.hbrs.seka.wirschaffendas.analysismanagement.infrastructure;

public record ConfigurationSnapshot(
        String configurationId,
        String oilSystem,
        String fuelSystem,
        String coolingSystem,
        String electricalSystem,
        String engineManagementSystem) {
}
