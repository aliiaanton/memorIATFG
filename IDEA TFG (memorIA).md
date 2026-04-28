# <a name="_wox18gmkd8re"></a>**RESUMEN IDEA TFG (memorIA)**
### <a name="_h1uwe416o58"></a>**1. Descripción**
La idea del trabajo podría definirse como una **solución de teleasistencia inteligente dual**. Su propósito principal es ofrecer "tiempo de calidad y respiro mental" a cuidadores, en principio de personas con alzheimer y demencia, permitiéndole delegar la interacción repetitiva en una Inteligencia Artificial, manteniendo siempre el control y la supervisión.

El sistema consta de dos interfaces sincronizadas:

1. **Terminal del Paciente (tablet/móvil):** Un asistente virtual con avatar visual.
1. **Terminal del Cuidador (tablet/móvil):** Un centro de control, configuración y monitoreo en tiempo real.

### <a name="_2t0gs938qfjm"></a>**2. Requisitos Funcionales (RF)**
#### <a name="_cw10dbed2i3i"></a>**A. Módulo de Lógica Conversacional**
- **RF1. Gestión de "Bucles" conversacionales:**
  - El sistema debe identificar preguntas que el paciente repite compulsivamente (ej: "¿Dónde está mi hijo Juan?").
  - La IA responderá automáticamente con una respuesta pre-configurada por el cuidador (ej: "Juan está trabajando y vendrá a cenar luego"), repitiéndola con infinita paciencia y consistencia tantas veces como sea necesario.
- **RF2. Filtros de "Temas Prohibidos":**
  - La IA debe tener una lista negra de temas (ej: personas fallecidas, dinero, conflictos pasados).
  - Si el paciente intenta hablar de estos temas, la IA debe desviar la conversación suavemente hacia un tema positivo pre-aprobado.
- **RF3. Personalización Profunda de Intereses:**
  - Base de datos de temas positivos (pueblos, oficios antiguos, aficiones) para mantener al paciente en un estado de ánimo seguro.
  - Posibilidad de subir audios propios (canciones o audios hablando) para que puedan ser utilizados por la IA para reconducir en caso de ser necesario.
- **RF4. Adaptación Empática:**
  - La IA debe usar un tono validante, nunca confrontativo. Si el paciente dice algo incorrecto pero inofensivo, la IA debe seguir la corriente o redirigir, nunca corregir bruscamente.


#### <a name="_1jstpb5afoal"></a>**A. Módulo de Supervisión y Telepresencia (Terminal del cuidador**
Funcionalidades diseñadas para otorgar tranquilidad al cuidador, permitiéndole monitorizar el bienestar del paciente de forma remota y no intrusiva.

- **RF5. Monitorización de Video "Silenciosa" en Tiempo Real:**
  - El cuidador debe poder activar la cámara y el micrófono de la tablet en cualquier momento para verificar el estado del paciente.
  - Esta acción debe ser **imperceptible** para el paciente (sin ruidos ni luces de aviso en la tablet) para no alterar su conducta ni interrumpir su interacción con la IA.
- **RF6. Intervención de Emergencia (Modo "Intercomunicador"):**
  - Botón de acceso rápido ("Intervenir Ahora") que transforma inmediatamente la sesión en una videollamada bidireccional.
  - Esto permite al cuidador hacerse presente con voz e imagen instantáneamente para calmar al paciente, resolver una duda urgente o gestionar una crisis.
- **RF7. Sistema de Alertas Inteligentes y Configurables (Movimiento y Audio):**
  - **Detección de Anomalías Visuales:** El sistema utilizará la cámara para identificar cambios de estado críticos: paciente poniéndose de pie, caídas o abandono del encuadre (salida de la habitación).
  - **Detección de Anomalías Acústicas:** El sistema monitorizará el nivel de decibelios y el tono de voz. Debe ser capaz de enviar una alerta si detecta:
    - Un aumento brusco del volumen (gritos o llamadas de auxilio).
    - Ruidos fuertes repentinos (objetos cayendo o golpes).
  - **Gestión Granular de Notificaciones:** Para evitar la fatiga por alarmas, el cuidador debe tener un panel de control donde pueda **activar o desactivar** cada tipo de alerta individualmente según sus necesidades del momento (ej: "Avísame si se cae, pero no si habla alto" o "Silenciar todas las alertas de movimiento").

#### <a name="_b8imxqt3sp8v"></a>**C. Módulo de Interacción (Terminal del Paciente)**
- **RF9. Interfaz Visual Adaptable (Avatar vs. Audio):**
  - **Modo Avatar (Predeterminado):** La pantalla debe mostrar un personaje virtual ("muñeco" o humano estilizado) amigable, con expresiones faciales suaves. Esto busca generar confianza y la sensación de compañía.
  - **Modo "Solo Audio" (Estilo Teléfono/Radio):** El sistema debe permitir ocultar completamente el avatar si este genera rechazo, miedo o confusión en el paciente (configurable por cuidador). En este modo, la pantalla mostrará una interfaz minimalista simulando una llamada de voz tradicional.
