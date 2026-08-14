# Implementierungs-Bootstrap

Dieser Bootstrap setzt die Architektur-Baseline aus `docs/ddd.md` und
`docs/architecture.md` technisch als Maven-Multimodul um.

## Module

- configuration-service
- analysis-management-service
- fluid-analysis-service
- thermal-analysis-service
- electrical-analysis-service
- engine-management-analysis-service

## Stand

- `configuration-service`: persistente Engine-Konfiguration mit H2/JPA
- `analysis-management-service`: AnalysisRun, Status-/Result-Callbacks, Retry-Vertrag
- vier Analyse-Services: lauffähige Spring-Boot-Grundgerüste
- Choreographie/Circuit Breaker werden im nächsten Schritt ergänzt

## Start

Zuerst bauen:

```bash
mvn clean verify
```

Danach einzelne Services aus ihren Modulverzeichnissen starten:

```bash
mvn spring-boot:run
```
