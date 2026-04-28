import os
from typing import List

from dotenv import load_dotenv
from fastapi import FastAPI
from google import genai
from pydantic import BaseModel, Field

load_dotenv()

APP_NAME = "memorIA AI Service"
DEFAULT_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

app = FastAPI(title=APP_NAME, version="0.1.0")


class SafeMemory(BaseModel):
    title: str = Field(..., min_length=1)
    content: str = Field(..., min_length=1)


class GenerateResponseRequest(BaseModel):
    patientName: str | None = None
    patientNotes: str | None = None
    message: str = Field(..., min_length=1)
    safeMemories: List[SafeMemory] = Field(default_factory=list)
    dangerousTerms: List[str] = Field(default_factory=list)


class GenerateResponseResponse(BaseModel):
    text: str
    provider: str
    model: str


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
            text=fallback_response(request.patientName),
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
            text = fallback_response(request.patientName)
        return GenerateResponseResponse(text=text, provider="gemini", model=DEFAULT_MODEL)
    except Exception:
        return GenerateResponseResponse(
            text=fallback_response(request.patientName),
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
{request.message}

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


def fallback_response(patient_name: str | None) -> str:
    if patient_name:
        return f"Te entiendo, {patient_name}. Vamos a pensar en algo tranquilo y agradable."
    return "Te entiendo. Vamos a pensar en algo tranquilo y agradable."
