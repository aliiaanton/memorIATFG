import os
import logging
from typing import Any, List

from dotenv import load_dotenv
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from google import genai
from pydantic import BaseModel, Field, field_validator, model_validator

load_dotenv()

APP_NAME = "memorIA AI Service"
DEFAULT_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")
LOGGER = logging.getLogger("memoria-ai-service")

app = FastAPI(title=APP_NAME, version="0.1.0")


class SafeMemory(BaseModel):
    title: str = ""
    content: str = ""

    @field_validator("title", "content", mode="before")
    @classmethod
    def normalize_text(cls, value: Any) -> str:
        if value is None:
            return ""
        return str(value).strip()


class GenerateResponseRequest(BaseModel):
    patientName: str | None = None
    patientNotes: str | None = None
    message: str = ""
    safeMemories: List[SafeMemory] = Field(default_factory=list)
    dangerousTerms: List[str] = Field(default_factory=list)

    @field_validator("patientName", "patientNotes", "message", mode="before")
    @classmethod
    def normalize_optional_text(cls, value: Any) -> str | None:
        if value is None:
            return None
        return str(value).strip()

    @field_validator("safeMemories", mode="before")
    @classmethod
    def normalize_safe_memories(cls, value: Any) -> list[Any]:
        if value is None:
            return []
        if isinstance(value, list):
            return [item for item in value if item is not None]
        return []

    @field_validator("dangerousTerms", mode="before")
    @classmethod
    def normalize_dangerous_terms(cls, value: Any) -> list[str]:
        if value is None:
            return []
        if not isinstance(value, list):
            return []
        return [str(term).strip() for term in value if term is not None and str(term).strip()]

    @model_validator(mode="after")
    def remove_blank_context(self) -> "GenerateResponseRequest":
        self.safeMemories = [
            memory for memory in self.safeMemories if memory.title and memory.content
        ]
        return self


class GenerateResponseResponse(BaseModel):
    text: str
    provider: str
    model: str


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exception: RequestValidationError) -> JSONResponse:
    body = await request.body()
    LOGGER.warning(
        "Invalid AI request at %s: errors=%s body=%s",
        request.url.path,
        exception.errors(),
        body.decode("utf-8", errors="replace")[:1000],
    )
    return JSONResponse(status_code=422, content={"detail": exception.errors()})


@app.get("/")
def root() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "memoria-ai-service",
        "health": "/health",
        "docs": "/docs",
    }


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "memoria-ai-service"}


@app.post("/generate-response")
def generate_response(request: GenerateResponseRequest) -> GenerateResponseResponse:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        return GenerateResponseResponse(
            text=fallback_response(request),
            provider="fallback",
            model="local",
        )

    prompt = build_prompt(request)
    try:
        client = genai.Client(api_key=api_key)
        response = client.models.generate_content(
            model=DEFAULT_MODEL,
            contents=prompt,
        )
        text = (response.text or "").strip()
        if not text:
            text = fallback_response(request)
        return GenerateResponseResponse(text=text, provider="gemini", model=DEFAULT_MODEL)
    except Exception:
        return GenerateResponseResponse(
            text=fallback_response(request),
            provider="fallback",
            model="local",
        )


def build_prompt(request: GenerateResponseRequest) -> str:
    name = request.patientName or "la persona"
    notes = request.patientNotes or "No hay notas adicionales."
    memories = format_memories(request.safeMemories)
    dangerous_terms = format_terms(request.dangerousTerms)

    return f"""
Eres memorIA, un asistente conversacional de apoyo para una persona con deterioro cognitivo.

Objetivo:
- Responder con calma, empatia y frases breves.
- No discutir ni corregir bruscamente.
- No dar consejo medico.
- Evitar temas delicados.
- Redirigir suavemente hacia recuerdos seguros cuando sea posible.

Paciente:
- Nombre: {name}
- Notas del cuidador: {notes}

Temas que debes evitar:
{dangerous_terms}

Recuerdos o temas seguros que puedes usar:
{memories}

Mensaje del paciente:
{request.message or "No se ha recibido un mensaje claro."}

Responde en espanol, con 1 o 2 frases como maximo.
""".strip()


def format_memories(memories: List[SafeMemory]) -> str:
    if not memories:
        return "- No hay recuerdos seguros configurados."
    return "\n".join(f"- {memory.title}: {memory.content}" for memory in memories)


def format_terms(terms: List[str]) -> str:
    clean_terms = [term for term in terms if term.strip()]
    if not clean_terms:
        return "- No hay temas peligrosos configurados."
    return "\n".join(f"- {term}" for term in clean_terms)


def fallback_response(request: GenerateResponseRequest) -> str:
    name = f", {request.patientName}" if request.patientName else ""
    if request.safeMemories:
        memory = request.safeMemories[0]
        return (
            f"Te entiendo{name}. Podemos hablar de {memory.title}, "
            "algo tranquilo y agradable."
        )
    if request.message and "hola" in request.message.lower():
        return f"Hola{name}. Estoy aqui contigo; cuentame despacio como te sientes."
    return f"Te entiendo{name}. Vamos a respirar despacio y hablar de algo tranquilo."
