package de.hbrs.seka.wirschaffendas.electrical.api;

import java.util.List;
import java.util.Map;

public record AnalysisCommand(
        String analysisId,
        Map<String, String> configuration,
        List<Map<String, String>> previousResults) {
}
