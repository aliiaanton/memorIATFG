package com.memoria.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
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
import com.memoria.app.data.AuthResult
import com.memoria.app.data.AuthSession
import com.memoria.app.data.AuthSessionStore
import com.memoria.app.data.ConversationMessageSummary
import com.memoria.app.data.DangerousTopicSummary
import com.memoria.app.data.LoopRuleSummary
import com.memoria.app.data.MemoriaApiClient
import com.memoria.app.data.PatientDeviceSummary
import com.memoria.app.data.PatientSummary
import com.memoria.app.data.SafeMemorySummary
import com.memoria.app.data.SessionEventSummary
import com.memoria.app.data.SessionSummary
import com.memoria.app.data.SupabaseAuthClient
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
    val context = LocalContext.current
    val sessionStore = remember { AuthSessionStore(context) }
    var authSession by remember { mutableStateOf(sessionStore.load()) }
    val api = remember { MemoriaApiClient() }
    val authClient = remember { SupabaseAuthClient() }
    var screen by rememberSaveable {
        mutableStateOf(if (authSession == null) AppScreen.Login.name else AppScreen.ModeSelector.name)
    }

    LaunchedEffect(authSession?.accessToken) {
        api.updateAccessToken(authSession?.accessToken)
    }

    when (AppScreen.valueOf(screen)) {
        AppScreen.Login -> LoginScreen(
            api = api,
            authClient = authClient,
            onAuthenticated = { session ->
                sessionStore.save(session)
                authSession = session
                api.updateAccessToken(session.accessToken)
                screen = AppScreen.ModeSelector.name
            },
            onDemo = {
                sessionStore.clear()
                authSession = null
                api.updateAccessToken(null)
                screen = AppScreen.ModeSelector.name
            }
        )
        AppScreen.ModeSelector -> ModeSelectorScreen(
            onCaregiver = { screen = AppScreen.Caregiver.name },
            onPatient = { screen = AppScreen.Patient.name },
            onLogout = {
                sessionStore.clear()
                authSession = null
                api.updateAccessToken(null)
                screen = AppScreen.Login.name
            }
        )
        AppScreen.Caregiver -> CaregiverShell(api = api, onBack = { screen = AppScreen.ModeSelector.name })
        AppScreen.Patient -> PatientShell(api = api, onBack = { screen = AppScreen.ModeSelector.name })
    }
}

