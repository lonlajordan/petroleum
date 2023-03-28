package com.petroleum.services;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.*;
import com.petroleum.models.Invoice;
import com.petroleum.models.Product;
import com.petroleum.models.Transfer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Configuration
public class PrintHelper {

    private static String TAX_PATH;

    @Value("${tax.location}")
    public void setTaxPath(String taxPath){
        TAX_PATH = taxPath;
    }

    public static void print(Invoice invoice, File output) throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfWriter(output.getAbsolutePath(),new WriterProperties().addXmpMetadata()));
        pdf.setDefaultPageSize(PageSize.A4);
        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
        Document document = new Document(pdf).setFont(font).setFontSize(8);
        float ratio = (float) 820.0 / 297;
        Paragraph paragraph = new Paragraph(String.format("%07d", invoice.getId()));
        paragraph.setFixedPosition(76 * ratio, 267 * ratio, 100);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getClient());
        paragraph.setFixedPosition(32 * ratio, 256 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getTransporter());
        paragraph.setFixedPosition(137 * ratio, 256 * ratio, 200);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getProduct().getName());
        paragraph.setFixedPosition(33 * ratio, 251 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getTruckNumber());
        paragraph.setFixedPosition(174 * ratio, 251 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getVolume() + "");
        paragraph.setFixedPosition(35 * ratio, 246 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getDriver());
        paragraph.setFixedPosition(173 * ratio, 246 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(invoice.getLoadingDate()));
        paragraph.setFixedPosition(53 * ratio, 241 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getDeliveryPlace());
        paragraph.setFixedPosition(183 * ratio, 241 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(invoice.getLoadingDepot().getName());
        paragraph.setFixedPosition(56 * ratio, 236 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(invoice.getReceiptDate()));
        paragraph.setFixedPosition(184 * ratio, 236 * ratio, 300);
        document.add(paragraph);

        document.close();
    }

    public static void print(Transfer transfer, File output) throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfWriter(output.getAbsolutePath(),new WriterProperties().addXmpMetadata()));
        pdf.setDefaultPageSize(PageSize.A4);
        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
        Document document = new Document(pdf).setFont(font).setFontSize(8);
        float ratio = (float) 820.0 / 297;
        Paragraph paragraph = new Paragraph(String.format("%07d", transfer.getId()));
        paragraph.setFixedPosition(76 * ratio, 267 * ratio, 100);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getClient());
        paragraph.setFixedPosition(32 * ratio, 256 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getTransporter());
        paragraph.setFixedPosition(137 * ratio, 256 * ratio, 200);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getProduct().getName());
        paragraph.setFixedPosition(33 * ratio, 251 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getTruckNumber());
        paragraph.setFixedPosition(174 * ratio, 251 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getVolume() + "");
        paragraph.setFixedPosition(35 * ratio, 246 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getDriver());
        paragraph.setFixedPosition(173 * ratio, 246 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(transfer.getLoadingDate()));
        paragraph.setFixedPosition(53 * ratio, 241 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getDeliveryPlace().getName());
        paragraph.setFixedPosition(183 * ratio, 241 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(transfer.getLoadingDepot().getName());
        paragraph.setFixedPosition(56 * ratio, 236 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(transfer.getReceiptDate()));
        paragraph.setFixedPosition(184 * ratio, 236 * ratio, 300);
        document.add(paragraph);

        document.close();
    }

    public static File report(List<Product> products, LocalDate date) throws IOException {
        File folder = new File(TAX_PATH);
        if (!folder.exists()) folder.mkdirs();
        File output = new File(folder.getAbsolutePath() + File.separator + "taxes_" + date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() + "_" + date.getYear() + ".pdf");
        PdfDocument pdf = new PdfDocument(new PdfReader(new File("template.pdf")), new PdfWriter(output));
        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
        Document document = new Document(pdf).setFont(font).setFontSize(12);
        int n = products.size();
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRENCH);
        formatter.setMaximumFractionDigits(2);
        Table table = new Table(new float[n+2]);
        table.setWidthPercent(100.0f);
        table.flushContent();
        Paragraph paragraph = new Paragraph("RECAPITULATIF DES TAXES");
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginTop(120.0f);
        paragraph.setMarginBottom(10.0f);
        document.add(paragraph);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
        paragraph = new Paragraph("DU 1ER AU " + calendar.getActualMaximum(Calendar.DATE) + " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() + " " + date.getYear());
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginBottom(20.0f);
        document.add(paragraph);
        Cell cell;
        List<String> headers = new ArrayList<>();
        headers.add("");
        headers.add("");
        products.forEach(product -> headers.add(product.getName()));
        for(String header: headers){
            cell = new Cell().add(header);
            cell.setBold();
            cell.setPaddingLeft(5.0f);
            cell.setPaddingRight(5.0f);
            cell.setBackgroundColor(Color.GRAY);
            table.addHeaderCell(cell);
        }
        cell = new Cell(3,1).add("Taxe liée au bon de transfert");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        cell = new Cell().add("Passage dépôt");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format((product.getTransferVolume() * product.getPassage())));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell().add("TVA sur le passage dépôt");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getTransferVolume() * product.getPassageTax()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell().add("Soutien à la raffinerie");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getTransferVolume() * product.getRefinery()));
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }
        cell = new Cell(1, 2).add("Total redevance transfert");
        cell.setPaddingLeft(10.0f);
        cell.setBold();
        cell.setBackgroundColor(Color.GRAY);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getTransferVolume() * (product.getPassage() + product.getPassageTax() + product.getRefinery())));
            cell.setBold();
            cell.setBackgroundColor(Color.GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }

        cell = new Cell(4,1).add("Taxe liée au bon d'enlèvement");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        cell = new Cell().add("Taxe spéciale");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getSpecialTax()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell().add("Péréquation transport");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getTransport()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell().add("Marquage chimique");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getMarking()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell().add("TVA sur marquage");
        cell.setBold();
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getMarkingTax()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell(1, 2).add("Total redevance enlèvement");
        cell.setPaddingLeft(10.0f);
        cell.setBold();
        cell.setBackgroundColor(Color.GRAY);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * (product.getSpecialTax() + product.getTransport() + product.getMarking() + product.getMarkingTax())));
            cell.setBold();
            cell.setBackgroundColor(Color.GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }

        table.setMarginLeft(40);
        table.setMarginRight(20);
        document.add(table);
        document.close();
        return output;
    }
}
