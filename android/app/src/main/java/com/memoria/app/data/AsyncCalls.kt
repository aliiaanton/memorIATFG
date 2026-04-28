package com.memoria.app.data

import android.os.Handler
import android.os.Looper

private val mainHandler = Handler(Looper.getMainLooper())

fun <T> runAsync(
    action: () -> T,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    Thread {
        try {
            val result = action()
            mainHandler.post { onSuccess(result) }
        } catch (exception: Exception) {
            mainHandler.post { onError(exception.message ?: "Error inesperado") }
        }
    }.start()
}

