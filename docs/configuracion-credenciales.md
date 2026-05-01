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
SUPABASE_DB_URL=postgresql://postgres.<project-id>:<password>@aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
```

Para este proyecto, el `project-id` actual es:

```text
fhjifzpopiegsyizzhbp
```

Importante: el host directo `db.<project-id>.supabase.co` puede funcionar solo por IPv6. Si Windows o tu red no tienen IPv6, Spring Boot mostrara `Failed to obtain JDBC Connection`. Para el MVP local usa la connection string de **Session pooler**:

1. Supabase Dashboard.
2. Boton **Connect**.
3. Seccion **Session pooler**.
4. Copiar la URI.
5. Sustituir `[YOUR-PASSWORD]` por la password de la base de datos.

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

Para una demo local sin seguridad, usando el cuidador fijo:

```powershell
$env:APP_STORE="supabase"
$env:APP_SECURITY_ENABLED="false"
$env:SUPABASE_DB_URL="postgresql://postgres.<project-id>:<password>@aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require"
$env:AI_SERVICE_BASE_URL="http://localhost:8000"

cd backend
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

Mientras `APP_SECURITY_ENABLED=false`, el backend usa un cuidador demo fijo:

```text
00000000-0000-0000-0000-000000000001
```

Esto permite probar persistencia remota sin activar la validacion JWT.

Para probar autenticacion real con JWT:

```powershell
$env:APP_STORE="supabase"
$env:APP_SECURITY_ENABLED="true"
$env:SUPABASE_JWT_SECRET="<jwt-secret-del-proyecto>"
$env:SUPABASE_JWT_ISSUER="https://<project-id>.supabase.co/auth/v1"
$env:SUPABASE_DB_URL="postgresql://postgres.<project-id>:<password>@aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require"
$env:AI_SERVICE_BASE_URL="http://localhost:8000"

cd backend
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

En este modo Android debe compilarse con `SUPABASE_URL` y `SUPABASE_ANON_KEY` para poder registrar e iniciar sesion de cuidadores.

```powershell
$env:SUPABASE_URL="https://<project-id>.supabase.co"
$env:SUPABASE_ANON_KEY="<anon-key>"
cd android
.\gradlew.bat :app:assembleDebug
```

Tambien se puede dejar configurado localmente en `android/local.properties`, que esta ignorado por Git:

```properties
sdk.dir=C\:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://<project-id>.supabase.co
SUPABASE_ANON_KEY=<anon-key>
```

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
