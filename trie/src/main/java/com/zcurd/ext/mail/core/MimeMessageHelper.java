package com.zcurd.ext.mail.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

public class MimeMessageHelper {
	public static final int MULTIPART_MODE_NO = 0;

	public static final int MULTIPART_MODE_MIXED = 1;

	public static final int MULTIPART_MODE_RELATED = 2;

	public static final int MULTIPART_MODE_MIXED_RELATED = 3;

	private static final String MULTIPART_SUBTYPE_MIXED = "mixed";

	private static final String MULTIPART_SUBTYPE_RELATED = "related";

	private static final String MULTIPART_SUBTYPE_ALTERNATIVE = "alternative";

	private static final String CONTENT_TYPE_ALTERNATIVE = "text/alternative";

	private static final String CONTENT_TYPE_HTML = "text/html";

	private static final String CONTENT_TYPE_CHARSET_SUFFIX = ";charset=";

	private static final String HEADER_PRIORITY = "X-Priority";

	private static final String HEADER_CONTENT_ID = "Content-ID";

	private final MimeMessage mimeMessage;

	private MimeMultipart rootMimeMultipart;

	private MimeMultipart mimeMultipart;

	private final String encoding;

	private FileTypeMap fileTypeMap;

	private boolean validateAddresses;

	public MimeMessageHelper(MimeMessage mimeMessage) {
		this(mimeMessage, null);
	}

	public MimeMessageHelper(MimeMessage mimeMessage, String encoding) {
		this.validateAddresses = false;
		this.mimeMessage = mimeMessage;
		this.encoding = (encoding != null) ? encoding : getDefaultEncoding(mimeMessage);
		this.fileTypeMap = getDefaultFileTypeMap(mimeMessage);
	}

	public MimeMessageHelper(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
		this(mimeMessage, multipart, null);
	}

	public MimeMessageHelper(MimeMessage mimeMessage, boolean multipart, String encoding) throws MessagingException {
		this(mimeMessage, multipart ? 3 : 0, encoding);
	}

	public MimeMessageHelper(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
		this(mimeMessage, multipartMode, null);
	}

	public MimeMessageHelper(MimeMessage mimeMessage, int multipartMode, String encoding) throws MessagingException {
		this.validateAddresses = false;
		this.mimeMessage = mimeMessage;
		createMimeMultiparts(mimeMessage, multipartMode);
		this.encoding = (encoding != null) ? encoding : getDefaultEncoding(mimeMessage);
		this.fileTypeMap = getDefaultFileTypeMap(mimeMessage);
	}

	public final MimeMessage getMimeMessage() {
		return this.mimeMessage;
	}

	protected void createMimeMultiparts(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
		MimeBodyPart relatedBodyPart;
		MimeMultipart nestedRelatedMultipart;
		MimeMultipart rootMixedMultipart;
		MimeMultipart relatedMultipart;
		MimeMultipart mixedMultipart;
		switch (multipartMode) {
		case 0:
			setMimeMultiparts(null, null);
			return;
		case 1:
			mixedMultipart = new MimeMultipart("mixed");
			mimeMessage.setContent(mixedMultipart);
			setMimeMultiparts(mixedMultipart, mixedMultipart);
			return;
		case 2:
			relatedMultipart = new MimeMultipart("related");
			mimeMessage.setContent(relatedMultipart);
			setMimeMultiparts(relatedMultipart, relatedMultipart);
			return;
		case 3:
			rootMixedMultipart = new MimeMultipart("mixed");
			mimeMessage.setContent(rootMixedMultipart);
			nestedRelatedMultipart = new MimeMultipart("related");
			relatedBodyPart = new MimeBodyPart();
			relatedBodyPart.setContent(nestedRelatedMultipart);
			rootMixedMultipart.addBodyPart(relatedBodyPart);
			setMimeMultiparts(rootMixedMultipart, nestedRelatedMultipart);
			return;
		}
		throw new IllegalArgumentException("Only multipart modes MIXED_RELATED, RELATED and NO supported");
	}

