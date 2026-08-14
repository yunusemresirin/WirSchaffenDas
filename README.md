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

## Gesamtsystem mit Docker Compose starten

```bash
docker compose up --build
```

Im Hintergrund:

```bash
docker compose up --build -d
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

- `docs/requirements.md`
- `docs/requirements_short.md`
- `docs/ddd.md`
- `docs/architecture.md`
