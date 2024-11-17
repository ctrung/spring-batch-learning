Exemples Spring Batch.

## Configuration multi-jobs

Spring Boot 3 n'autorise plus qu'un seul job au démarrage sous peine d'erreur. 

La configuration suivante spécifie le job à démarrer si plusieurs sont présents dans le contexte.

```properties
spring.batch.job.name=<nom du job>
```

Une propriété existe pour désactiver le lancement auto. Utile pour d'autres types de lancement (endpoint REST, scheduling).

```properties
spring.batch.job.enabled=true|false
```

Source : https://github.com/spring-projects/spring-boot/issues/25373


## MultiThreaded step

Exemple : `com.example.spring.batch.config.ChunkMultiThreadJob`.

Remarques :
- Les steps sont exécutés dans des transactions -> ajuster la taille du pool (eg. `spring.datasource.hikari.maximum-pool-size`).
- Pour ne pas saturer les threads, notion de throttle (4 par défaut). Voir [Throttle limit deprecation](https://docs.spring.io/spring-batch/reference/scalability.html#multithreadedStep) pour la nouvelle recommandation.
- L'ordre d'exécution des éléments n'est pas garanti (notamment lors du write) !

Configurer les logs Hikari en `debug` pour confirmer la configuration du pool de connexions (propriété `logging.level.com.zaxxer.hikari`).

## Async step

Exemple : `com.example.spring.batch.config.ChunkAsyncJob`.

Remarques : 
- Nécessite la dépendance `org.springframework.batch:spring-batch-integration`.
- Le processor est multithreadé et le writer gère la lecture des `Future`.
- L'ordre d'exécution des chunks est garanti mais pas les éléments qui y sont contenus. Si l'ordre importe et les éléments 
sont interdépendants, c'est au code applicatif de faire le nécessaire.