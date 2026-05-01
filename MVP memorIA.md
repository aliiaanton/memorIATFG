# MVP memorIA - Documento maestro del MVP

## 1. Resumen del proyecto

**memorIA** sera una aplicacion Android de teleasistencia inteligente dual para personas con demencia/Alzheimer y sus cuidadores.

El MVP se centrara en una version funcional, defendible y desarrollable en un mes: una terminal sencilla para el paciente y un panel de control para el cuidador, con IA conversacional controlada, configuracion CRUD, alertas, diario de actividad y transcripcion post-sesion.

La idea original completa se mantiene como vision futura, pero el desarrollo inicial se limitara a las funcionalidades descritas en este documento.

## 2. Objetivo del MVP

Permitir que un cuidador configure respuestas seguras, temas delicados y datos basicos del paciente para que una IA pueda mantener conversaciones simples, empaticas y supervisadas con el paciente.

El sistema debera detectar preguntas repetitivas, reconducir conversaciones ante temas peligrosos, avisar al cuidador mediante alertas en tiempo real y guardar un registro consultable de cada sesion.

## 3. Alcance funcional incluido

### 3.1 Autenticacion y perfiles

- Registro e inicio de sesion de cuidadores.
- Validacion de usuarios mediante Supabase Auth.
- Perfil basico del cuidador.
- Gestion de uno o varios pacientes asociados al cuidador.
- Asociacion entre dispositivo paciente y paciente mediante codigo de vinculacion.

### 3.2 Vinculacion por codigo

- El cuidador generara un codigo alfanumerico desde el modo cuidador.
- El modo paciente introducira ese codigo para vincularse con un paciente concreto.
- No se usara QR ni camara en el MVP.
- Una vez vinculado, el dispositivo paciente recordara la asociacion hasta que el cuidador lo desvincule.

### 3.3 Panel del cuidador

El cuidador podra gestionar mediante operaciones CRUD:

- Pacientes.
- Preguntas repetitivas.
- Respuestas preconfiguradas para esas preguntas.
- Palabras o temas peligrosos.
- Temas positivos o recuerdos seguros.

Tambien podra consultar y controlar:

- Estado actual de la sesion.
- Alertas generadas.
- Ultimos eventos relevantes.
- Diario de actividad.
- Transcripcion post-sesion.
- Controles de sesion: iniciar, pausar, reanudar y terminar.

### 3.4 Terminal del paciente

La terminal del paciente tendra una interfaz extremadamente sencilla, estilo telefono amable.

Estados principales:

- Esperando.
- Escuchando.
- Pensando.
- Respondiendo.
- Pausado.
- Finalizado.
- Error suave o redireccion.

La interaccion principal sera por voz, con feedback visual basico, estados grandes y sin menus complejos.

### 3.5 Logica conversacional con IA

El sistema debera:

- Recibir el texto transcrito de la voz del paciente.
- Detectar si el paciente repite una pregunta configurada por el cuidador.
- Responder con la respuesta predefinida correspondiente.
- Detectar palabras o temas peligrosos.
- Reconducir la conversacion hacia temas positivos.
- Usar un tono empatico, calmado y no confrontativo.
- Responder con IA cuando no exista una regla especifica aplicable.
- Guardar mensajes, eventos y alertas asociados a la sesion.

### 3.6 Alertas y tiempo real

El cuidador recibira alertas cuando ocurra un evento relevante.

Eventos incluidos en el MVP:

- Inicio de sesion.
- Pausa o reanudacion de sesion.
- Fin de sesion.
- Deteccion de palabra o tema peligroso.
- Deteccion de pregunta repetitiva.
- Error o fallo de comunicacion relevante.

El tiempo real del MVP se limitara a:

- Estado de sesion.
- Alertas.
- Ultimos eventos relevantes.

Las alertas se mostraran mediante:

- Banner visible en la pantalla de Inicio del cuidador.
- Registro permanente en el Diario.
- Notificacion Android cuando corresponda.

No se incluira transcripcion en directo para el cuidador durante el MVP.

### 3.7 Diario y transcripcion post-sesion

Al finalizar una sesion, el cuidador podra consultar:

- Fecha y hora de la sesion.
- Duracion aproximada.
- Mensajes transcritos del paciente.
- Respuestas generadas por el sistema.
- Eventos detectados.
- Alertas generadas.

Por privacidad y simplicidad, el MVP guardara texto, no audio completo.

## 4. Funcionalidades excluidas del MVP

Quedan fuera de esta primera version:

