# Guide de configuration et d'utilisation

## Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+
- JMeter 5.6+

## Configuration initiale

### 1. Démarrer l'infrastructure

```bash
docker-compose up -d
```

Cela démarre:
- PostgreSQL sur le port 5432
- Prometheus sur le port 9090
- Grafana sur le port 3000
- InfluxDB sur le port 8086

### 2. Générer les données de test

```bash
cd datagen
mvn exec:java
```

Génère:
- 2000 catégories (CAT0001 à CAT2000)
- 100000 items (~50 items par catégorie)

### 3. Compiler le projet

```bash
mvn clean install
```

## Exécution des variantes

### Variante A: Jersey (Port 8081)

```bash
cd variant-a-jersey
mvn exec:java
```

Métriques Prometheus: http://localhost:9091/metrics

### Variante C: Spring MVC (Port 8082)

```bash
cd variant-c-spring-mvc
mvn spring-boot:run
```

Métriques Prometheus: http://localhost:8082/actuator/prometheus

### Variante D: Spring Data REST (Port 8083)

```bash
cd variant-d-spring-data-rest
mvn spring-boot:run
```

Métriques Prometheus: http://localhost:8083/actuator/prometheus

## Tests de charge avec JMeter

### Configuration des tests

Modifier la variable `BASE_URL` dans chaque plan JMeter selon la variante testée:
- Variante A: `http://localhost:8081`
- Variante C: `http://localhost:8082`
- Variante D: `http://localhost:8083`

### Scénarios de test

1. **READ-heavy** (read-heavy.jmx)
   - 50% GET /items?page=&size=50
   - 20% GET /items?categoryId=...&page=&size=
   - 20% GET /categories/{id}/items?page=&size=
   - 10% GET /categories?page=&size=
   - Threads: 50 → 100 → 200, ramp-up 60s, 10 min/palier

2. **JOIN-filter** (join-filter.jmx)
   - 70% GET /items?categoryId=...&page=&size=
   - 30% GET /items/{id}
   - Threads: 60 → 120, ramp-up 60s, 8 min/palier

3. **MIXED** (mixed.jmx)
   - 40% GET /items?page=...
   - 20% POST /items (1 KB)
   - 10% PUT /items/{id} (1 KB)
   - 10% DELETE /items/{id}
   - 10% POST /categories (0.5–1 KB)
   - 10% PUT /categories/{id}
   - Threads: 50 → 100, ramp-up 60s, 10 min/palier

4. **HEAVY-body** (heavy-body.jmx)
   - 50% POST /items (5 KB)
   - 50% PUT /items/{id} (5 KB)
   - Threads: 30 → 60, ramp-up 60s, 8 min/palier

### Exécution des tests

```bash
# Créer le répertoire de résultats
mkdir -p jmeter/results

# Exécuter les tests
jmeter -n -t jmeter/read-heavy.jmx -l jmeter/results/read-heavy.jtl
jmeter -n -t jmeter/join-filter.jmx -l jmeter/results/join-filter.jtl
jmeter -n -t jmeter/mixed.jmx -l jmeter/results/mixed.jtl
jmeter -n -t jmeter/heavy-body.jmx -l jmeter/results/heavy-body.jtl
```

## Mode JOIN FETCH

Pour activer le mode JOIN FETCH et éviter les requêtes N+1:

```bash
export USE_JOIN_FETCH=true
```

Puis relancer la variante concernée.

## Monitoring

### Prometheus

- URL: http://localhost:9090
- Configuration: `monitoring/prometheus.yml`

### Grafana

- URL: http://localhost:3000
- Identifiants: admin/admin
- Datasources configurés automatiquement

### InfluxDB

- URL: http://localhost:8086
- Organisation: `perf`
- Bucket: `jmeter`
- Token: à récupérer depuis l'interface web

## Collecte des métriques

### Métriques JVM (Prometheus)

- CPU usage
- Heap memory
- GC time
- Threads actifs
- HikariCP pool stats

### Métriques JMeter (InfluxDB)

- RPS (Requests Per Second)
- Latence (p50, p95, p99)
- Taux d'erreurs
- Temps de réponse

## Analyse des résultats

1. Exporter les métriques Prometheus vers CSV
2. Analyser les résultats JMeter depuis InfluxDB/Grafana
3. Comparer les variantes selon les critères:
   - Débit (RPS)
   - Latence (p50/p95/p99)
   - Taux d'erreurs
   - Empreinte CPU/RAM
   - Temps GC
   - Threads actifs

