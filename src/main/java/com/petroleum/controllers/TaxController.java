package com.petroleum.controllers;

import com.petroleum.enums.Status;
import com.petroleum.models.Invoice;
import com.petroleum.models.Product;
import com.petroleum.models.Transfer;
import com.petroleum.repositories.InvoiceRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.repositories.TransferRepository;
import com.petroleum.services.PrintHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/taxes")
public class TaxController {
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransferRepository transferRepository;

    @GetMapping
    public String getAll(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month, Model model){
        if(month == null) month = YearMonth.now();
        LocalDateTime start = month.atDay(1).atTime(LocalTime.MIN);
        LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);
        List<Product> products = productRepository.findAllByOrderByNameAsc();
        products.forEach(product -> {
            product.setInvoiceVolume(invoiceRepository.findAllByProductAndStatusAndDateBetween(product, Status.APPROVED, start, end).stream().map(Invoice::getVolume).reduce(Double::sum).orElse(0.0));
            product.setTransferVolume(transferRepository.findAllByProductAndStatusAndDateBetween(product, Status.APPROVED, start, end).stream().map(Transfer::getVolume).reduce(Double::sum).orElse(0.0));
        });
        model.addAttribute("products", products);
        model.addAttribute("month", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        return "taxes";
    }

    @GetMapping("/report")
    public void downloadReport(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month, HttpServletResponse response) {
        try {
            if(month == null) month = YearMonth.now();
            LocalDateTime start = month.atDay(1).atTime(LocalTime.MIN);
            LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);
            List<Product> products = productRepository.findAllByOrderByNameAsc();
            products.forEach(product -> {
                product.setInvoiceVolume(invoiceRepository.findAllByProductAndStatusAndDateBetween(product, Status.APPROVED, start, end).stream().map(Invoice::getVolume).reduce(Double::sum).orElse(0.0));
                product.setTransferVolume(transferRepository.findAllByProductAndStatusAndDateBetween(product, Status.APPROVED, start, end).stream().map(Transfer::getVolume).reduce(Double::sum).orElse(0.0));
            });
            File file = PrintHelper.report(products, month);
            if(file.exists()){
                InputStream inputStream = new InputStreamResource(Files.newInputStream(file.toPath())).getInputStream();
                response.setContentType(String.valueOf(MediaType.APPLICATION_OCTET_STREAM));
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
                IOUtils.copy(inputStream, response.getOutputStream());
                inputStream.close();
                response.flushBuffer();
                response.setStatus(HttpServletResponse.SC_OK);
            }else{
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception e) {
            log.error("Error while downloading monthly tax report", e);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
