package androidx.lifecycle.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

abstract class ComponentAccessibilityService : AccessibilityService(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    @Suppress("LeakingThis")
    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    @Suppress("LeakingThis")
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry = savedStateRegistryController.savedStateRegistry

    override fun getLifecycle(): Lifecycle = lifecycleDispatcher.lifecycle

    // region main lifecycle
    @CallSuper
    override fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onServiceConnected() {
        lifecycleDispatcher.onServicePreSuperOnBind()
        super.onServiceConnected()
        onSetOverlay()
    }

    open fun onSetOverlay() {
        // noop
    }

    @CallSuper
    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onStart(intent: Intent?, startId: Int) {
        lifecycleDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    @CallSuper
    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }
    // endregion
}