	protected final void setMimeMultiparts(MimeMultipart root, MimeMultipart main) {
		this.rootMimeMultipart = root;
		this.mimeMultipart = main;
	}

	public final boolean isMultipart() {
		return (this.rootMimeMultipart != null);
	}

	private void checkMultipart() throws IllegalStateException {
		if (!isMultipart())
			throw new IllegalStateException(
					"Not in multipart mode - create an appropriate MimeMessageHelper via a constructor that takes a 'multipart' flag if you need to set alternative texts or add inline elements or attachments.");
	}

	public final MimeMultipart getRootMimeMultipart() throws IllegalStateException {
		checkMultipart();
		return this.rootMimeMultipart;
	}

	public final MimeMultipart getMimeMultipart() throws IllegalStateException {
		checkMultipart();
		return this.mimeMultipart;
	}

	protected String getDefaultEncoding(MimeMessage mimeMessage) {
		if (mimeMessage instanceof SmartMimeMessage)
			return ((SmartMimeMessage) mimeMessage).getDefaultEncoding();
		return null;
	}

	public String getEncoding() {
		return this.encoding;
	}

	protected FileTypeMap getDefaultFileTypeMap(MimeMessage mimeMessage) {
		if (mimeMessage instanceof SmartMimeMessage) {
			FileTypeMap fileTypeMap = ((SmartMimeMessage) mimeMessage).getDefaultFileTypeMap();
			if (fileTypeMap != null)
				return fileTypeMap;
		}
		ConfigurableMimeFileTypeMap fileTypeMap = new ConfigurableMimeFileTypeMap();
		fileTypeMap.afterPropertiesSet();
		return fileTypeMap;
	}

	public void setFileTypeMap(FileTypeMap fileTypeMap) {
		this.fileTypeMap = (fileTypeMap != null) ? fileTypeMap : getDefaultFileTypeMap(getMimeMessage());
	}

	public FileTypeMap getFileTypeMap() {
		return this.fileTypeMap;
	}

	public void setValidateAddresses(boolean validateAddresses) {
		this.validateAddresses = validateAddresses;
	}

	public boolean isValidateAddresses() {
		return this.validateAddresses;
	}

	protected void validateAddress(InternetAddress address) throws AddressException {
		if (isValidateAddresses())
			address.validate();
	}

	protected void validateAddresses(InternetAddress[] addresses) throws AddressException {
		byte b;
		int i;
		InternetAddress[] arrayOfInternetAddress;
		for (i = (arrayOfInternetAddress = addresses).length, b = 0; b < i;) {
			InternetAddress address = arrayOfInternetAddress[b];
			validateAddress(address);
			b++;
		}
	}

	public void setFrom(InternetAddress from) throws MessagingException {
		Assert.notNull(from, "From address must not be null");
		validateAddress(from);
		this.mimeMessage.setFrom((Address) from);
	}

	public void setFrom(String from) throws MessagingException {
		Assert.notNull(from, "From address must not be null");
		setFrom(parseAddress(from));
	}

	public void setFrom(String from, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(from, "From address must not be null");
		setFrom((getEncoding() != null) ? new InternetAddress(from, personal, getEncoding())
				: new InternetAddress(from, personal));
	}

	public void setReplyTo(InternetAddress replyTo) throws MessagingException {
		Assert.notNull(replyTo, "Reply-to address must not be null");
		validateAddress(replyTo);
		this.mimeMessage.setReplyTo((Address[]) new InternetAddress[] { replyTo });
	}

	public void setReplyTo(String replyTo) throws MessagingException {
		Assert.notNull(replyTo, "Reply-to address must not be null");
		setReplyTo(parseAddress(replyTo));
	}

	public void setReplyTo(String replyTo, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(replyTo, "Reply-to address must not be null");
		InternetAddress replyToAddress = (getEncoding() != null) ? new InternetAddress(replyTo, personal, getEncoding())
				: new InternetAddress(replyTo, personal);
		setReplyTo(replyToAddress);
	}

