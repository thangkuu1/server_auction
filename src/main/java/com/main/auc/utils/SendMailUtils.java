package com.main.auc.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Service
public class SendMailUtils {
    @Autowired
    public JavaMailSender emailSender;

    public void sendSimpleEmail(String subject, String content, String toMail) {

        // Create a Simple MailMessage.
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toMail);
        message.setSubject(subject);
        message.setText(content);



        // Send Message!
        this.emailSender.send(message);

    }
    public void sendSimpleEmailSignUp(String toMail, String code) throws UnsupportedEncodingException, MessagingException {

        String toAddress = toMail;
        String fromAddress = "dinhthangms96@gmail.com";
        String senderName = "Auction VVI";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + ".";

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", toAddress);

        String verifyURL = Constants.Login.URI_CONFIRM_SIGNUP + "/verify?code=" + code + "&username=" + toMail;
        content = content.replace("[[URL]]", verifyURL);
        message.setContent(content, "text/html");
        // Send Message!
        this.emailSender.send(message);

    }

    public void sendSimpleEmailForgotPass(String toMail, String code) throws UnsupportedEncodingException, MessagingException {

        String toAddress = toMail;
        String fromAddress = "dinhthangms96@gmail.com";
        String senderName = "Auction VVI";
        String subject = "Please verify your forgot password";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to forgot password:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + ".";

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", toAddress);

        String verifyURL = Constants.Login.URI_CONFIRM_FORGOT + "/verify?code=" + code + "&username=" + toMail;
        content = content.replace("[[URL]]", verifyURL);
        message.setContent(content, "text/html");
        // Send Message!
        this.emailSender.send(message);

    }
}
