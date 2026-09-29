package com.digicoffer.lauditor.feature.profile.presentation.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen

@Composable
fun PhotoChooserDialog(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(Unit) {
        val fileChooser = Filecosen(activity, object : Filecosen.FileChooserCallback {
            override fun openCamera() {
                onCameraClick()
            }

            override fun openGallery() {
                onGalleryClick()
            }
        })
        fileChooser.show(onDismiss = onDismiss)

        onDispose {
            // Dismiss if still active when Composable leaves composition
        }
    }
}
