# memorIA

Proyecto TFG para desarrollar un MVP de teleasistencia inteligente dual para pacientes con demencia/Alzheimer y sus cuidadores.

El documento maestro del alcance, decisiones tecnicas y plan de desarrollo esta en:

- [MVP memorIA.md](MVP%20memorIA.md)

La idea original completa se conserva en:

- [IDEA TFG (memorIA).md](IDEA%20TFG%20(memorIA).md)

## Estructura del monorepo

```text
.
├── android/      # Aplicacion Android nativa en Kotlin + Jetpack Compose
├── backend/      # Backend principal en Java + Spring Boot
├── ai-service/   # Microservicio IA en Python + FastAPI + Gemini API
├── web/          # Web responsive en React + Vite + TypeScript
├── docs/         # Documentacion auxiliar del proyecto
├── .env.example  # Variables de entorno de referencia
└── .gitignore
```

## Stack previsto

- Android: Kotlin, Jetpack Compose, Material 3, SpeechRecognizer y TextToSpeech.
- Web: React, Vite y TypeScript.
- Backend principal: Java y Spring Boot.
- Servicio IA: Python, FastAPI y Google Gemini API.
- Base de datos, autenticacion y tiempo real: Supabase.
- Despliegue final: VPS/hosting con Docker si encaja con el entorno disponible.

## Orden de trabajo

1. Documentacion MVP.
2. Estructura del monorepo.
3. Supabase, datos y autenticacion.
4. Backend Spring Boot.
5. App Android base.
6. Modo cuidador.
7. Microservicio IA.
8. Modo paciente con voz.
9. Alertas, tiempo real, diario y transcripcion.
10. Pruebas, despliegue y memoria.

## Estado actual

- Documento maestro del MVP actualizado.
- Monorepo creado.
- Esquema SQL inicial de Supabase documentado.
- Backend Spring Boot scaffolded y compilado.
- Microservicio FastAPI scaffolded con Gemini y fallback local.
- App Android Compose conectada al backend local y compilada.
- Registro e inicio de sesion de cuidadores mediante Supabase Auth.
- Envio de JWT desde Android y validacion en Spring Boot cuando `APP_SECURITY_ENABLED=true`.
- Perfil basico del cuidador asociado al usuario autenticado.
- Modo paciente con entrada por voz y TextToSpeech.
- Modo cuidador con formularios reales para pacientes, bucles, temas peligrosos y recuerdos seguros.
- Gestion de dispositivos paciente vinculados y desvinculacion desde modo cuidador.
- Diario con sesiones, transcripcion, eventos y alertas por sesion.
- Web publica y panel responsive para cuidadores.
- Tests unitarios del backend para la logica conversacional principal.

## Probar localmente

Ver la guia:

- [docs/guia-ejecucion-local.md](docs/guia-ejecucion-local.md)
- [docs/configuracion-credenciales.md](docs/configuracion-credenciales.md)
- [docs/despliegue.md](docs/despliegue.md)
- [docs/checklist-pruebas-mvp.md](docs/checklist-pruebas-mvp.md)

Scripts de ejemplo:

- [scripts/run-backend-supabase.example.ps1](scripts/run-backend-supabase.example.ps1)
- [scripts/run-ai-service.example.ps1](scripts/run-ai-service.example.ps1)
