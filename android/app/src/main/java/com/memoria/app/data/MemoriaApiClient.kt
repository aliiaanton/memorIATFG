package com.memoria.app.data

import com.memoria.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class PatientSummary(
    val id: String,
    val fullName: String,
    val preferredName: String?,
    val notes: String?
)

data class PairingCodeSummary(
    val code: String
)

data class LoopRuleSummary(
    val id: String,
    val question: String,
    val answer: String,
    val active: Boolean
)

data class DangerousTopicSummary(
    val id: String,
    val term: String,
    val redirectHint: String?,
    val active: Boolean
)

data class SafeMemorySummary(
    val id: String,
    val title: String,
    val content: String,
    val active: Boolean
)

data class SessionSummary(
    val id: String,
    val status: String,
    val startedAt: String?,
    val endedAt: String?,
    val createdAt: String?
)

data class ConversationMessageSummary(
    val sender: String,
    val content: String,
    val createdAt: String?
)

data class SessionEventSummary(
    val eventType: String,
    val description: String,
    val createdAt: String?
)

data class ConversationResult(
    val responseText: String,
    val source: String,
    val alertCreated: Boolean
)

data class AlertSummary(
    val id: String?,
    val sessionId: String?,
    val title: String,
    val message: String,
    val createdAt: String?
)

data class TerminalStatus(
    val linked: Boolean,
    val patientName: String?,
    val sessionId: String?,
    val sessionStatus: String
)

