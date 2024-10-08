package com.co.utils;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

	@Autowired
	private JavaMailSender mailSender;
	
	private Logger logger = LoggerFactory.getLogger(EmailUtils.class);
	
	public boolean sendEmail(String subject, String body, String to, File file) {
		
		System.out.println("Method sendEmail called.");
		logger.debug("Sending email to: " + to);
	    logger.debug("Subject: " + subject);
	    logger.debug("Body: " + body);
	    logger.debug("Attachment: " + file.getName());
		
		boolean isMailSent = false;
		
		try {
			
			if (to == null || to.isEmpty() || !to.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                logger.error("Invalid email address: {}", to);
                return false;
            }
			
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);
			
			if (file != null && file.exists()) {
                helper.addAttachment(file.getName(), file);
            } else {
                logger.warn("No file found for attachment or file is null.");
            }
			
//			helper.addAttachment(file.getName(), file);
			
			mailSender.send(message);
			isMailSent = true;
			
		} catch (javax.mail.MessagingException e) {
	        logger.error("MessagingException occurred while sending email", e);
	    } catch (org.springframework.mail.MailException e) {
	        logger.error("MailException occurred while sending email", e);
	    } catch (Exception e) {
	        logger.error("Exception occurred while sending email", e);
	    }
		
		return isMailSent;
	}
}
