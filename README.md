# README

## Setup

1. Start infrastructure:
```bash
docker-compose up -d
```

2. Generate test data:
```bash
cd datagen
mvn exec:java
```

3. Build all variants:
```bash
mvn clean install
```

4. Run variants (in separate terminals):
```bash
# Variant A (Jersey) - Port 8081
cd variant-a-jersey
mvn exec:java

# Variant C (Spring MVC) - Port 8082
cd variant-c-spring-mvc
mvn spring-boot:run

# Variant D (Spring Data REST) - Port 8083
cd variant-d-spring-data-rest
mvn spring-boot:run
```

5. Run JMeter tests:
```bash
jmeter -n -t jmeter/read-heavy.jmx -l results/read-heavy.jtl
```

## Environment Variables

- `USE_JOIN_FETCH=true` - Enable JOIN FETCH mode to avoid N+1 queries

## Monitoring

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- InfluxDB: http://localhost:8086

## Ports

- Variant A: 8081
- Variant C: 8082
- Variant D: 8083
- Prometheus metrics: 9091 (A), 8082/actuator/prometheus (C), 8083/actuator/prometheus (D)

