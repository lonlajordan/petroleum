package com.petroleum.security;

import com.petroleum.enums.Role;
import com.petroleum.enums.Status;
import com.petroleum.models.Invoice;
import com.petroleum.models.Product;
import com.petroleum.models.Transfer;
import com.petroleum.models.User;
import com.petroleum.repositories.InvoiceRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.repositories.TransferRepository;
import com.petroleum.repositories.UserRepository;
import com.petroleum.services.EmailHelper;
import com.petroleum.services.PrintHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class Scheduler {
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    // Delete all logs every first of each month at midnight
    @Scheduled(cron = "@monthly", zone = "GMT+1")
    public void generateTaxReport(){
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime start = today.minusMonths(1).withDayOfMonth(1).toLocalDate().atTime(LocalTime.MIN);
        LocalDateTime end = today.minusDays(1).toLocalDate().atTime(LocalTime.MAX);
        List<Product> products = productRepository.findAllByOrderByNameAsc();
        products.forEach(product -> {
            product.setInvoiceVolume(invoiceRepository.findAllByProductAndStatusAndDateBetween(product, Status.APPROVED, start, end).stream().map(Invoice::getVolume).reduce(Double::sum).orElse(0.0));
            product.setTransferVolume(transferRepository.findAllByProductAndStatusAndDateBetween(product, Status.APPROVED, start, end).stream().map(Transfer::getVolume).reduce(Double::sum).orElse(0.0));
        });
        try {
            File output = PrintHelper.report(products, YearMonth.now().minusMonths(1));
            String attachment = output.getAbsolutePath();
            String to = userRepository.findFirstByRole(Role.ROLE_DIRECTOR).orElse(new User()).getEmail();
            String cc = userRepository.findFirstByRole(Role.ROLE_OPERATING_OFFICER).orElse(new User()).getEmail();
            if(StringUtils.isNotBlank(to)){
                String object = "RECAPITULATIF DES TAXES - " + start.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() + " " + start.getYear();
                String body = "Bonjour, <br>" +
                        "Bien vouloir trouver en pièce jointe le récapitulation des taxes de la période mentionnée en objet.<br>" +
                        "Cordialement.";
                EmailHelper.sendMailWithAttachments(to, cc, object, body, Collections.singletonList(attachment));
            }
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }

}
