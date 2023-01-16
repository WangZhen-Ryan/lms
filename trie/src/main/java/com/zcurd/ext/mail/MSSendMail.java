package com.zcurd.ext.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MSSendMail {
	public static void xx() {
		Properties prop = System.getProperties();
		prop.put("mail.smtp.host", "CASarray.ad4.ad.alcatel.com");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.port", "80");
		EmailAuthenticator mailauth = new EmailAuthenticator("CVS020379", "Admin!pass5");
		Session mailSession = Session.getInstance(prop, mailauth);
		try {
			MimeMessage mimeMessage = new MimeMessage(mailSession);
			mimeMessage.setFrom(new InternetAddress("Central-lab.Nsb@nokia-sbell.com"));
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("pingxiang.lin@nokia-sbell.com"));
			mimeMessage.setSubject("标题");
			mimeMessage.setContent("内容", "text/html;charset=gbk");
			mimeMessage.setSentDate(new Date());
			Transport tran = mailSession.getTransport("smtp");
			Transport.send(mimeMessage, mimeMessage.getAllRecipients());
			tran.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
