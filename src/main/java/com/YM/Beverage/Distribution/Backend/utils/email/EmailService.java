package com.YM.Beverage.Distribution.Backend.utils.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendAccountVerificationEmail(String[] to, String email, String password, int otp) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Verify Account");

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("password", password);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("EmailTemplate", context);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    public void sendForgotPasswordEmail(String[] to, String name, int otp) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Forgot Password");

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("ForgotPasswordTemplate", context);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    public void replyToMessage(String[] to, String name, String messageReply) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Message Reply");

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("message", messageReply);

        String htmlContent = templateEngine.process("MessageReplyTemplate", context);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }
}
