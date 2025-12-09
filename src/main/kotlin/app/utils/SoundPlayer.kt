package app.utils

import java.io.BufferedInputStream
import java.io.InputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

object SoundPlayer {
    private var lastPlayTime = 0L
    private const val MIN_DELAY_MS = 80L  // антиспам — не больше 12 звуков в секунду

    fun play(soundName: String) {
        val now = System.currentTimeMillis()
        if (now - lastPlayTime < MIN_DELAY_MS) return
        lastPlayTime = now

        Thread {
            try {
                val resourcePath = "/sounds/$soundName"
                val inputStream: InputStream = SoundPlayer::class.java.getResourceAsStream(resourcePath)
                    ?: return@Thread

                BufferedInputStream(inputStream).use { bis ->
                    AudioSystem.getAudioInputStream(bis).use { audioStream ->
                        AudioSystem.getClip().apply {
                            open(audioStream)
                            addLineListener { event ->
                                if (event.type == LineEvent.Type.STOP) {
                                    close()
                                }
                            }
                            start()
                        }
                    }
                }
            } catch (e: Exception) {
                // Звук не сработал — молчим, чтобы не падало приложение
            }
        }.start()
    }


    fun zoom() = play("zoom.wav")
}