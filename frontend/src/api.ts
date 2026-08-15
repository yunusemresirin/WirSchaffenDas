import type {
  AnalysisResponse,
  AlgorithmName,
  CircuitBreakerSnapshot,
  CircuitBreakerState,
  CreateConfigurationRequest,
  EngineConfiguration,
  ServiceHealth,
  ServiceKey,
} from './types';

async function requestJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<T>;
}

export function createConfiguration(
  request: CreateConfigurationRequest,
): Promise<EngineConfiguration> {
  return requestJson('/api/configurations', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function loadConfiguration(
  configurationId: string,
): Promise<EngineConfiguration> {
  return requestJson(`/api/configurations/${encodeURIComponent(configurationId)}`);
}

export function startAnalysis(
  configurationId: string,
): Promise<AnalysisResponse> {
  return requestJson('/api/analyses', {
    method: 'POST',
    body: JSON.stringify({ configurationId }),
  });
}

export function loadAnalysis(analysisId: string): Promise<AnalysisResponse> {
  return requestJson(`/api/analyses/${encodeURIComponent(analysisId)}`);
}

export function retryAlgorithm(
  analysisId: string,
  algorithm: AlgorithmName,
): Promise<AnalysisResponse> {
  return requestJson(
    `/api/analyses/${encodeURIComponent(analysisId)}/algorithms/${algorithm}/retry`,
    { method: 'POST' },
  );
}

const serviceKeys: ServiceKey[] = [
  'configuration',
  'analysis-management',
  'fluid',
  'thermal',
  'electrical',
  'engine-management',
];

const breakerStates = new Set<CircuitBreakerState>([
  'CLOSED',
  'OPEN',
  'HALF_OPEN',
  'DISABLED',
  'FORCED_OPEN',
  'METRICS_ONLY',
]);

function findCircuitBreaker(value: unknown): CircuitBreakerSnapshot | null {
  if (!value || typeof value !== 'object') {
    return null;
  }

  const record = value as Record<string, unknown>;
  const state = record.state;

  if (typeof state === 'string' && breakerStates.has(state as CircuitBreakerState)) {
    return {
      state: state as CircuitBreakerState,
      failureRate: record.failureRate as string | number | undefined,
      bufferedCalls:
        typeof record.bufferedCalls === 'number' ? record.bufferedCalls : undefined,
      failedCalls:
        typeof record.failedCalls === 'number' ? record.failedCalls : undefined,
      notPermittedCalls:
        typeof record.notPermittedCalls === 'number'
          ? record.notPermittedCalls
          : undefined,
    };
  }

  for (const child of Object.values(record)) {
    const result = findCircuitBreaker(child);
    if (result) {
      return result;
    }
  }

  return null;
}

async function fetchHealth(key: ServiceKey): Promise<ServiceHealth> {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), 1500);

  try {
    const response = await fetch(`/monitor/${key}/actuator/health`, {
      signal: controller.signal,
      cache: 'no-store',
    });

    const text = await response.text();
    const payload = text ? (JSON.parse(text) as Record<string, unknown>) : {};

    // A Resilience4j OPEN breaker intentionally makes Actuator health DOWN/503.
    // Receiving an HTTP response still proves that the source service itself is reachable.
    return {
      key,
      reachable: true,
      actuatorStatus:
        typeof payload.status === 'string' ? payload.status : response.ok ? 'UP' : 'DOWN',
      circuitBreaker: findCircuitBreaker(payload),
      checkedAt: new Date().toISOString(),
    };
  } catch {
    return {
      key,
      reachable: false,
      actuatorStatus: 'UNREACHABLE',
      circuitBreaker: null,
      checkedAt: new Date().toISOString(),
    };
  } finally {
    window.clearTimeout(timeout);
  }
}

export async function loadSystemHealth(): Promise<ServiceHealth[]> {
  return Promise.all(serviceKeys.map(fetchHealth));
}
