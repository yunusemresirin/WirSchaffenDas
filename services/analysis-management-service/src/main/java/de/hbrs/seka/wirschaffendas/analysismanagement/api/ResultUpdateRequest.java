package de.hbrs.seka.wirschaffendas.analysismanagement.api;

import de.hbrs.seka.wirschaffendas.analysismanagement.domain.AnalysisResult;
import de.hbrs.seka.wirschaffendas.analysismanagement.domain.AnalysisStatus;
import jakarta.validation.constraints.NotNull;

public record ResultUpdateRequest(
        @NotNull AnalysisStatus status,
        @NotNull AnalysisResult result,
        String message) {
}