class MemoriaApiClient(
    private val baseUrl: String = BuildConfig.BACKEND_BASE_URL
) {

    fun health(): String {
        return JSONObject(request("GET", "/health")).getString("status")
    }

    fun listPatients(): List<PatientSummary> {
        val array = JSONArray(request("GET", "/patients"))
        return List(array.length()) { index ->
            parsePatient(array.getJSONObject(index))
        }
    }

    fun createPatient(fullName: String, preferredName: String, notes: String = ""): PatientSummary {
        val body = JSONObject()
            .put("fullName", fullName)
            .put("preferredName", preferredName)
            .put("notes", notes)
            .put("textSize", "large")
            .put("ttsSpeed", 1.0)
        val item = JSONObject(request("POST", "/patients", body))
        return parsePatient(item)
    }

    fun updatePatient(patientId: String, fullName: String, preferredName: String, notes: String = ""): PatientSummary {
        val body = JSONObject()
            .put("fullName", fullName)
            .put("preferredName", preferredName)
            .put("notes", notes)
            .put("textSize", "large")
            .put("ttsSpeed", 1.0)
        val item = JSONObject(request("PUT", "/patients/$patientId", body))
        return parsePatient(item)
    }

    fun deletePatient(patientId: String) {
        request("DELETE", "/patients/$patientId")
    }

    fun listLoopRules(patientId: String): List<LoopRuleSummary> {
        val array = JSONArray(request("GET", "/patients/$patientId/loop-rules"))
        return List(array.length()) { index ->
            parseLoopRule(array.getJSONObject(index))
        }
    }

    fun createLoopRule(patientId: String, question: String, answer: String): LoopRuleSummary {
        val body = JSONObject()
            .put("question", question)
            .put("answer", answer)
            .put("active", true)
        val item = JSONObject(request("POST", "/patients/$patientId/loop-rules", body))
        return parseLoopRule(item)
    }

    fun updateLoopRule(
        patientId: String,
        ruleId: String,
        question: String,
        answer: String,
        active: Boolean
    ): LoopRuleSummary {
        val body = JSONObject()
            .put("question", question)
            .put("answer", answer)
            .put("active", active)
        val item = JSONObject(request("PUT", "/patients/$patientId/loop-rules/$ruleId", body))
        return parseLoopRule(item)
    }

    fun deleteLoopRule(patientId: String, ruleId: String) {
        request("DELETE", "/patients/$patientId/loop-rules/$ruleId")
    }

    fun listDangerousTopics(patientId: String): List<DangerousTopicSummary> {
        val array = JSONArray(request("GET", "/patients/$patientId/dangerous-topics"))
        return List(array.length()) { index ->
            parseDangerousTopic(array.getJSONObject(index))
        }
    }

    fun createDangerousTopic(patientId: String, term: String, redirectHint: String): DangerousTopicSummary {
        val body = JSONObject()
            .put("term", term)
            .put("redirectHint", redirectHint)
            .put("active", true)
        val item = JSONObject(request("POST", "/patients/$patientId/dangerous-topics", body))
        return parseDangerousTopic(item)
    }

    fun updateDangerousTopic(
        patientId: String,
        topicId: String,
        term: String,
        redirectHint: String,
        active: Boolean
    ): DangerousTopicSummary {
        val body = JSONObject()
            .put("term", term)
            .put("redirectHint", redirectHint)
            .put("active", active)
        val item = JSONObject(request("PUT", "/patients/$patientId/dangerous-topics/$topicId", body))
        return parseDangerousTopic(item)
    }

    fun deleteDangerousTopic(patientId: String, topicId: String) {
        request("DELETE", "/patients/$patientId/dangerous-topics/$topicId")
    }

    fun listSafeMemories(patientId: String): List<SafeMemorySummary> {
        val array = JSONArray(request("GET", "/patients/$patientId/safe-memories"))
        return List(array.length()) { index ->
            parseSafeMemory(array.getJSONObject(index))
        }
    }

    fun createSafeMemory(patientId: String, title: String, content: String): SafeMemorySummary {
        val body = JSONObject()
            .put("title", title)
            .put("content", content)
            .put("active", true)
        val item = JSONObject(request("POST", "/patients/$patientId/safe-memories", body))
        return parseSafeMemory(item)
    }

    fun updateSafeMemory(
        patientId: String,
        memoryId: String,
        title: String,
        content: String,
        active: Boolean
    ): SafeMemorySummary {
        val body = JSONObject()
            .put("title", title)
            .put("content", content)
            .put("active", active)
        val item = JSONObject(request("PUT", "/patients/$patientId/safe-memories/$memoryId", body))
        return parseSafeMemory(item)
    }

    fun deleteSafeMemory(patientId: String, memoryId: String) {
        request("DELETE", "/patients/$patientId/safe-memories/$memoryId")
    }

    fun createPairingCode(patientId: String): PairingCodeSummary {
        val item = JSONObject(request("POST", "/patients/$patientId/pairing-codes"))
        return PairingCodeSummary(code = item.getString("code"))
    }

    fun linkDevice(code: String, deviceIdentifier: String, deviceName: String) {
        val body = JSONObject()
            .put("code", code)
            .put("deviceIdentifier", deviceIdentifier)
            .put("deviceName", deviceName)
        request("POST", "/patient-devices/link", body)
    }

    fun createSession(patientId: String): SessionSummary {
        val item = JSONObject(request("POST", "/patients/$patientId/sessions"))
        return parseSession(item)
    }

    fun listSessions(patientId: String): List<SessionSummary> {
        val array = JSONArray(request("GET", "/patients/$patientId/sessions"))
        return List(array.length()) { index ->
            parseSession(array.getJSONObject(index))
        }
    }

    fun startSession(sessionId: String): SessionSummary {
        return sessionCommand(sessionId, "start")
    }

    fun pauseSession(sessionId: String): SessionSummary {
        return sessionCommand(sessionId, "pause")
    }

    fun resumeSession(sessionId: String): SessionSummary {
        return sessionCommand(sessionId, "resume")
    }

    fun endSession(sessionId: String): SessionSummary {
        return sessionCommand(sessionId, "end")
    }

    fun terminalStatus(deviceIdentifier: String): TerminalStatus {
        val item = JSONObject(request("GET", "/patient-terminal/$deviceIdentifier/status"))
        return TerminalStatus(
            linked = item.getBoolean("linked"),
            patientName = nullableString(item, "patientName"),
            sessionId = nullableString(item, "sessionId"),
            sessionStatus = item.getString("sessionStatus")
        )
    }

    fun sendPatientMessage(deviceIdentifier: String, sessionId: String, text: String): ConversationResult {
        val body = JSONObject().put("text", text)
        val item = JSONObject(request("POST", "/patient-terminal/$deviceIdentifier/sessions/$sessionId/messages", body))
        return ConversationResult(
            responseText = item.getString("responseText"),
            source = item.getString("source"),
            alertCreated = item.getBoolean("alertCreated")
        )
    }

    fun listTranscript(sessionId: String): List<ConversationMessageSummary> {
        val array = JSONArray(request("GET", "/sessions/$sessionId/transcript"))
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            ConversationMessageSummary(
                sender = item.getString("sender"),
                content = item.getString("content"),
                createdAt = nullableString(item, "createdAt")
            )
        }
    }

    fun listEvents(sessionId: String): List<SessionEventSummary> {
        val array = JSONArray(request("GET", "/sessions/$sessionId/events"))
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            SessionEventSummary(
                eventType = item.getString("eventType"),
                description = item.getString("description"),
                createdAt = nullableString(item, "createdAt")
            )
        }
    }

    fun listAlerts(): List<AlertSummary> {
        val array = JSONArray(request("GET", "/alerts"))
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            AlertSummary(
                id = nullableString(item, "id"),
                sessionId = nullableString(item, "sessionId"),
                title = item.getString("title"),
                message = item.getString("message"),
                createdAt = nullableString(item, "createdAt")
            )
        }
    }

    private fun sessionCommand(sessionId: String, command: String): SessionSummary {
        val item = JSONObject(request("POST", "/sessions/$sessionId/$command"))
        return parseSession(item)
    }

    private fun parsePatient(item: JSONObject): PatientSummary {
        return PatientSummary(
            id = item.getString("id"),
            fullName = item.getString("fullName"),
            preferredName = nullableString(item, "preferredName"),
            notes = nullableString(item, "notes")
        )
    }

    private fun parseLoopRule(item: JSONObject): LoopRuleSummary {
        return LoopRuleSummary(
            id = item.getString("id"),
            question = item.getString("question"),
            answer = item.getString("answer"),
            active = item.getBoolean("active")
        )
    }

    private fun parseDangerousTopic(item: JSONObject): DangerousTopicSummary {
        return DangerousTopicSummary(
            id = item.getString("id"),
            term = item.getString("term"),
            redirectHint = nullableString(item, "redirectHint"),
            active = item.getBoolean("active")
        )
    }

    private fun parseSafeMemory(item: JSONObject): SafeMemorySummary {
        return SafeMemorySummary(
            id = item.getString("id"),
            title = item.getString("title"),
            content = item.getString("content"),
            active = item.getBoolean("active")
        )
    }

    private fun parseSession(item: JSONObject): SessionSummary {
        return SessionSummary(
            id = item.getString("id"),
            status = item.getString("status"),
            startedAt = nullableString(item, "startedAt"),
            endedAt = nullableString(item, "endedAt"),
            createdAt = nullableString(item, "createdAt")
        )
    }

    private fun nullableString(item: JSONObject, key: String): String? {
        if (!item.has(key) || item.isNull(key)) {
            return null
        }
        return item.optString(key).ifBlank { null }
    }

    private fun request(method: String, path: String, body: JSONObject? = null): String {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        if (body != null) {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }

        connection.disconnect()

        if (status !in 200..299) {
            throw IllegalStateException(response.ifBlank { "HTTP $status" })
        }

        return response
    }
}