- **RF10. Gestión de Estímulos y Actualización Dinámica:**
  - **Estímulos Automáticos:** Si la conversación decae o se detecta apatía, el sistema debe reproducir automáticamente música nostálgica o videos cargados previamente para reactivar la atención.
  - **Recepción en Tiempo Real:** La terminal debe ser capaz de recibir cambios enviados por el cuidador desde su móvil (ej: cambiar de tema de conversación o poner una canción específica) y aplicarlos **instantáneamente** sin pausar la sesión ni requerir reinicios, manteniendo la fluidez de la experiencia.
- **RF11. Interacción "Cero Toque" (Manos Libres):**
  - La interacción debe ser puramente vocal y visual. El paciente no debe necesitar pulsar ningún botón para responder o activar la escucha.
  - La interfaz debe carecer de elementos complejos (menús, botones de "atrás" o "cerrar") para evitar que el paciente cierre la aplicación o se pierda por error, eliminando la frustración motriz y cognitiva.

### <a name="_o6iiwfaohcw0"></a>**3. Requisitos No Funcionales (RNF)**
- **RNF1. Latencia de Video Crítica:** La transmisión de video entre la tablet y el móvil debe tener un retraso mínimo para permitir una intervención de emergencia efectiva.
- **RNF2. Modelo de Distribución SaaS (Software as a Service):** La arquitectura debe soportar un sistema de cuentas basado en la nube, donde la configuración del paciente se guarda en el servidor, permitiendo cambiar de dispositivo sin perder los datos (ideal para el modelo de suscripción).
- **RNF3. Robustez ante Desconexiones:** Si falla el Wi-Fi, la tablet debe ser capaz de mantener la conversación básica (modo local) y reconectar el video automáticamente cuando vuelva la red.
- **RNF4. Privacidad Ética:** Dado que hay transmisión de video, el sistema debe garantizar que las imágenes no se graban ni almacenan en servidores externos, solo se transmiten en vivo punto a punto (P2P) por seguridad del paciente.
- **RNF5. Consumo de Batería Optimizado:** Dado que la aplicación usa IA, cámara y pantalla simultáneamente, el software debe estar optimizado para no drenar la batería de la tablet en menos de 2 horas (tiempo promedio de un "respiro" para el cuidador).

  ### <a name="_owolnygvu4u1"></a>**Resumen del Flujo (Caso de Uso Real)**
1. **Inicio:** María (cuidadora) sienta a su madre frente a la tablet y activa la App. Aparece el Avatar sonriendo.
1. **El Respiro:** María se va a la cocina a leer un libro.
1. **El Bucle:** La madre pregunta a la tablet: "¿Dónde está María?".
1. **La IA Responde (RF1):** "María está en la cocina preparando una sorpresa para la cena, no te preocupes. ¿Te acuerdas de la receta de rosquillas que hacías?".
1. **La Alerta:** La madre intenta levantarse bruscamente.
1. **La Acción (RF7):** El móvil de María vibra: "Alerta de Movimiento". María mira la pantalla de su móvil, ve que su madre solo buscaba el mando de la tele, y se relaja sin tener que ir al salón.
1. **Intervención (RF6):** Si la madre se pusiera a llorar, María pulsaría "Hablar" y su cara aparecería en la tablet para calmarla al instante.


# <a name="_j5tqwbkijmud"></a>**INTERFACES IDEA**
## <a name="_2w554q2wwst7"></a>**1. Flujo de Entrada (Común)**
### <a name="_2vy8u0rxnfph"></a>**A. La Pantalla de Bienvenida**
- **Diseño:** Minimalista, fondo limpio con el logotipo en el centro superior.
- **Elementos:**
  - **Botón de Ajustes (Engranaje):** Discreto en la esquina superior derecha (para accesibilidad: tamaño de letra, contraste).
  - **Botón A (Principal):** "Soy Cuidador". 
  - **Botón B (Secundario):** "Modo Pantalla del Paciente"
  - **Pie de página:** Enlace de texto simple: "¿Ya tienes cuenta? Iniciar Sesión".

### <a name="_oi1jebtmoetx"></a>**B. El Proceso de Conexión ("Pairing")**
**Paso 1: En la Tablet (Tras pulsar "Modo Paciente")**

- **Estado:** La pantalla muestra el mensaje "Esperando vinculación".
- **Elemento Central:** Un **Código QR grande** y nítido.
- **Respaldo:** Un código alfanumérico pequeño debajo (ej: AB-123) por si la cámara falla.

**Paso 2: En el Móvil (Tras pulsar "Soy Cuidador")**

