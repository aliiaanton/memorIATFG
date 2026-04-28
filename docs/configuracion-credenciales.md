# Configuracion de credenciales

No guardes claves reales en archivos versionables. Usa variables de entorno o archivos `.env` locales ignorados por Git.

## 1. Gemini API

Variable necesaria para `ai-service`:

```text
GEMINI_API_KEY=...
```

Si no se configura, el microservicio devuelve una respuesta local de fallback.

## 2. Supabase

Datos necesarios:

```text
SUPABASE_URL=https://<project-id>.supabase.co
SUPABASE_ANON_KEY=...
SUPABASE_SERVICE_ROLE_KEY=...
SUPABASE_DB_URL=postgresql://postgres:<password>@db.<project-id>.supabase.co:5432/postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=...
```

Para este proyecto, el `project-id` actual es:

```text
fhjifzpopiegsyizzhbp
```

## 3. Crear tablas en Supabase

Como no hay `psql` ni Supabase CLI instalados en la maquina, hay que hacerlo desde el panel web:

1. Entrar en Supabase.
2. Abrir el proyecto.
3. Ir a **SQL Editor**.
4. Copiar el contenido de `docs/supabase-schema.sql`.
5. Ejecutarlo.

Este paso crea:

- Perfiles de cuidadores.
- Pacientes.
- Codigos de vinculacion.
- Dispositivos paciente.
- Reglas de bucle.
- Temas peligrosos.
- Recuerdos seguros.
- Sesiones.
- Mensajes.
- Eventos.
- Alertas.
- Politicas RLS basicas.

## 4. Arrancar backend con Supabase

Desde PowerShell, usando variables de entorno locales:

```powershell
$env:APP_STORE="supabase"
$env:APP_SECURITY_ENABLED="false"
$env:SUPABASE_DB_URL="postgresql://postgres:<password>@db.<project-id>.supabase.co:5432/postgres"
$env:SUPABASE_DB_USER="postgres"
$env:SUPABASE_DB_PASSWORD="<password>"
$env:AI_SERVICE_BASE_URL="http://localhost:8000"

cd backend
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

Mientras `APP_SECURITY_ENABLED=false`, el backend usa un cuidador demo fijo:

```text
00000000-0000-0000-0000-000000000001
```

Esto permite probar persistencia remota antes de integrar Supabase Auth en Android.

Tambien puedes duplicar el script de ejemplo:

```text
scripts/run-backend-supabase.example.ps1
```

Renombrarlo localmente a:

```text
scripts/run-backend-supabase.ps1
```

Y rellenar tus valores reales. Ese archivo queda ignorado por Git.

## 4.1 Si los pacientes no se guardan o no aparecen

Si la app permite rellenar un paciente pero no aparece en la lista, comprueba:

```text
GET http://localhost:8080/api/patients
```

Si devuelve `500`, lo mas probable es que Supabase tenga una version antigua o incompleta del esquema.

Solucion:

1. Abre Supabase.
2. Entra en **SQL Editor**.
3. Ejecuta `docs/supabase-reparacion-mvp.sql`.
4. Reinicia Spring Boot.
5. Vuelve a probar la creacion del paciente.

## 5. Arrancar AI service con Gemini

```powershell
$env:GEMINI_API_KEY="<gemini-api-key>"
cd ai-service
.\.venv\Scripts\activate
uvicorn app.main:app --reload --port 8000
```

Tambien puedes duplicar:

```text
scripts/run-ai-service.example.ps1
```

Como:

```text
scripts/run-ai-service.ps1
```

Y rellenar tu clave real. Ese archivo queda ignorado por Git.

## 6. Seguridad

Recomendacion importante:

- Regenerar la `service role key` si se ha compartido por chat o documentos.
- Regenerar la clave de Gemini si se ha expuesto.
- No incluir `.env`, capturas de claves ni tokens en la memoria del TFG.
- Para la memoria, hablar de variables de entorno, no de valores reales.
