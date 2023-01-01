package com.aug16vcc.accessibility

import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.accessibilityservice.ComponentAccessibilityService
import androidx.lifecycle.accessibilityservice.setComposeOverlay

class BoundingOverlayService : ComponentAccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        // noop
    }

    override fun onSetOverlay() {
        setComposeOverlay(this, this) {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color.Black)
                )
            }
        }
    }
}
