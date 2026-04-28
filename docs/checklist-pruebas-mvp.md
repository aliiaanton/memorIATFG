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
- Seleccionar paciente.
- Crear regla de bucle.
- Crear tema peligroso.
- Crear recuerdo seguro.
- Generar codigo de vinculacion.

## Flujo paciente

- Entrar en modo paciente.
- Introducir codigo de vinculacion.
- Ver estado de espera.
- Confirmar que cambia a sesion activa cuando el cuidador inicia sesion.
- No enviar mensajes hasta que el estado mostrado sea `active`.
- Enviar mensaje escrito.
- Probar reconocimiento de voz si el emulador o movil lo permite.
- Escuchar respuesta por TextToSpeech.

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
- Si el backend funciona pero la IA no, comprobar `http://localhost:8000/health`; el backend tiene respuesta local de respaldo para no bloquear la demo.

## Criterio de demo superada

La demo se considera superada si se puede mostrar de principio a fin:

1. Configuracion del paciente.
2. Vinculacion por codigo.
3. Sesion activa paciente-cuidador.
4. Respuesta por regla o IA.
5. Alerta al cuidador.
6. Diario con transcripcion, eventos y alertas.
