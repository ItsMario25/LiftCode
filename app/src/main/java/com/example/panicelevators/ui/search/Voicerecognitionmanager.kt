// ui/search/VoiceRecognitionManager.kt
package com.example.panicelevators.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Estados posibles del reconocimiento de voz
 */
sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Result(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

/**
 * Normaliza el texto reconocido por voz al formato de código de error.
 * Cubre Schindler (E 10–E 60, E 1A–E 1F) y MONARCH (Err01–Err30).
 *
 * Casos cubiertos:
 *   "e catorce"  → "E 14"
 *   "e 14"       → "E 14"
 *   "e14"        → "E 14"   ← este era el caso problemático
 *   "E14"        → "E 14"
 *   "e uno a"    → "E 1A"
 *   "e1a"        → "E 1A"
 *   "error dos"  → "Err02"
 *   "err 12"     → "Err12"
 *   "parada..."  → "parada..." (búsqueda libre)
 */
fun normalizeVoiceResult(input: String): String {
    val text = input.trim()
    val lower = text.lowercase()

    // ── Mapa de números en español ────────────────────────────────────────────
    val numberWords = mapOf(
        "uno" to "1", "un" to "1",
        "dos" to "2",
        "tres" to "3",
        "cuatro" to "4",
        "cinco" to "5",
        "seis" to "6",
        "siete" to "7",
        "ocho" to "8",
        "nueve" to "9",
        "diez" to "10",
        "once" to "11",
        "doce" to "12",
        "trece" to "13",
        "catorce" to "14",
        "quince" to "15",
        "dieciséis" to "16", "dieciseis" to "16",
        "diecisiete" to "17",
        "dieciocho" to "18",
        "diecinueve" to "19",
        "veinte" to "20",
        "veintiuno" to "21", "veintiún" to "21",
        "veintidós" to "22", "veintidos" to "22",
        "veintitrés" to "23", "veintitres" to "23",
        "veinticuatro" to "24",
        "veinticinco" to "25",
        "veintiséis" to "26", "veintiseis" to "26",
        "veintisiete" to "27",
        "veintiocho" to "28",
        "veintinueve" to "29",
        "treinta" to "30",
        "cuarenta" to "40",
        "cuarenta y uno" to "41",
        "cuarenta y dos" to "42",
        "cuarenta y tres" to "43",
        "cuarenta y cuatro" to "44",
        "cuarenta y cinco" to "45",
        "cincuenta" to "50",
        "sesenta" to "60"
    )

    // ── Mapa de letras hex en español ─────────────────────────────────────────
    val hexWords = mapOf(
        "a" to "A",
        "be" to "B", "b" to "B",
        "ce" to "C", "c" to "C",
        "de" to "D", "d" to "D",
        "efe" to "F", "f" to "F"
    )

    // ── 1. MONARCH: "error X" / "err X" ──────────────────────────────────────
    val errRegex = Regex("^(?:error|err)\\s+(.+)$")
    errRegex.find(lower)?.let { match ->
        val numPart = match.groupValues[1].trim()
        val n = numPart.toIntOrNull()
            ?: numberWords[numPart]?.toIntOrNull()
        if (n != null) return "Err${n.toString().padStart(2, '0')}"
    }

    // ── 2. Schindler: normalizar "e14" / "e1a" → "e 14" / "e 1a" ────────────
    //    Esta es la clave del fix: agregar el espacio ANTES de parsear
    val lowerFixed = lower
        .replace(Regex("^e(\\d+[a-f]?)$")) { m ->
            "e ${m.groupValues[1]}"
        }

    // ── 3. Schindler: parsear "e ..." ─────────────────────────────────────────
    val eRegex = Regex("^e\\s+(.+)$")
    eRegex.find(lowerFixed)?.let { match ->
        val rest = match.groupValues[1].trim()

        // Caso A: número directo — "e 14"
        rest.toIntOrNull()?.let { n ->
            return "E $n"
        }

        // Caso B: número + hex directo — "e 1a"
        val digitHexRegex = Regex("^(\\d+)([a-f])$")
        digitHexRegex.find(rest)?.let { m ->
            return "E ${m.groupValues[1]}${m.groupValues[2].uppercase()}"
        }

        // Caso C: palabra + letra hex — "e uno a"
        val parts = rest.split(" ")
        if (parts.size == 2) {
            val num = numberWords[parts[0]] ?: parts[0]
            val hex = hexWords[parts[1]]
            if (hex != null) return "E $num$hex"
        }

        // Caso D: solo palabra — "e catorce"
        numberWords[rest]?.let { n -> return "E $n" }
    }

    // ── 4. Fallback: texto libre para búsqueda por título o descripción ───────
    return text
}

/**
 * Manager que encapsula el ciclo de vida del SpeechRecognizer.
 */
class VoiceRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var onStateChange: ((VoiceState) -> Unit)? = null

    fun setOnStateChange(listener: (VoiceState) -> Unit) {
        onStateChange = listener
    }

    fun startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onStateChange?.invoke(VoiceState.Listening)
            }

            override fun onBeginningOfSpeech() {
                onStateChange?.invoke(VoiceState.Listening)
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // ✅ Cierre automático — no requiere 2do toque
                onStateChange?.invoke(VoiceState.Processing)
            }

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Sin permisos de micrófono"
                    SpeechRecognizer.ERROR_NETWORK -> "Sin conexión a red"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de red agotado"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ningún texto"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                    else -> "Error desconocido"
                }
                onStateChange?.invoke(VoiceState.Error(message))
            }

            override fun onResults(results: Bundle?) {
                val raw = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""

                if (raw.isNotBlank()) {
                    // ✅ Normalizar antes de insertar en el campo
                    val normalized = normalizeVoiceResult(raw)
                    onStateChange?.invoke(VoiceState.Result(normalized))
                } else {
                    onStateChange?.invoke(VoiceState.Error("No se reconoció ningún texto"))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // ✅ Cierre automático tras 1.5s de silencio
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                1500L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                1000L
            )
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onStateChange?.invoke(VoiceState.Idle)
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        onStateChange = null
    }
}