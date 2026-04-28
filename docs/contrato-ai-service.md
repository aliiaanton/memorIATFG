# Contrato entre Spring Boot y FastAPI

El microservicio `ai-service` solo encapsula la llamada a Gemini API. La logica de negocio, reglas, alertas y persistencia permanecen en Spring Boot.

Base URL local prevista:

```text
http://localhost:8000
```

## POST `/generate-response`

Genera una respuesta empatica cuando no existe una regla preconfigurada aplicable.

### Request

```json
{
  "patientName": "Maria",
  "patientNotes": "Le tranquiliza hablar de costura.",
  "message": "Hoy estoy inquieta.",
  "safeMemories": [
    {
      "title": "Costura",
      "content": "Le gustaba coser con su hermana por las tardes."
    }
  ],
  "dangerousTerms": ["dinero", "hospital"]
}
```

### Response

```json
{
  "text": "Te entiendo, Maria. Vamos a pensar en algo tranquilo. Recuerdo que te gustaba mucho coser por las tardes.",
  "provider": "gemini",
  "model": "gemini"
}
```

## Reglas de respuesta

La respuesta generada debe:

- Ser breve.
- Usar tono calmado, empatico y no confrontativo.
- No diagnosticar ni dar consejo medico.
- No contradecir bruscamente al paciente.
- Redirigir hacia recuerdos seguros si la conversacion puede generar ansiedad.
- Evitar temas peligrosos enviados en la peticion.

