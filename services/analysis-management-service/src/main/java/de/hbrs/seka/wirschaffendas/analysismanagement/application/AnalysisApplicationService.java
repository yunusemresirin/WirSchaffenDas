package de.hbrs.seka.wirschaffendas.analysismanagement.application;

import de.hbrs.seka.wirschaffendas.analysismanagement.domain.*;
import de.hbrs.seka.wirschaffendas.analysismanagement.infrastructure.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AnalysisApplicationService {

    private final AnalysisRunRepository repository;
    private final ConfigurationServiceClient configurationClient;
    private final AnalysisServiceStarter serviceStarter;

    public AnalysisApplicationService(
            AnalysisRunRepository repository,
            ConfigurationServiceClient configurationClient,
            AnalysisServiceStarter serviceStarter) {
        this.repository = repository;
        this.configurationClient = configurationClient;
        this.serviceStarter = serviceStarter;
    }

    public AnalysisRun start(String configurationId) {
        ConfigurationSnapshot configuration = configurationClient.get(configurationId);
        AnalysisRun run = AnalysisRun.start("A-" + UUID.randomUUID(), configurationId);

        AlgorithmExecution fluid = run.execution(AlgorithmName.FLUID);
        fluid.updateStatus(AnalysisStatus.RUNNING, null);
        repository.save(run);

        try {
            serviceStarter.start(
                    AlgorithmName.FLUID,
                    new AnalysisCommand(run.getAnalysisId(), configuration, List.of()));
        } catch (RestClientException exception) {
            fluid.updateStatus(AnalysisStatus.FAILED, "Fluid analysis service unavailable");
            run.recalculateOverallResult();
        }

        return repository.save(run);
    }

    @Transactional(readOnly = true)
    public AnalysisRun get(String analysisId) {
        return find(analysisId);
    }

    public AnalysisRun updateStatus(
            String analysisId,
            AlgorithmName algorithm,
            AnalysisStatus status,
            String message) {
        AnalysisRun run = find(analysisId);
        run.execution(algorithm).updateStatus(status, message);
        run.recalculateOverallResult();
        return repository.save(run);
    }

    public AnalysisRun updateResult(
            String analysisId,
            AlgorithmName algorithm,
            AnalysisStatus status,
            AnalysisResult result,
            String message) {
        AnalysisRun run = find(analysisId);
        run.execution(algorithm).updateResult(status, result, message);
        run.recalculateOverallResult();
        return repository.save(run);
    }

    public AnalysisRun retry(String analysisId, AlgorithmName algorithm) {
        AnalysisRun run = find(analysisId);
        AlgorithmExecution execution = run.execution(algorithm);

        if (execution.getStatus() != AnalysisStatus.FAILED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only failed algorithms can be retried");
        }

        ConfigurationSnapshot configuration = configurationClient.get(run.getConfigurationId());
        List<PreviousResult> previousResults = run.getExecutions().stream()
                .filter(item -> item.getResult() != null)
                .map(item -> new PreviousResult(item.getAlgorithm(), item.getResult()))
                .toList();

        execution.updateStatus(AnalysisStatus.RUNNING, null);
        repository.save(run);

        try {
            serviceStarter.start(
                    algorithm,
                    new AnalysisCommand(run.getAnalysisId(), configuration, previousResults));
        } catch (RestClientException exception) {
            execution.updateStatus(AnalysisStatus.FAILED, algorithm + " service unavailable");
        }

        run.recalculateOverallResult();
        return repository.save(run);
    }

    private AnalysisRun find(String analysisId) {
        return repository.findById(analysisId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Analysis not found"));
    }
}