	public void setTo(InternetAddress to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		validateAddress(to);
		this.mimeMessage.setRecipient(Message.RecipientType.TO, to);
	}

	public void setTo(InternetAddress[] to) throws MessagingException {
		Assert.notNull(to, "To address array must not be null");
		validateAddresses(to);
		this.mimeMessage.setRecipients(Message.RecipientType.TO, to);
	}

	public void setTo(String to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		setTo(parseAddress(to));
	}

	public void setTo(String[] to) throws MessagingException {
		Assert.notNull(to, "To address array must not be null");
		InternetAddress[] addresses = new InternetAddress[to.length];
		for (int i = 0; i < to.length; i++)
			addresses[i] = parseAddress(to[i]);
		setTo(addresses);
	}

	public void addTo(InternetAddress to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		validateAddress(to);
		this.mimeMessage.addRecipient(Message.RecipientType.TO, to);
	}

	public void addTo(String to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		addTo(parseAddress(to));
	}

	public void addTo(String to, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(to, "To address must not be null");
		addTo((getEncoding() != null) ? new InternetAddress(to, personal, getEncoding())
				: new InternetAddress(to, personal));
	}

	public void setCc(InternetAddress cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		validateAddress(cc);
		this.mimeMessage.setRecipient(Message.RecipientType.CC, cc);
	}

	public void setCc(InternetAddress[] cc) throws MessagingException {
		Assert.notNull(cc, "Cc address array must not be null");
		validateAddresses(cc);
		this.mimeMessage.setRecipients(Message.RecipientType.CC, cc);
	}

	public void setCc(String cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		setCc(parseAddress(cc));
	}

	public void setCc(String[] cc) throws MessagingException {
		Assert.notNull(cc, "Cc address array must not be null");
		InternetAddress[] addresses = new InternetAddress[cc.length];
		for (int i = 0; i < cc.length; i++)
			addresses[i] = parseAddress(cc[i]);
		setCc(addresses);
	}

	public void addCc(InternetAddress cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		validateAddress(cc);
		this.mimeMessage.addRecipient(Message.RecipientType.CC, cc);
	}

	public void addCc(String cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		addCc(parseAddress(cc));
	}

	public void addCc(String cc, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(cc, "Cc address must not be null");
		addCc((getEncoding() != null) ? new InternetAddress(cc, personal, getEncoding())
				: new InternetAddress(cc, personal));
	}

	public void setBcc(InternetAddress bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		validateAddress(bcc);
		this.mimeMessage.setRecipient(Message.RecipientType.BCC, bcc);
	}

	public void setBcc(InternetAddress[] bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address array must not be null");
		validateAddresses(bcc);
		this.mimeMessage.setRecipients(Message.RecipientType.BCC, bcc);
	}

	public void setBcc(String bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		setBcc(parseAddress(bcc));
	}

	public void setBcc(String[] bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address array must not be null");
		InternetAddress[] addresses = new InternetAddress[bcc.length];
		for (int i = 0; i < bcc.length; i++)
			addresses[i] = parseAddress(bcc[i]);
		setBcc(addresses);
	}

	public void addBcc(InternetAddress bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		validateAddress(bcc);
		this.mimeMessage.addRecipient(Message.RecipientType.BCC, bcc);
	}

	public void addBcc(String bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		addBcc(parseAddress(bcc));
	}

	public void addBcc(String bcc, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		addBcc((getEncoding() != null) ? new InternetAddress(bcc, personal, getEncoding())
				: new InternetAddress(bcc, personal));
	}

	private InternetAddress parseAddress(String address) throws MessagingException {
		InternetAddress[] parsed = InternetAddress.parse(address);
		if (parsed.length != 1)
			throw new AddressException("Illegal address", address);
		InternetAddress raw = parsed[0];
		try {
			return (getEncoding() != null) ? new InternetAddress(raw.getAddress(), raw.getPersonal(), getEncoding())
					: raw;
		} catch (UnsupportedEncodingException ex) {
			throw new MessagingException("Failed to parse embedded personal name to correct encoding", ex);
		}
	}

