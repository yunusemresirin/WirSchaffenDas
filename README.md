# WirSchaffenDas – Engine Quality Analysis

Proof-of-Concept einer Microservice-basierten Qualitätsanalyse für Diesel-Engine-Konfigurationen im Modul SEKA.

## Services

- `configuration-service` – Port 8081
- `analysis-management-service` – Port 8082
- `fluid-analysis-service` – Port 8083
- `thermal-analysis-service` – Port 8084
- `electrical-analysis-service` – Port 8085
- `engine-management-analysis-service` – Port 8086

Die Analyse läuft choreographiert:

```text
Analysis Management -> Fluid -> Thermal -> Electrical -> Engine Management
```

Status und Ergebnisse werden von den Analyse-Services proaktiv an `analysis-management-service` zurückgemeldet. Service-zu-Service-Aufrufe sind mit Resilience4j Circuit Breakern abgesichert.

## Lokal mit Maven prüfen

```bash
mvn clean verify
```

Damit werden unter anderem die Unit-/Service-Tests für `AnalysisRun` und den `configuration-service` ausgeführt.

## Docker-Variante 1 – Images aus Docker Hub

Die Standarddatei `docker-compose.yml` verwendet die veröffentlichten Images aus `ysirin2s/seka-wirschaffendas`.

```bash
docker compose pull
docker compose up -d
```

Status anzeigen:

```bash
docker compose ps
```

Logs verfolgen:

```bash
docker compose logs -f
```

Stoppen:

```bash
docker compose down
```

Persistierte H2-Daten ebenfalls löschen:

```bash
docker compose down -v
```

## Docker-Variante 2 – Images lokal aus dem Source Code bauen

Für die lokale Entwicklung bleibt `alternative_docker-compose.yml` erhalten:

```bash
docker compose -f alternative_docker-compose.yml up --build -d
```

Damit werden die sechs Images aus den Dockerfiles des Repositories gebaut.

## Automatisierter End-to-End-Test

Voraussetzungen: Docker Compose, `curl` und `jq`.

Mit Docker-Hub-Images:

```bash
docker compose up -d
bash scripts/e2e.sh
```

Oder mit lokal gebauten Images:

```bash
docker compose -f alternative_docker-compose.yml up --build -d
COMPOSE_FILE=alternative_docker-compose.yml bash scripts/e2e.sh
```

Das Skript testet sowohl den Happy Path als auch den Ausfall von `thermal-analysis-service` mit anschließendem Retry.

Weitere Details: `docs/testing.md`.

## Postman-Demo

Collection importieren:

`postman/WirSchaffenDas.postman_collection.json`

Happy Path:

1. `01 Create Configuration`
2. `02 Get Configuration`
3. `03 Start Analysis`
4. einige Sekunden warten
5. `04 Get Analysis Status / Result`

Erwartetes Endergebnis: alle vier Algorithmen `READY / OK` und `overallResult = OK`.

## Circuit-Breaker- und Retry-Demo

Thermal Service stoppen:

```bash
docker compose stop thermal-analysis-service
```

Danach eine neue Konfiguration und Analyse über Postman starten. Nach der Fluid Analysis kann Thermal nicht erreicht werden und wird als `FAILED` sichtbar.

Service wieder starten:

```bash
docker compose start thermal-analysis-service
```

In Postman `retryAlgorithm = THERMAL` verwenden und `05 Retry Failed Algorithm` ausführen. Die Choreographie läuft anschließend ab Thermal weiter.

## Dokumentation

- `docs/arc42.md` – kompakte Architekturdokumentation für das Semesterprojekt
- `docs/requirements.md` – vollständige Anforderungen
- `docs/requirements_short.md` – kompakte Requirements-Übersicht
- `docs/ddd.md` – Domänenmodell und Bounded Contexts
- `docs/architecture.md` – Architektur- und REST-Entscheidungen
- `docs/testing.md` – Teststrategie und E2E-Szenarien
- `docs/architecture-views.md` – Übersicht des 4-Sichten-Modells
- `docs/diagrams/context.puml` – Kontextsicht
- `docs/diagrams/building-blocks.puml` – Bausteinsicht
- `docs/diagrams/runtime.puml` – Laufzeitsicht
- `docs/diagrams/deployment.puml` – Verteilungssicht
