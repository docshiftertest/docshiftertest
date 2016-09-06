package com.docbyte.docshifter.util.reporting;

import com.aspose.email.*;
import com.docbyte.docshifter.util.Logger;
import com.docbyte.docshifter.utils.aspose.LicenseHelper;

import java.io.File;

public class EmailNotification extends Notification {

	private String host;

	public EmailNotification(String destination, String origin, String host, String subject, String message, File attachemnt) {
		super(destination, origin, subject, message, attachemnt);
		this.host = host;
	}

	public EmailNotification(String destination, String origin, String host) {
		super(destination, origin, "", "");
		this.host = host;
	}


	@Override
	public boolean send() {
		LicenseHelper.getLicenceHelper();

		try {
			MailMessage message = new MailMessage();
			message.setFrom(new MailAddress(this.getOrigin()));
			message.getTo().addItem(new MailAddress(this.getDestination()));
			message.setSubject(this.getSubject());
			message.setHtmlBody(this.getMessage());
			Attachment attachment = null;
			if (this.getAttachment() != null) {
				attachment = new Attachment(this.getAttachment().getAbsolutePath());
				message.getAttachments().addItem(attachment);
			}

			SmtpClient client = new SmtpClient(host);
			client.setSecurityOptions(SecurityOptions.Auto);
			client.send(message);
			client.dispose();

			if (attachment != null) {
				attachment.dispose();
			}

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.info("MAILERROR:", ex);
			return false;
		}
	}

//	public static void main(String[] args) {
//
//
//		Properties prop = new Properties();
//		prop.setProperty("docshifter.notification.type", "MAIL");
//		prop.setProperty("docshifter.notification.destination", "michiel.vandriessche@docbyte.com");
//		prop.setProperty("docshifter.notification.origin", "dev@docshifter.com");
//		prop.setProperty("docshifter.notification.host", "smtp.telenet.be");
//
//
//		Notification not = Notification.getFromProps(prop);
//
//
//		String filename = "ikel";
//		String format = "douche";
//		String fullPath = "test/ikel";
//
//		not.setSubject(String.format("Validation of document, %s, failed!", filename));
//		not.setMessage("Dear reciever,\r\n"
//				+ "The document, " + filename + " faild to validate against the format " + format + ".\r\n"
//				+ "The full path to the document is " + fullPath + ".\r\n"
//				+ "\r\n"
//				+ "Please undertake the nessecary steps.\r\n"
//				+ "\r\n");
//
//		System.out.println(not.send());
//
//	}

}