@Composable
private fun LoginScreen(
    api: MemoriaApiClient,
    authClient: SupabaseAuthClient,
    onAuthenticated: (AuthSession) -> Unit,
    onDemo: () -> Unit
) {
    var isRegistering by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var backendStatus by rememberSaveable { mutableStateOf("Sin comprobar") }
    var authStatus by rememberSaveable { mutableStateOf("Inicia sesion con tu cuenta de cuidador.") }
    var authInProgress by rememberSaveable { mutableStateOf(false) }

    MemoriaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandMark(icon = MemoriaGlyph.Heart, size = 88.dp)
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "memorIA",
                color = MemoriaInk,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Teleasistencia inteligente para cuidar con calma.",
                color = MemoriaMuted,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(26.dp))
            MemoriaPanel(
                modifier = Modifier.widthIn(max = 460.dp),
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SoftListRow {
                        Column {
                            Text("Conexion", color = MemoriaInk, fontWeight = FontWeight.Bold)
                            Text(backendStatus, color = MemoriaMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    SoftListRow {
                        Column {
                            Text("Cuenta", color = MemoriaInk, fontWeight = FontWeight.Bold)
                            Text(authStatus, color = MemoriaMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (isRegistering) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nombre del cuidador") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contrasena") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    SecondaryActionButton(
                        text = "Comprobar conexion",
                        onClick = {
                            backendStatus = "Comprobando conexion..."
                            runAsync(
                                action = { api.health() },
                                onSuccess = { backendStatus = "Conexion lista" },
                                onError = { backendStatus = "No se pudo conectar" }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrimaryActionButton(
                        text = if (isRegistering) "Crear cuenta" else "Entrar",
                        onClick = {
                            authInProgress = true
                            authStatus = if (isRegistering) "Creando cuenta..." else "Iniciando sesion..."
                            runAsync(
                                action = {
                                    val result = if (isRegistering) {
                                        authClient.signUp(email.trim(), password, fullName.trim())
                                    } else {
                                        AuthResult.Authenticated(authClient.signIn(email.trim(), password))
                                    }
                                    if (result is AuthResult.Authenticated) {
                                        api.updateAccessToken(result.session.accessToken)
                                        val profileName = fullName
                                            .ifBlank { result.session.email?.substringBefore("@").orEmpty() }
                                            .ifBlank { email.substringBefore("@") }
                                        api.saveCaregiverProfile(profileName)
                                    }
                                    result
                                },
                                onSuccess = { result ->
                                    authInProgress = false
                                    when (result) {
                                        is AuthResult.Authenticated -> {
                                            authStatus = "Sesion iniciada."
                                            onAuthenticated(result.session)
                                        }
                                        is AuthResult.PendingConfirmation -> {
                                            authStatus = "Cuenta creada. Confirma el email ${result.email} e inicia sesion."
                                            isRegistering = false
                                        }
                                    }
                                },
                                onError = {
                                    authInProgress = false
                                    authStatus = "Error de autenticacion: $it"
                                }
                            )
                        },
                        enabled = email.isNotBlank() && password.isNotBlank() && !authInProgress &&
                            (!isRegistering || fullName.isNotBlank()),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = {
                            isRegistering = !isRegistering
                            authStatus = if (isRegistering) {
                                "Crea una cuenta para guardar y consultar la informacion."
                            } else {
                                "Inicia sesion con tu cuenta de cuidador."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRegistering) "Ya tengo cuenta" else "Crear cuenta nueva", color = MemoriaSageDark)
                    }
                    TextButton(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                        Text("Continuar sin cuenta", color = MemoriaMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorScreen(onCaregiver: () -> Unit, onPatient: () -> Unit, onLogout: () -> Unit) {
    MemoriaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BrandMark(icon = MemoriaGlyph.Heart, size = 120.dp)
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "memorIA",
                color = MemoriaInk,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Asistente de teleasistencia inteligente",
                color = MemoriaMuted,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ModeChoiceCard(
                    title = "Modo Cuidador",
                    subtitle = "Panel de control y configuracion",
                    icon = MemoriaGlyph.Caregiver,
                    selected = true,
                    onClick = onCaregiver
                )
                ModeChoiceCard(
                    title = "Modo Paciente",
                    subtitle = "Interfaz simplificada con voz",
                    icon = MemoriaGlyph.Heart,
                    selected = false,
                    onClick = onPatient
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Teleasistencia para personas con demencia/Alzheimer",
                color = MemoriaMuted,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onLogout) {
                Text("Cerrar sesion", color = MemoriaMuted)
            }
        }
    }
}

@Composable
private fun CaregiverShell(api: MemoriaApiClient, onBack: () -> Unit) {
    val context = LocalContext.current
    val alertNotifier = remember { AlertNotifier(context) }
    val tabs = listOf(
        CaregiverNavItem("Inicio", MemoriaGlyph.Home),
        CaregiverNavItem("Pacientes", MemoriaGlyph.Patients),
        CaregiverNavItem("Asistente", MemoriaGlyph.Brain),
        CaregiverNavItem("Diario", MemoriaGlyph.Book)
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var patients by remember { mutableStateOf<List<PatientSummary>>(emptyList()) }
    var selectedPatient by remember { mutableStateOf<PatientSummary?>(null) }
    var patientDevices by remember { mutableStateOf<List<PatientDeviceSummary>>(emptyList()) }
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

    fun refreshPatientDevices(patient: PatientSummary? = selectedPatient) {
        val current = patient ?: return
        runAsync(
            action = { api.listPatientDevices(current.id) },
            onSuccess = { patientDevices = it },
            onError = { statusMessage = "Error cargando dispositivos: $it" }
        )
    }

    fun refreshPatients() {
        runAsync(
            action = { api.listPatients() },
            onSuccess = { loaded ->
                patients = loaded
                val currentSelection = selectedPatient ?: loaded.firstOrNull()
                selectedPatient = currentSelection
                statusMessage = if (loaded.isEmpty()) "No hay pacientes registrados." else "Datos cargados."
                currentSelection?.let { refreshPatientDevices(it) }
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
                statusMessage = "Ajustes del asistente actualizados."
            },
            onError = { statusMessage = "Error cargando ajustes del asistente: $it" }
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
        containerColor = MemoriaBackground,
        topBar = {
            CaregiverTopBar(onExit = onBack)
        },
        bottomBar = {
            CaregiverBottomBar(
                items = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 10.dp, vertical = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> CaregiverHome(
                    selectedPatient = selectedPatient,
                    activeSession = activeSession,
                    recentAlerts = alerts.take(2),
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
                    patientDevices = patientDevices,
                    pairingCode = pairingCode,
                    statusMessage = statusMessage,
                    onSelectPatient = {
                        selectedPatient = it
                        pairingCode = ""
                        refreshAiConfig(it)
                        refreshSessions(it)
                        refreshPatientDevices(it)
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
                                refreshPatientDevices(it)
                            },
                            onError = { statusMessage = "Error creando paciente: $it" }
                        )
                    },
                    onUpdatePatient = { patient, fullName, preferredName, notes, onSaved ->
                        statusMessage = "Actualizando paciente..."
                        runAsync(
                            action = { api.updatePatient(patient.id, fullName, preferredName, notes) },
                            onSuccess = {
                                patients = patients.map { current -> if (current.id == it.id) it else current }
                                selectedPatient = it
                                statusMessage = "Paciente actualizado."
                                onSaved()
                                refreshPatients()
                                refreshAiConfig(it)
                                refreshSessions(it)
                                refreshPatientDevices(it)
                            },
                            onError = { statusMessage = "Error actualizando paciente: $it" }
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
                                patientDevices = emptyList()
                                sessions = emptyList()
                                transcript = emptyList()
                                events = emptyList()
                                statusMessage = "Paciente eliminado."
                                refreshPatients()
                            },
                            onError = { statusMessage = "Error eliminando paciente: $it" }
                        )
                    },
                    onGenerateCode = { patient ->
                        selectedPatient = patient
                        runAsync(
                            action = { api.createPairingCode(patient.id) },
                            onSuccess = {
                                pairingCode = it.code
                                statusMessage = "Codigo generado."
                            },
                            onError = { statusMessage = "Error generando codigo: $it" }
                        )
                    },
                    onRefreshDevices = { refreshPatientDevices() },
                    onUnlinkDevice = { device ->
                        val patient = selectedPatient ?: return@PatientsTab
                        statusMessage = "Desvinculando dispositivo..."
                        runAsync(
                            action = { api.unlinkPatientDevice(patient.id, device.id) },
                            onSuccess = {
                                statusMessage = "Dispositivo desvinculado."
                                refreshPatientDevices(patient)
                            },
                            onError = { statusMessage = "Error desvinculando dispositivo: $it" }
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
                    onUpdateLoopRule = { rule, question, answer ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Actualizando regla de bucle..."
                        runAsync(
                            action = { api.updateLoopRule(patient.id, rule.id, question, answer, rule.active) },
                            onSuccess = {
                                statusMessage = "Regla actualizada."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error actualizando regla: $it" }
                        )
                    },
                    onCreateDangerousTopic = { term, redirect ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Creando tema sensible..."
                        runAsync(
                            action = { api.createDangerousTopic(patient.id, term, redirect) },
                            onSuccess = {
                                statusMessage = "Tema sensible creado."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error creando tema: $it" }
                        )
                    },
                    onUpdateDangerousTopic = { topic, term, redirect ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Actualizando tema sensible..."
                        runAsync(
                            action = { api.updateDangerousTopic(patient.id, topic.id, term, redirect, topic.active) },
                            onSuccess = {
                                statusMessage = "Tema sensible actualizado."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error actualizando tema: $it" }
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
                    onUpdateSafeMemory = { memory, title, content ->
                        val patient = selectedPatient ?: return@AiConfigTab
                        statusMessage = "Actualizando recuerdo seguro..."
                        runAsync(
                            action = { api.updateSafeMemory(patient.id, memory.id, title, content, memory.active) },
                            onSuccess = {
                                statusMessage = "Recuerdo actualizado."
                                refreshAiConfig(patient)
                            },
                            onError = { statusMessage = "Error actualizando recuerdo: $it" }
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
                    selectedPatient = selectedPatient,
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
    recentAlerts: List<AlertSummary>,
    statusMessage: String,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onRefreshAlerts: () -> Unit
) {
    val sessionStatus = activeSession?.status ?: "inactive"
    val statusText = when (sessionStatus.lowercase()) {
        "active" -> "Activa"
        "paused" -> "Pausada"
        "ended" -> "Finalizada"
        "created" -> "Creada"
        else -> "Inactiva"
    }

    ScreenHeading("Panel de Control")
    Spacer(modifier = Modifier.height(24.dp))
    MemoriaPanel {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Estado de la Sesion",
                    color = MemoriaInk,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                PillBadge(text = statusText)
            }
            SessionInfoRow("Paciente", selectedPatient?.fullName ?: "-")
            SessionInfoRow("Inicio", activeSession?.startedAt?.let { formatTimestamp(it) } ?: "-")
            SessionInfoRow("Duracion", if (activeSession?.endedAt == null) "-" else "Finalizada")
            when (sessionStatus.lowercase()) {
                "active" -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryActionButton(
                        text = "Pausar",
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        contentColor = MemoriaSageDark
                    )
                    PrimaryActionButton(
                        text = "Finalizar",
                        onClick = onEnd,
                        modifier = Modifier.weight(1f)
                    )
                }
                "paused" -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryActionButton(
                        text = "Reanudar",
                        onClick = onResume,
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryActionButton(
                        text = "Finalizar",
                        onClick = onEnd,
                        modifier = Modifier.weight(1f),
                        contentColor = MemoriaSageDark
                    )
                }
                else -> PrimaryActionButton(
                    text = "Iniciar",
                    icon = MemoriaGlyph.Play,
                    onClick = onStart,
                    enabled = selectedPatient != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    MemoriaPanel(
        background = MemoriaWarningPanel,
        borderColor = Color(0xFFF6D7D1)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemoriaLineIcon(MemoriaGlyph.Bell, MemoriaWarning, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Alertas Recientes",
                    color = MemoriaInk,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (recentAlerts.isEmpty()) {
                SoftListRow {
                    Text(
                        text = "Sin alertas recientes",
                        color = MemoriaMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                recentAlerts.forEach { alert ->
                    SoftListRow {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    color = MemoriaInk,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = alert.message,
                                    color = MemoriaMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = alert.createdAt?.let { formatTimestamp(it).takeLast(5) } ?: "",
                                color = MemoriaMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            TextButton(onClick = onRefreshAlerts, modifier = Modifier.fillMaxWidth()) {
                Text("Actualizar alertas", color = MemoriaWarningDark)
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    MemoriaPanel {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Ultimos Eventos",
                color = MemoriaInk,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            EventBullet(
                color = MemoriaSage,
                title = if (activeSession == null) "Sesion inactiva" else "Sesion ${statusLabel(activeSession.status)}",
                time = activeSession?.startedAt?.let { formatTimestamp(it).takeLast(5) } ?: "-"
            )
            recentAlerts.firstOrNull()?.let { alert ->
                EventBullet(
                    color = MemoriaWarning,
                    title = alert.title,
                    time = alert.createdAt?.let { formatTimestamp(it).takeLast(5) } ?: "-"
                )
            }
            if (recentAlerts.isEmpty() && activeSession == null) {
                Text(
                    text = statusMessage,
                    color = MemoriaMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PatientsTab(
    patients: List<PatientSummary>,
    selectedPatient: PatientSummary?,
    patientDevices: List<PatientDeviceSummary>,
    pairingCode: String,
    statusMessage: String,
    onSelectPatient: (PatientSummary) -> Unit,
    onCreatePatient: (String, String, String, () -> Unit) -> Unit,
    onUpdatePatient: (PatientSummary, String, String, String, () -> Unit) -> Unit,
    onDeletePatient: (PatientSummary) -> Unit,
    onGenerateCode: (PatientSummary) -> Unit,
    onRefreshDevices: () -> Unit,
    onUnlinkDevice: (PatientDeviceSummary) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var preferredName by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var formVisible by rememberSaveable { mutableStateOf(false) }
    var editingPatient by remember { mutableStateOf<PatientSummary?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ScreenHeading("Pacientes", modifier = Modifier.weight(1f))
        IconOnlyButton(
            glyph = MemoriaGlyph.Plus,
            onClick = {
                editingPatient = null
                fullName = ""
                preferredName = ""
                notes = ""
                formVisible = true
            },
            containerColor = MemoriaSage,
            contentColor = Color.White,
            size = 48.dp
        )
    }
    Spacer(modifier = Modifier.height(22.dp))
    if (patients.isEmpty()) {
        MemoriaPanel {
            Text("Sin pacientes registrados", color = MemoriaInk, fontWeight = FontWeight.Bold)
        }
    } else {
        patients.forEach { patient ->
            val linked = patient.id == selectedPatient?.id && patientDevices.isNotEmpty()
            MemoriaPanel(contentPadding = PaddingValues(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = patient.fullName,
                                color = MemoriaInk,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = patient.preferredName
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { "Nombre preferido: $it" }
                                    ?: patient.notes?.takeIf { it.isNotBlank() }
                                    ?: "Paciente registrado",
                                color = MemoriaMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        PillBadge(
                            text = if (linked) "Vinculado" else "No vinculado",
                            background = if (linked) MemoriaSagePale else MemoriaListSurface,
                            contentColor = MemoriaMuted
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryActionButton(
                            text = "Editar",
                            icon = MemoriaGlyph.Edit,
                            onClick = {
                                onSelectPatient(patient)
                                editingPatient = patient
                                fullName = patient.fullName
                                preferredName = patient.preferredName.orEmpty()
                                notes = patient.notes.orEmpty()
                                formVisible = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryActionButton(
                            text = "Generar Codigo",
                            onClick = {
                                onSelectPatient(patient)
                                onGenerateCode(patient)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconOnlyButton(
                            glyph = MemoriaGlyph.Trash,
                            onClick = { onDeletePatient(patient) },
                            containerColor = MemoriaWarningSoft,
                            contentColor = MemoriaWarningDark,
                            size = 40.dp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    if (formVisible) {
        Spacer(modifier = Modifier.height(8.dp))
        MemoriaPanel {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (editingPatient == null) "Nuevo paciente" else "Editar paciente",
                    color = MemoriaInk,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = preferredName,
                    onValueChange = { preferredName = it },
                    label = { Text("Nombre preferido") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Indicaciones de cuidado") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton(
                        text = "Cancelar",
                        onClick = {
                            formVisible = false
                            editingPatient = null
                            fullName = ""
                            preferredName = ""
                            notes = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryActionButton(
                        text = if (editingPatient == null) "Crear" else "Guardar",
                        onClick = {
                            val onSaved = {
                                fullName = ""
                                preferredName = ""
                                notes = ""
                                editingPatient = null
                                formVisible = false
                            }
                            val currentPatient = editingPatient
                            if (currentPatient == null) {
                                onCreatePatient(fullName.trim(), preferredName.trim(), notes.trim(), onSaved)
                            } else {
                                onUpdatePatient(currentPatient, fullName.trim(), preferredName.trim(), notes.trim(), onSaved)
                            }
                        },
                        enabled = fullName.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    if (pairingCode.isNotBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        MemoriaPanel(background = MemoriaSagePale, borderColor = MemoriaSageSoft) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Codigo paciente", color = MemoriaInk, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(pairingCode, color = MemoriaSageDark, style = MaterialTheme.typography.headlineSmall)
                }
                MemoriaLineIcon(MemoriaGlyph.Heart, MemoriaSage, Modifier.size(32.dp))
            }
        }
    }
    if (selectedPatient != null) {
        Spacer(modifier = Modifier.height(16.dp))
        MemoriaPanel {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Dispositivos vinculados",
                        color = MemoriaInk,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRefreshDevices) {
                        Text("Actualizar", color = MemoriaSageDark)
                    }
                }
                if (patientDevices.isEmpty()) {
                    Text(
                        text = "Genera un codigo y vinculalo desde el modo paciente.",
                        color = MemoriaMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    patientDevices.forEach { device ->
                        SoftListRow {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.deviceName ?: "Dispositivo paciente",
                                        color = MemoriaInk,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Vinculado el ${formatTimestamp(device.linkedAt)}",
                                        color = MemoriaMuted,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconOnlyButton(
                                    glyph = MemoriaGlyph.Trash,
                                    onClick = { onUnlinkDevice(device) },
                                    containerColor = MemoriaWarningSoft,
                                    contentColor = MemoriaWarningDark,
                                    size = 34.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (statusMessage.startsWith("Error")) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(statusMessage, color = MemoriaWarningDark, style = MaterialTheme.typography.bodySmall)
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
    onUpdateLoopRule: (LoopRuleSummary, String, String) -> Unit,
    onCreateDangerousTopic: (String, String) -> Unit,
    onUpdateDangerousTopic: (DangerousTopicSummary, String, String) -> Unit,
    onCreateSafeMemory: (String, String) -> Unit,
    onUpdateSafeMemory: (SafeMemorySummary, String, String) -> Unit,
    onDeleteLoopRule: (LoopRuleSummary) -> Unit,
    onDeleteDangerousTopic: (DangerousTopicSummary) -> Unit,
    onDeleteSafeMemory: (SafeMemorySummary) -> Unit
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(-1) }
    var loopQuestion by rememberSaveable { mutableStateOf("") }
    var loopAnswer by rememberSaveable { mutableStateOf("") }
    var dangerousTerm by rememberSaveable { mutableStateOf("") }
    var redirectHint by rememberSaveable { mutableStateOf("") }
    var memoryTitle by rememberSaveable { mutableStateOf("") }
    var memoryContent by rememberSaveable { mutableStateOf("") }
    var editingLoopRule by remember { mutableStateOf<LoopRuleSummary?>(null) }
    var editingDangerousTopic by remember { mutableStateOf<DangerousTopicSummary?>(null) }
    var editingSafeMemory by remember { mutableStateOf<SafeMemorySummary?>(null) }

    fun clearLoopEditor() {
        editingLoopRule = null
        loopQuestion = ""
        loopAnswer = ""
        selectedSection = -1
    }

    fun clearDangerEditor() {
        editingDangerousTopic = null
        dangerousTerm = ""
        redirectHint = ""
        selectedSection = -1
    }

    fun clearMemoryEditor() {
        editingSafeMemory = null
        memoryTitle = ""
        memoryContent = ""
        selectedSection = -1
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ScreenHeading("Asistente", modifier = Modifier.weight(1f))
        TextButton(onClick = onRefresh) {
            Text("Actualizar", color = MemoriaSageDark)
        }
    }
    Spacer(modifier = Modifier.height(22.dp))
    if (selectedPatient == null) {
        MemoriaPanel {
            Text("Sin paciente seleccionado", color = MemoriaInk, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Selecciona un paciente antes de ajustar el asistente.", color = MemoriaMuted)
        }
        return
    }

    MemoriaPanel {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ConfigSectionHeader(
                title = "Preguntas Repetitivas",
                onAdd = {
                    selectedSection = 0
                    editingLoopRule = null
                    loopQuestion = ""
                    loopAnswer = ""
                }
            )
            if (loopRules.isEmpty()) {
                SoftListRow {
                    Text("No hay preguntas repetitivas configuradas.", color = MemoriaMuted)
                }
            } else {
                loopRules.forEach { rule ->
                    SoftListRow(
                        onClick = {
                            selectedSection = 0
                            editingLoopRule = rule
                            loopQuestion = rule.question
                            loopAnswer = rule.answer
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rule.question,
                                    color = MemoriaInk,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Respuesta: ${rule.answer}",
                                    color = MemoriaMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconOnlyButton(
                                glyph = MemoriaGlyph.Trash,
                                onClick = { onDeleteLoopRule(rule) },
                                containerColor = Color.Transparent,
                                contentColor = MemoriaWarningDark,
                                size = 32.dp
                            )
                            MemoriaLineIcon(MemoriaGlyph.ChevronRight, MemoriaMuted, Modifier.size(18.dp))
                        }
                    }
                }
            }
            if (selectedSection == 0) {
                OutlinedTextField(
                    value = loopQuestion,
                    onValueChange = { loopQuestion = it },
                    label = { Text("Pregunta") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = loopAnswer,
                    onValueChange = { loopAnswer = it },
                    label = { Text("Respuesta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton("Cancelar", onClick = { clearLoopEditor() }, modifier = Modifier.weight(1f))
                    PrimaryActionButton(
                        text = if (editingLoopRule == null) "Guardar" else "Actualizar",
                        onClick = {
                            val currentRule = editingLoopRule
                            if (currentRule == null) {
                                onCreateLoopRule(loopQuestion.trim(), loopAnswer.trim())
                            } else {
                                onUpdateLoopRule(currentRule, loopQuestion.trim(), loopAnswer.trim())
                            }
                            clearLoopEditor()
                        },
                        enabled = loopQuestion.isNotBlank() && loopAnswer.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    MemoriaPanel {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ConfigSectionHeader(
                title = "Temas Sensibles",
                onAdd = {
                    selectedSection = 1
                    editingDangerousTopic = null
                    dangerousTerm = ""
                    redirectHint = ""
                }
            )
            if (dangerousTopics.isEmpty()) {
                Text("No hay temas sensibles definidos.", color = MemoriaMuted)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dangerousTopics.forEach { topic ->
                        DeletableChip(
                            text = topic.term,
                            onDelete = { onDeleteDangerousTopic(topic) },
                            onClick = {
                                selectedSection = 1
                                editingDangerousTopic = topic
                                dangerousTerm = topic.term
                                redirectHint = topic.redirectHint.orEmpty()
                            }
                        )
                    }
                }
            }
            if (selectedSection == 1) {
                OutlinedTextField(
                    value = dangerousTerm,
                    onValueChange = { dangerousTerm = it },
                    label = { Text("Tema") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = redirectHint,
                    onValueChange = { redirectHint = it },
                    label = { Text("Frase para reconducir") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton("Cancelar", onClick = { clearDangerEditor() }, modifier = Modifier.weight(1f))
                    PrimaryActionButton(
                        text = if (editingDangerousTopic == null) "Guardar" else "Actualizar",
                        onClick = {
                            val currentTopic = editingDangerousTopic
                            if (currentTopic == null) {
                                onCreateDangerousTopic(dangerousTerm.trim(), redirectHint.trim())
                            } else {
                                onUpdateDangerousTopic(currentTopic, dangerousTerm.trim(), redirectHint.trim())
                            }
                            clearDangerEditor()
                        },
                        enabled = dangerousTerm.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    MemoriaPanel {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ConfigSectionHeader(
                title = "Recuerdos Seguros",
                onAdd = {
                    selectedSection = 2
                    editingSafeMemory = null
                    memoryTitle = ""
                    memoryContent = ""
                }
            )
            if (safeMemories.isEmpty()) {
                SoftListRow {
                    Text("No hay recuerdos seguros todavia.", color = MemoriaMuted)
                }
            }
            safeMemories.forEach { memory ->
                SoftListRow(
                    onClick = {
                        selectedSection = 2
                        editingSafeMemory = memory
                        memoryTitle = memory.title
                        memoryContent = memory.content
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = memory.content,
                            color = MemoriaInk,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconOnlyButton(
                            glyph = MemoriaGlyph.Trash,
                            onClick = { onDeleteSafeMemory(memory) },
                            containerColor = Color.Transparent,
                            contentColor = MemoriaWarningDark,
                            size = 32.dp
                        )
                    }
                }
            }
            if (selectedSection == 2) {
                OutlinedTextField(
                    value = memoryTitle,
                    onValueChange = { memoryTitle = it },
                    label = { Text("Titulo del recuerdo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = memoryContent,
                    onValueChange = { memoryContent = it },
                    label = { Text("Recuerdo seguro") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton("Cancelar", onClick = { clearMemoryEditor() }, modifier = Modifier.weight(1f))
                    PrimaryActionButton(
                        text = if (editingSafeMemory == null) "Guardar" else "Actualizar",
                        onClick = {
                            val currentMemory = editingSafeMemory
                            if (currentMemory == null) {
                                onCreateSafeMemory(memoryTitle.trim(), memoryContent.trim())
                            } else {
                                onUpdateSafeMemory(currentMemory, memoryTitle.trim(), memoryContent.trim())
                            }
                            clearMemoryEditor()
                        },
                        enabled = memoryTitle.isNotBlank() && memoryContent.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaryTab(
    selectedPatient: PatientSummary?,
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

    Row(verticalAlignment = Alignment.CenterVertically) {
        ScreenHeading("Diario de Sesiones", modifier = Modifier.weight(1f))
        TextButton(onClick = onRefresh) {
            Text("Actualizar", color = MemoriaSageDark)
        }
    }
    Spacer(modifier = Modifier.height(22.dp))
    if (sessions.isEmpty()) {
        MemoriaPanel {
            Text("Sin sesiones registradas", color = MemoriaInk, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Cuando finalice una conversacion, aparecera aqui.", color = MemoriaMuted)
        }
    } else {
        sessions.forEach { session ->
            MemoriaPanel(
                modifier = Modifier.clickable { onLoadTranscript(session) },
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedPatient?.fullName ?: "Paciente",
                            color = MemoriaInk,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${formatTimestamp(session.startedAt ?: session.createdAt)}   -   ${statusLabel(session.status)}",
                            color = MemoriaMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    MemoriaLineIcon(MemoriaGlyph.ChevronRight, MemoriaMuted, Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (activeSession != null) {
        Spacer(modifier = Modifier.height(12.dp))
        MemoriaPanel {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Detalle de sesion",
                    color = MemoriaInk,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                SessionInfoRow("Estado", statusLabel(activeSession.status))
                SessionInfoRow("Inicio", formatTimestamp(activeSession.startedAt))
                SessionInfoRow("Fin", formatTimestamp(activeSession.endedAt))
            }
        }
        if (transcript.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            MemoriaPanel {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Transcripcion", color = MemoriaInk, fontWeight = FontWeight.Bold)
                    transcript.forEach { message ->
                        SoftListRow {
                            Column {
                                Text(
                                    text = "${senderLabel(message.sender)} - ${formatTimestamp(message.createdAt)}",
                                    color = MemoriaMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(message.content, color = MemoriaInk)
                            }
                        }
                    }
                }
            }
        }
        if (events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            MemoriaPanel {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Eventos", color = MemoriaInk, fontWeight = FontWeight.Bold)
                    events.forEach { event ->
                        EventBullet(
                            color = MemoriaSage,
                            title = event.description,
                            time = formatTimestamp(event.createdAt).takeLast(5)
                        )
                    }
                }
            }
        }
    }

    if (visibleAlerts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        MemoriaPanel(background = MemoriaWarningPanel, borderColor = Color(0xFFF6D7D1)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Alertas", color = MemoriaInk, fontWeight = FontWeight.Bold)
                visibleAlerts.forEach { alert ->
                    SoftListRow {
                        Column {
                            Text(
                                text = "${alert.title} - ${formatTimestamp(alert.createdAt)}",
                                color = MemoriaInk,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(alert.message, color = MemoriaMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionInfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = MemoriaMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = MemoriaInk,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun EventBullet(color: Color, title: String, time: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(7.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MemoriaInk,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = time,
                color = MemoriaMuted,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ConfigSectionHeader(title: String, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            color = MemoriaInk,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconOnlyButton(
            glyph = MemoriaGlyph.Plus,
            onClick = onAdd,
            containerColor = Color.Transparent,
            contentColor = MemoriaSage,
            size = 34.dp
        )
    }
}

private fun formatTimestamp(value: String?): String {
    return value
        ?.replace("T", " ")
        ?.removeSuffix("Z")
        ?.take(16)
        ?: "Sin fecha"
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

@Composable
private fun PatientShell(api: MemoriaApiClient, onBack: () -> Unit) {
    val context = LocalContext.current
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "demo-device"
    }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
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
    var autoConversationEnabled by rememberSaveable { mutableStateOf(true) }
    var microphonePermissionGranted by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var microphonePermissionAsked by rememberSaveable { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var recognizerAvailable by remember { mutableStateOf(SpeechRecognizer.isRecognitionAvailable(context)) }
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            configureSpanishSpainTts(textToSpeech)
        }
    }

    fun recognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        microphonePermissionGranted = granted
        statusMessage = if (granted) {
            "Microfono listo."
        } else {
            "Permiso de microfono denegado. Puedes escribir el texto manualmente."
        }
    }

    fun refreshStatus() {
        runAsync(
            action = { api.terminalStatus(deviceId) },
            onSuccess = {
                linked = true
                status = it
                if (it.sessionStatus != "active" || (!isListening && !isSending && !isSpeaking)) {
                    statusMessage = "Estado: ${it.sessionStatus}"
                }
            },
            onError = { statusMessage = "Sin vinculacion o sin conexion: $it" }
        )
    }

    fun startVoiceInput() {
        if (!recognizerAvailable || speechRecognizer == null) {
            statusMessage = "Reconocimiento de voz no disponible en este dispositivo."
            return
        }
        if (!microphonePermissionGranted) {
            microphonePermissionAsked = true
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (status?.sessionStatus != "active") {
            statusMessage = "Espera a que el cuidador inicie la sesion."
            refreshStatus()
            return
        }
        if (isListening || isSending || isSpeaking) {
            return
        }
        try {
            speechRecognizer.startListening(recognitionIntent())
            isListening = true
            statusMessage = "Escuchando..."
        } catch (exception: Exception) {
            isListening = false
            recognizerAvailable = false
            statusMessage = "No se pudo iniciar el reconocimiento de voz: ${exception.message}"
        }
    }

    fun speak(text: String) {
        speechRecognizer?.cancel()
        isListening = false
        isSpeaking = true
        configureSpanishSpainTts(textToSpeech)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "memoria-response")
    }

    fun sendRecognizedMessage(text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank() || isSending) {
            return
        }
        if (status?.sessionStatus != "active") {
            statusMessage = "Espera a que el cuidador inicie la sesion."
            refreshStatus()
            return
        }
        val sessionId = status?.sessionId
        if (sessionId == null) {
            statusMessage = "El cuidador debe iniciar una sesion."
            return
        }
        speechRecognizer?.cancel()
        isListening = false
        isSending = true
        patientText = cleanText
        statusMessage = "Pensando..."
        runAsync(
            action = { api.sendPatientMessage(deviceId, sessionId, cleanText) },
            onSuccess = {
                isSending = false
                responseText = it.responseText
                patientText = ""
                statusMessage = "Respondiendo..."
                speak(it.responseText)
            },
            onError = {
                isSending = false
                statusMessage = "Error enviando mensaje: $it"
            }
        )
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                statusMessage = "Escuchando..."
            }

            override fun onBeginningOfSpeech() {
                statusMessage = "Te escucho..."
            }

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                isListening = false
                statusMessage = "Pensando..."
            }

            override fun onError(error: Int) {
                isListening = false
                statusMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No he entendido nada. Vuelvo a escuchar."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de microfono denegado."
                    else -> "Reconocimiento interrumpido. Vuelvo a escuchar."
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val recognizedText = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (recognizedText.isBlank()) {
                    statusMessage = "No he entendido nada. Vuelvo a escuchar."
                    return
                }
                sendRecognizedMessage(recognizedText)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialText = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (partialText.isNotBlank()) {
                    patientText = partialText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose {
            speechRecognizer?.setRecognitionListener(null)
        }
    }

    DisposableEffect(textToSpeech) {
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post { isSpeaking = true }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    isSpeaking = false
                    statusMessage = "Escuchando..."
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    isSpeaking = false
                    statusMessage = "No se pudo leer la respuesta."
                }
            }
        })
        onDispose {
            textToSpeech.setOnUtteranceProgressListener(null)
        }
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

    LaunchedEffect(linked, status?.sessionStatus, autoConversationEnabled, microphonePermissionGranted) {
        if (linked && status?.sessionStatus == "active" && autoConversationEnabled && !microphonePermissionGranted &&
            !microphonePermissionAsked
        ) {
            microphonePermissionAsked = true
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(linked, status?.sessionStatus, autoConversationEnabled, microphonePermissionGranted, isListening, isSending, isSpeaking) {
        if (!linked || status?.sessionStatus != "active" || !autoConversationEnabled) {
            speechRecognizer?.cancel()
            isListening = false
            return@LaunchedEffect
        }
        if (microphonePermissionGranted && !isListening && !isSending && !isSpeaking) {
            delay(900)
            startVoiceInput()
        }
    }

    Scaffold(containerColor = MemoriaBackground) { padding ->
        Surface(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            color = MemoriaBackground
        ) {
            if (!linked) {
                PatientLinkView(
                    code = code,
                    statusMessage = statusMessage,
                    onCodeChange = { code = it.uppercase().take(8) },
                    onLink = {
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
                    }
                )
            } else {
                PatientSessionView(
                    patientName = status?.patientName,
                    state = status?.sessionStatus ?: "waiting",
                    responseText = responseText,
                    patientText = patientText,
                    statusMessage = statusMessage,
                    autoConversationEnabled = autoConversationEnabled,
                    isListening = isListening,
                    isSending = isSending,
                    isSpeaking = isSpeaking,
                    onTextChange = { patientText = it },
                    onRefresh = { refreshStatus() },
                    onListen = { startVoiceInput() },
                    onSpeak = { speak(responseText) },
                    onToggleAutoConversation = {
                        autoConversationEnabled = !autoConversationEnabled
                        statusMessage = if (autoConversationEnabled) {
                            "Escucha automatica activada."
                        } else {
                            "Escucha automatica pausada."
                        }
                    },
                    onSend = {
                        sendRecognizedMessage(patientText)
                    }
                )
            }
        }
    }
}

@Composable
private fun PatientLinkView(
    code: String,
    statusMessage: String,
    onCodeChange: (String) -> Unit,
    onLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandMark(
            icon = MemoriaGlyph.Dot,
            size = 68.dp,
            outerColor = MemoriaSagePale,
            innerColor = MemoriaSageSoft,
            iconColor = MemoriaSage
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = "Vincular Dispositivo",
            color = MemoriaInk,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Introduce el codigo proporcionado por tu cuidador",
            color = MemoriaMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = {
                Text(
                    text = "XXXXXX",
                    modifier = Modifier.fillMaxWidth(),
                    color = MemoriaMuted.copy(alpha = 0.62f),
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = MemoriaInk,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            shape = MemoriaButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 374.dp)
                .height(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryActionButton(
            text = "Vincular",
            onClick = onLink,
            enabled = code.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 374.dp),
            height = 58.dp
        )
        if (statusMessage.startsWith("Error")) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = statusMessage,
                color = MemoriaWarningDark,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
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
    autoConversationEnabled: Boolean,
    isListening: Boolean,
    isSending: Boolean,
    isSpeaking: Boolean,
    onTextChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onListen: () -> Unit,
    onSpeak: () -> Unit,
    onToggleAutoConversation: () -> Unit,
    onSend: () -> Unit
) {
    val hasError = statusMessage.startsWith("Error") ||
        statusMessage.contains("No se pudo", ignoreCase = true) ||
        statusMessage.contains("denegado", ignoreCase = true)
    val visualState = when {
        hasError -> PatientVisualState(
            glyph = MemoriaGlyph.Error,
            accent = MemoriaWarningDark,
            title = "Ups, algo fallo",
            subtitle = "No pasa nada. Intentalo de nuevo en un momento",
            ring = false
        )
        state == "ended" -> PatientVisualState(
            glyph = MemoriaGlyph.Check,
            accent = MemoriaSage,
            title = "Sesion Finalizada",
            subtitle = "Ha sido un placer hablar contigo. Hasta pronto!",
            ring = false
        )
        state == "paused" || !autoConversationEnabled -> PatientVisualState(
            glyph = MemoriaGlyph.Pause,
            accent = MemoriaWarning,
            title = "Sesion Pausada",
            subtitle = "Volveremos a hablar en un momento",
            ring = false
        )
        isSending -> PatientVisualState(
            glyph = MemoriaGlyph.Brain,
            accent = MemoriaSage,
            title = "Estoy pensando",
            subtitle = "Preparando una respuesta tranquila",
            ring = true
        )
        isSpeaking -> PatientVisualState(
            glyph = MemoriaGlyph.Heart,
            accent = MemoriaSage,
            title = "Te respondo",
            subtitle = responseText,
            ring = true
        )
        state == "active" || isListening -> PatientVisualState(
            glyph = MemoriaGlyph.Mic,
            accent = MemoriaSage,
            title = "Te escucho",
            subtitle = "Habla con claridad y tranquilidad",
            ring = true
        )
        else -> PatientVisualState(
            glyph = MemoriaGlyph.Dot,
            accent = MemoriaSage,
            title = patientName ?: "Esperando sesion",
            subtitle = "El cuidador iniciara la conversacion",
            ring = false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PatientStateArt(
            glyph = visualState.glyph,
            accent = visualState.accent,
            size = 156.dp,
            ring = visualState.ring
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = visualState.title,
            color = MemoriaInk,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = visualState.subtitle,
            color = MemoriaMuted,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 440.dp)
        )
    }
}

private data class PatientVisualState(
    val glyph: MemoriaGlyph,
    val accent: Color,
    val title: String,
    val subtitle: String,
    val ring: Boolean
)

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