- Camara.
- Video en directo.
- Monitorizacion silenciosa.
- Deteccion de caidas.
- Deteccion de movimiento.
- Videollamada de emergencia.
- Avatar 3D.
- Reproduccion automatica de musica o videos.
- Subida de multimedia.
- Deteccion emocional avanzada.
- Transcripcion en vivo para el cuidador.
- Aplicacion iOS.
- Aplicacion web completa.

Estas funcionalidades podran plantearse como lineas futuras en la memoria del TFG.

## 5. UI/UX del MVP

### 5.1 Enfoque general

- Una unica app Android con dos modos: cuidador y paciente.
- Diseno principal para movil vertical.
- Adaptacion razonable del modo paciente para tablet si el tiempo lo permite.
- Estetica calmada, sencilla y accesible.
- Uso de Material 3 con Jetpack Compose.

### 5.2 Estilo visual

La direccion visual sera **primavera suave**:

- Verde salvia como color principal.
- Azul claro como color secundario.
- Coral suave como color de acento para alertas no criticas.
- Fondo claro casi blanco.
- Texto oscuro de alto contraste.

La interfaz debera sentirse cercana, tranquila y no infantil.

### 5.3 Flujo de entrada

- El cuidador inicia sesion.
- Desde el modo cuidador puede gestionar pacientes y generar codigos de vinculacion.
- El modo paciente introduce el codigo alfanumerico.
- El dispositivo paciente queda vinculado al paciente seleccionado.
- El cuidador inicia, pausa, reanuda o termina la sesion desde su interfaz.

### 5.4 Modo cuidador

El modo cuidador tendra navegacion inferior con 4 pestanas:

1. **Inicio**
   - Estado actual de la sesion.
   - Paciente activo.
   - Banner de alerta.
   - Ultimos eventos.
   - Controles de sesion: iniciar, pausar, reanudar y terminar.

2. **Pacientes**
   - Lista de pacientes.
   - Crear, editar y eliminar paciente.
   - Acceso a generar codigo de vinculacion para un paciente.

3. **Configuracion IA**
   - Seccion de bucles conversacionales.
   - Seccion de temas peligrosos.
   - Seccion de recuerdos o temas seguros.
   - Patron de lista mas pantalla de formulario para crear y editar.

4. **Diario**
   - Lista de sesiones.
   - Detalle de sesion.
   - Eventos, alertas y transcripcion post-sesion.

### 5.5 Modo paciente

El modo paciente tendra una experiencia estilo telefono amable:

- Pantalla de vinculacion por codigo.
- Pantalla de espera hasta que el cuidador inicie la sesion.
- Pantalla de sesion activa con estados grandes.
- Texto minimo: estado actual y una frase breve de la respuesta.
- Voz como canal principal.
- Sin menus complejos ni navegacion profunda.
- Pantalla de error suave si falla la conexion o el reconocimiento de voz.

### 5.6 Accesibilidad

El MVP incluira ajustes basicos:

- Tamano de texto del modo paciente.
- Velocidad de voz del TextToSpeech.

Se priorizara legibilidad, botones grandes y mensajes cortos.

## 6. Decisiones tecnologicas

### 6.1 Plataforma principal

El MVP se desarrollara como una aplicacion **Android nativa**.

Se hara una sola aplicacion con dos modos:

- Modo cuidador.
- Modo paciente.

Esta decision reduce duplicacion, facilita la demo y permite mantener un unico proyecto Android.

### 6.2 Frontend Android

Tecnologias elegidas:

- Kotlin.
- Jetpack Compose.
- Material 3.
- Android SpeechRecognizer para voz a texto.
- Android TextToSpeech para leer las respuestas generadas.

La aplicacion Android sera responsable de:

- Mostrar la interfaz del cuidador.
- Mostrar la terminal simplificada del paciente.
- Capturar la voz del paciente.
- Reproducir por voz la respuesta del sistema.
- Comunicarse con el backend principal.
- Mostrar alertas y estados en tiempo real.
- Mostrar notificaciones Android al cuidador.

### 6.3 Backend principal

Tecnologia elegida:

- Java.
- Spring Boot.

Spring Boot sera el backend principal y centralizara:

- API REST.
- Validacion del token de usuario.
- Logica de negocio.
- CRUD principal.
- Gestion de sesiones conversacionales.
- Deteccion de reglas configuradas.
- Registro de eventos.
- Generacion de alertas.
- Comunicacion con Supabase.
- Comunicacion con el microservicio de IA.

### 6.4 Microservicio de IA

Tecnologia elegida:

- Python.
- FastAPI.

El servicio de Python sera deliberadamente pequeno. Su responsabilidad principal sera encapsular la comunicacion con la API de IA.

No sera un segundo backend completo. Spring Boot seguira siendo el centro de la aplicacion.

