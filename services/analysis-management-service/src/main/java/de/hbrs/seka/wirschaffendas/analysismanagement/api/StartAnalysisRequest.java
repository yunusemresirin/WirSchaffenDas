package de.hbrs.seka.wirschaffendas.analysismanagement.api;

import jakarta.validation.constraints.NotBlank;

public record StartAnalysisRequest(@NotBlank String configurationId) {
}
