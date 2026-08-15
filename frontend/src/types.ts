export type AnalysisStatus = 'PENDING' | 'RUNNING' | 'READY' | 'FAILED';
export type AnalysisResult = 'OK' | 'FAILED';
export type AlgorithmName =
  | 'FLUID'
  | 'THERMAL'
  | 'ELECTRICAL'
  | 'ENGINE_MANAGEMENT';

export interface EngineConfiguration {
  configurationId: string;
  oilSystem: string;
  fuelSystem: string;
  coolingSystem: string;
  electricalSystem: string;
  engineManagementSystem: string;
}

export type CreateConfigurationRequest = Omit<
  EngineConfiguration,
  'configurationId'
>;

export interface AlgorithmExecution {
  algorithm: AlgorithmName;
  status: AnalysisStatus;
  result: AnalysisResult | null;
  message: string | null;
}

export interface AnalysisResponse {
  analysisId: string;
  configurationId: string;
  overallResult: AnalysisResult | null;
  algorithms: AlgorithmExecution[];
}

export type CircuitBreakerState =
  | 'CLOSED'
  | 'OPEN'
  | 'HALF_OPEN'
  | 'DISABLED'
  | 'FORCED_OPEN'
  | 'METRICS_ONLY'
  | 'UNKNOWN';

export interface CircuitBreakerSnapshot {
  state: CircuitBreakerState;
  failureRate?: string | number;
  bufferedCalls?: number;
  failedCalls?: number;
  notPermittedCalls?: number;
}

export type ServiceKey =
  | 'configuration'
  | 'analysis-management'
  | 'fluid'
  | 'thermal'
  | 'electrical'
  | 'engine-management';

export interface ServiceHealth {
  key: ServiceKey;
  reachable: boolean;
  actuatorStatus: string;
  circuitBreaker: CircuitBreakerSnapshot | null;
  checkedAt: string;
}
