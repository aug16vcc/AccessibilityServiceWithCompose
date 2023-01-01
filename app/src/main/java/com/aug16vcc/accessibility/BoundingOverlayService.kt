package com.aug16vcc.accessibility

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.accessibility.scanInteractableAccessibilityNodeInfo
import androidx.lifecycle.accessibilityservice.ComponentAccessibilityService
import androidx.lifecycle.accessibilityservice.setComposeOverlay
import androidx.lifecycle.lifecycleScope
import com.aug16vcc.app.ui.LabeledBound
import com.aug16vcc.app.ui.LabeledBoundState
import com.aug16vcc.app.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoundingOverlayService : ComponentAccessibilityService() {

    companion object {
        private val TAG: String = BoundingOverlayService::class.java.simpleName
    }

    @Suppress("SpellCheckingInspection")
    private val scanningInteractableNodesFlow = MutableSharedFlow<Unit>()

    private val highlightedIndex = MutableStateFlow(0)

    @OptIn(FlowPreview::class)
    @Suppress("SpellCheckingInspection")
    private val interactableNodes = scanningInteractableNodesFlow
        .sample(100L)
        .map {
            Log.d(TAG, "scaning interactable ui elements $rootInActiveWindow")
            rootInActiveWindow
                ?.let(::scanInteractableAccessibilityNodeInfo)
                ?.filter { (_, rect) ->
                    rect.height * rect.width > 1f
                }
                ?.sortedBy { (_, rect) ->
                    rect.left + rect.top * rect.top
                }
                ?: emptyList()
        }
        .distinctUntilChanged()
        .onEach {
            Log.d(TAG, "scanned ${it.size} elements")
            highlightedIndex.value = -1
        }
        .flowOn(Dispatchers.Default)
        .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val bondingElements = interactableNodes
        .combine(highlightedIndex) { nodes, highlighted ->
            nodes.mapIndexed { index, (_, rect) ->
                LabeledBoundState(index + 1, rect, index == highlighted)
            }
        }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Notice: clipboard can't be retrieved by a background service in Android 10 and above.
        // Therefore we do not include the function of clipboard tracking from the application
        Log.d(TAG, "onAccessibilityEvent: $event")
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_ANNOUNCEMENT,
            -> lifecycleScope.launch {
                scanningInteractableNodesFlow.emit(Unit)
            }
            else -> Unit
        }
    }

    override fun onInterrupt() {
        // noop
    }

    override fun onSetOverlay() {
        setComposeOverlay(this, this) {
            AppTheme {
                val elements by bondingElements.collectAsState(initial = emptyList())
                Box(modifier = Modifier.fillMaxSize()) {
                    for (element in elements) {
                        ReusableContent(element.index) {
                            LabeledBound(element)
                        }
                    }
                }
            }
        }
    }

    private fun highlightNext() {
        highlightedIndex.update {
            val nodes = interactableNodes.value
            when {
                nodes.isEmpty() -> -1
                else -> (it + 1).mod(nodes.size)
            }
        }
    }

    private fun highlightPrevious() {
        highlightedIndex.update {
            val nodes = interactableNodes.value
            when {
                nodes.isEmpty() -> -1
                else -> (it - 1).mod(nodes.size)
            }
        }
    }
}
