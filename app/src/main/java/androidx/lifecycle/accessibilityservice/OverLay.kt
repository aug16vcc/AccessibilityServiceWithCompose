package androidx.lifecycle.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

fun AccessibilityService.setComposeOverlay(
    lifecycleOwner: LifecycleOwner,
    savedStateRegistryOwner: SavedStateRegistryOwner,
    content: @Composable () -> Unit,
): ComposeView {
    val wm = ContextCompat.getSystemService(this, WindowManager::class.java)!!
    val lp = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY

        format = PixelFormat.TRANSLUCENT

        flags =
            flags or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        gravity = Gravity.TOP
    }

    val statusBarHeight = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> getStatusBarHeightPx(wm)
        else -> getStatusBarHeightPx(resources)
    }

    val composeView = ComposeView(this).apply {
        setContent {
            CompositionLocalProvider(LocalStatusBarHeight provides statusBarHeight) {
                content()
            }
        }
        isClickable = false
        isFocusable = false
        ViewTreeLifecycleOwner.set(this, lifecycleOwner)
        setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
    }
    wm.addView(composeView, lp)

    return composeView
}

@RequiresApi(Build.VERSION_CODES.R)
private fun getStatusBarHeightPx(wm: WindowManager) = wm.maximumWindowMetrics
    .windowInsets
    .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
    .top

val LocalStatusBarHeight = staticCompositionLocalOf { 0 }

@SuppressLint("DiscouragedApi", "InternalInsetResource")
private fun getStatusBarHeightPx(resources: Resources): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}
