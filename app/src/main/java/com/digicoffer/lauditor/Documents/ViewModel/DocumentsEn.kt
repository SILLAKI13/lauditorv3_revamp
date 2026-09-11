package com.digicoffer.lauditor.Documents.ViewModel

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile
import com.digicoffer.lauditor.feature.documents.data.repository.DocumentsRepository
import com.digicoffer.lauditor.feature.documents.presentation.screen.DocumentsScreen
import com.digicoffer.lauditor.feature.documents.presentation.state.DocumentsUiEvent
import com.digicoffer.lauditor.feature.documents.presentation.viewmodel.DocumentsViewModel
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import java.io.File

class DocumentsEn : Fragment(), BottomSheetUploadFile.OnPhotoSelectedListner {

    private lateinit var viewModel: DocumentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val repository = DocumentsRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DocumentsViewModel(requireActivity().application, repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(DocumentsViewModel::class.java)

        // Parse arguments matching legacy drawer click bundle parameter
        val docType = arguments?.getString("document_type") ?: "matter"
        viewModel.onEvent(DocumentsUiEvent.SwitchTab(docType))

        val titleViewModel = try {
            ViewModelProvider(requireActivity()).get(com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel::class.java)
        } catch (e: Exception) {
            null
        }

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    DocumentsScreen(
                        viewModel = viewModel,
                        onTitleChanged = { title ->
                            titleViewModel?.setData(title)
                        },
                        onBrowseClick = {
                            val bottommSheetUploadDocument = BottomSheetUploadFile(null)
                            bottommSheetUploadDocument.setOnPhotoSelectedListener(this@DocumentsEn)
                            bottommSheetUploadDocument.setTargetFragment(this@DocumentsEn, 1)
                            bottommSheetUploadDocument.show(parentFragmentManager, "BottomSheetUploadFile")
                        }
                    )
                }
            }
        }
    }

    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        if (imagepath != null) {
            val name = imagepath.name
            viewModel.onEvent(DocumentsUiEvent.AddStagedFile(imagepath, name))
        } else if (ImageURI != null) {
            val context = requireContext()
            val uploadDir = File(context.filesDir, "staged_uploads").apply {
                if (!exists()) mkdirs()
            }
            val destinationFilename = File(uploadDir, queryName(context, ImageURI))
            try {
                context.contentResolver.openInputStream(ImageURI).use { ins ->
                    if (ins != null) {
                        createFileFromStream(ins, destinationFilename)
                    }
                }
                viewModel.onEvent(DocumentsUiEvent.AddStagedFile(destinationFilename, destinationFilename.name))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun getImageBitmap(bitmap: android.graphics.Bitmap?) {}

    // Helper functions to read selected file name and stream it
    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null) ?: return ""
        val nameIndex = returnCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    private fun createFileFromStream(ins: java.io.InputStream, destination: File) {
        try {
            java.io.FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
