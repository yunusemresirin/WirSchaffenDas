# WirSchaffenDas – Architekturentwurf

## 1. Ziel

Dieses Dokument konkretisiert die in `requirements.md` und `ddd.md` festgelegten Anforderungen und Bounded Contexts in eine umsetzbare Microservice-Architektur.

Die Architektur bleibt bewusst klein und prüfungsorientiert. Es werden keine neuen fachlichen Anforderungen eingeführt.

## 2. Bausteinsicht – High Level

```text
                           ┌───────────────────────┐
                           │        Postman        │
                           └───────────┬───────────┘
                                       │
                         ┌─────────────┴─────────────┐
                         │                           │
                         ▼                           ▼
              ┌────────────────────┐      ┌────────────────────────┐
              │ configuration-     │      │ analysis-management-    │
              │ service            │      │ service                 │
              └─────────┬──────────┘      └───────────┬────────────┘
                        │                             │
                        ▼                             │ Start Anchor
              ┌────────────────────┐                 ▼
              │ Configuration DB   │       ┌────────────────────────┐
              └────────────────────┘       │ fluid-analysis-service │
                                           └───────────┬────────────┘
                                                       │
                                                       ▼
                                           ┌────────────────────────┐
                                           │ thermal-analysis-       │
                                           │ service                 │
                                           └───────────┬────────────┘
                                                       │
                                                       ▼
                                           ┌────────────────────────┐
                                           │ electrical-analysis-    │
                                           │ service                 │
                                           └───────────┬────────────┘
                                                       │
                                                       ▼
                                           ┌────────────────────────┐
                                           │ engine-management-      │
                                           │ analysis-service        │
                                           └────────────────────────┘

              Alle Analyse-Services ── Status / Result ──► analysis-management-service
```

## 3. Verantwortlichkeiten der Services

| Service | Hauptverantwortung |
|---|---|
| `configuration-service` | Engine-Konfiguration anlegen, validieren, speichern und lesen |
| `analysis-management-service` | Analyse-Durchlauf anlegen, Status/Resultate sammeln, Overall Result berechnen, Retry anbieten |
| `fluid-analysis-service` | Fluid-bezogene Konfiguration analysieren |
| `thermal-analysis-service` | Thermischen Bereich simuliert analysieren |
| `electrical-analysis-service` | Elektrischen Bereich simuliert analysieren |
| `engine-management-analysis-service` | Engine-Management-Analyse unter Einbezug vorheriger Ergebnisse |

## 4. Externe REST-Schnittstellen

### 4.1 Configuration Service

#### Konfiguration anlegen

```http
POST /api/configurations
```

Beispiel-Request:

```json
{
  "oilSystem": "STANDARD",
  "fuelSystem": "PREMIUM",
  "coolingSystem": "STANDARD",
  "electricalSystem": "PREMIUM",
  "engineManagementSystem": "ADVANCED"
}
```

Beispiel-Response:

```http
201 Created
```

```json
{
  "configurationId": "C-100",
  "oilSystem": "STANDARD",
  "fuelSystem": "PREMIUM",
  "coolingSystem": "STANDARD",
  "electricalSystem": "PREMIUM",
  "engineManagementSystem": "ADVANCED"
}
```

#### Konfiguration lesen

```http
GET /api/configurations/{configurationId}
```

Mögliche Antworten:

- `200 OK` – Konfiguration gefunden
- `404 Not Found` – unbekannte ID

### 4.2 Analysis Management Service

#### Analyse starten

```http
POST /api/analyses
```

Request:

```json
{
  "configurationId": "C-100"
}
```

Response:

```http
202 Accepted
```

```json
{
  "analysisId": "A-100",
  "status": "RUNNING"
}
```

Der Service erzeugt den Analyse-Durchlauf und startet ausschließlich den Anchor-Algorithmus `FLUID`.

#### Analyse abfragen

```http
GET /api/analyses/{analysisId}
```

Beispiel-Response:

```json
{
  "analysisId": "A-100",
  "configurationId": "C-100",
  "overallResult": null,
  "algorithms": [
    {
      "algorithm": "FLUID",
      "status": "READY",
      "result": "OK"
    },
    {
      "algorithm": "THERMAL",
      "status": "RUNNING",
      "result": null
    },
    {
      "algorithm": "ELECTRICAL",
      "status": "PENDING",
      "result": null
    },
    {
      "algorithm": "ENGINE_MANAGEMENT",
      "status": "PENDING",
      "result": null
    }
  ]
}
```

#### Fehlgeschlagenen Algorithmus erneut starten

