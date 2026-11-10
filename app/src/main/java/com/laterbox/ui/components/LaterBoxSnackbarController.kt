package com.laterbox.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onAction: (() -> Unit)? = null
)

/**
 * Global singleton controller for triggering standardized Later Box snackbars.
 * Provides consistent feedback across all screens without relying on raw platform Toasts.
 */
object LaterBoxSnackbarController {

    private val _events = MutableSharedFlow<SnackbarMessage>(replay = 1, extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    fun showMessage(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        _events.tryEmit(
            SnackbarMessage(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
                onAction = onAction
            )
        )
    }

    suspend fun observeAndShow(
        hostState: SnackbarHostState,
        coroutineScope: CoroutineScope
    ) {
        events.collect { event ->
            coroutineScope.launch {
                val result = hostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    duration = event.duration,
                    withDismissAction = true
                )
                if (result == SnackbarResult.ActionPerformed) {
                    event.onAction?.invoke()
                }
            }
        }
    }
}
