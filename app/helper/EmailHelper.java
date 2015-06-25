package helper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import controllers.routes;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import play.Play;

public class EmailHelper {

	public static void sendEmail(String toEmail, String subject, String body, String ftl) {
		  String to = toEmail;
	      final String username = Play.application().configuration().getString("emailUserName");
	      final String password = Play.application().configuration().getString("emailUserPassword");

	      // Sender's email ID needs to be mentioned
	      String from = Play.application().configuration().getString("emailFrom");

	      // Get system properties
	      Properties props = System.getProperties();

	      // Setup mail server
	      props.put("mail.smtp.auth", "true");
		  props.put("mail.smtp.starttls.enable", "true");
		  props.put("mail.smtp.host", "smtp.gmail.com");
		  props.put("mail.smtp.port", "587");

	      // Get the default Session object.
		  Session session = Session.getInstance(props,
				  new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				  });

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message1 = new MimeMessage(session);

	         // Set From: header field of the header.
	         message1.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message1.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Set Subject: header field
	         message1.setSubject(subject);
	         
	         MimeMultipart mp = new MimeMultipart();
	         MimeBodyPart part = new MimeBodyPart();
//	         part.setText(body);
	         
	      // freemarker stuff.
	            Configuration cfg = new Configuration();
	            System.out.println("Working Directory = " +System.getProperty("user.dir"));
	            Template template = cfg.getTemplate("/emailTemplate.html");
	            Map<String, String> rootMap = new HashMap<String, String>();
	            rootMap.put("body", body);
	            rootMap.put("from", "KinCards Team");
	            Writer out = new StringWriter();
	            template.process(rootMap, out);
	            // freemarker stuff ends.
	 
	            /* you can add html tags in your text to decorate it. */
	            part.setContent(out.toString(), "text/html");
	         
	         mp.addBodyPart(part);
	         message1.setContent(mp);

	         // Content type has to be set after the message is put together
	         // Then saveChanges() must be called for it to take effect
	         part.setHeader("Content-Type", "text/html");
	         message1.saveChanges();

	         // Send message
	         Transport.send(message1);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void sendEmailWithAttachment(String toEmail, String subject, String body, String filename) {
		  String to = toEmail;
	      final String username = Play.application().configuration().getString("emailUserName");
	      final String password = Play.application().configuration().getString("emailUserPassword");

	      // Sender's email ID needs to be mentioned
	      String from = Play.application().configuration().getString("emailFrom");

	      // Get system properties
	      Properties props = System.getProperties();

	      // Setup mail server
	      props.put("mail.smtp.auth", "true");
		  props.put("mail.smtp.starttls.enable", "true");
		  props.put("mail.smtp.host", "smtp.gmail.com");
		  props.put("mail.smtp.port", "587");

	      // Get the default Session object.
		  Session session = Session.getInstance(props,
				  new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				  });

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message1 = new MimeMessage(session);

	         // Set From: header field of the header.
	         message1.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message1.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Set Subject: header field
	         message1.setSubject(subject);
	         
	         MimeMultipart mp = new MimeMultipart();
	         MimeBodyPart part = new MimeBodyPart();
	         DataSource source = new FileDataSource(filename);
	         part.setText(body);
	         part.setDataHandler(new DataHandler(source));
	         part.setFileName(filename);
	         mp.addBodyPart(part);
	         message1.setContent(mp);

	         // Content type has to be set after the message is put together
	         // Then saveChanges() must be called for it to take effect
	         part.setHeader("Content-Type", "text/html");
	         message1.saveChanges();

	         // Send message
	         Transport.send(message1);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
		
	}

}
