# Guia de despliegue MVP

Esta guia recoge una estrategia simple para desplegar el MVP sin cambiar la arquitectura: Spring Boot como backend principal, FastAPI como servicio IA y Supabase como base de datos remota.

## Opcion recomendada para demo local

Para desarrollo diario sigue siendo mas comodo usar:

- Spring Boot desde `backend/`.
- FastAPI desde `ai-service/`.
- Android Studio para ejecutar la app.
- Supabase remoto.

La guia local esta en `docs/guia-ejecucion-local.md`.

## Opcion Docker Compose

El repositorio incluye:

- `backend/Dockerfile`
- `ai-service/Dockerfile`
- `docker-compose.yml`

Antes de arrancar, crea un archivo `.env` local a partir de `.env.example` y rellena solo las variables necesarias. Ese archivo no debe subirse al repositorio.

Para una demo sin persistencia real:

```powershell
APP_STORE=memory
APP_SECURITY_ENABLED=false
```

Para conectar con Supabase:

```powershell
APP_STORE=supabase
APP_SECURITY_ENABLED=false
SUPABASE_DB_URL=<jdbc-postgres-url>
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=<password>
GEMINI_API_KEY=<gemini-api-key>
```

Arranque:

```powershell
docker compose up --build
```

Comprobaciones:

```text
http://localhost:8080/api/health
http://localhost:8000/health
```

## Despliegue en VPS

Pasos previstos:

1. Instalar Docker y Docker Compose en el VPS.
2. Subir el proyecto al VPS.
3. Crear `.env` en la raiz del proyecto con variables reales.
4. Ejecutar `docker compose up -d --build`.
5. Comprobar `/api/health` y `/health`.
6. Configurar firewall para exponer solo los puertos necesarios.
7. Cambiar `BACKEND_BASE_URL` en Android para apuntar a la URL del VPS.

Para la memoria, esta estrategia permite explicar un despliegue reproducible manteniendo Android como cliente nativo.

## Notas importantes

- No se despliega Android en Docker.
- No se guardan credenciales en archivos versionados.
- Supabase sigue siendo el servicio remoto de base de datos y autenticacion.
- Si Docker consume demasiado tiempo, la entrega puede defenderse con ejecucion local documentada y despliegue como linea futura inmediata.
