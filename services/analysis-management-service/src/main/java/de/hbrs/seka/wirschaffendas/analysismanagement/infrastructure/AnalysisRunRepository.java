package de.hbrs.seka.wirschaffendas.analysismanagement.infrastructure;

import de.hbrs.seka.wirschaffendas.analysismanagement.domain.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, String> {
}