	public void setPriority(int priority) throws MessagingException {
		this.mimeMessage.setHeader("X-Priority", Integer.toString(priority));
	}

	public void setSentDate(Date sentDate) throws MessagingException {
		Assert.notNull(sentDate, "Sent date must not be null");
		this.mimeMessage.setSentDate(sentDate);
	}

	public void setSubject(String subject) throws MessagingException {
		Assert.notNull(subject, "Subject must not be null");
		if (getEncoding() != null) {
			this.mimeMessage.setSubject(subject, getEncoding());
		} else {
			this.mimeMessage.setSubject(subject);
		}
	}

	public void setText(String text) throws MessagingException {
		setText(text, false);
	}

	public void setText(String text, boolean html) throws MessagingException {
		Object object;
		Assert.notNull(text, "Text must not be null");

		if (isMultipart()) {
			object = getMainPart();
		} else {
			object = this.mimeMessage;
		}
		if (html) {
			setHtmlTextToMimePart((MimePart) object, text);
		} else {
			setPlainTextToMimePart((MimePart) object, text);
		}
	}

	public void setText(String plainText, String htmlText) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(plainText, "Plain text must not be null");
		Assert.notNull(htmlText, "HTML text must not be null");
		MimeMultipart messageBody = new MimeMultipart("alternative");
		getMainPart().setContent(messageBody, "text/alternative");
		MimeBodyPart plainTextPart = new MimeBodyPart();
		setPlainTextToMimePart(plainTextPart, plainText);
		messageBody.addBodyPart(plainTextPart);
		MimeBodyPart htmlTextPart = new MimeBodyPart();
		setHtmlTextToMimePart(htmlTextPart, htmlText);
		messageBody.addBodyPart(htmlTextPart);
	}

	private MimeBodyPart getMainPart() throws MessagingException {
		MimeMultipart mimeMultipart = getMimeMultipart();
		MimeBodyPart bodyPart = null;
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bp = mimeMultipart.getBodyPart(i);
			if (bp.getFileName() == null)
				bodyPart = (MimeBodyPart) bp;
		}
		if (bodyPart == null) {
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeMultipart.addBodyPart(mimeBodyPart);
			bodyPart = mimeBodyPart;
		}
		return bodyPart;
	}

	private void setPlainTextToMimePart(MimePart mimePart, String text) throws MessagingException {
		if (getEncoding() != null) {
			mimePart.setText(text, getEncoding());
		} else {
			mimePart.setText(text);
		}
	}

	private void setHtmlTextToMimePart(MimePart mimePart, String text) throws MessagingException {
		if (getEncoding() != null) {
			mimePart.setContent(text, "text/html;charset=" + getEncoding());
		} else {
			mimePart.setContent(text, "text/html");
		}
	}

	public void addInline(String contentId, DataSource dataSource) throws MessagingException {
		Assert.notNull(contentId, "Content ID must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDisposition("inline");
		mimeBodyPart.setHeader("Content-ID", "<" + contentId + ">");
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		getMimeMultipart().addBodyPart(mimeBodyPart);
	}

	public void addInline(String contentId, File file) throws MessagingException {
		Assert.notNull(file, "File must not be null");
		FileDataSource dataSource = new FileDataSource(file);
		dataSource.setFileTypeMap(getFileTypeMap());
		addInline(contentId, dataSource);
	}

	public void addAttachment(String attachmentFilename, DataSource dataSource) throws MessagingException {
		Assert.notNull(attachmentFilename, "Attachment filename must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDisposition("attachment");
		mimeBodyPart.setFileName(attachmentFilename);
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		getRootMimeMultipart().addBodyPart(mimeBodyPart);
	}

	public void addAttachment(String attachmentFilename, File file) throws MessagingException {
		Assert.notNull(file, "File must not be null");
		FileDataSource dataSource = new FileDataSource(file);
		dataSource.setFileTypeMap(getFileTypeMap());
		addAttachment(attachmentFilename, dataSource);
	}
}
