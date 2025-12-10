// app/utils/MusicPlayer.kt
package app.utils

import javax.sound.sampled.*
import java.io.BufferedInputStream

object MusicPlayer {
    private var clip: Clip? = null
    private var isPlaying = false

    fun play(soundName: String) {
        if (isPlaying) return
        Thread {
            try {
                val resourcePath = "/sounds/$soundName"
                val stream = MusicPlayer::class.java.getResourceAsStream(resourcePath)
                    ?: return@Thread

                BufferedInputStream(stream).use { bis ->
                    AudioSystem.getAudioInputStream(bis).use { audioStream ->
                        AudioSystem.getClip().apply {
                            open(audioStream)
                            this@MusicPlayer.clip = this
                            isPlaying = true
                            loop(Clip.LOOP_CONTINUOUSLY)
                            start()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun stop() {
        clip?.let {
            if (it.isRunning || it.isActive) {
                it.stop()
                it.close()
            }
        }
        clip = null
        isPlaying = false
    }
}