- **Acción:** Tras el registro/login, el asistente solicita permiso de cámara.
- **Interfaz:** Un marco de escaneo sobre la cámara.
- **Feedback:** Al reconocer el QR, el móvil vibra y pasa al Dashboard. La tablet cambia automáticamente a la "Pantalla de Espera" del paciente.
##
## <a name="_db6m1t5cqqr9"></a><a name="_hga6difdgio1"></a>**2. Interfaz del Cuidador**
### <a name="_8dyoeav5qywk"></a>**Estructura: 4 Pestañas de Navegación Inferior**
#### <a name="_tnfleslg7okv"></a>**Pantalla 1: Dashboard (Inicio y Monitorización)**
- **Semáforo de Estado (Header):** Una barra superior que cambia de color y texto según la situación en tiempo real:
  - 🟢 **Verde:** "Todo tranquilo. Interactuando suavemente".
  - 🟠 **Naranja:** "Emoción negativa detectada / Ruido moderado". (La IA está actuando).
  - 🔴 **Rojo:** "ALERTA: Caída, Grito o Salida de cámara".
- **Visor:** Recuadro central. Por defecto está **borroso** (privacidad).
  - *Acción:* Un toque sobre el recuadro activa el video en vivo y audio (modo espía/silencioso).
- **Botón de Emergencia:** Botón flotante rojo: "Intervenir (Video/Audio)".

#### <a name="_ho7a4lm06zi2"></a>**Pantalla 2: Personalización IA**
- **Sub-pestaña: "Romper Bucles"**
  - Lista de tarjetas editables. Botón flotante "+" para añadir nueva.
  - *Input:* "Si pregunta..." (ej: ¿Dónde está mi madre?).
  - *Input:* "Responder exactamente..." (ej: Tu madre te cuida desde el cielo, no te preocupes).
- **Sub-pestaña: "Gustos y Temas"**
  - Sistema de **Etiquetas (Tags)**.
  - *Gustos:* Campo para escribir y añadir etiquetas verdes (ej: #Pueblo, #Costura, #ManoloEscobar).
  - *Temas Prohibidos:* Campo para etiquetas rojas (ej: #Dinero, #Hospital, #Fallecidos).
  - (Tal vez se podrían meter historias/recuerdos positivos que la IA pueda mencionar o contar a la persona)
- **Sub-pestaña: "Multimedia"**
  - Lista simple de canciones subidas con botones de "Play" para pre-escuchar y un icono de papelera para borrar.

#### <a name="_82z0ey4pk4fu"></a>**Pantalla 3: Ajustes del Sistem**
**A. Sección: Preferencias de Interfaz del Paciente**

- **Selector de Modo Visual:** Un control tipo "Switch" o dos tarjetas seleccionables:
  - Opción 1: **"Modo Avatar"** (Muestra el personaje 3D).
  - Opción 2: **"Modo Teléfono"** (Muestra un icono estático, para pacientes que rechazan el avatar).

**B. Sección: Centro de Notificaciones (Gestión Granular)**

- *Texto:* "Elige qué situaciones deben enviarte una alerta al móvil."
- **Interruptor 1:** "Detección de Caída / Movimiento Brusco" [ON/OFF].
- **Interruptor 2:** "Paciente sale del encuadre" [ON/OFF].
- **Interruptor 3:** "Detección de Voz Alta / Gritos" [ON/OFF]
- **Interruptor 4:** "Cambio de Humor (Tristeza/Llanto)" [ON/OFF]
#### <a name="_v4nzf6qd1ua7"></a>**Pantalla 4: Diario (Registro)**
- Historial de última conversación
- Muestra eventos automáticos ("10:00 - Canción reproducida") y alertas pasadas ("11:30 - Ruido fuerte detectado").

## <a name="_40ijdyn5ozj"></a>**3. Interfaz del Paciente** 
### <a name="_bkpu92nw85u1"></a>**Estados Visuales (Automáticos)**
#### <a name="_cler9km22asc"></a>**Estado A: Modo Espera (Standby)**
- **Botón:** Para activar una sesión de conversación con la IA 
#### <a name="_gga4cgb076z3"></a>**Estado B: Modo Conversación (Activo)**
- **Opción 1 (Modo Avatar):**
  - Avatar en el centro
- **Opción 2 (Modo Teléfono/Radio):**
  - Fondo oscuro o de color plano relajante.
  - Imagen central estática o texto
#### <a name="_3nytj4ai84tb"></a>**Estado C: Modo Intervención (Videollamada)**
- Interrupción de la IA.
- **Video Full-Screen:** Aparece la cara del cuidador ocupando toda la pantalla.
- **Audio:** Se activa el altavoz al máximo volumen configurado.
- **Sin controles:** No hay botones de colgar para evitar cortes accidentales. Solo el cuidador puede finalizar la llamada.
##
## <a name="_74p6r5teaxjv"></a><a name="_bab6kq284pxy"></a>**Resumen de Pantallas (Mapa de Navegación)**
1. **Inicio (Común):** Splash -> Selector de Rol.
1. **Configuración Inicial:** QR en una terminal <-> Escáner en la otra
1. **App Cuidador:**
   1. Dashboard (Semáforo + Video Oculto).
   1. Personalización (Bucles, Gustos, Música).
   1. Ajustes Sistema (Selector Avatar/Teléfono, Notificaciones Granulares).
   1. Diario de Actividad.
   1. Vista Cámara (Pantalla completa).
1. **App Paciente:**
   1. Standby (Fotos).
   1. Activo (Avatar O Teléfono Vintage).
   1. Intervención (Video del familiar).

