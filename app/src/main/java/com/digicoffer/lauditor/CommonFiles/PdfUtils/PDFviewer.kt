package com.digicoffer.lauditor.CommonFiles.PdfUtils

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.IOException
import java.nio.file.Files

class PDFviewer : AppCompatActivity {
    private var pdfView: PDFView? = null
    private var decryptedString: String? = null

    constructor(idPDFView: PDFView?, decryptedString1: String?) : super() {
        pdfView = idPDFView
        decryptedString = decryptedString1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pdfFile = File(getExternalFilesDir(null), "decrypted_data.pdf")

        try {
            val writer = PdfWriter(Files.newOutputStream(pdfFile.toPath()))
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)

            document.add(Paragraph(decryptedString))

            document.close()

            pdfView?.fromFile(pdfFile)?.load()
            pdfView?.visibility = View.VISIBLE
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
