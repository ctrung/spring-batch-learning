Exemples Spring Batch.

## Configuration multi-jobs

Spring Boot 3 n'autorise plus qu'un seul job au démarrage sous peine d'erreur. 

La configuration suivante spécifie le job à démarrer si plusieurs sont présents dans le contexte.

```properties
spring.batch.job.name=<nom du job>
```

La propriété pour désactiver le lancement auto pour les usecases manuels (endpoint REST, scheduling).

```properties
spring.batch.job.enabled=true|false
```

Source : https://github.com/spring-projects/spring-boot/issues/25373


## MultiThreaded step

Exemple : `com.example.spring.batch.config.ChunkMultiThreadJob`.

Remarques :
- Les steps sont exécutés dans des transactions -> ajuster la taille du pool en fonction (eg. `spring.datasource.hikari.maximum-pool-size`).
- Pour éviter la saturation des threads, notion de throttle (4 par défaut). Voir [Throttle limit deprecation](https://docs.spring.io/spring-batch/reference/scalability.html#multithreadedStep) pour les détails.
- L'ordre d'exécution des éléments n'est pas garanti (notamment lors du write) !

Passer Hikari en mode debug permet de confirmer la configuration du pool de connexions.