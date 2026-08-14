# WirSchaffenDas – Architekturentwurf

## 1. Ziel

Dieses Dokument konkretisiert die in `requirements.md` und `ddd.md` festgelegten Anforderungen und Bounded Contexts in eine umsetzbare Microservice-Architektur. Es werden keine neuen fachlichen Anforderungen eingeführt.

## 2. Service-Baseline

- `configuration-service`
- `analysis-management-service`
- `fluid-analysis-service`
- `thermal-analysis-service`
- `electrical-analysis-service`
- `engine-management-analysis-service`

## 3. Choreographie

Version 1 verwendet eine sequenzielle Choreographie:

```text
Client -> Analysis Management -> Fluid -> Thermal -> Electrical -> Engine Management
```

`Analysis Management` erzeugt den Analyse-Durchlauf und startet nur den Anchor-Algorithmus `FLUID`. Anschließend gibt jeder erfolgreich abgeschlossene Analyse-Service die Verarbeitung selbst an den nächsten Service weiter.

Jeder Analyse-Service:

1. meldet `RUNNING` proaktiv an Analysis Management,
2. simuliert seine Analyse asynchron,
3. meldet `READY/OK` oder `FAILED/FAILED`,
4. startet bei Erfolg den nächsten Analyse-Service.

Der Engine-Management-Service berücksichtigt die vorherigen Resultate und bildet das Ende der Choreographie.

## 4. REST-Verträge

### Extern

```http
POST /api/configurations
GET  /api/configurations/{configurationId}
POST /api/analyses
GET  /api/analyses/{analysisId}
POST /api/analyses/{analysisId}/algorithms/{algorithm}/retry
```

### Intern

Jeder Analyse-Service:

```http
POST /internal/analyses
```

Callbacks an Analysis Management:

```http
PUT /internal/analyses/{analysisId}/algorithms/{algorithm}/status
PUT /internal/analyses/{analysisId}/algorithms/{algorithm}/result
```

## 5. Analysis Command

```text
AnalysisCommand
├── analysisId
├── configuration
└── previousResults[]
```

Der Command wird entlang der Choreographie weitergegeben. Jeder erfolgreiche Service ergänzt sein eigenes Resultat, sodass Engine Management die Resultate der vorherigen Algorithmen berücksichtigen kann.

## 6. Retry

Ein Retry ist nur für einen Algorithmus im Status `FAILED` erlaubt.

```text
Client -> Analysis Management -> failed Analysis Service
```

Bereits erfolgreiche Vorgänger werden nicht erneut ausgeführt. Wenn der wiederholte Algorithmus erfolgreich ist, wird die Choreographie ab diesem Punkt mit den noch ausstehenden nachfolgenden Analysen fortgesetzt. Damit kann ein durch einen temporär nicht erreichbaren Service unterbrochener Ablauf nach Wiederherstellung weiterlaufen.

## 7. Circuit Breaker

Service-zu-Service-Aufrufe werden mit Resilience4j abgesichert.

Beispiele:

```text
Analysis Management --Circuit Breaker--> Fluid / Retry-Ziel
Fluid              --Circuit Breaker--> Thermal
Thermal            --Circuit Breaker--> Electrical
Electrical         --Circuit Breaker--> Engine Management
```

Ist der nächste Service nicht erreichbar, markiert der Fallback den betroffenen nächsten Algorithmus als `FAILED`. Der aufrufende Service bleibt erreichbar und die Analyse kann später über Retry fortgesetzt werden.

## 8. Overall Result

- Sobald ein Algorithmus `FAILED` ist, lautet das Gesamtergebnis `FAILED`.
- Solange kein Fehler vorliegt, aber noch Algorithmen `PENDING` oder `RUNNING` sind, bleibt `overallResult = null`.
- Wenn alle vier Algorithmen `READY/OK` sind, lautet das Gesamtergebnis `OK`.

## 9. Datenhoheit

- `configuration-service`: persistiert `EngineConfiguration`
- `analysis-management-service`: persistiert `AnalysisRun`, Status und Resultate
- vier Analyse-Services: stateless

Analyse-Services greifen nicht direkt auf fremde Datenbanken zu.

## 10. Implementierungsstruktur

```text
api/             REST-Endpunkte und DTOs
application/     fachlicher Ablauf / AnalysisWorker
domain/          lokales Domänenmodell
infrastructure/  REST-Clients, Persistenz, Circuit Breaker
```

## 11. Zentrale Entwurfsentscheidungen

- fachliche Service-Grenzen statt technischer Layer (`Wrong Cut` vermeiden)
- REST/HTTP für Version 1
- Choreographie statt zentraler Ablauf-Orchestration
- asynchrone Algorithmusausführung für Responsiveness
- stateless Analyse-Services
- Circuit Breaker mit Resilience4j
- Docker/Docker Compose als Deployment-Ziel
