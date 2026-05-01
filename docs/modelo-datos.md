# Modelo de datos inicial

Este documento describe el modelo de datos previsto para el MVP. El SQL ejecutable esta en `docs/supabase-schema.sql`.

## Entidades principales

### caregiver_profiles

Perfil del cuidador. Durante el MVP local puede existir como perfil de demo. Cuando se integre Supabase Auth completamente, se vinculara con `auth.users` mediante `auth_user_id`.

Campos clave:

- `id`: identificador interno del cuidador.
- `auth_user_id`: usuario autenticado de Supabase, opcional durante el desarrollo local.
- `full_name`: nombre visible del cuidador.

### patients

Pacientes gestionados por un cuidador.

Campos clave:

- `caregiver_id`: propietario del paciente.
- `full_name`, `preferred_name`, `birth_year`, `relationship`, `notes`.
- `text_size` y `tts_speed`: ajustes basicos del modo paciente.

### pairing_codes

Codigos temporales para vincular el modo paciente con un paciente concreto.

Campos clave:

- `code`: codigo alfanumerico mostrado al cuidador.
- `expires_at`: fecha de caducidad.
- `consumed_at`: marca si ya fue usado.

### patient_devices

Dispositivos vinculados al modo paciente.

Campos clave:

- `device_identifier`: identificador local generado por la app Android.
- `patient_id`: paciente asociado.
- `revoked_at`: permite desvincular sin borrar historico.

## Configuracion conversacional

### loop_rules

Preguntas repetitivas y respuestas configuradas por el cuidador.

Ejemplo:

- Pregunta: "Donde esta Maria?"
- Respuesta: "Maria esta cerca, no te preocupes. En un rato vendra a verte."

### dangerous_topics

Palabras o temas delicados que deben generar redireccion y alerta.

Ejemplo:

- Termino: "dinero"
- Redireccion: "Hablar de una actividad tranquila o recuerdo positivo."

### safe_memories

Recuerdos, gustos o temas seguros que la IA puede usar para reconducir la conversacion.

Ejemplo:

- Titulo: "Costura"
- Contenido: "Le gustaba coser con su hermana por las tardes."

## Sesiones, mensajes y trazabilidad

### conversation_sessions

Sesion conversacional entre paciente e IA.

Estados:

- `waiting`
- `active`
- `paused`
- `ended`
- `error`

### conversation_messages

Turnos de conversacion guardados como texto.

Remitentes:

- `patient`: mensaje transcrito del paciente.
- `rule`: respuesta preconfigurada.
- `ai`: respuesta generada por Gemini.
- `system`: mensaje tecnico o de estado.

### session_events

Eventos relevantes de la sesion.

Ejemplos:

- Inicio de sesion.
- Deteccion de bucle.
- Deteccion de tema peligroso.
- Pausa o fin de sesion.

### alerts

Alertas visibles para el cuidador.

Se usaran para:

- Banner en la pantalla de Inicio.
- Diario de actividad.
- Notificacion Android.

## Seguridad y acceso

Todas las tablas tienen `caregiver_id` cuando contienen datos de un cuidador.

Cuando Supabase Auth este activo, el criterio de acceso principal del perfil sera:

```sql
auth.uid() = auth_user_id
```

Las tablas de datos se agrupan por `caregiver_id`. El backend escribira en Supabase con credenciales seguras y validara la propiedad de los datos desde Spring Boot.

El modo paciente no deberia escribir directamente en Supabase. Para mantener el control, la app paciente enviara sus acciones a Spring Boot y Spring Boot escribira en Supabase.

## Tiempo real

Las tablas candidatas para suscripcion en tiempo real son:

- `conversation_sessions`
- `session_events`
- `alerts`

El cuidador escuchara cambios de estas tablas para actualizar el estado de sesion, mostrar banners y disparar notificaciones Android.

En la implementacion Android actual, el MVP usa refresco periodico corto sobre la API REST para estado de sesion y alertas. Esto mantiene la demo funcional sin acoplar la app al canal Realtime de Supabase; la sustitucion por suscripciones Realtime queda como mejora directa si el tiempo de despliegue lo permite.
