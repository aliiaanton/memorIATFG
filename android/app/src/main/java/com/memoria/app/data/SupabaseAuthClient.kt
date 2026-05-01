package com.memoria.app.data

import com.memoria.app.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val userId: String?,
    val email: String?
)

sealed class AuthResult {
    data class Authenticated(val session: AuthSession) : AuthResult()
    data class PendingConfirmation(val email: String) : AuthResult()
}

class SupabaseAuthClient(
    private val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    private val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
) {

    fun signIn(email: String, password: String): AuthSession {
        ensureConfigured()
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
        return parseRequiredSession(request("POST", "/auth/v1/token?grant_type=password", body))
    }

    fun signUp(email: String, password: String, fullName: String): AuthResult {
        ensureConfigured()
        val data = JSONObject()
        if (fullName.isNotBlank()) {
            data.put("full_name", fullName)
        }
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("data", data)
        val response = JSONObject(request("POST", "/auth/v1/signup", body))
        val session = parseOptionalSession(response)
        return if (session == null) {
            AuthResult.PendingConfirmation(email)
        } else {
            AuthResult.Authenticated(session)
        }
    }

    private fun parseRequiredSession(response: String): AuthSession {
        val session = parseOptionalSession(JSONObject(response))
        return session ?: throw IllegalStateException("No se pudo iniciar sesion. Revisa tus datos e intentalo de nuevo.")
    }

    private fun parseOptionalSession(item: JSONObject): AuthSession? {
        val sessionObject = if (item.has("session") && !item.isNull("session")) {
            item.getJSONObject("session")
        } else {
            item
        }
        val accessToken = nullableString(sessionObject, "access_token") ?: return null
        val user = item.optJSONObject("user") ?: sessionObject.optJSONObject("user")
        return AuthSession(
            accessToken = accessToken,
            refreshToken = nullableString(sessionObject, "refresh_token"),
            userId = user?.let { nullableString(it, "id") },
            email = user?.let { nullableString(it, "email") }
        )
    }

    private fun request(method: String, path: String, body: JSONObject): String {
        val url = supabaseUrl.trimEnd('/') + path
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", anonKey)
            doOutput = true
        }

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body.toString())
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val response = if (stream == null) {
            ""
        } else {
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
        connection.disconnect()

        if (status !in 200..299) {
            throw IllegalStateException(parseError(response, status))
        }

        return response
    }

    private fun parseError(response: String, status: Int): String {
        val rawMessage = if (response.isBlank()) {
            null
        } else try {
            val item = JSONObject(response)
            nullableString(item, "msg")
                ?: nullableString(item, "message")
                ?: nullableString(item, "error_description")
                ?: response
        } catch (exception: Exception) {
            response
        }

        return when (status) {
            400, 401 -> "Email o contrasena incorrectos."
            422 -> rawMessage?.takeIf { it.isNotBlank() } ?: "Revisa los datos de la cuenta."
            429 -> "Demasiados intentos. Espera un momento y vuelve a probar."
            in 500..599 -> "El acceso con cuenta no esta disponible ahora."
            else -> rawMessage?.takeIf { it.isNotBlank() } ?: "No se pudo completar la autenticacion."
        }
    }

    private fun nullableString(item: JSONObject, key: String): String? {
        if (!item.has(key) || item.isNull(key)) {
            return null
        }
        return item.optString(key).ifBlank { null }
    }

    private fun ensureConfigured() {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            throw IllegalStateException("El acceso con cuenta no esta configurado en este dispositivo.")
        }
    }
}
