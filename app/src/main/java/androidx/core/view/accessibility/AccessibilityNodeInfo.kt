package androidx.core.view.accessibility

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.graphics.toComposeRect

fun AccessibilityNodeInfo.getRect(): Rect {
    val r = Rect()
    this.getBoundsInScreen(r)
    return r
}

@Suppress("SpellCheckingInspection")
fun scanInteractableAccessibilityNodeInfo(
    root: AccessibilityNodeInfo,
    enabled: Boolean = true,
): List<Pair<AccessibilityNodeInfo, androidx.compose.ui.geometry.Rect>> {
    val accumulatingResult =
        mutableListOf<Pair<AccessibilityNodeInfo, androidx.compose.ui.geometry.Rect>>()
    scanInteractableAccessibilityNodeInfo(accumulatingResult, root, 0) { nodeInfo ->
        nodeInfo.isEnabled == enabled
            && (nodeInfo.isClickable
            || nodeInfo.isFocusable
            || nodeInfo.isCheckable
            || nodeInfo.isDismissable
            )
            && nodeInfo.isVisibleToUser
    }
    return accumulatingResult.toList()
}

@Suppress("SpellCheckingInspection")
private fun scanInteractableAccessibilityNodeInfo(
    accumulatingList: MutableList<Pair<AccessibilityNodeInfo, androidx.compose.ui.geometry.Rect>>,
    nodeInfo: AccessibilityNodeInfo?,
    depth: Int,
    filteringCondition: (AccessibilityNodeInfo) -> Boolean = { true },
) {
    if (nodeInfo == null) return

    if (filteringCondition(nodeInfo)) {
        accumulatingList.add(nodeInfo to nodeInfo.getRect().toComposeRect())
    }
    for (index in 0 until nodeInfo.childCount) {
        scanInteractableAccessibilityNodeInfo(
            accumulatingList, nodeInfo.getChild(index), depth + 1, filteringCondition
        )
    }
}

@Deprecated("For logcat debug purpose")
fun logNodeHierarchy(nodeInfo: AccessibilityNodeInfo?, depth: Int, tag: String) {
    if (nodeInfo == null) return

    if (nodeInfo.isEnabled && (nodeInfo.isClickable || nodeInfo.isFocusable || nodeInfo.isCheckable || nodeInfo.isDismissable)) {

        val b = if (depth == 0) "" else "|"
        val c = b + "-".repeat(depth)
        val r = Rect()
        r.toComposeRect()
        nodeInfo.getBoundsInScreen(r)
        val s = getText(nodeInfo)
        val logString = "${c}$s\n(x=${r.left}, y=${r.top}, w=${r.width()}, h=${r.height()}) "
        "${nodeInfo.actionList}"
        Log.v(tag, logString)
    }
    for (i in 0 until nodeInfo.childCount) {
        @Suppress("DEPRECATION")
        (logNodeHierarchy(
            nodeInfo.getChild(i),
            depth + 1,
            tag
        ))
    }
}

private fun getText(nodeInfo: AccessibilityNodeInfo): String {
    val a = buildString {
        append(nodeInfo.text ?: "")
        nodeInfo.contentDescription?.let { append("-$it") }
    }
    if (a.isBlank()) {
        for (i in 0 until nodeInfo.childCount) {
            val b = getText(nodeInfo.getChild(0))
            if (b.isNotBlank()) {
                return b
            }
        }
    }
    return a
}
