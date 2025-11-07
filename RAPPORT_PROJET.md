# Rapport de Projet - Benchmark de Performance REST

## Table des matières

1. [Vue d'ensemble du projet](#vue-densemble-du-projet)
2. [Architecture du projet](#architecture-du-projet)
3. [Technologies utilisées](#technologies-utilisées)
4. [Structure du projet](#structure-du-projet)
5. [Variantes implémentées](#variantes-implémentées)
6. [Modèle de données](#modèle-de-données)
7. [Infrastructure et configuration](#infrastructure-et-configuration)
8. [Tests de performance](#tests-de-performance)
9. [Monitoring et métriques](#monitoring-et-métriques)
10. [Génération de données](#génération-de-données)
11. [Optimisations réalisées](#optimisations-réalisées)
12. [Conclusion](#conclusion)

---

## Vue d'ensemble du projet

Ce projet a pour objectif de comparer les performances de différentes technologies et frameworks Java pour implémenter des API REST. L'objectif principal est d'évaluer les caractéristiques de performance de trois variantes d'implémentation :

- **Variant A** : Jersey (JAX-RS)
- **Variant C** : Spring MVC
- **Variant D** : Spring Data REST

Le projet comprend une infrastructure complète de benchmarking avec génération de données, tests de charge, monitoring et collecte de métriques.

### Objectifs

- Comparer les performances (débit, latence, consommation de ressources) de différentes technologies REST
- Identifier les optimisations possibles (notamment pour éviter les requêtes N+1)
- Fournir une infrastructure de test reproductible et automatisée
- Collecter des métriques détaillées sur les performances de chaque variante

---

## Architecture du projet

Le projet est organisé en modules Maven multi-modules :

```
Benchmark/
├── common/                 # Module commun (entités, DTOs, utilitaires)
├── datagen/                # Générateur de données de test
├── variant-a-jersey/       # Implémentation Jersey
├── variant-c-spring-mvc/   # Implémentation Spring MVC
├── variant-d-spring-data-rest/  # Implémentation Spring Data REST
├── db/                     # Scripts de base de données
├── jmeter/                 # Plans de test JMeter
└── monitoring/             # Configuration Prometheus et Grafana
```

### Architecture technique

- **Base de données** : PostgreSQL 14
- **ORM** : Hibernate 6.2.7
- **Connection Pool** : HikariCP 5.0.1
- **Sérialisation JSON** : Jackson 2.15.2
- **Tests de charge** : Apache JMeter 5.6+
- **Monitoring** : Prometheus + Grafana + InfluxDB

---

## Technologies utilisées

### Backend

- **Java 17** : Version minimale requise
- **Maven 3.8+** : Gestion des dépendances et build
- **Jersey 3.1.3** : Framework JAX-RS pour Variant A
- **Spring Boot 3.1.5** : Framework Spring pour Variants C et D
- **Hibernate 6.2.7** : ORM pour la persistance
- **HikariCP 5.0.1** : Connection pool haute performance
- **PostgreSQL Driver 42.6.0** : Driver JDBC pour PostgreSQL

### Infrastructure

- **Docker & Docker Compose** : Orchestration des services
- **PostgreSQL 14** : Base de données relationnelle
- **Prometheus** : Collecte de métriques
- **Grafana** : Visualisation des métriques
- **InfluxDB 2.7** : Stockage des métriques JMeter

### Outils de test

- **Apache JMeter 5.6+** : Tests de charge et performance
- **Grizzly** : Serveur HTTP pour Jersey (Variant A)

---

## Structure du projet

### Module `common`

Ce module contient les éléments partagés entre toutes les variantes :

#### Entités JPA

- **Category.java** : Entité représentant une catégorie
  - Champs : `id`, `code` (unique), `name`, `updatedAt`
  - Relation One-to-Many avec Item
  - FetchType LAZY pour optimiser les performances

- **Item.java** : Entité représentant un item
  - Champs : `id`, `sku` (unique), `name`, `price`, `stock`, `categoryId`, `updatedAt`
  - Relation Many-to-One avec Category
  - FetchType LAZY pour la relation avec Category

#### DTOs

- **PageResult.java** : Structure de pagination pour les réponses API
  - Contient la liste des éléments, le nombre total, la page courante, la taille

#### Utilitaires

- **HikariCPConfig.java** : Configuration du pool de connexions HikariCP
  - Pool size : 20 connexions maximum, 10 minimum
  - Timeouts configurés pour optimiser les performances

### Module `datagen`

Générateur de données de test pour peupler la base de données :

- **DataGenerator.java** : Classe principale de génération
  - Génère **2000 catégories** (CAT0001 à CAT2000)
  - Génère **100 000 items** (~50 items par catégorie)
  - Utilise des transactions batch pour optimiser l'insertion
  - Prix et stocks générés aléatoirement

### Module `variant-a-jersey`

Implémentation utilisant Jersey (JAX-RS) sur le serveur Grizzly :

#### Ressources REST

- **ItemResource.java** : Endpoints pour la gestion des items
  - `GET /items` : Liste paginée avec filtrage optionnel par catégorie
  - `GET /items/{id}` : Récupération d'un item par ID
  - `POST /items` : Création d'un item
  - `PUT /items/{id}` : Mise à jour d'un item
  - `DELETE /items/{id}` : Suppression d'un item

- **CategoryResource.java** : Endpoints pour la gestion des catégories
  - `GET /categories` : Liste paginée des catégories
  - `GET /categories/{id}` : Récupération d'une catégorie par ID
  - `GET /categories/{id}/items` : Items d'une catégorie (paginé)
  - `POST /categories` : Création d'une catégorie
  - `PUT /categories/{id}` : Mise à jour d'une catégorie

- **CategoryItemResource.java** : Endpoint combiné pour les catégories et leurs items

#### Configuration

- **JerseyBinder.java** : Configuration de l'injection de dépendances
- **TransactionFilter.java** : Filtre pour la gestion des transactions
- **MetricsServer.java** : Serveur de métriques Prometheus (port 9091)
- **JerseyApplication.java** : Point d'entrée de l'application

#### Port

- Application : **8081**
- Métriques Prometheus : **9091**

### Module `variant-c-spring-mvc`

Implémentation utilisant Spring MVC avec Spring Data JPA :

#### Contrôleurs REST

- **ItemController.java** : Contrôleur pour la gestion des items
  - Support du mode JOIN FETCH via variable d'environnement `USE_JOIN_FETCH`
  - Endpoints similaires à Variant A avec annotations Spring

- **CategoryController.java** : Contrôleur pour la gestion des catégories
- **CategoryItemController.java** : Contrôleur pour les relations catégorie-item

#### Repositories

- **ItemRepository.java** : Repository Spring Data JPA
  - Méthodes de requête personnalisées avec et sans JOIN FETCH
  - Optimisation pour éviter les requêtes N+1

- **CategoryRepository.java** : Repository Spring Data JPA pour les catégories

#### Configuration

- **SpringMvcApplication.java** : Application Spring Boot
- **application.properties** : Configuration de la base de données et JPA

#### Port

- Application : **8082**
- Métriques Prometheus : **8082/actuator/prometheus**

### Module `variant-d-spring-data-rest`

Implémentation utilisant Spring Data REST (génération automatique d'API REST) :

#### Repositories

- **ItemRepository.java** : Repository Spring Data JPA exposé automatiquement
- **CategoryRepository.java** : Repository Spring Data JPA exposé automatiquement

#### Configuration

- **SpringDataRestApplication.java** : Application Spring Boot
- **RestConfig.java** : Configuration Spring Data REST
- **application.properties** : Configuration de la base de données

#### Port

- Application : **8083**
- Métriques Prometheus : **8083/actuator/prometheus**

---

## Modèle de données

### Schéma de base de données

#### Table `category`

```sql
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

- **Clé primaire** : `id` (auto-incrémenté)
- **Contrainte d'unicité** : `code`
- **Index** : Sur `id` (primaire)

#### Table `item`

```sql
CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    stock INT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES category(id),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

- **Clé primaire** : `id` (auto-incrémenté)
- **Clé étrangère** : `category_id` référençant `category(id)`
- **Contrainte d'unicité** : `sku`
- **Index** :
  - Sur `id` (primaire)
  - Sur `category_id` (pour optimiser les jointures)
  - Sur `updated_at` (pour les requêtes temporelles)

### Relations

- **Category → Item** : Relation One-to-Many (une catégorie a plusieurs items)
- **Item → Category** : Relation Many-to-One (un item appartient à une catégorie)

---

## Infrastructure et configuration

### Docker Compose

Le fichier `docker-compose.yml` définit l'infrastructure complète :

#### Services

1. **PostgreSQL**
   - Port : 5432
   - Base de données : `benchmark_db`
   - Utilisateur : `benchmark_user`
   - Mot de passe : `benchmark_pass`
   - Volume persistant pour les données
   - Script d'initialisation : `db/init.sql`

2. **Prometheus**
   - Port : 9090
   - Configuration : `monitoring/prometheus.yml`
   - Volume persistant pour les métriques
   - Collecte de métriques toutes les 15 secondes

3. **Grafana**
   - Port : 3000
   - Identifiants par défaut : admin/admin
   - Dashboards automatiquement provisionnés
   - Datasources configurés automatiquement

4. **InfluxDB**
   - Port : 8086
   - Organisation : `perf`
   - Bucket : `jmeter`
   - Utilisé pour stocker les résultats des tests JMeter

### Configuration Prometheus

Le fichier `monitoring/prometheus.yml` configure la collecte de métriques :

- **Intervalle de scraping** : 15 secondes
- **Targets** :
  - Variant A : `host.docker.internal:9091`
  - Variant C : `host.docker.internal:8082/actuator/prometheus`
  - Variant D : `host.docker.internal:8083/actuator/prometheus`

### Configuration HikariCP

Pool de connexions configuré pour optimiser les performances :

- **Maximum pool size** : 20 connexions
- **Minimum idle** : 10 connexions
- **Connection timeout** : 30 secondes
- **Idle timeout** : 10 minutes
- **Max lifetime** : 30 minutes

---

## Tests de performance

### Plans de test JMeter

Quatre scénarios de test différents ont été créés :

#### 1. READ-heavy (read-heavy.jmx)

Scénario orienté lecture intensive :

- **50%** : `GET /items?page=&size=50` - Liste paginée des items
- **20%** : `GET /items?categoryId=...&page=&size=` - Items filtrés par catégorie
- **20%** : `GET /categories/{id}/items?page=&size=` - Items d'une catégorie spécifique
- **10%** : `GET /categories?page=&size=` - Liste des catégories

**Configuration de charge** :
- Threads : 50 → 100 → 200
- Ramp-up : 60 secondes par palier
- Durée : 10 minutes par palier

#### 2. JOIN-filter (join-filter.jmx)

Scénario testant les jointures et filtres :

- **70%** : `GET /items?categoryId=...&page=&size=` - Items filtrés par catégorie
- **30%** : `GET /items/{id}` - Récupération d'un item par ID

**Configuration de charge** :
- Threads : 60 → 120
- Ramp-up : 60 secondes par palier
- Durée : 8 minutes par palier

#### 3. MIXED (mixed.jmx)

Scénario mixte lecture/écriture :

- **40%** : `GET /items?page=...` - Lecture
- **20%** : `POST /items` (1 KB) - Création
- **10%** : `PUT /items/{id}` (1 KB) - Mise à jour
- **10%** : `DELETE /items/{id}` - Suppression
- **10%** : `POST /categories` (0.5-1 KB) - Création de catégorie
- **10%** : `PUT /categories/{id}` - Mise à jour de catégorie

**Configuration de charge** :
- Threads : 50 → 100
- Ramp-up : 60 secondes par palier
- Durée : 10 minutes par palier

#### 4. HEAVY-body (heavy-body.jmx)

Scénario testant les gros payloads :

- **50%** : `POST /items` (5 KB) - Création avec gros payload
- **50%** : `PUT /items/{id}` (5 KB) - Mise à jour avec gros payload

**Configuration de charge** :
- Threads : 30 → 60
- Ramp-up : 60 secondes par palier
- Durée : 8 minutes par palier

### Données de test JMeter

Le répertoire `jmeter/jmeter-data/` contient :

- **category-payload.json** : Modèle de payload pour les catégories
- **item-payload-1kb.json** : Modèle de payload d'item (1 KB)
- **item-payload-5kb.json** : Modèle de payload d'item (5 KB)
- **ids.csv** : Liste d'IDs pour les tests

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

---

## Monitoring et métriques

### Métriques collectées

#### Métriques JVM (via Prometheus)

- **CPU usage** : Utilisation du processeur
- **Heap memory** : Utilisation de la mémoire heap
- **GC time** : Temps passé en garbage collection
- **Threads actifs** : Nombre de threads actifs
- **HikariCP pool stats** : Statistiques du pool de connexions
  - Connexions actives
  - Connexions idle
  - Temps d'attente

#### Métriques JMeter (via InfluxDB)

- **RPS** : Requests Per Second (requêtes par seconde)
- **Latence** : Temps de réponse
  - P50 (médiane)
  - P95 (95e percentile)
  - P99 (99e percentile)
- **Taux d'erreurs** : Pourcentage de requêtes en erreur
- **Temps de réponse** : Temps de réponse moyen, min, max

### Visualisation

#### Grafana

- Dashboards pré-configurés pour visualiser les métriques
- Datasources automatiquement configurés :
  - Prometheus pour les métriques JVM
  - InfluxDB pour les métriques JMeter

#### Accès

- **Prometheus** : http://localhost:9090
- **Grafana** : http://localhost:3000 (admin/admin)
- **InfluxDB** : http://localhost:8086

---

## Génération de données

### Module datagen

Le générateur de données crée un jeu de données réaliste pour les tests :

#### Données générées

- **2000 catégories** :
  - Codes : CAT0001 à CAT2000
  - Noms : "Category 1" à "Category 2000"

- **100 000 items** :
  - Répartition : ~50 items par catégorie
  - SKUs : SKU000001 à SKU100000
  - Noms : "Item 1" à "Item 100000"
  - Prix : Aléatoires entre 10 et 1010 (arrondis à 2 décimales)
  - Stock : Aléatoire entre 0 et 999

#### Optimisations

- **Transactions batch** : Insertions par lots de 100 catégories et 1000 items
- **Préparation des requêtes** : Utilisation de PreparedStatement
- **Contrôle des transactions** : Commit par batch pour optimiser les performances

#### Exécution

```bash
cd datagen
mvn exec:java
```

---

## Optimisations réalisées

### 1. Mode JOIN FETCH

Pour éviter le problème des requêtes N+1, un mode JOIN FETCH a été implémenté :

#### Problème N+1

Sans optimisation, lors de la récupération d'une liste d'items avec leurs catégories, Hibernate exécute :
1. 1 requête pour récupérer les items
2. N requêtes supplémentaires pour récupérer chaque catégorie

#### Solution JOIN FETCH

Avec JOIN FETCH, une seule requête SQL est exécutée avec une jointure :

```java
@Query("SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId")
Page<Item> findByCategoryIdWithJoinFetch(@Param("categoryId") Long categoryId, Pageable pageable);
```

#### Activation

Le mode JOIN FETCH est activé via une variable d'environnement :

```bash
export USE_JOIN_FETCH=true
```

Cette optimisation est disponible dans les variantes Spring (C et D).

### 2. Pagination

Toutes les listes sont paginées pour limiter la quantité de données transférées :

- **Taille par défaut** : 20 éléments
- **Taille maximale** : Configurable
- **Paramètres** : `page` (0-based) et `size`

### 3. Index de base de données

Index créés pour optimiser les requêtes fréquentes :

- **Index sur `item.category_id`** : Pour les jointures et filtres par catégorie
- **Index sur `item.updated_at`** : Pour les requêtes temporelles

### 4. Connection Pool

Configuration optimale de HikariCP :

- Pool size adapté à la charge
- Timeouts configurés pour éviter les connexions bloquées
- Monitoring des statistiques du pool

### 5. FetchType LAZY

Relations configurées en LAZY pour charger les données uniquement quand nécessaire :

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id", nullable = false)
private Category category;
```

---

## Comparaison des variantes

### Variant A : Jersey

**Avantages** :
- Implémentation JAX-RS standard
- Contrôle fin sur le code
- Légèreté relative

**Inconvénients** :
- Configuration manuelle plus importante
- Pas de support automatique du mode JOIN FETCH
- Métriques Prometheus à implémenter manuellement

### Variant C : Spring MVC

**Avantages** :
- Framework mature et populaire
- Support intégré de Spring Data JPA
- Support du mode JOIN FETCH
- Métriques Prometheus via Spring Actuator
- Configuration simplifiée

**Inconvénients** :
- Stack plus lourde que Jersey
- Moins de contrôle sur les détails d'implémentation

### Variant D : Spring Data REST

**Avantages** :
- Génération automatique de l'API REST
- Configuration minimale
- Support intégré de Spring Data JPA
- Métriques Prometheus via Spring Actuator

**Inconvénients** :
- Moins de contrôle sur les endpoints
- Configuration moins flexible
- Potentiellement moins optimisé pour des cas spécifiques

---

## Installation et utilisation

### Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+
- Apache JMeter 5.6+

### Étapes d'installation

1. **Démarrer l'infrastructure** :
   ```bash
   docker-compose up -d
   ```

2. **Générer les données de test** :
   ```bash
   cd datagen
   mvn exec:java
   ```

3. **Compiler le projet** :
   ```bash
   mvn clean install
   ```

4. **Lancer les variantes** (dans des terminaux séparés) :
   ```bash
   # Variant A
   cd variant-a-jersey
   mvn exec:java
   
   # Variant C
   cd variant-c-spring-mvc
   mvn spring-boot:run
   
   # Variant D
   cd variant-d-spring-data-rest
   mvn spring-boot:run
   ```

5. **Exécuter les tests JMeter** :
   ```bash
   jmeter -n -t jmeter/read-heavy.jmx -l jmeter/results/read-heavy.jtl
   ```

### Variables d'environnement

- `USE_JOIN_FETCH=true` : Active le mode JOIN FETCH pour éviter les requêtes N+1

---

## Résultats et analyse

### Critères d'évaluation

Les variantes sont comparées selon les critères suivants :

1. **Débit (RPS)** : Nombre de requêtes par seconde
2. **Latence** : Temps de réponse (P50, P95, P99)
3. **Taux d'erreurs** : Pourcentage de requêtes en erreur
4. **Empreinte CPU/RAM** : Consommation de ressources
5. **Temps GC** : Temps passé en garbage collection
6. **Threads actifs** : Utilisation des threads

### Analyse des résultats

Les résultats des tests sont stockés dans :
- **Prometheus** : Métriques JVM et applicatives
- **InfluxDB** : Métriques JMeter
- **Fichiers JTL** : Résultats bruts JMeter

L'analyse se fait via :
- Dashboards Grafana
- Export CSV depuis Prometheus
- Analyse des fichiers JTL avec JMeter ou outils tiers

---

## Conclusion

Ce projet de benchmark fournit une infrastructure complète pour comparer les performances de différentes technologies REST en Java. Il permet de :

1. **Comparer objectivement** les performances de Jersey, Spring MVC et Spring Data REST
2. **Identifier les optimisations** possibles (JOIN FETCH, pagination, index)
3. **Collecter des métriques détaillées** sur les performances de chaque variante
4. **Reproduire les tests** de manière fiable grâce à Docker et JMeter

Le projet est extensible et peut être adapté pour tester d'autres frameworks ou configurations.

### Points d'amélioration possibles

- Ajout de tests de charge avec des volumes plus importants
- Implémentation de cache (Redis, Caffeine)
- Tests de résilience (timeouts, circuit breakers)
- Comparaison avec d'autres frameworks (Quarkus, Micronaut)
- Analyse plus approfondie des requêtes SQL générées
- Tests de montée en charge progressive plus détaillés

### Technologies et compétences démontrées

- **Développement Java** : Java 17, Maven
- **Frameworks REST** : Jersey, Spring MVC, Spring Data REST
- **Persistence** : Hibernate, JPA, PostgreSQL
- **Tests de performance** : JMeter
- **Monitoring** : Prometheus, Grafana, InfluxDB
- **Infrastructure** : Docker, Docker Compose
- **Optimisation** : Requêtes N+1, pagination, index, connection pooling

---

## Auteur et date

**Projet** : Benchmark de Performance REST  
**Date** : 2024  
**Contexte** : Architecture des Composants - 5IIR

