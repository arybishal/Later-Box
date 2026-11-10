package com.laterbox.util.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    fun startRecording(targetFile: File): Boolean {
        if (_recordingState.value != RecordingState.IDLE) {
            stopRecording()
        }

        return try {
            currentOutputFile = targetFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(targetFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _recordingState.value = RecordingState.RECORDING
            _elapsedSeconds.value = 0
            startTicker()
            true
        } catch (e: Exception) {
            cancelRecording()
            false
        }
    }

    fun pauseRecording(): Boolean {
        if (_recordingState.value != RecordingState.RECORDING) return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                _recordingState.value = RecordingState.PAUSED
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun resumeRecording(): Boolean {
        if (_recordingState.value != RecordingState.PAUSED) return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                _recordingState.value = RecordingState.RECORDING
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun stopRecording(): File? {
        timerJob?.cancel()
        timerJob = null

        val file = currentOutputFile
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
            // Ignore if already stopped or failed
        } finally {
            mediaRecorder = null
            _recordingState.value = RecordingState.IDLE
            _amplitude.value = 0
            currentOutputFile = null
        }
        return file
    }

    fun cancelRecording() {
        timerJob?.cancel()
        timerJob = null

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
            // Safe cleanup
        } finally {
            mediaRecorder = null
            _recordingState.value = RecordingState.IDLE
            _amplitude.value = 0
            currentOutputFile?.let {
                if (it.exists()) it.delete()
            }
            currentOutputFile = null
        }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _recordingState.value != RecordingState.IDLE) {
                delay(100)
                if (_recordingState.value == RecordingState.RECORDING) {
                    try {
                        _amplitude.value = mediaRecorder?.maxAmplitude ?: 0
                    } catch (_: Exception) {
                        _amplitude.value = 0
                    }
                }
            }
        }

        // Second counter ticker
        scope.launch {
            while (isActive && _recordingState.value != RecordingState.IDLE) {
                delay(1000)
                if (_recordingState.value == RecordingState.RECORDING) {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }
}
