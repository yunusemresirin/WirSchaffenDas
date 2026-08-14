package de.hbrs.seka.wirschaffendas.analysismanagement.infrastructure;

import de.hbrs.seka.wirschaffendas.analysismanagement.domain.AlgorithmName;
import de.hbrs.seka.wirschaffendas.analysismanagement.domain.AnalysisResult;

public record PreviousResult(AlgorithmName algorithm, AnalysisResult result) {
}
