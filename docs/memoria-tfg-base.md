# Base para la memoria del TFG

Este documento sirve como borrador tecnico para redactar la memoria final de memorIA. No sustituye a la memoria academica, pero recoge las ideas principales que conviene explicar.

## 1. Resumen

memorIA es un MVP de teleasistencia inteligente para personas con deterioro cognitivo y sus cuidadores. El sistema se compone de una aplicacion Android con dos modos de uso, un backend principal en Spring Boot, un microservicio de IA en FastAPI y una base de datos remota en Supabase.

El objetivo del MVP es demostrar un flujo completo: el cuidador configura un paciente, define reglas conversacionales, vincula un dispositivo paciente mediante codigo, inicia una sesion, el paciente conversa por voz o texto, el sistema responde de forma calmada y se registra la actividad en un diario consultable.

## 2. Objetivos

Objetivo principal:

- Desarrollar una aplicacion funcional que ayude al cuidador a supervisar conversaciones simples con un paciente vulnerable.

Objetivos secundarios:

- Implementar CRUD de pacientes y configuracion IA.
- Usar una base de datos remota.
- Incorporar una interfaz paciente muy simple.
- Integrar voz a texto y texto a voz en Android.
- Detectar temas peligrosos y preguntas repetitivas.
- Generar alertas y registrar eventos.
- Guardar transcripcion post-sesion.
- Preparar una arquitectura modular y explicable.

## 3. Alcance del MVP

Incluido:

- App Android nativa con modo cuidador y modo paciente.
- CRUD de pacientes.
- CRUD de reglas de bucle, temas peligrosos y recuerdos seguros.
- Vinculacion por codigo alfanumerico.
- Sesiones conversacionales con estados.
- SpeechRecognizer y TextToSpeech.
- Microservicio FastAPI con Gemini y respuesta local de respaldo.
- Persistencia remota en Supabase PostgreSQL.
- Diario con mensajes, eventos y alertas.
- Notificaciones Android para alertas.

Excluido:

- Camara y video.
- Deteccion de caidas.
- Avatares 3D.
- Videollamadas.
- Multimedia automatica.
- App web o iOS.
- Transcripcion en directo para el cuidador.

## 4. Arquitectura

La arquitectura se organiza como monorepo:

- `android/`: aplicacion Android Kotlin + Jetpack Compose.
- `backend/`: API REST principal Java + Spring Boot.
- `ai-service/`: microservicio Python + FastAPI.
- `docs/`: documentacion tecnica.
- `scripts/`: scripts locales ignorados o ejemplos seguros.

Flujo general:

1. Android modo cuidador configura datos mediante API REST.
2. Spring Boot valida y centraliza la logica de negocio.
3. Spring Boot persiste datos en Supabase PostgreSQL.
4. Android modo paciente envia texto transcrito al backend.
5. Spring Boot decide si aplicar una regla preconfigurada o pedir respuesta al servicio IA.
6. FastAPI consulta Gemini o devuelve respuesta de respaldo.
7. Spring Boot guarda mensajes, eventos y alertas.
8. Android cuidador consulta alertas y diario.

## 5. Tecnologias

Android:

- Kotlin.
- Jetpack Compose.
- Material 3.
- SpeechRecognizer.
- TextToSpeech.

Backend:

- Java 21.
- Spring Boot.
- Spring Web.
- Spring Security.
- JDBC.
- PostgreSQL driver.

IA:

- Python.
- FastAPI.
- Google Gemini API.

Datos:

- Supabase PostgreSQL.
- Supabase Auth previsto para endurecimiento final.

Despliegue:

- Desarrollo local.
- Dockerfiles para Spring Boot y FastAPI.
- Docker Compose como opcion para VPS.

## 6. Modelo de datos

Entidades principales:

- `caregiver_profiles`: perfil del cuidador.
- `patients`: pacientes asociados a un cuidador.
- `pairing_codes`: codigos temporales de vinculacion.
- `patient_devices`: dispositivos paciente vinculados.
- `loop_rules`: preguntas repetitivas y respuestas predefinidas.
- `dangerous_topics`: palabras o temas delicados.
- `safe_memories`: recuerdos seguros o temas positivos.
- `conversation_sessions`: sesiones conversacionales.
- `conversation_messages`: transcripcion textual de turnos.
- `session_events`: eventos relevantes de una sesion.
- `alerts`: alertas visibles para el cuidador.

