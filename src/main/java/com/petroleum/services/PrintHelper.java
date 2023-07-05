package com.petroleum.services;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.petroleum.models.Invoice;
import com.petroleum.models.Product;
import com.petroleum.models.Transfer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

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

        paragraph = new Paragraph(Math.round(invoice.getVolume()) + " LITRES");
        paragraph.setFixedPosition(35 * ratio, 246 * ratio, 300);
        document.add(paragraph);

        paragraph = new Paragraph("VALIDITÉ : " + invoice.getValidity() + " JOURS");
        paragraph.setFixedPosition(76 * ratio, 165 * ratio, 300);
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

    public static File report(List<Product> products, YearMonth month) throws IOException {
        File folder = new File(TAX_PATH);
        if (!folder.exists()) folder.mkdirs();
        File output = new File(folder.getAbsolutePath() + File.separator + "taxes_" + month.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() + "_" + month.getYear() + ".pdf");
       // PdfDocument pdf = new PdfDocument(new PdfReader(new File("template.pdf")), new PdfWriter(output));
        PdfDocument pdf = new PdfDocument(new PdfWriter(output.getAbsolutePath(),new WriterProperties().addXmpMetadata()));
        pdf.setDefaultPageSize(PageSize.A4.rotate());
        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
        Document document = new Document(pdf).setFont(font).setFontSize(11);
        int n = products.size();
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRENCH);
        formatter.setMaximumFractionDigits(2);
        Table table = new Table(new float[n * 3 + 2]);
        table.setWidthPercent(100.0f);
        table.flushContent();
        Paragraph paragraph = new Paragraph("RECAPITULATIF DES TAXES");
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginTop(50.0f);
        paragraph.setMarginBottom(10.0f);
        document.add(paragraph);
        paragraph = new Paragraph("DU 1ER AU " + month.atEndOfMonth().getDayOfMonth() + " " + month.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() + " " + month.getYear());
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginBottom(20.0f);
        document.add(paragraph);
        Cell cell = new Cell(2,2).add("");
        table.addHeaderCell(cell);
        products.forEach(product -> {
            Cell header = new Cell(1,3).add(product.getName());
            header.setBold();
            header.setPaddingLeft(5.0f);
            header.setPaddingRight(5.0f);
            header.setTextAlignment(TextAlignment.CENTER);
            header.setBackgroundColor(Color.GRAY);
            table.addHeaderCell(header);
        });
        products.forEach(product -> {
            Stream.of("Taux", "QTE", "Montant")
                .forEach(subtitle -> {
                    Cell header = new Cell().add(subtitle);
                    header.setPaddingLeft(5.0f);
                    header.setPaddingRight(5.0f);
                    header.setTextAlignment(TextAlignment.CENTER);
                    table.addHeaderCell(header);
                });
        });
        cell = new Cell(3,1).add("Taxe liée \nau bon de transfert");
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
            cell = new Cell().add(formatter.format(product.getPassage()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getTransferVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format((product.getTransferVolume() * product.getPassage())));
            cell.setBackgroundColor(Color.PINK);
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
            cell = new Cell().add(formatter.format(product.getPassageTax()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getTransferVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getTransferVolume() * product.getPassageTax()));
            cell.setBackgroundColor(Color.PINK);
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
            cell = new Cell().add(formatter.format(product.getRefinery()));
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getTransferVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getTransferVolume() * product.getRefinery()));
            cell.setBackgroundColor(Color.PINK);
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }
        cell = new Cell(1, 2).add("Total redevance transfert");
        cell.setPaddingLeft(10.0f);
        cell.setBold();
        cell.setBackgroundColor(Color.ORANGE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell(1, 3).add(formatter.format(product.getTransferVolume() * (product.getPassage() + product.getPassageTax() + product.getRefinery())));
            cell.setBold();
            cell.setBackgroundColor(Color.ORANGE);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }

        cell = new Cell(4,1).add("Taxe liée \nau bon d'enlèvement");
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
            cell = new Cell().add(formatter.format(product.getSpecialTax()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getSpecialTax()));
            cell.setBackgroundColor(Color.PINK);
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
            cell = new Cell().add(formatter.format(product.getTransport()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getTransport()));
            cell.setBackgroundColor(Color.PINK);
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
            cell = new Cell().add(formatter.format(product.getMarking()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getMarking()));
            cell.setBackgroundColor(Color.PINK);
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
            cell = new Cell().add(formatter.format(product.getMarkingTax()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume()));
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
            cell = new Cell().add(formatter.format(product.getInvoiceVolume() * product.getMarkingTax()));
            cell.setBackgroundColor(Color.PINK);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell(1, 2).add("Total redevance enlèvement");
        cell.setPaddingLeft(10.0f);
        cell.setBold();
        cell.setBackgroundColor(Color.ORANGE);
        table.addCell(cell);
        for(Product product: products){
            cell = new Cell(1, 3).add(formatter.format(product.getInvoiceVolume() * (product.getSpecialTax() + product.getTransport() + product.getMarking() + product.getMarkingTax())));
            cell.setBold();
            cell.setBackgroundColor(Color.ORANGE);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }
        cell = new Cell(1, 2).add("Total redevance");
        cell.setPaddingLeft(10.0f);
        cell.setBold();
        cell.setBackgroundColor(Color.RED);
        table.addCell(cell);
        double sum = products.stream().map(product -> product.getTransferVolume() * (product.getPassage() + product.getPassageTax() + product.getRefinery()) + product.getInvoiceVolume() * (product.getSpecialTax() + product.getTransport() + product.getMarking() + product.getMarkingTax())).reduce(Double::sum).orElse(0.0);
        cell = new Cell(1, 3 * n).add(formatter.format(sum));
        cell.setBold();
        cell.setBackgroundColor(Color.RED);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);

        table.setMarginLeft(10);
        table.setMarginRight(10);
        document.add(table);
        final com.ibm.icu.text.NumberFormat translator = new RuleBasedNumberFormat(ULocale.FRENCH, 1);
        paragraph = new Paragraph("La somme totale des redevances pour le mois en cours est de");
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginTop(20.0f);
        document.add(paragraph);
        paragraph = new Paragraph(formatter.format(sum));
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginTop(10.0f);
        document.add(paragraph);
        paragraph = new Paragraph(translator.format(sum).toUpperCase().replace("-", " ") + "   FRANCS CFA");
        paragraph.setTextAlignment(TextAlignment.CENTER);
        paragraph.setBold();
        paragraph.setMarginTop(10.0f);
        document.add(paragraph);
        document.close();
        return output;
    }
}
