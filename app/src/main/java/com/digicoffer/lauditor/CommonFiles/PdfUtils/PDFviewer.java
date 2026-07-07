package com.digicoffer.lauditor.CommonFiles.PdfUtils;

import com.github.barteksc.pdfviewer.PDFView;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PDFviewer extends AppCompatActivity {
    PDFView pdfView;
    String decryptedString;

    public PDFviewer(PDFView idPDFView,String decryptedString1) {
        pdfView = idPDFView;
        decryptedString=decryptedString1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Assuming your decrypted data is a string

        // Create PDF file path
        File pdfFile = new File(getExternalFilesDir(null), "decrypted_data.pdf");

        try {
            // Create a PdfWriter instance
            PdfWriter writer = new PdfWriter(Files.newOutputStream(pdfFile.toPath()));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Add the decrypted string to the PDF document
            document.add(new Paragraph(decryptedString));

            // Close the document
            document.close();

            // Get reference to PDFView

            // Display the PDF file in PDFView
            pdfView.fromFile(pdfFile)
                    .load();
            pdfView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
