package br.com.loom.emailforwardlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class ForwardLambda implements RequestHandler<S3Event, String> {

    private static final Logger logger = LoggerFactory.getLogger(ForwardLambda.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        try {
//            logger.info("ENVIRONMENT VARIABLES: {}", gson.toJson(System.getenv()));
//            logger.info("CONTEXT: {}", gson.toJson(context));
//            logger.info("EVENT: {}", gson.toJson(s3event));

            s3event.getRecords().forEach((record) -> {


                String srcBucket = record.getS3().getBucket().getName();

                // Object key may have spaces or unicode non-ASCII characters.
                String srcKey = record.getS3().getObject().getUrlDecodedKey();

                AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
                S3Object s3Object = s3Client.getObject(srcBucket, srcKey);
                InputStream objectData = s3Object.getObjectContent();

                Properties prop = new Properties();
                prop.put("mail.smtp.auth", true);
                prop.put("mail.smtp.starttls.enable", "true");
                prop.put("mail.smtp.ssl.enable", "false");
                prop.put("mail.transport.protocol", "smtp");
                prop.put("mail.smtp.host", System.getenv("smtp_server"));
                prop.put("mail.smtp.port", System.getenv("smtp_port"));

                try {
                    Session session = Session.getInstance(prop, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(System.getenv("smtp_username"), System.getenv("smtp_password"));
                        }
                    });

                    Message original = new MimeMessage(session, objectData);
                    Message forward = new MimeMessage(session);

                    String from = Arrays.stream(original.getAllRecipients())
                            .filter(address -> address instanceof InternetAddress && ((InternetAddress) address).getAddress().endsWith("loom.com.br"))
                            .findFirst()
                            .map(address -> ((InternetAddress) address).getAddress())
                            .orElse(System.getenv("smtp_from"));

                    String to = System.getenv("smtp_to");

                    String user = StringUtils.substringBefore(from, "@");
                    String subject;
                    String looomEmail = System.getenv("smtp_looom_email");
                    if (looomEmail != null && looomEmail.contains(user)) {
                        subject = "Fwd: [looom.com.br] " + original.getSubject();
                    } else {
                        subject = "Fwd: [loom.com.br] " + original.getSubject();
                    }

                    forward.setFrom(new InternetAddress(from));
                    forward.setReplyTo(original.getFrom());
                    forward.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    forward.setSubject(subject);

                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    Multipart multipart = new MimeMultipart();
                    messageBodyPart.setContent(original, "message/rfc822");
                    multipart.addBodyPart(messageBodyPart);
                    forward.setContent(multipart);

                    logger.info("Forwarding message from " + InternetAddress.toString(original.getFrom()) + " [" + original.getSubject() + "]");

                    Transport.send(forward);

                    logger.info("Message forwarded");
                } catch (MessagingException e) {
                    logger.error(e.toString(), e);
                }

                s3Client.deleteObject(srcBucket, srcKey);


            });

            return "ok";
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