### 6.5 Base de datos, autenticacion y tiempo real

Tecnologia elegida:

- Supabase.

Supabase se usara para:

- Base de datos remota PostgreSQL.
- Autenticacion de usuarios.
- Gestion de sesiones de autenticacion.
- Tiempo real para alertas y estado de sesion.

La app Android iniciara sesion mediante Supabase Auth. Spring Boot validara el JWT recibido antes de permitir operaciones protegidas.

### 6.6 Proveedor de IA

Proveedor elegido:

- Google Gemini API.

Motivos:

- Tiene una capa gratuita adecuada para desarrollo y pruebas.
- Ofrece buena calidad conversacional.
- Permite integracion sencilla desde un servicio backend.
- Es mas estable para el MVP que depender de creditos gratuitos limitados de Hugging Face.

La integracion se hara mediante una capa/adaptador para que en el futuro se pueda sustituir Gemini por otro proveedor si fuese necesario.

### 6.7 Despliegue

Durante el desarrollo:

- Android se ejecutara en emulador o dispositivo fisico.
- Spring Boot se ejecutara localmente.
- FastAPI se ejecutara localmente.
- Supabase estara en remoto.
- Gemini API se consumira desde el microservicio de IA.

Para la entrega final:

- Se intentara desplegar Spring Boot y FastAPI en el hosting/VPS ofrecido por el companero.
- Se valorara usar Docker y Docker Compose para facilitar despliegue.
- Supabase seguira funcionando como base de datos remota y servicio de autenticacion.

## 7. Valor academico del MVP

El proyecto cumple los requisitos minimos indicados para el TFG porque incluye:

- Aplicacion CRUD.
- Validacion de usuarios.
- Gestion de perfiles.
- Base de datos remota.
- Comunicacion entre dos interfaces.
- Alertas/notificaciones.
- Integracion con IA.
- Registro de actividad.
- Transcripcion post-sesion.
- Diseno centrado en accesibilidad y simplicidad cognitiva.
- Backend Java con Spring Boot.
- Microservicio Python especializado en IA.
- Aplicacion Android nativa.

Estado actual de implementacion:

- Registro e inicio de sesion de cuidadores integrado con Supabase Auth.
- Envio de JWT desde Android y validacion en Spring Boot cuando se activa `APP_SECURITY_ENABLED=true`.
- Perfil basico del cuidador creado desde el backend para el usuario autenticado.
- CRUD de pacientes y configuracion IA operativo desde Android.
- Vinculacion por codigo operativa.
- Gestion de dispositivos vinculados y desvinculacion desde el modo cuidador.
- Modo paciente con voz y lectura de respuestas.
- Alertas guardadas y visibles en el cuidador.
- Notificaciones Android para nuevas alertas.
- Diario con sesiones, transcripcion post-sesion, eventos y alertas por sesion.

## 8. Criterio de exito del MVP

El MVP se considerara funcional si permite demostrar el siguiente flujo:

1. Un cuidador se registra e inicia sesion.
2. Crea un perfil de paciente.
3. Configura preguntas repetitivas, respuestas y temas peligrosos.
4. Genera un codigo de vinculacion para el paciente.
5. El modo paciente introduce el codigo y queda vinculado.
6. El cuidador inicia una sesion.
7. El paciente habla con la IA.
8. El sistema transcribe la intervencion del paciente.
9. El sistema detecta bucles o temas peligrosos.
10. El sistema responde con una regla configurada o con IA.
11. El cuidador recibe alertas en tiempo real.
12. El cuidador puede pausar, reanudar o terminar la sesion.
13. Al terminar, el cuidador consulta el diario y la transcripcion de la sesion.

## 9. Plan por modulos

### Modulo 0 - Documentacion base

- Actualizar este documento con todas las decisiones actuales.
- Anadir seccion de plan modular.
- Dejar el documento como referencia principal para desarrollo y memoria.

Criterio de exito: el documento permite explicar que se va a construir, que no, con que tecnologias y en que orden.

### Modulo 1 - Estructura del monorepo

- Crear estructura raiz:
  - `backend/` para Spring Boot.
  - `ai-service/` para FastAPI.
  - `android/` para la app Kotlin.
  - `docs/` para documentacion auxiliar.
- Anadir `.gitignore`, README inicial y configuracion base.

Criterio de exito: el proyecto queda ordenado y preparado para desarrollo modular.

### Modulo 2 - Supabase, datos y auth

- Crear proyecto Supabase.
- Definir tablas iniciales: cuidadores, pacientes, reglas de bucle, temas peligrosos, recuerdos seguros, sesiones, mensajes, eventos y alertas.
- Configurar Supabase Auth.
- Preparar politicas minimas de acceso por usuario.

