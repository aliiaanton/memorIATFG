# Modulo de estimulacion cognitiva

Este modulo introduce juegos de voz breves para mantener actividad cognitiva y reconducir
conversaciones ansiosas sin confrontar al paciente.

## Objetivos clinico-funcionales

- Priorizar seguridad emocional y sensacion de logro.
- Mantener juegos sencillos, orales y de baja carga cognitiva.
- Usar aprendizaje sin errores: validar siempre y modelar la respuesta.
- Evitar convertir la actividad en un examen de memoria.
- Permitir silencios largos antes de responder por el paciente.

Referencias de diseno:

- Alzheimer Association, actividades: https://www.alz.org/help-support/caregiving/daily-care/activities
- Alzheimer Association, musica y arte: https://www.alz.org/help-support/caregiving/daily-care/art-music
- Alzheimer Association, reminiscencia: https://www.alz.org/help-support/caregiving/daily-care/reminiscence-and-reminiscence-therapy

## Arquitectura

El backend decide el flujo de juego de forma determinista. El LLM queda reservado para
conversacion libre validante cuando no hay juego activo, regla configurada ni trigger.

Clases principales:

- `CognitiveStimulationService`: analiza estado, repeticion y estado activo por sesion.
- `CognitiveGameType`: tipos de juego disponibles.
- `CognitiveTrigger`: motivo de activacion.
- `CognitiveTurnResponse`: respuesta generada para guardar en transcript.
- `ConversationService`: integra el modulo en el flujo paciente.

## Maquina de estados

Estados:

- `IDLE`: conversacion normal, sin juego pendiente.
- `ACTIVE_GAME`: el backend espera respuesta a un juego.
- `COMPLETED`: se genera validacion y se vuelve a `IDLE`.
- `SILENCE_SUPPORT`: silencio prolongado fuera de juego, respuesta de acompanamiento.

Transiciones:

- `IDLE -> ACTIVE_GAME`: Trigger A o Trigger B.
- `ACTIVE_GAME -> COMPLETED`: respuesta, fallo, palabra confusa o silencio.
- `COMPLETED -> IDLE`: despues de guardar la respuesta cognitiva.
- `IDLE -> SILENCE_SUPPORT`: silencio prolongado sin juego activo.
- `SILENCE_SUPPORT -> IDLE`: despues de responder con calma.

Orden de decision en una entrada del paciente:

1. Guardar mensaje del paciente.
2. Aplicar tema peligroso configurado.
3. Resolver juego activo, si existe.
4. Responder a silencio prolongado, si aplica.
5. Aplicar Trigger B por ansiedad, frustracion o repeticion.
6. Aplicar regla de bucle configurada.
7. Aplicar Trigger A por estado positivo, calma o aburrimiento.
8. Enviar al LLM con system instructions terapeuticas.

## Triggers

Trigger A, estimulacion:

- Estado positivo: `bien`, `contento`, `feliz`, `alegre`.
- Estado tranquilo: `tranquilo`, `calmado`.
- Aburrimiento: `aburrido`, `me aburro`, `que hago`.
- Juegos preferidos: refranes, categorias, opuestos.

Trigger B, redireccion cognitiva:

- Ansiedad leve: `miedo`, `ansiedad`, `preocupado`.
- Frustracion: `frustrado`, `enfadado`, `agobiado`.
- Bucle de repeticion: idea similar repetida en los ultimos mensajes.
- Juego preferido: completar cancion tradicional.

## Prompts exactos de juegos

Todos los textos dirigidos al paciente tienen menos de 15 palabras.

| Juego | Prompt inicial | Acierto | Fallo o silencio |
| --- | --- | --- | --- |
| Completa la cancion | `Cantemos: Que llueva, que llueva...` | `¡Exacto! Que buena memoria musical.` | `¡Casi! Yo pensaba en: la Virgen de la Cueva.` |
| Completar refranes | `Completemos un refran: A quien madruga...` | `¡Exacto! Que buena memoria.` | `¡Casi! Yo pensaba en: Dios le ayuda.` |
| La maleta / categorias | `Vamos de compra. Dime algo para la cesta.` | `Muy bien. Eso nos sirve para la compra.` | `Muy bien, yo pondria pan en la cesta.` |
| Asociacion de opuestos | `Dime el contrario de blanco.` | `¡Exacto! Blanco y negro, muy bien.` | `¡Casi! Yo pensaba en negro.` |
| Silencio sin juego | - | - | `Estoy aqui contigo. Tomate tu tiempo.` |

## System instructions del LLM

El microservicio `ai-service` usa estas instrucciones como base:

```text
Eres memorIA, una voz terapeutica, calmada y validante para una persona vulnerable.

Principios:
- Prioriza seguridad emocional, dignidad y sensacion de logro.
- Usa aprendizaje sin errores: valida, acompana y modela la respuesta.
- Evita negaciones directas, correcciones duras y juicios de fallo.
- Si la persona falla, duda o calla, asume el peso conversacional.
- Ofrece la respuesta como aportacion propia y manten tono afectuoso.
- Usa frases claras, concretas y cortas.
- Cada frase dirigida al paciente debe tener menos de 15 palabras.
- Evita consejo medico, diagnosticos y confrontacion.
- Redirige con suavidad hacia recuerdos seguros o actividades sencillas.
- Si hay ansiedad, usa musica, refranes o recuerdos familiares.
```

## Configuracion de latencia de voz

La interfaz Android usa valores largos para evitar interrupciones:

- `VoiceCompleteSilenceMillis = 8000`
- `VoicePossiblyCompleteSilenceMillis = 5000`
- `VoiceMinimumListeningMillis = 2500`
- `VoiceAutoRestartDelayMillis = 1200`

Cuando Android detecta silencio prolongado envia `[silencio prolongado]`. El backend lo
interpreta como una senal tecnica, no como contenido literal del paciente.
