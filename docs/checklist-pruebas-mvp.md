# Checklist de pruebas del MVP

## Preparacion

- Backend Spring Boot arrancado.
- Servicio FastAPI arrancado o fallback local aceptado.
- Supabase configurado si se prueba persistencia remota.
- App Android instalada en emulador o movil fisico.

## Flujo cuidador

- Abrir app y comprobar backend.
- Entrar en modo cuidador.
- Crear paciente.
- Editar paciente y comprobar que los cambios se mantienen al actualizar.
- Seleccionar paciente.
- Crear regla de bucle.
- Editar regla de bucle.
- Crear tema peligroso.
- Editar tema peligroso.
- Crear recuerdo seguro.
- Editar recuerdo seguro.
- Generar codigo de vinculacion.
- Eliminar una regla, tema o recuerdo de prueba si se quiere demostrar el borrado.

## Flujo paciente

- Entrar en modo paciente.
- Introducir codigo de vinculacion.
- Cerrar y volver a abrir modo paciente para comprobar que recuerda el dispositivo vinculado.
- Ver estado de espera.
- Confirmar que cambia a sesion activa cuando el cuidador inicia sesion.
- No enviar mensajes hasta que el estado mostrado sea `active`.
- Enviar mensaje escrito.
- Probar reconocimiento de voz si el emulador o movil lo permite.
- Escuchar respuesta por TextToSpeech.
- Confirmar que la voz TTS suena en espanol de Espana si el dispositivo tiene esa voz instalada.

## Conversacion y alertas

- Enviar una pregunta que coincida con una regla de bucle.
- Confirmar respuesta preconfigurada.
- Enviar mensaje con tema peligroso.
- Confirmar redireccion.
- Confirmar banner de alerta en modo cuidador.
- Confirmar notificacion Android si el permiso esta concedido.

## Diario

- Terminar sesion desde modo cuidador.
- Entrar en Diario.
- Actualizar sesiones.
- Abrir detalle de sesion.
- Revisar transcripcion.
- Revisar eventos.
- Revisar alertas asociadas.

## Problemas frecuentes durante la demo

- Si aparece error de conexion, comprobar `http://localhost:8080/api/health`.
- Si el paciente no recibe respuesta, comprobar que el backend esta encendido y que el estado del modo paciente es `active`.
- Si el reconocimiento de voz no escribe nada en el campo de texto, escribir una frase manualmente y pulsar **Enviar a memorIA**.
- Si la voz suena con acento ingles, instalar/seleccionar una voz espanola en Android: **Ajustes > Sistema > Idiomas e introduccion de texto > Sintesis de voz > Motor preferido / Instalar datos de voz > Espanol (Espana)**.
- Si el backend funciona pero la IA no, comprobar `http://localhost:8000/health`; el backend tiene respuesta local de respaldo para no bloquear la demo.

## Criterio de demo superada

La demo se considera superada si se puede mostrar de principio a fin:

1. Configuracion del paciente.
2. Vinculacion por codigo.
3. Sesion activa paciente-cuidador.
4. Respuesta por regla o IA.
5. Alerta al cuidador.
6. Diario con transcripcion, eventos y alertas.
