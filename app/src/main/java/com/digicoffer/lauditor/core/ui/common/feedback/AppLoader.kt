package com.digicoffer.lauditor.core.ui.common.feedback

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AppLoader(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        var progressDialog: Dialog? = null
        val activity = context.findActivity()
        if (activity != null && !activity.isFinishing) {
            progressDialog = AndroidUtils.get_progress(activity)
        }
        onDispose {
            if (progressDialog != null) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }
}