```http
POST /api/analyses/{analysisId}/algorithms/{algorithm}/retry
```

Beispiel:

```http
POST /api/analyses/A-100/algorithms/THERMAL/retry
```

Mögliche Antworten:

- `202 Accepted` – Retry wurde gestartet
- `404 Not Found` – Analyse oder Algorithmus unbekannt
- `409 Conflict` – Algorithmus befindet sich nicht im Status `FAILED`

## 5. Interne REST-Schnittstellen

Interne Endpunkte dienen ausschließlich der Kommunikation zwischen den Microservices.

### 5.1 Analyse starten

Jeder Analyse-Service stellt denselben konzeptionellen Endpunkt bereit:

```http
POST /internal/analyses
```

Request:

```json
{
  "analysisId": "A-100",
  "configuration": {
    "oilSystem": "STANDARD",
    "fuelSystem": "PREMIUM",
    "coolingSystem": "STANDARD",
    "electricalSystem": "PREMIUM",
    "engineManagementSystem": "ADVANCED"
  },
  "previousResults": [
    {
      "algorithm": "FLUID",
      "result": "OK"
    }
  ]
}
```

`previousResults` kann bei den ersten Analyseschritten leer sein. Der Engine-Management-Service verwendet die vorherigen Ergebnisse explizit.

### 5.2 Status an Analysis Management melden

```http
PUT /internal/analyses/{analysisId}/algorithms/{algorithm}/status
```

Request:

```json
{
  "status": "RUNNING"
}
```

oder:

```json
{
  "status": "FAILED",
  "message": "thermal-analysis-service is unavailable"
}
```

### 5.3 Resultat melden

```http
PUT /internal/analyses/{analysisId}/algorithms/{algorithm}/result
```

Request:

```json
{
  "status": "READY",
  "result": "OK"
}
```

Damit melden die Analyse-Services Status und Resultat proaktiv zurück; Polling zwischen den Services ist nicht erforderlich.

## 6. DTOs

Die DTOs sind Integrationsverträge und gehören nicht zu einem gemeinsamen Domain-Modul.

### CreateConfigurationRequest

```text
CreateConfigurationRequest
├── oilSystem
├── fuelSystem
├── coolingSystem
├── electricalSystem
└── engineManagementSystem
```

### StartAnalysisRequest

```text
StartAnalysisRequest
└── configurationId
```

### AnalysisCommand

```text
AnalysisCommand
├── analysisId
├── configuration
└── previousResults[]
```

### AlgorithmResultDto

```text
AlgorithmResultDto
├── algorithm
├── status
├── result
└── message (optional)
```

## 7. Choreographie-Datenfluss

Für Version 1 wird eine sequenzielle Choreographie verwendet.

```text
1. Client
      │ POST /api/analyses
      ▼
2. Analysis Management
      │ erzeugt AnalysisRun
      │ lädt Configuration
      │ startet nur FLUID
      ▼
3. Fluid Analysis
      │ RUNNING -> Management
      │ Analyse simulieren
      │ READY/OK -> Management
      │ startet THERMAL
      ▼
4. Thermal Analysis
      │ RUNNING -> Management
      │ Analyse simulieren
      │ READY/OK -> Management
      │ startet ELECTRICAL
      ▼
5. Electrical Analysis
      │ RUNNING -> Management
      │ Analyse simulieren
      │ READY/OK -> Management
      │ startet ENGINE_MANAGEMENT
      ▼
6. Engine Management Analysis
      │ berücksichtigt previousResults
      │ RUNNING -> Management
      │ READY/OK oder FAILED -> Management
      ▼
7. Analysis Management
      │ berechnet Overall Result
      ▼
   Analyse abgeschlossen
```

Der Management-Service steuert die Sequenz nicht zentral. Nach dem Anchor-Aufruf liegt die Weitergabe bei den Analyse-Services.

## 8. Overall Result

Für den PoC gilt die einfache Regel:

```text
alle relevanten Einzelresultate = OK
                ↓
       Overall Result = OK
```

```text
mindestens ein Einzelresultat = FAILED
                ↓
     Overall Result = FAILED
```

Solange noch ein Algorithmus `PENDING` oder `RUNNING` ist, bleibt das `overallResult` leer (`null`).

## 9. Retry-Fluss

```text
Client
  │ POST /api/analyses/A-100/algorithms/THERMAL/retry
  ▼
Analysis Management
  │ prüft: THERMAL == FAILED
  │ setzt THERMAL -> RUNNING
  │ erstellt neuen AnalysisCommand
  ▼
Thermal Analysis
  │ führt ausschließlich THERMAL erneut aus
  │ meldet neues Resultat
  ▼
Analysis Management
  │ aktualisiert Status/Resultat
  │ berechnet Overall Result neu
```

