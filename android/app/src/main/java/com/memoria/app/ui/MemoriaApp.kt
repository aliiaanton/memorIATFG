package com.memoria.app.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.memoria.app.notifications.AlertNotifier
import com.memoria.app.data.AlertSummary
import com.memoria.app.data.ConversationMessageSummary
import com.memoria.app.data.DangerousTopicSummary
import com.memoria.app.data.LoopRuleSummary
import com.memoria.app.data.MemoriaApiClient
import com.memoria.app.data.PatientSummary
import com.memoria.app.data.SafeMemorySummary
import com.memoria.app.data.SessionEventSummary
import com.memoria.app.data.SessionSummary
import com.memoria.app.data.TerminalStatus
import com.memoria.app.data.runAsync
import kotlinx.coroutines.delay
import java.util.Locale

private enum class AppScreen {
    Login,
    ModeSelector,
    Caregiver,
    Patient
}

private val SpanishSpainLocale: Locale = Locale.forLanguageTag("es-ES")

@Composable
fun MemoriaApp() {
    val api = remember { MemoriaApiClient() }
    var screen by rememberSaveable { mutableStateOf(AppScreen.Login.name) }

    when (AppScreen.valueOf(screen)) {
        AppScreen.Login -> LoginScreen(api = api, onLogin = { screen = AppScreen.ModeSelector.name })
        AppScreen.ModeSelector -> ModeSelectorScreen(
            onCaregiver = { screen = AppScreen.Caregiver.name },
            onPatient = { screen = AppScreen.Patient.name },
            onBack = { screen = AppScreen.Login.name }
        )
        AppScreen.Caregiver -> CaregiverShell(api = api, onBack = { screen = AppScreen.ModeSelector.name })
        AppScreen.Patient -> PatientShell(api = api, onBack = { screen = AppScreen.ModeSelector.name })
    }
}

@Composable
private fun LoginScreen(api: MemoriaApiClient, onLogin: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var backendStatus by rememberSaveable { mutableStateOf("Sin comprobar") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "memorIA",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Teleasistencia inteligente para cuidar con calma.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            StatusBanner("Backend", backendStatus)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contrasena") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                backendStatus = "Comprobando conexion..."
                runAsync(
                    action = { api.health() },
                    onSuccess = { backendStatus = "Conectado: $it" },
                    onError = { backendStatus = "No conectado: $it" }
                )
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Comprobar backend")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Entrar")
            }
        }
    }
}

