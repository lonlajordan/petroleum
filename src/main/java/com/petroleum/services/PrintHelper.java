package com.petroleum.services;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.petroleum.models.Invoice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PrintHelper {

    public static void print(Invoice invoice, File output) throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfWriter(output.getAbsolutePath(),new WriterProperties().addXmpMetadata()));
        pdf.setDefaultPageSize(PageSize.A5.rotate());
        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
        Document document = new Document(pdf).setFont(font).setFontSize(7);
        float ratio = (float) 820.0 / 297;
        Paragraph paragraph = new Paragraph(String.format("%07d", invoice.getId()));
        paragraph.setFixedPosition(76 * ratio, 110 * ratio, 100);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getClient());
        paragraph.setFixedPosition(32 * ratio, 99 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getTransporter());
        paragraph.setFixedPosition(177 * ratio, 99 * ratio, 200);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getProduct().getName());
        paragraph.setFixedPosition(33 * ratio, 94 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getTruckNumber());
        paragraph.setFixedPosition(174 * ratio, 94 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getVolume() + "");
        paragraph.setFixedPosition(35 * ratio, 89 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getDriver());
        paragraph.setFixedPosition(173 * ratio, 89 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(invoice.getLoadingDate()));
        paragraph.setFixedPosition(53 * ratio, 84 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getDeliveryPlace());
        paragraph.setFixedPosition(183 * ratio, 84 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getLoadingDepot());
        paragraph.setFixedPosition(56 * ratio, 79 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(invoice.getReceiptDate()));
        paragraph.setFixedPosition(184 * ratio, 79 * ratio, 300);
        document.add(paragraph);

        document.close();
    }
}
