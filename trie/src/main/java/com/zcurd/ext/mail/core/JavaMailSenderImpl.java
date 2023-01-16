package com.zcurd.ext.mail.core;

import com.zcurd.ext.mail.exception.MailAuthenticationException;
import com.zcurd.ext.mail.exception.MailException;
import com.zcurd.ext.mail.exception.MailParseException;
import com.zcurd.ext.mail.exception.MailPreparationException;
import com.zcurd.ext.mail.exception.MailSendException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.FileTypeMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class JavaMailSenderImpl implements JavaMailSender {
	public static final String DEFAULT_PROTOCOL = "smtp";

	public static final int DEFAULT_PORT = -1;

	private static final String HEADER_MESSAGE_ID = "Message-ID";

	private Properties javaMailProperties = new Properties();

	private Session session;

	private String protocol = "smtp";

	private String host;

	private int port = -1;

	private String username;

	private String password;

	private String defaultEncoding;

	private FileTypeMap defaultFileTypeMap;

	public JavaMailSenderImpl() {
		ConfigurableMimeFileTypeMap fileTypeMap = new ConfigurableMimeFileTypeMap();
		fileTypeMap.afterPropertiesSet();
		this.defaultFileTypeMap = fileTypeMap;
	}

	public void setJavaMailProperties(Properties javaMailProperties) {
		this.javaMailProperties = javaMailProperties;
		synchronized (this) {
			this.session = null;
		}
	}

	public Properties getJavaMailProperties() {
		return this.javaMailProperties;
	}

	public void setSession(Session session) {
		Assert.notNull(session, "Session must not be null");
		this.session = session;
	}

	public Session getSession() {
		if (this.session == null)
			this.session = Session.getInstance(this.javaMailProperties);
		return this.session;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return this.host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public String getDefaultEncoding() {
		return this.defaultEncoding;
	}

	public void setDefaultFileTypeMap(FileTypeMap defaultFileTypeMap) {
		this.defaultFileTypeMap = defaultFileTypeMap;
	}

	public FileTypeMap getDefaultFileTypeMap() {
		return this.defaultFileTypeMap;
	}

	public void send(SimpleMailMessage simpleMessage) throws MailException {
		send(new SimpleMailMessage[] { simpleMessage });
	}

	public void send(SimpleMailMessage[] simpleMessages) throws MailException {
		List<MimeMessage> mimeMessages = new ArrayList<>(simpleMessages.length);
		byte b;
		int i;
		SimpleMailMessage[] arrayOfSimpleMailMessage;
		for (i = (arrayOfSimpleMailMessage = simpleMessages).length, b = 0; b < i;) {
			SimpleMailMessage simpleMessage = arrayOfSimpleMailMessage[b];
			MimeMailMessage message = new MimeMailMessage(createMimeMessage());
			simpleMessage.copyTo(message);
			mimeMessages.add(message.getMimeMessage());
			b++;
		}
		doSend(mimeMessages.toArray(new MimeMessage[mimeMessages.size()]), (Object[]) simpleMessages);
	}

	public MimeMessage createMimeMessage() {
		return new SmartMimeMessage(getSession(), getDefaultEncoding(), getDefaultFileTypeMap());
	}

	public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
		try {
			return new MimeMessage(getSession(), contentStream);
		} catch (MessagingException ex) {
			throw new MailParseException("Could not parse raw MIME content", ex);
		}
	}

	public void send(MimeMessage mimeMessage) throws MailException {
		send(new MimeMessage[] { mimeMessage });
	}

	public void send(MimeMessage[] mimeMessages) throws MailException {
		doSend(mimeMessages, null);
	}

	public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
		send(new MimeMessagePreparator[] { mimeMessagePreparator });
	}

	public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
		try {
			List<MimeMessage> mimeMessages = new ArrayList<>(mimeMessagePreparators.length);
			byte b;
			int i;
			MimeMessagePreparator[] arrayOfMimeMessagePreparator;
			for (i = (arrayOfMimeMessagePreparator = mimeMessagePreparators).length, b = 0; b < i;) {
				MimeMessagePreparator preparator = arrayOfMimeMessagePreparator[b];
				MimeMessage mimeMessage = createMimeMessage();
				preparator.prepare(mimeMessage);
				mimeMessages.add(mimeMessage);
				b++;
			}

			send(mimeMessages.toArray(new MimeMessage[mimeMessages.size()]));
		} catch (MailException ex) {
			throw ex;
		} catch (MessagingException ex) {
			throw new MailParseException((Throwable) ex);
		} catch (IOException ex) {
			throw new MailPreparationException(ex);
		} catch (Exception ex) {
			throw new MailPreparationException(ex);
		}
	}

	protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
		Transport transport;
		Map<Object, Exception> failedMessages = new LinkedHashMap<Object, Exception>();
		try {
			transport = getTransport(getSession());
			transport.connect(getHost(), getPort(), getUsername(), getPassword());
		} catch (AuthenticationFailedException ex) {
			throw new MailAuthenticationException(ex);
		} catch (MessagingException ex) {
			for (int i = 0; i < mimeMessages.length; i++) {
				Object original = (originalMessages != null) ? originalMessages[i] : mimeMessages[i];
				failedMessages.put(original, ex);
			}
			throw new MailSendException("Mail server connection failed", ex, failedMessages);
		}
		try {
			for (int i = 0; i < mimeMessages.length; i++) {
				MimeMessage mimeMessage = mimeMessages[i];
				try {
					if (mimeMessage.getSentDate() == null)
						mimeMessage.setSentDate(new Date());
					String messageId = mimeMessage.getMessageID();
					mimeMessage.saveChanges();
					if (messageId != null)
						mimeMessage.setHeader("Message-ID", messageId);
					transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
				} catch (MessagingException ex) {
					Object original = (originalMessages != null) ? originalMessages[i] : mimeMessage;
					failedMessages.put(original, ex);
				}
			}
		} finally {
			try {
				transport.close();
			} catch (MessagingException ex) {
				if (!failedMessages.isEmpty())
					throw new MailSendException("Failed to close server connection after message failures", ex,
							failedMessages);
				throw new MailSendException("Failed to close server connection after message sending", ex);
			}
		}
		if (!failedMessages.isEmpty())
			throw new MailSendException(failedMessages);
	}

	protected Transport getTransport(Session session) throws NoSuchProviderException {
		return session.getTransport(getProtocol());
	}
}
