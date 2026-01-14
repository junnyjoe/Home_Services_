package com.home.services.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service pour l'envoi d'emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Home Services <noreply@home-services.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}", to, e);
        }
    }

    /**
     * Notification de nouvelle candidature pour un client
     */
    public void sendNewApplicationNotification(String clientEmail, String clientNom, String requestTitre,
            String providerNom) {
        String subject = "Nouvelle candidature reçue - Home Services";
        String text = String.format(
                "Bonjour %s,\n\n" +
                        "Bonne nouvelle ! Le prestataire %s a postulé à votre annonce : \"%s\".\n\n" +
                        "Connectez-vous à votre tableau de bord pour consulter sa proposition.\n\n" +
                        "L'équipe Home Services",
                clientNom, providerNom, requestTitre);
        sendEmail(clientEmail, subject, text);
    }
}
