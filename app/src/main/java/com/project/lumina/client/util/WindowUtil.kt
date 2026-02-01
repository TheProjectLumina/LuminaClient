package com.project.lumina.client.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun getDialogWindow(): Window? {
    return (LocalView.current.parent as? DialogWindowProvider)?.window
}

@Composable
fun getActivityWindow(): Window? {
    return LocalView.current.context.getActivityWindow()
}

fun windowFullScreen(
    activityWindow: Window?,
    dialogWindow: Window?,
) {
    if (activityWindow != null && dialogWindow != null) {
        val attributes = WindowManager.LayoutParams()
        attributes.copyFrom(activityWindow.attributes)
        attributes.type = dialogWindow.attributes.type

        dialogWindow.attributes = attributes
        dialogWindow.setLayout(
            activityWindow.decorView.width,
            activityWindow.decorView.height
        )
    }
}

private tailrec fun Context.getActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.getActivityWindow()
    else -> null
}