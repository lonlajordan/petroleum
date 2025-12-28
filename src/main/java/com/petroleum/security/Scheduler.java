package com.petroleum.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;


@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class Scheduler {
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = new ClassPathResource("one-bills-firebase.json").getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            log.error("Error loading Firebase configurations", e);
        }
    }

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
            log.error("Error while generating monthly tax report", e);
        }
    }

    @Scheduled(cron = "0 0 10 ? * MON", zone = "GMT+1")
    public void sendReminder(){
        sendNotification("ONE BILLS", new Random().nextBoolean() ? "Paye tes factuers ENEO facilement" : "Pay your ENEO bills easily");
    }


    @Scheduled(cron = "0 0 15 ? * SAT", zone = "GMT+1")
    public void sendReminder2(){
        sendNotification("ONE BILLS", new Random().nextBoolean() ? "Collecte tes points bonus, et enjoy avec tes proches" : "Collect your bonus points, and enjoy with your loved ones");
    }



    public void sendNotification(String title, String body) {
        // Create the notification message
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // Create the full FCM message with the target token
        Message message = Message.builder()
                .setTopic("all")
                .setNotification(notification)
                .build();

        // Send the message
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: {}", response);
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

}
