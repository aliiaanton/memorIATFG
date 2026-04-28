# AI Service

Microservicio de IA del proyecto memorIA.

Tecnologia prevista:

- Python.
- FastAPI.
- Google Gemini API.

Responsabilidades:

- Recibir desde Spring Boot el mensaje del paciente y el contexto seguro.
- Construir una peticion controlada al proveedor de IA.
- Devolver una respuesta empatica, breve y adecuada para el paciente.

Este servicio no sustituye al backend principal. La logica de negocio seguira centralizada en Spring Boot.

## Ejecucion local

Instalar dependencias:

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

Ejecutar:

```bash
uvicorn app.main:app --reload --port 8000
```

Variables necesarias:

```text
GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.5-flash
```

Si no existe `GEMINI_API_KEY`, el servicio devuelve una respuesta local de fallback para no bloquear el desarrollo.

Health check:

```text
GET http://localhost:8000/health
```

Documentacion interactiva:

```text
http://localhost:8000/docs
```