Bereits erfolgreiche Analysealgorithmen werden bei einem Retry nicht erneut ausgeführt.

Für Version 1 setzt ein manueller Retry **nicht automatisch die restliche Choreographie erneut in Gang**. Damit bleibt das Verhalten einfach und vorhersehbar. Falls das Ergebnis des Retries für Engine Management relevant ist, kann Engine Management anschließend separat erneut ausgeführt werden.

## 10. Circuit-Breaker-Fehlerfluss

Circuit Breaker werden an den ausgehenden Service-zu-Service-Aufrufen eingesetzt.

Beispiel: `fluid-analysis-service` möchte `thermal-analysis-service` starten.

```text
Fluid Analysis
      │
      │ REST call
      ▼
Thermal Analysis [DOWN]
      X
      │
Circuit Breaker / Fallback
      │
      ├── Fehler nicht weiter eskalieren
      └── THERMAL = FAILED an Analysis Management melden
```

Ziel:

- kein unkontrollierter Fehler über mehrere Services
- aufrufender Service bleibt stabil
- Fehler wird für den Benutzer sichtbar
- späterer Retry ist möglich

Für die Implementierung wird Resilience4j verwendet.

## 11. Datenhaltung

### Configuration Service

Persistiert `EngineConfiguration`.

### Analysis Management Service

Persistiert `AnalysisRun`, Status und Resultate.

### Analyse-Services

Bleiben für den PoC stateless und besitzen keine eigene Datenbank.

Dadurch wird eine gemeinsame Datenbank zwischen den Analyse-Services vermieden.

## 12. Service-interne Struktur

Jeder Spring-Boot-Service soll klein und ähnlich aufgebaut sein:

```text
src/main/java/.../
├── api/
├── application/
├── domain/
└── infrastructure/
```

Beispiel:

```text
fluid-analysis-service/
└── src/main/java/.../
    ├── api/
    │   └── FluidAnalysisController.java
    ├── application/
    │   └── FluidAnalysisService.java
    ├── domain/
    │   ├── FluidAnalysisInput.java
    │   └── AnalysisResult.java
    └── infrastructure/
        ├── AnalysisManagementClient.java
        └── ThermalAnalysisClient.java
```

Die Struktur ist bewusst einfach und soll keine unnötigen Abstraktionsschichten erzeugen.

## 13. Geplante Docker-Komposition

```text
configuration-service
analysis-management-service
fluid-analysis-service
thermal-analysis-service
electrical-analysis-service
engine-management-analysis-service
```

Jeder Service erhält später ein eigenes Dockerfile und einen eigenen Eintrag in `docker-compose.yml`.

Nur die extern benötigten Services müssen Ports auf den Host veröffentlichen. Die Analyse-Services können innerhalb des Docker-Compose-Netzwerks über ihre Service-Namen kommunizieren.

## 14. Architekturentscheidungen

### AD-01 – Fachlicher Schnitt

Die vier Analyse-Services werden nach Analysefähigkeiten statt nach technischen Layern geschnitten.

### AD-02 – REST

Für Version 1 erfolgt die Kommunikation über REST/HTTP. Kafka bleibt außerhalb des Pflichtumfangs.

### AD-03 – Choreographie

Nach Start des Anchor-Algorithmus geben die Analyse-Services die Verarbeitung selbst weiter. Es gibt keinen zentralen Orchestrator für die gesamte Sequenz.

### AD-04 – Stateless Analysis Services

Die vier Analyse-Services speichern im PoC keinen langfristigen Zustand. Status und Ergebnisse liegen beim Analysis Management Service.

### AD-05 – Resilience4j

Service-zu-Service-Aufrufe werden mit Circuit Breakern abgesichert.

### AD-06 – Docker Compose

Jeder Microservice wird separat containerisiert und gemeinsam mit Docker Compose gestartet.

## 15. Nächster Implementierungsschritt

Auf Basis dieses Architekturentwurfs kann nun das Spring-Boot-Grundgerüst erzeugt werden.

Empfohlene Reihenfolge:

1. `configuration-service`
2. `analysis-management-service`
3. `fluid-analysis-service`
4. übrige drei Analyse-Services anhand desselben Musters
5. Choreographie verbinden
6. Circuit Breaker ergänzen
7. Dockerfiles und Docker Compose erstellen
