package com.petroleum.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Configuration
public class EmailHelper {
    private static String MAIL_HOST;
    private static String MAIL_USERNAME;
    private static String MAIL_PASSWORD;
    private static int MAIL_PORT;

    @Value("${spring.mail.host}")
    private void setMailHost(String host){
        MAIL_HOST = host;
    }

    @Value("${spring.mail.port}")
    private void setMailPort(int port){
        MAIL_PORT = port;
    }

    @Value("${spring.mail.username}")
    private void setMailUsername(String username){
        MAIL_USERNAME = username;
    }

    @Value("${spring.mail.password}")
    private void setMailPassword(String password){
        MAIL_PASSWORD = password;
    }

    public static void sendMail(String to, String cc, String subject, String body) {
        try {
            cc = purge(cc);
            final JavaMailSender emailSender = getJavaMailSender();
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false,"UTF-8");
            helper.setFrom(MAIL_USERNAME);
            helper.setTo(InternetAddress.parse(purge(to)));
            if(StringUtils.isNotEmpty(cc)) helper.setCc(InternetAddress.parse(cc));
            helper.setSubject(subject);
            helper.setText(body, true);
            new Thread(() -> emailSender.send(message)).start();
        } catch (Exception ignored){}
    }

    public static void sendMailWithAttachments(String to, String cc, String subject, String body, List<String> attachments) throws MessagingException {
        cc = purge(cc);
        final JavaMailSender emailSender = getJavaMailSender();
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");
        helper.setFrom(MAIL_USERNAME);
        helper.setTo(InternetAddress.parse(purge(to)));
        if(StringUtils.isNotEmpty(cc)) helper.setCc(InternetAddress.parse(cc));
        helper.setSubject(subject);
        helper.setText(body, true);
        for(int i = 0; i < attachments.size(); i++){
            FileSystemResource file = new FileSystemResource(new File(attachments.get(i)));
            helper.addAttachment(StringUtils.defaultString(file.getFilename() , "Pièce jointe " + (i + 1)), file);
        }
        new Thread(() -> emailSender.send(message)).start();
    }

    public static JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(MAIL_HOST);
        mailSender.setPort(MAIL_PORT);
        mailSender.setUsername(MAIL_USERNAME);
        mailSender.setPassword(MAIL_PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    private static String purge(String emails){
        return String.join(",", Arrays.stream(emails.replaceAll(";", ",").split(",")).filter(mail -> !mail.isEmpty()).collect(Collectors.toSet()));
    }
}
