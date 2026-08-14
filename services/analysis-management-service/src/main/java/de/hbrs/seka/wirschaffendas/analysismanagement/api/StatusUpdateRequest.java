package de.hbrs.seka.wirschaffendas.analysismanagement.api;

import de.hbrs.seka.wirschaffendas.analysismanagement.domain.AnalysisStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull AnalysisStatus status,
        String message) {
}