Criterio de exito: existe base de datos remota y login funcional.

### Modulo 3 - Backend Spring Boot

- Crear API REST principal.
- Validar JWT de Supabase.
- Implementar CRUD de pacientes, bucles, temas peligrosos y recuerdos.
- Implementar sesiones conversacionales, eventos y alertas.
- Exponer endpoints para la app Android.

Criterio de exito: el backend permite gestionar todos los datos del cuidador de forma segura.

### Modulo 4 - Microservicio IA FastAPI

- Crear servicio Python pequeno.
- Integrar Gemini API.
- Definir endpoint para generar respuesta empatica.
- Recibir contexto del paciente, recuerdos seguros y mensaje actual.
- Devolver respuesta limpia para Spring Boot.

Criterio de exito: Spring puede pedir una respuesta IA cuando no exista regla preconfigurada.

### Modulo 5 - App Android base

- Crear proyecto Kotlin con Jetpack Compose.
- Configurar tema visual primavera suave.
- Implementar navegacion base.
- Implementar login con Supabase.
- Crear selector de modo cuidador/paciente.

Criterio de exito: la app abre, permite login y navega entre modos.

### Modulo 6 - Modo cuidador

- Implementar navegacion inferior con 4 pestanas.
- Inicio: estado de sesion, alertas, ultimos eventos y controles.
- Pacientes: lista y formularios CRUD.
- Configuracion IA: bucles, temas peligrosos y recuerdos seguros.
- Diario: lista de sesiones y detalle.

Criterio de exito: el cuidador puede configurar todo el MVP desde Android.

### Modulo 7 - Modo paciente

- Implementar vinculacion por codigo.
- Recordar dispositivo vinculado.
- Pantalla de espera hasta inicio de sesion.
- Pantalla de conversacion con estados: escuchando, pensando, respondiendo, pausado y finalizado.
- Integrar SpeechRecognizer y TextToSpeech.

Criterio de exito: el paciente puede participar en una sesion guiada por voz.

### Modulo 8 - Logica conversacional y alertas

- Procesar cada mensaje del paciente en Spring Boot.
- Detectar preguntas repetitivas.
- Detectar temas peligrosos.
- Decidir entre respuesta preconfigurada o respuesta IA.
- Guardar mensajes, eventos y alertas.
- Enviar cambios de estado y alertas en tiempo real mediante Supabase.
- Mostrar notificacion Android al cuidador.

Criterio de exito: se demuestra el flujo completo de bucle, redireccion, alerta y respuesta.

### Modulo 9 - Diario y transcripcion

- Guardar turnos de conversacion como texto.
- Asociar mensajes, eventos y alertas a una sesion.
- Mostrar detalle post-sesion al cuidador.
- No guardar audio completo.

Criterio de exito: al finalizar una sesion se puede revisar conversacion, eventos y alertas.

### Modulo 10 - Despliegue, pruebas y memoria

- Preparar configuracion local estable.
- Anadir Docker para Spring Boot y FastAPI si encaja con el VPS.
- Desplegar en hosting/VPS del companero si esta disponible.
- Hacer pruebas del flujo completo.
- Documentar arquitectura, tecnologias, limitaciones y lineas futuras.

Criterio de exito: el MVP puede ensenarse de principio a fin y explicarse en la memoria.

## 10. Orden de trabajo recomendado

1. Documento MVP actualizado.
2. Monorepo.
3. Supabase.
4. Spring Boot CRUD.
5. Android login y navegacion.
6. Modo cuidador.
7. FastAPI + Gemini.
8. Modo paciente con voz.
9. Alertas y tiempo real.
10. Diario, pruebas, despliegue y memoria.

## 11. Decisiones pendientes

Quedan pendientes para la fase final:

- Probar autenticacion real extremo a extremo con credenciales definitivas de Supabase en emulador y movil fisico.
- Preparar despliegue final de Spring Boot y FastAPI.
- Valorar Docker/Docker Compose para el VPS si encaja con el entorno disponible.
- Hacer pruebas completas con emulador y, si es posible, movil fisico.
- Documentar limitaciones, pruebas, arquitectura y lineas futuras en la memoria.

## 12. Supuestos de trabajo

- El MVP se desarrollara en un mes, priorizando funcionalidad demostrable.
- Android sera la plataforma principal.
- No se desarrollara iOS ni web completa.
- El despliegue en VPS sera objetivo final, pero no bloqueara el desarrollo local.
- Si una integracion avanzada pone en riesgo el plazo, se mantendra una version funcional simple y documentada.