## 7. Logica conversacional

El backend aplica una decision en cascada:

1. Comprueba que la sesion esta activa.
2. Guarda el mensaje del paciente.
3. Busca coincidencia con temas peligrosos.
4. Si existe tema peligroso, responde con redireccion segura y genera alerta.
5. Si no, busca regla de pregunta repetitiva.
6. Si existe regla, responde con la respuesta configurada y genera evento/alerta.
7. Si no existe regla, llama al microservicio IA.
8. Guarda la respuesta y el evento generado.

Esta logica permite explicar que la IA no actua sin control: primero se aplican reglas configuradas por el cuidador.

## 8. Seguridad y privacidad

Medidas actuales del MVP:

- No se guarda audio completo, solo texto transcrito.
- Las claves reales se gestionan mediante scripts locales o variables de entorno.
- `.gitignore` evita subir `.env`, scripts reales y builds.
- Supabase permite persistencia remota y politicas RLS documentadas.

Limitacion actual:

- En la demo local el backend puede ejecutarse con `APP_SECURITY_ENABLED=false` y un cuidador demo fijo para simplificar pruebas.
- Supabase Auth y validacion JWT estan preparados en backend, pero la integracion completa extremo a extremo queda como endurecimiento final.

## 9. Pruebas realizadas

Pruebas tecnicas:

- Compilacion del backend con Maven.
- Tests unitarios del servicio conversacional con JUnit:
  - deteccion de tema peligroso y generacion de alerta.
  - deteccion de regla de bucle sin llamar a IA.
  - respuesta IA cuando no hay regla configurada.
  - rechazo de mensajes si la sesion no esta activa.
- Compilacion de Android con Gradle.
- Validacion de FastAPI con `py_compile`.
- Comprobacion de `GET /api/health`.
- Comprobacion de `GET /api/patients`.
- Prueba de creacion de sesion.
- Prueba de envio de mensaje por endpoint cuidador.
- Prueba de envio de mensaje por endpoint paciente vinculado.
- Verificacion de respuesta IA o fallback local.

Pruebas funcionales:

- Crear paciente.
- Generar codigo de vinculacion.
- Vincular modo paciente.
- Iniciar sesion desde cuidador.
- Enviar mensaje desde paciente.
- Escuchar respuesta por TTS.
- Consultar diario.

## 10. Problemas encontrados y soluciones

Conexion Supabase:

- Problema: el host directo de Supabase resolvia por IPv6 y fallaba en local.
- Solucion: usar Session pooler de Supabase con `sslmode=require`.

URL JDBC:

- Problema: PostgreSQL JDBC no aceptaba directamente la URI con `usuario:password@host`.
- Solucion: el backend parsea la URI, separa credenciales y construye una URL JDBC limpia.

Voz TTS:

- Problema: el emulador podia usar una voz con acento ingles.
- Solucion: la app prioriza `es-ES` y se documenta instalar voz Espanol (Espana).

Estado de sesion:

- Problema: el paciente podia intentar enviar antes de que la sesion estuviera activa.
- Solucion: Android solo permite enviar cuando el estado es `active`.

## 11. Limitaciones

- No hay videollamada ni camara.
- No hay deteccion automatica de emergencias fisicas.
- La IA no sustituye al cuidador ni toma decisiones medicas.
- El reconocimiento de voz depende del dispositivo/emulador.
- La demo local requiere backend y servicio IA levantados.
- El despliegue final en VPS queda preparado pero pendiente de validacion en entorno real.

## 12. Lineas futuras

- Completar Supabase Auth extremo a extremo.
- Mejorar perfiles y permisos por cuidador real.
- Notificaciones push remotas.
- Panel web complementario para cuidadores.
- Integracion con calendarios o recordatorios.
- Analisis de patrones conversacionales.
- Exportacion de diario en PDF.
- Modo tablet optimizado para paciente.

## 13. Defensa del MVP

El MVP es defendible porque demuestra:

- CRUD real.
- Persistencia remota.
- App Android nativa.
- Backend propio.
- Microservicio IA separado.
- Comunicacion entre dos interfaces.
- Voz a texto y texto a voz.
- Alertas y diario.
- Criterios de privacidad.
- Arquitectura modular preparada para evolucionar.
