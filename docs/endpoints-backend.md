# Endpoints iniciales del backend

Base URL local prevista:

```text
http://localhost:8080/api
```

## Salud

### GET `/health`

Comprueba que el backend esta levantado.

## Pacientes

### GET `/patients`

Lista los pacientes del cuidador autenticado.

### POST `/patients`

Crea un paciente.

```json
{
  "fullName": "Maria Lopez",
  "preferredName": "Maria",
  "birthYear": 1942,
  "relationship": "Madre",
  "notes": "Le tranquiliza hablar de costura.",
  "textSize": "large",
  "ttsSpeed": 0.9
}
```

### GET `/patients/{patientId}`

Obtiene el detalle de un paciente.

### PUT `/patients/{patientId}`

Actualiza un paciente.

### DELETE `/patients/{patientId}`

Elimina un paciente.

## Vinculacion

### POST `/patients/{patientId}/pairing-codes`

Genera un codigo alfanumerico para vincular el modo paciente.

### POST `/patient-devices/link`

Vincula un dispositivo paciente usando un codigo.

```json
{
  "code": "AB123C",
  "deviceIdentifier": "android-device-id",
  "deviceName": "Tablet salon"
}
```

## Configuracion IA

### Bucles

- GET `/patients/{patientId}/loop-rules`
- POST `/patients/{patientId}/loop-rules`
- PUT `/patients/{patientId}/loop-rules/{ruleId}`
- DELETE `/patients/{patientId}/loop-rules/{ruleId}`

```json
{
  "question": "Donde esta Maria?",
  "answer": "Maria esta cerca, no te preocupes. En un rato vendra a verte.",
  "active": true
}
```

### Temas peligrosos

- GET `/patients/{patientId}/dangerous-topics`
- POST `/patients/{patientId}/dangerous-topics`
- PUT `/patients/{patientId}/dangerous-topics/{topicId}`
- DELETE `/patients/{patientId}/dangerous-topics/{topicId}`

```json
{
  "term": "dinero",
  "redirectHint": "Redirigir hacia recuerdos de costura o del pueblo.",
  "active": true
}
```

### Recuerdos seguros

- GET `/patients/{patientId}/safe-memories`
- POST `/patients/{patientId}/safe-memories`
- PUT `/patients/{patientId}/safe-memories/{memoryId}`
- DELETE `/patients/{patientId}/safe-memories/{memoryId}`

```json
{
  "title": "Costura",
  "content": "Le gustaba coser con su hermana por las tardes.",
  "active": true
}
```

## Sesiones

### POST `/patients/{patientId}/sessions`

Crea una sesion en estado `waiting`.

### POST `/sessions/{sessionId}/start`

Inicia una sesion.

### POST `/sessions/{sessionId}/pause`

Pausa una sesion.

### POST `/sessions/{sessionId}/resume`

Reanuda una sesion.

### POST `/sessions/{sessionId}/end`

Termina una sesion.

### POST `/sessions/{sessionId}/messages`

Procesa un mensaje transcrito del paciente.

```json
{
  "text": "Donde esta Maria?"
}
```

Respuesta:

```json
{
  "sessionId": "uuid",
  "patientMessageId": "uuid",
  "responseMessageId": "uuid",
  "responseText": "Maria esta cerca, no te preocupes...",
  "source": "loop_rule",
  "alertCreated": true
}
```

## Diario

### GET `/patients/{patientId}/sessions`

Lista sesiones de un paciente.

### GET `/sessions/{sessionId}`

Obtiene detalle de sesion.

### GET `/sessions/{sessionId}/transcript`

Obtiene los mensajes de la sesion.

### GET `/sessions/{sessionId}/events`

Obtiene eventos de la sesion.

## Alertas

### GET `/alerts`

Lista las alertas del cuidador autenticado.

### POST `/alerts/{alertId}/read`

Marca una alerta como leida.

## Terminal paciente

Estos endpoints permiten que el modo paciente funcione tras la vinculacion por codigo.

### GET `/patient-terminal/{deviceIdentifier}/status`

Devuelve el paciente vinculado y la ultima sesion abierta si existe.

Respuesta:

```json
{
  "linked": true,
  "patientId": "uuid",
  "patientName": "Maria",
  "sessionId": "uuid",
  "sessionStatus": "active"
}
```

### POST `/patient-terminal/{deviceIdentifier}/sessions/{sessionId}/messages`

Envia un mensaje transcrito del paciente a una sesion activa.

```json
{
  "text": "Donde esta Maria?"
}
```
