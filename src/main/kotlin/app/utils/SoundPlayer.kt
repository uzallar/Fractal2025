package app.utils

import java.io.BufferedInputStream
import java.io.InputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

object SoundPlayer {
    private var lastZoomTime = 0L
    private var lastPanTime = 0L
    private const val MIN_DELAY_ZOOM_MS = 80L
    private const val MIN_DELAY_PAN_MS = 2600L  // разная задержка для разных звуков

    private fun playSound(soundName: String, lastPlayTime: Long, minDelay: Long): Long {
        val now = System.currentTimeMillis()
        if (now - lastPlayTime < minDelay) return lastPlayTime

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
                // Звук не сработал — молчим
            }
        }.start()

        return now
    }

    fun zoom() {
        lastZoomTime = playSound("мяу.wav", lastZoomTime, MIN_DELAY_ZOOM_MS)
    }

    fun pan() {
        lastPanTime = playSound("мур.wav", lastPanTime, MIN_DELAY_PAN_MS)
    }
}