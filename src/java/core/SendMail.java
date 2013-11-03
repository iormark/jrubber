/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import freemarker.template.TemplateException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 *
 * @author mark
 */
public class SendMail {

    public SendMail(String template, HashMap root, String RealPath) throws Exception {

        sendEmail(template, root, RealPath);
    }

    private void sendEmail(String template, HashMap root, String RealPath) throws Exception {

        String content = "";

        content = new Templating().getTemplating(template, root, RealPath);

        //Properties props = new Properties();
        Properties mailProps = new Properties();

        mailProps.put("mail.smtp.host", "smtp.yandex.ru");
        //mailProps.put("mail.smtp.host", "smtp.gmail.com");
        mailProps.put("mail.smtp.auth", "true");
        //mailProps.put("mail.smtp.port", "587");
        //mailProps.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(mailProps, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication("robot@yourmood.ru", "DYh#cdfjFD65@DF"));
                //return (new PasswordAuthentication("balamutior", "FDfdgg6fhDF"));
            }
        });

        MimeMessage msg = new MimeMessage(session);

        msg.addHeader("from", MimeUtility.encodeText("Юмор + Интеллект") + " <robot@yourmood.ru>");
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress("iormark@yandex.ru"));
        msg.setSubject(root.get("subject").toString(), "utf-8");
        msg.setSentDate(new java.util.Date());

        Multipart mp = new MimeMultipart();
        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(content, "text/html;charset=" + "utf-8");
        mp.addBodyPart(htmlPart);
        msg.setContent(mp);
        // Transport.send(msg);
        //msg.setText("hello");
        Transport.send(msg);

    }
}
