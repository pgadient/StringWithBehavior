package ch.msc.demo.mail;

import java.util.HashMap;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailAgent {
    private static Properties getProps() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "mail.gmx.net");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "*");
        return props;
    }

    private static Session getSession() {
        Session session = Session.getInstance(getProps(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Secrets.s1, Secrets.s2);
            }
        });
        return session;
    }

    private static HashMap<String, Boolean> alreadySent = new HashMap<>();

    public static boolean sendMessage(String destination, String stacktrace) {
        // System.out.println(stacktrace);
        // return false;

        if(alreadySent.containsKey(destination)) return false;
        alreadySent.put(destination, true);

        Message message = new MimeMessage(getSession());
        try {
            message.setFrom(new InternetAddress(Secrets.s3));
            message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(destination));
            message.setSubject("Security alert Demo");

            String msg = "The demo project ran into the following security alert:\n"+stacktrace;
            msg = msg.replace("\n", "<br>");

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
