# Backend

Backend principal del proyecto memorIA.

Tecnologia prevista:

- Java.
- Spring Boot.
- API REST.
- Validacion de JWT de Supabase.
- Comunicacion con Supabase PostgreSQL.
- Comunicacion con `ai-service` para respuestas generadas por IA.

Responsabilidades:

- CRUD de pacientes, bucles, temas peligrosos y recuerdos seguros.
- Gestion de sesiones conversacionales.
- Registro de mensajes, eventos y alertas.
- Orquestacion de reglas conversacionales e IA.

## Ejecucion local

Desde esta carpeta:

```bash
mvn spring-boot:run
```

En esta maquina tambien se puede usar el Maven embebido de IntelliJ:

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

El backend arranca en:

```text
http://localhost:8080
```

Health check:

```text
GET http://localhost:8080/api/health
```

Por defecto `APP_SECURITY_ENABLED=false`, asi que los endpoints se pueden probar sin JWT durante el desarrollo inicial.