@Composable
private fun ModeSelectorScreen(onCaregiver: () -> Unit, onPatient: () -> Unit, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Elige modo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onCaregiver, modifier = Modifier.fillMaxWidth()) {
                Text("Modo cuidador")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onPatient, modifier = Modifier.fillMaxWidth()) {
                Text("Modo paciente")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaregiverShell(api: MemoriaApiClient, onBack: () -> Unit) {
    val context = LocalContext.current
    val alertNotifier = remember { AlertNotifier(context) }
    val tabs = listOf("Inicio", "Pacientes", "IA", "Diario")
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var patients by remember { mutableStateOf<List<PatientSummary>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<PatientSummary?>(null) }
    var activeSession by remember { mutableStateOf<SessionSummary?>(null) }
    var alerts by remember { mutableStateOf<List<AlertSummary>>(emptyList()) }
    var loopRules by remember { mutableStateOf<List<LoopRuleSummary>>(emptyList()) }
    var dangerousTopics by remember { mutableStateOf<List<DangerousTopicSummary>>(emptyList()) }
    var safeMemories by remember { mutableStateOf<List<SafeMemorySummary>>(emptyList()) }
    var sessions by remember { mutableStateOf<List<SessionSummary>>(emptyList()) }
    var transcript by remember { mutableStateOf<List<ConversationMessageSummary>>(emptyList()) }
    var events by remember { mutableStateOf<List<SessionEventSummary>>(emptyList()) }
    var pairingCode by rememberSaveable { mutableStateOf("") }
    var statusMessage by rememberSaveable { mutableStateOf("Cargando datos...") }
    var notifiedAlertKeys by remember { mutableStateOf<Set<String>>(emptySet()) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // If the user denies it, in-app banners and diary still keep the alert visible.
    }

    fun refreshPatients() {
        runAsync(
            action = { api.listPatients() },
            onSuccess = { loaded ->
                patients = loaded
                selectedPatient = selectedPatient ?: loaded.firstOrNull()
                statusMessage = if (loaded.isEmpty()) "Crea un paciente para empezar." else "Datos cargados."
            },
            onError = { statusMessage = "Error cargando pacientes: $it" }
        )
    }

    fun refreshAlerts(updateStatus: Boolean = true) {
        runAsync(
            action = { api.listAlerts() },
            onSuccess = { loaded ->
                alerts = loaded
                loaded.firstOrNull()?.let { alert ->
                    val key = alert.title + alert.message
                    if (!notifiedAlertKeys.contains(key)) {
                        alertNotifier.notifyAlert(alert)
                        notifiedAlertKeys = notifiedAlertKeys + key
                    }
                }
            },
            onError = {
                if (updateStatus) {
                    statusMessage = "Error cargando alertas: $it"
                }
            }
        )
    }

    fun refreshAiConfig(patient: PatientSummary? = selectedPatient) {
        val current = patient ?: return
        runAsync(
            action = {
                Triple(
                    api.listLoopRules(current.id),
                    api.listDangerousTopics(current.id),
                    api.listSafeMemories(current.id)
                )
            },
            onSuccess = {
                loopRules = it.first
                dangerousTopics = it.second
                safeMemories = it.third
                statusMessage = "Configuracion IA actualizada."
            },
            onError = { statusMessage = "Error cargando configuracion IA: $it" }
        )
    }

    fun refreshSessions(patient: PatientSummary? = selectedPatient, updateStatus: Boolean = true) {
        val current = patient ?: return
        runAsync(
            action = { api.listSessions(current.id) },
            onSuccess = {
                sessions = it
                if (updateStatus) {
                    statusMessage = "Diario actualizado."
                }
            },
            onError = {
                if (updateStatus) {
                    statusMessage = "Error cargando sesiones: $it"
                }
            }
        )
    }

    fun refreshTranscript(session: SessionSummary? = activeSession ?: sessions.firstOrNull()) {
        val current = session ?: return
        runAsync(
            action = { Pair(api.listTranscript(current.id), api.listEvents(current.id)) },
            onSuccess = {
                transcript = it.first
                events = it.second
            },
            onError = { statusMessage = "Error cargando detalle de sesion: $it" }
        )
    }

    LaunchedEffect(Unit) {
        alertNotifier.ensureChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        refreshPatients()
        refreshAlerts()
    }

    LaunchedEffect(selectedPatient?.id) {
        while (true) {
            refreshAlerts(updateStatus = false)
            refreshSessions(updateStatus = false)
            delay(5000)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Modo cuidador") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> CaregiverHome(
                    selectedPatient = selectedPatient,
                    activeSession = activeSession,
                    latestAlert = alerts.firstOrNull(),
                    statusMessage = statusMessage,
                    onStart = {
                        val patient = selectedPatient ?: return@CaregiverHome
                        statusMessage = "Iniciando sesion..."
                        runAsync(
                            action = {
                                val created = api.createSession(patient.id)
                                api.startSession(created.id)
                            },
                            onSuccess = {
                                activeSession = it
                                statusMessage = "Sesion activa."
                            },
                            onError = { statusMessage = "Error iniciando sesion: $it" }
                        )
                    },
                    onPause = {
                        val session = activeSession ?: return@CaregiverHome
                        runAsync(
                            action = { api.pauseSession(session.id) },
                            onSuccess = {
                                activeSession = it
                                statusMessage = "Sesion pausada."
                            },
                            onError = { statusMessage = "Error pausando sesion: $it" }
                        )
                    },
                    onResume = {
                        val session = activeSession ?: return@CaregiverHome
                        runAsync(
                            action = { api.resumeSession(session.id) },
                            onSuccess = {
                                activeSession = it
                                statusMessage = "Sesion reanudada."
                            },
                            onError = { statusMessage = "Error reanudando sesion: $it" }
                        )
                    },
                    onEnd = {
                        val session = activeSession ?: return@CaregiverHome
                        runAsync(
                            action = { api.endSession(session.id) },
                            onSuccess = {
                                activeSession = it
                                statusMessage = "Sesion terminada."
                                refreshAlerts()
                            },
                            onError = { statusMessage = "Error terminando sesion: $it" }
                        )
                    },
                    onRefreshAlerts = { refreshAlerts() }
                )
                1 -> PatientsTab(
                    patients = patients,
                    selectedPatient = selectedPatient,
                    pairingCode = pairingCode,
                    statusMessage = statusMessage,
                    onSelectPatient = {
                        selectedPatient = it
                        pairingCode = ""
                        refreshAiConfig(it)
                        refreshSessions(it)
                    },
                    onCreatePatient = { fullName, preferredName, notes, onSaved ->
                        statusMessage = "Creando paciente..."
                        runAsync(
                            action = { api.createPatient(fullName, preferredName, notes) },
                            onSuccess = {
                                patients = listOf(it) + patients.filterNot { patient -> patient.id == it.id }
                                selectedPatient = it
                                statusMessage = "Paciente creado."
                                onSaved()
                                refreshPatients()
                                refreshAiConfig(it)
                                refreshSessions(it)
                            },
                            onError = { statusMessage = "Error creando paciente: $it" }
                        )
                    },
                    onDeletePatient = { patient ->
                        statusMessage = "Eliminando paciente..."
                        runAsync(
                            action = { api.deletePatient(patient.id) },
                            onSuccess = {
                                selectedPatient = null
                                pairingCode = ""
                                activeSession = null
                                loopRules = emptyList()
                                dangerousTopics = emptyList()
                                safeMemories = emptyList()
                                sessions = emptyList()
                                transcript = emptyList()
                                events = emptyList()
                                statusMessage = "Paciente eliminado."
                                refreshPatients()
                            },
                            onError = { statusMessage = "Error eliminando paciente: $it" }
                        )
                    },
                    onGenerateCode = {
                        val patient = selectedPatient ?: return@PatientsTab
                        runAsync(
                            action = { api.createPairingCode(patient.id) },
                            onSuccess = {
                                pairingCode = it.code
                                statusMessage = "Codigo generado."
                            },
                            onError = { statusMessage = "Error generando codigo: $it" }
                        )
                    }
                )
                2 -> AiConfigTab(
                    selectedPatient = selectedPatient,
                    loopRules = loopRules,
                    dangerousTopics = dangerousTopics,
                    safeMemories = safeMemories,
                    onRefresh = { refreshAiConfig() },
                    onCreateLoopRule = { question, answer ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Creando regla de bucle..."
                        runAsync(
                            action = { api.createLoopRule(patient.id, question, answer) },
                            onSuccess = {
                                statusMessage = "Regla creada."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error creando regla: $it" }
                        )
                    },
                    onCreateDangerousTopic = { term, redirect ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Creando tema peligroso..."
                        runAsync(
                            action = { api.createDangerousTopic(patient.id, term, redirect) },
                            onSuccess = {
                                statusMessage = "Tema peligroso creado."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error creando tema: $it" }
                        )
                    },
                    onCreateSafeMemory = { title, content ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Creando recuerdo seguro..."
                        runAsync(
                            action = { api.createSafeMemory(patient.id, title, content) },
                            onSuccess = {
                                statusMessage = "Recuerdo creado."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error creando recuerdo: $it" }
                        )
                    },
                    onDeleteLoopRule = { rule ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        runAsync(
                            action = { api.deleteLoopRule(patient.id, rule.id) },
                            onSuccess = { refreshAiConfig(patient) },
                            onError = { statusMessage = "Error eliminando regla: $it" }
                        )
                    },
                    onDeleteDangerousTopic = { topic ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        runAsync(
                            action = { api.deleteDangerousTopic(patient.id, topic.id) },
                            onSuccess = { refreshAiConfig(patient) },
                            onError = { statusMessage = "Error eliminando tema: $it" }
                        )
                    },
                    onDeleteSafeMemory = { memory ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        runAsync(
                            action = { api.deleteSafeMemory(patient.id, memory.id) },
                            onSuccess = { refreshAiConfig(patient) },
                            onError = { statusMessage = "Error eliminando recuerdo: $it" }
                        )
                    }
                )
                3 -> DiaryTab(
                    activeSession = activeSession,
                    sessions = sessions,
                    transcript = transcript,
                    events = events,
                    alerts = alerts,
                    onRefresh = {
                        refreshSessions()
                        refreshAlerts()
                        refreshTranscript()
                    },
                    onLoadTranscript = {
                        activeSession = it
                        refreshTranscript(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun CaregiverHome(
    selectedPatient: PatientSummary?,
    activeSession: SessionSummary?,
    latestAlert: AlertSummary?,
    statusMessage: String,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onRefreshAlerts: () -> Unit
) {
    SectionTitle("Estado de sesion")
    StatusBanner(
        title = activeSession?.status ?: "Sin sesion activa",
        body = selectedPatient?.fullName ?: "Selecciona o crea un paciente."
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(statusMessage)
    Spacer(modifier = Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onStart, enabled = selectedPatient != null) { Text("Iniciar") }
        Button(onClick = onPause, enabled = activeSession != null) { Text("Pausar") }
        Button(onClick = onResume, enabled = activeSession != null) { Text("Reanudar") }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onEnd, enabled = activeSession != null, modifier = Modifier.fillMaxWidth()) {
        Text("Terminar sesion")
    }
    Spacer(modifier = Modifier.height(24.dp))
    SectionTitle("Ultima alerta")
    if (latestAlert == null) {
        SimpleCard("Sin alertas", "Cuando se detecte un bucle o tema delicado aparecera aqui.")
    } else {
        SimpleCard(latestAlert.title, latestAlert.message)
    }
    Spacer(modifier = Modifier.height(12.dp))
    TextButton(onClick = onRefreshAlerts, modifier = Modifier.fillMaxWidth()) {
        Text("Actualizar alertas")
    }
}

@Composable
private fun PatientsTab(
    patients: List<PatientSummary>,
    selectedPatient: PatientSummary?,
    pairingCode: String,
    statusMessage: String,
    onSelectPatient: (PatientSummary) -> Unit,
    onCreatePatient: (String, String, String, () -> Unit) -> Unit,
    onDeletePatient: (PatientSummary) -> Unit,
    onGenerateCode: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var preferredName by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    SectionTitle("Pacientes")
    StatusBanner("Estado", statusMessage)
    Spacer(modifier = Modifier.height(16.dp))
    if (patients.isEmpty()) {
        SimpleCard("Sin pacientes", "Crea un paciente para probar el flujo completo.")
    } else {
        patients.forEach { patient ->
            SimpleCard(
                title = if (patient.id == selectedPatient?.id) "${patient.fullName} - seleccionado" else patient.fullName,
                body = "Nombre preferido: ${patient.preferredName ?: patient.fullName}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSelectPatient(patient) }, modifier = Modifier.weight(1f)) {
                    Text("Seleccionar")
                }
                TextButton(onClick = { onDeletePatient(patient) }, modifier = Modifier.weight(1f)) {
                    Text("Eliminar")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    SectionTitle("Nuevo paciente")
    OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        label = { Text("Nombre completo") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = preferredName,
        onValueChange = { preferredName = it },
        label = { Text("Nombre preferido") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notas para la IA") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = {
            onCreatePatient(fullName.trim(), preferredName.trim(), notes.trim()) {
                fullName = ""
                preferredName = ""
                notes = ""
            }
        },
        enabled = fullName.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Crear paciente")
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onGenerateCode, enabled = selectedPatient != null, modifier = Modifier.fillMaxWidth()) {
        Text("Generar codigo de vinculacion")
    }
    if (pairingCode.isNotBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        StatusBanner("Codigo paciente", pairingCode)
    }
}

@Composable
private fun AiConfigTab(
    selectedPatient: PatientSummary?,
    loopRules: List<LoopRuleSummary>,
    dangerousTopics: List<DangerousTopicSummary>,
    safeMemories: List<SafeMemorySummary>,
    onRefresh: () -> Unit,
    onCreateLoopRule: (String, String) -> Unit,
    onCreateDangerousTopic: (String, String) -> Unit,
    onCreateSafeMemory: (String, String) -> Unit,
    onDeleteLoopRule: (LoopRuleSummary) -> Unit,
    onDeleteDangerousTopic: (DangerousTopicSummary) -> Unit,
    onDeleteSafeMemory: (SafeMemorySummary) -> Unit
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    var loopQuestion by rememberSaveable { mutableStateOf("") }
    var loopAnswer by rememberSaveable { mutableStateOf("") }
    var dangerousTerm by rememberSaveable { mutableStateOf("") }
    var redirectHint by rememberSaveable { mutableStateOf("") }
    var memoryTitle by rememberSaveable { mutableStateOf("") }
    var memoryContent by rememberSaveable { mutableStateOf("") }

    SectionTitle("Configuracion IA")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selectedSection == 0, onClick = { selectedSection = 0 }, label = { Text("Bucles") })
        FilterChip(selected = selectedSection == 1, onClick = { selectedSection = 1 }, label = { Text("Peligros") })
        FilterChip(selected = selectedSection == 2, onClick = { selectedSection = 2 }, label = { Text("Recuerdos") })
    }
    Spacer(modifier = Modifier.height(12.dp))
    if (selectedPatient == null) {
        SimpleCard("Sin paciente seleccionado", "Selecciona un paciente antes de configurar la IA.")
        return
    }
    TextButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
        Text("Actualizar configuracion")
    }
    when (selectedSection) {
        0 -> {
            SectionTitle("Bucles conversacionales")
            loopRules.forEach { rule ->
                SimpleCard(rule.question, rule.answer)
                TextButton(onClick = { onDeleteLoopRule(rule) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Eliminar regla")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = loopQuestion,
                onValueChange = { loopQuestion = it },
                label = { Text("Si pregunta...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = loopAnswer,
                onValueChange = { loopAnswer = it },
                label = { Text("Responder...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onCreateLoopRule(loopQuestion.trim(), loopAnswer.trim())
                    loopQuestion = ""
                    loopAnswer = ""
                },
                enabled = loopQuestion.isNotBlank() && loopAnswer.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar bucle")
            }
        }
        1 -> {
            SectionTitle("Temas peligrosos")
            dangerousTopics.forEach { topic ->
                SimpleCard(topic.term, topic.redirectHint ?: "Sin redireccion configurada")
                TextButton(onClick = { onDeleteDangerousTopic(topic) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Eliminar tema")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = dangerousTerm,
                onValueChange = { dangerousTerm = it },
                label = { Text("Palabra o tema") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = redirectHint,
                onValueChange = { redirectHint = it },
                label = { Text("Redireccion sugerida") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onCreateDangerousTopic(dangerousTerm.trim(), redirectHint.trim())
                    dangerousTerm = ""
                    redirectHint = ""
                },
                enabled = dangerousTerm.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar tema peligroso")
            }
        }
        2 -> {
            SectionTitle("Recuerdos seguros")
            safeMemories.forEach { memory ->
                SimpleCard(memory.title, memory.content)
                TextButton(onClick = { onDeleteSafeMemory(memory) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Eliminar recuerdo")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = memoryTitle,
                onValueChange = { memoryTitle = it },
                label = { Text("Titulo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = memoryContent,
                onValueChange = { memoryContent = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onCreateSafeMemory(memoryTitle.trim(), memoryContent.trim())
                    memoryTitle = ""
                    memoryContent = ""
                },
                enabled = memoryTitle.isNotBlank() && memoryContent.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar recuerdo")
            }
        }
    }
}

@Composable
private fun DiaryTab(
    activeSession: SessionSummary?,
    sessions: List<SessionSummary>,
    transcript: List<ConversationMessageSummary>,
    events: List<SessionEventSummary>,
    alerts: List<AlertSummary>,
    onRefresh: () -> Unit,
    onLoadTranscript: (SessionSummary) -> Unit
) {
    val visibleAlerts = activeSession?.let { session ->
        alerts.filter { it.sessionId == session.id }
    } ?: alerts

    SectionTitle("Diario")
    SimpleCard(
        title = activeSession?.let { "Sesion ${statusLabel(it.status)}" } ?: "Sin sesion cargada",
        body = activeSession?.let {
            "ID: ${shortId(it.id)}\nCreada: ${formatTimestamp(it.createdAt)}\nInicio: ${formatTimestamp(it.startedAt)}\nFin: ${formatTimestamp(it.endedAt)}"
        } ?: "Selecciona una sesion para revisar su transcripcion, eventos y alertas."
    )
    Spacer(modifier = Modifier.height(16.dp))
    SectionTitle("Sesiones")
    if (sessions.isEmpty()) {
        SimpleCard("Sin sesiones", "Inicia y termina una sesion para verla aqui.")
    } else {
        sessions.forEach { session ->
            SimpleCard(
                title = "Sesion ${statusLabel(session.status)}",
                body = "ID: ${shortId(session.id)}\nCreada: ${formatTimestamp(session.createdAt)}"
            )
            TextButton(onClick = { onLoadTranscript(session) }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver detalle")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    SectionTitle("Transcripcion")
    if (transcript.isEmpty()) {
        SimpleCard("Sin transcripcion cargada", "Selecciona una sesion para ver sus mensajes.")
    } else {
        transcript.forEach { message ->
            SimpleCard(
                title = "${senderLabel(message.sender)} - ${formatTimestamp(message.createdAt)}",
                body = message.content
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    SectionTitle("Eventos")
    if (events.isEmpty()) {
        SimpleCard("Sin eventos cargados", "Selecciona una sesion para ver sus hitos principales.")
    } else {
        events.forEach { event ->
            SimpleCard(
                title = "${event.eventType} - ${formatTimestamp(event.createdAt)}",
                body = event.description
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    SectionTitle(if (activeSession == null) "Alertas" else "Alertas de la sesion")
    if (visibleAlerts.isEmpty()) {
        SimpleCard("Sin alertas", "No hay alertas registradas para esta vista.")
    } else {
        visibleAlerts.forEach { alert ->
            SimpleCard(
                title = "${alert.title} - ${formatTimestamp(alert.createdAt)}",
                body = "${alert.message}\nSesion: ${alert.sessionId?.let { shortId(it) } ?: "sin sesion"}"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    TextButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
        Text("Actualizar diario")
    }
}

private fun formatTimestamp(value: String?): String {
    return value
        ?.replace("T", " ")
        ?.removeSuffix("Z")
        ?.take(16)
        ?: "Sin fecha"
}

private fun shortId(value: String): String {
    return if (value.length <= 8) value else value.take(8)
}

private fun senderLabel(sender: String): String {
    return when (sender.lowercase()) {
        "patient" -> "Paciente"
        "assistant" -> "Asistente"
        "caregiver" -> "Cuidador"
        else -> sender
    }
}

private fun statusLabel(status: String): String {
    return when (status.lowercase()) {
        "created" -> "creada"
        "active" -> "activa"
        "paused" -> "pausada"
        "ended" -> "terminada"
        "waiting" -> "en espera"
        else -> status
    }
}

private fun configureSpanishSpainTts(textToSpeech: TextToSpeech): Boolean {
    val availability = textToSpeech.setLanguage(SpanishSpainLocale)
    textToSpeech.setSpeechRate(0.92f)
    textToSpeech.setPitch(1.0f)

    if (availability == TextToSpeech.LANG_MISSING_DATA || availability == TextToSpeech.LANG_NOT_SUPPORTED) {
        return false
    }

    val spanishVoice = textToSpeech.voices
        ?.filter { voice ->
            voice.locale.language == SpanishSpainLocale.language &&
                voice.locale.country == SpanishSpainLocale.country
        }
        ?.sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.name }))
        ?.firstOrNull()

    if (spanishVoice != null) {
        textToSpeech.voice = spanishVoice
    }

    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientShell(api: MemoriaApiClient, onBack: () -> Unit) {
    val context = LocalContext.current
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "demo-device"
    }
    var ttsReady by remember { mutableStateOf(false) }
    val textToSpeech = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
    }
    var linked by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<TerminalStatus?>(null) }
    var patientText by rememberSaveable { mutableStateOf("") }
    var responseText by rememberSaveable { mutableStateOf("Estoy aqui contigo.") }
    var statusMessage by rememberSaveable { mutableStateOf("Introduce el codigo del cuidador.") }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            configureSpanishSpainTts(textToSpeech)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val recognizedText = matches?.firstOrNull().orEmpty()
        if (recognizedText.isNotBlank()) {
            patientText = recognizedText
            statusMessage = "Texto reconocido."
        }
    }

    fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con memorIA")
        }
        try {
            speechLauncher.launch(intent)
        } catch (exception: ActivityNotFoundException) {
            statusMessage = "Reconocimiento de voz no disponible en este dispositivo."
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechRecognizer()
        } else {
            statusMessage = "Permiso de microfono denegado. Puedes escribir el texto manualmente."
        }
    }

    fun startVoiceInput() {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            launchSpeechRecognizer()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun speak(text: String) {
        configureSpanishSpainTts(textToSpeech)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "memoria-response")
    }

    fun refreshStatus() {
        runAsync(
            action = { api.terminalStatus(deviceId) },
            onSuccess = {
                linked = true
                status = it
                statusMessage = "Estado: ${it.sessionStatus}"
            },
            onError = { statusMessage = "Sin vinculacion o sin conexion: $it" }
        )
    }

    LaunchedEffect(Unit) {
        runAsync(
            action = { api.terminalStatus(deviceId) },
            onSuccess = {
                linked = true
                status = it
                statusMessage = "Dispositivo vinculado. Estado: ${it.sessionStatus}"
            },
            onError = {
                statusMessage = "Introduce el codigo del cuidador."
            }
        )
    }

    LaunchedEffect(linked) {
        while (linked) {
            refreshStatus()
            delay(4000)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Modo paciente") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!linked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Codigo de vinculacion", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Codigo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        statusMessage = "Vinculando..."
                        runAsync(
                            action = {
                                api.linkDevice(code.trim(), deviceId, "Dispositivo paciente")
                                api.terminalStatus(deviceId)
                            },
                            onSuccess = {
                                linked = true
                                status = it
                                statusMessage = "Vinculado. Estado: ${it.sessionStatus}"
                            },
                            onError = { statusMessage = "Error vinculando: $it" }
                        )
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Vincular")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(statusMessage, textAlign = TextAlign.Center)
                }
            } else {
                PatientSessionView(
                    patientName = status?.patientName,
                    state = status?.sessionStatus ?: "waiting",
                    responseText = responseText,
                    patientText = patientText,
                    statusMessage = statusMessage,
                    onTextChange = { patientText = it },
                    onRefresh = { refreshStatus() },
                    onListen = { startVoiceInput() },
                    onSpeak = { speak(responseText) },
                    onSend = {
                        if (status?.sessionStatus != "active") {
                            statusMessage = "Espera a que el cuidador inicie la sesion."
                            refreshStatus()
                            return@PatientSessionView
                        }
                        val sessionId = status?.sessionId
                        if (sessionId == null) {
                            statusMessage = "El cuidador debe iniciar una sesion."
                            return@PatientSessionView
                        }
                        statusMessage = "Enviando mensaje..."
                        runAsync(
                            action = { api.sendPatientMessage(deviceId, sessionId, patientText.trim()) },
                            onSuccess = {
                                responseText = it.responseText
                                speak(it.responseText)
                                patientText = ""
                                statusMessage = "Respuesta: ${it.source}"
                            },
                            onError = { statusMessage = "Error enviando mensaje: $it" }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PatientSessionView(
    patientName: String?,
    state: String,
    responseText: String,
    patientText: String,
    statusMessage: String,
    onTextChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onListen: () -> Unit,
    onSpeak: () -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(148.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("IA", color = MaterialTheme.colorScheme.onPrimary, fontSize = 34.sp)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = patientName ?: "Paciente",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = state,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = responseText,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onListen, modifier = Modifier.fillMaxWidth()) {
            Text("Hablar")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = patientText,
            onValueChange = onTextChange,
            label = { Text("Texto transcrito de prueba") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSend,
            enabled = patientText.isNotBlank() && state == "active",
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar a memorIA")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onSpeak, modifier = Modifier.fillMaxWidth()) {
            Text("Leer respuesta")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar estado")
        }
        Text(statusMessage, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun StatusBanner(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun SimpleCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body)
        }
    }
}
