# Guia de ejecucion local

Esta guia permite probar el flujo actual sin Supabase real. El backend usa memoria temporal por defecto, por lo que los datos se pierden al reiniciar Spring Boot.

Para activar Supabase remoto, consulta `docs/configuracion-credenciales.md`.

## 1. Arrancar backend Spring Boot

Desde `backend/`:

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

Comprobar:

```text
GET http://localhost:8080/api/health
```

## 2. Arrancar microservicio IA

El backend funciona aunque el microservicio IA no este levantado, porque tiene fallback local.

Cuando quieras probar Gemini:

```powershell
cd ai-service
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
$env:GEMINI_API_KEY="<TU_GEMINI_API_KEY>"
uvicorn app.main:app --reload --port 8000
```

Si ya tienes creado `scripts/run-ai-service.ps1`, puedes usar ese script local para no escribir la clave cada vez. Ese archivo no debe subirse al repositorio.

Comprobar:

```text
GET http://localhost:8000/health
```

## 3. Compilar app Android

Desde `android/`:

```powershell
& "C:\Users\USUARIO\.gradle\wrapper\dists\gradle-9.1.0-bin\9agqghryom9wkf8r80qlhnts3\gradle-9.1.0\bin\gradle.bat" :app:assembleDebug
```

APK:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## 4. Probar flujo en emulador Android

La app esta configurada para llamar al backend en:

```text
http://10.0.2.2:8080/api
```

Esto funciona desde emulador Android porque `10.0.2.2` apunta al `localhost` del PC.

Flujo:

1. Abrir la app.
2. Pulsar **Comprobar backend**.
3. Entrar en **Modo cuidador**.
4. Crear un paciente desde la pestana **Pacientes**.
5. Editar el paciente y guardar cambios para comprobar el flujo CRUD.
6. Seleccionar el paciente.
7. Entrar en **IA** y crear:
   - Un bucle conversacional.
   - Un tema peligroso.
   - Un recuerdo seguro.
8. Editar al menos una regla, un tema o un recuerdo y comprobar que se actualiza.
9. Volver a **Pacientes** y generar codigo de vinculacion.
10. Volver y entrar en **Modo paciente**.
11. Introducir el codigo generado.
12. Volver a **Modo cuidador** e iniciar sesion.
13. En **Modo paciente**, actualizar estado.
14. Hablar o escribir un mensaje.
15. Enviar a memorIA.
16. Revisar respuesta y alertas en el modo cuidador.
17. Entrar en **Diario**, actualizar sesiones y abrir el detalle de la sesion.
18. Revisar transcripcion, eventos y alertas asociadas.

## 4.1 Probar notificaciones Android

Al entrar en **Modo cuidador**, Android puede pedir permiso para mostrar notificaciones. Hay que aceptarlo para probar la alerta emergente del sistema.

Para generar una notificacion:

1. Crea un paciente.
2. Crea un bucle o un tema peligroso en **IA**.
3. Vincula el modo paciente.
4. Inicia una sesion desde **Inicio**.
5. Desde **Modo paciente**, envia un mensaje que coincida con el bucle o contenga el tema peligroso.
6. Vuelve al **Modo cuidador** o espera unos segundos.

La app refresca alertas de forma periodica y muestra:

- Banner en Inicio.
- Registro en Diario, con sesion, eventos, transcripcion y alertas.
- Notificacion Android si el permiso esta concedido.

El modo paciente tambien consulta periodicamente el estado de sesion, por lo que deberia pasar de `waiting` a `active`, `paused` o `ended` unos segundos despues de que el cuidador cambie el estado.

Ejemplos utiles:

- Bucle: pregunta `Donde esta Maria`; respuesta `Maria esta cerca, no te preocupes. En un rato vendra a verte.`
- Tema peligroso: termino `dinero`; redireccion `Podemos pensar en la costura o en una tarde tranquila en casa.`
- Recuerdo seguro: titulo `Costura`; contenido `Le gustaba coser por las tardes y hablar de telas bonitas.`

## 5. Probar en movil fisico

Si usas un movil fisico, `10.0.2.2` no sirve.

Debes cambiar temporalmente `BACKEND_BASE_URL` en:

```text
android/app/build.gradle.kts
```

Por la IP local del PC:

```kotlin
buildConfigField("String", "BACKEND_BASE_URL", "\"http://IP_DEL_PC:8080/api\"")
```

El movil y el PC deben estar en la misma red Wi-Fi.

## 6. Conexion real y credenciales

Para una maquina nueva o una configuracion limpia hara falta:

- Crear proyecto Supabase.
- Ejecutar `docs/supabase-schema.sql`.
- Copiar `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` y `SUPABASE_JWT_SECRET`.
- Conseguir una clave de Google AI Studio para `GEMINI_API_KEY`.
- Crear scripts locales a partir de `scripts/run-backend-supabase.example.ps1` y `scripts/run-ai-service.example.ps1`.
