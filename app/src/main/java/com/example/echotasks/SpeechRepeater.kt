package com.example.echotasks

import android.content.Context
import android.speech.tts.TextToSpeech
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import java.util.*

object SpeechRepeater {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var tts: TextToSpeech? = null
    private const val NOTIF_ID = 1
    private var repeatCount = 0
    private const val MAX_REPEATS = 5


    fun start(context: Context, text: String) {
        handler = Handler(Looper.getMainLooper())
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                repeatCount = 0
                runnable = object : Runnable {
                    override fun run() {
                        if (repeatCount < MAX_REPEATS) {
                            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TASK_TTS")
                            repeatCount++
                            handler?.postDelayed(this, 5000)
                        }
                    }
                }
                handler?.post(runnable!!)
            }
        }
    }

    fun stop(context: Context) {
        runnable?.let { handler?.removeCallbacks(it) }
        tts?.stop()
        tts?.shutdown()

        try {
            NotificationManagerCompat.from(context).cancel(NOTIF_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
