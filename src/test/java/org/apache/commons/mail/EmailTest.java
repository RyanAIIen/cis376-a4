package org.apache.commons.mail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@ExtendWith(MockitoExtension.class)
public class EmailTest {

    @Mock
    private Session mockSession;

    private Email email;

    @BeforeEach
    void setUp() {
        email = spy(new SimpleEmail());
    }

    @AfterEach
    void tearDown() {
        email = null;
    }

    @Test
    public void testAddBcc() throws Exception {
        // test initial state
        List<InternetAddress> bccAddresses = email.getBccAddresses();
        assertEquals(0, bccAddresses.size(), "bcc list should have no addresses");

        // test adding valid bcc emails
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";
        String name2 = "Test 2";

        email.addBcc(email1);
        email.addBcc(email2, name2);

        // test adding multiple bcc emails at once (cover addCc(String... emails))
        String email3 = "test3@example.com";
        String email4 = "test4@example.com";

        String[] additionalEmails = {email3, email4};
        email.addBcc(additionalEmails);

        bccAddresses = email.getBccAddresses();
        assertEquals(4, bccAddresses.size(), "bcc list should contain 4 addresses");
        assertEquals(email1, bccAddresses.get(0).getAddress());
        assertEquals(email2, bccAddresses.get(1).getAddress());
        assertNull(bccAddresses.get(0).getPersonal());
        assertEquals(name2, bccAddresses.get(1).getPersonal());
        assertEquals(email3, bccAddresses.get(2).getAddress());
        assertEquals(email4, bccAddresses.get(3).getAddress());

        // test adding an invalid email fails
        assertThrows(EmailException.class, () -> email.addBcc("invalid-email"),
            "should throw EmailException for invalid email");

        // test adding an empty list fails
        assertThrows(EmailException.class, () -> email.addBcc(),
            "should throw EmailException for empty email list");
    }

    @Test
    public void testAddCc() throws Exception {
        // test initial state
        List<InternetAddress> ccAddresses = email.getCcAddresses();
        assertEquals(0, ccAddresses.size(), "cc list should have no addresses");

        // test adding valid cc emails
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";
        String name2 = "Test 2";

        email.addCc(email1);
        email.addCc(email2, name2);

        // test adding multiple cc emails at once
        String email3 = "test3@example.com";
        String email4 = "test4@example.com";

        String[] additionalEmails = {email3, email4};
        email.addCc(additionalEmails);

        ccAddresses = email.getCcAddresses();
        assertEquals(4, ccAddresses.size(), "cc list should contain 4 addresses");
        assertEquals(email1, ccAddresses.get(0).getAddress());
        assertEquals(email2, ccAddresses.get(1).getAddress());
        assertNull(ccAddresses.get(0).getPersonal());
        assertEquals(name2, ccAddresses.get(1).getPersonal());
        assertEquals(email3, ccAddresses.get(2).getAddress());
        assertEquals(email4, ccAddresses.get(3).getAddress());

        // test adding an invalid email fails
        assertThrows(EmailException.class, () -> email.addCc("invalid-email"),
            "should throw EmailException for invalid email");

        // test adding an empty list fails
        assertThrows(EmailException.class, () -> email.addCc(),
            "should throw EmailException for empty email list");
    }

    @Test
    public void testAddHeader() throws Exception {
        email.setHostName("localhost");
        email.setFrom("sender@example.com");
        email.addTo("receiver@example.com");
        email.setSubject("Test Email");

        // add a header
        String headerName = "X-Custom-Header";
        String headerValue = "CustomValue";
        email.addHeader(headerName, headerValue);

        // build the message
        email.buildMimeMessage();
        MimeMessage message = email.getMimeMessage();

        // test the added header is present
        assertEquals(headerValue, message.getHeader(headerName, null),
            "headers should contain the added custom header");

        // test adding a header with an empty name fails
        assertThrows(IllegalArgumentException.class, () -> email.addHeader("", "value"),
            "should throw IllegalArgumentException for empty header name");

        // test adding a header with an empty value fails
        assertThrows(IllegalArgumentException.class, () -> email.addHeader("X-Empty-Value", ""),
            "should throw IllegalArgumentException for empty header value");
    }

    @Test
    public void testAddReplyTo() throws Exception {
        // test initial state
        List<InternetAddress> replyToAddresses = email.getReplyToAddresses();
        assertEquals(0, replyToAddresses.size(), "reply-to list should have no addresses");

        // test adding valid reply-to emails
        String email1 = "reply1@example.com";
        String email2 = "reply2@example.com";
        String name2 = "Reply 2";

        email.addReplyTo(email1);
        email.addReplyTo(email2, name2);

        replyToAddresses = email.getReplyToAddresses();
        assertEquals(2, replyToAddresses.size(), "reply-to list should contain 2 addresses");
        assertEquals(email1, replyToAddresses.get(0).getAddress());
        assertEquals(email2, replyToAddresses.get(1).getAddress());
        assertNull(replyToAddresses.get(0).getPersonal());
        assertEquals(name2, replyToAddresses.get(1).getPersonal());

        // test adding an invalid email fails
        assertThrows(EmailException.class, () -> email.addReplyTo("invalid-email", "Invalid"),
            "should throw EmailException for invalid email");

        // test adding an empty list fails
        assertThrows(EmailException.class, () -> email.addReplyTo("", ""),
            "should throw EmailException for empty email list");
    }

    @Test
    public void testBuildMimeMessage() throws Exception {
        // test initial state
        assertNull(email.getMimeMessage(), "mime message should not exist yet");

        // set up a valid email
        String senderEmail = "sender@example.com";
        String senderName = "Sender Name";
        String receiverEmail = "receiver@example.com";
        String receiverName = "Receiver Name";
        String ccEmail = "cc@example.com";
        String ccName = "CC";
        String bccEmail = "bcc@example.com";
        String bccName = "BCC";
        String replyEmail = "reply@example.com";
        String replyName = "Reply";
        String charset = "UTF-8";
        String subj = "Test Subject";
        String textBody = "Test email body.";

        email.setHostName("localhost");
        email.setFrom(senderEmail, senderName);
        email.addTo(receiverEmail, receiverName);
        email.addCc(ccEmail, ccName);
        email.addBcc(bccEmail, bccName);
        email.addReplyTo(replyEmail, replyName);
        email.setCharset(charset);
        email.setSubject(subj);
        email.setContent(textBody, EmailConstants.TEXT_PLAIN);

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.example.com");
        props.setProperty("mail.store.protocol", "pop3");
        props.setProperty("mail.pop3.host", "pop3.example.com");
        props.setProperty("mail.pop3.user", "popUser");
        props.setProperty("mail.pop3.password", "popPass");

        Session session = Session.getInstance(props);
        email.setMailSession(session);

        // build the message
        email.buildMimeMessage();
        MimeMessage message = email.getMimeMessage();

        // test that the message is built
        assertNotNull(message, "mime message should exist after building");

        // test building message twice throws IllegalStateException
        assertThrows(IllegalStateException.class, email::buildMimeMessage,
            "should throw IllegalStateException if buildMimeMessage() is called twice");

        // test message is correct
        String expectedSender = String.format("%s <%s>", senderName, senderEmail);
        String expectedReceiver = String.format("%s <%s>", receiverName, receiverEmail);
        String expectedCc = String.format("%s <%s>", ccName, ccEmail);
        String expectedBcc = String.format("%s <%s>", bccName, bccEmail);
        String expectedReply = String.format("%s <%s>", replyName, replyEmail);

        assertEquals(expectedSender, message.getFrom()[0].toString(),
            "from address should be set correctly");
        assertEquals(expectedReceiver, message.getRecipients(Message.RecipientType.TO)[0].toString(),
            "to address should be set correctly");
        assertEquals(expectedCc, message.getRecipients(Message.RecipientType.CC)[0].toString(),
            "cc address should be set correctly");
        assertEquals(expectedBcc, message.getRecipients(Message.RecipientType.BCC)[0].toString(),
            "bcc address should be set correctly");
        assertEquals(expectedReply, message.getReplyTo()[0].toString(),
            "reply address should be set correctly");
        assertEquals(subj, message.getSubject(), "subject should be set correctly");
        assertEquals(textBody, message.getContent().toString(),
            "mime message content should match the plain text content");


        // test missing from fails
        Email emailMissingFrom = new SimpleEmail();
        emailMissingFrom.setHostName("localhost");
        emailMissingFrom.addTo("receiver@example.com");
        assertThrows(EmailException.class, emailMissingFrom::buildMimeMessage,
            "should throw EmailException when From is missing");

        // test missing to fails
        Email emailMissingTo = new SimpleEmail();
        emailMissingTo.setHostName("localhost");
        emailMissingTo.setFrom("sender@example.com");
        assertThrows(EmailException.class, emailMissingTo::buildMimeMessage,
            "should throw EmailException when To is missing");

        // test multipart content
        email = new SimpleEmail();
        email.setHostName("localhost");
        email.setFrom(senderEmail, senderName);
        email.addTo(receiverEmail, receiverName);
        email.setCharset(charset);
        email.setSubject(subj);
        MimeMultipart multipart = new MimeMultipart();
        email.setContent(multipart, "multipart/mixed");

        // build the message
        email.buildMimeMessage();
        message = email.getMimeMessage();

        // test that the message is built and has the correct content type
        assertNotNull(message, "mime message should exist after building");

        // test emailBody with null contentType
        email = new SimpleEmail();
        email.setHostName("localhost");
        email.setFrom(senderEmail, senderName);
        email.addTo(receiverEmail, receiverName);
        email.setCharset(charset);
        email.setSubject(subj);

        Field emailBodyField = Email.class.getDeclaredField("emailBody");
        emailBodyField.setAccessible(true);
        emailBodyField.set(email, new MimeMultipart());

        Field contentTypeField = Email.class.getDeclaredField("contentType");
        contentTypeField.setAccessible(true);
        contentTypeField.set(email, null);

        email.buildMimeMessage();
        message = email.getMimeMessage();

        // test emailBody with non-null contentType
        email = new SimpleEmail();
        email.setHostName("localhost");
        email.setFrom(senderEmail, senderName);
        email.addTo(receiverEmail, receiverName);
        email.setCharset(charset);
        email.setSubject(subj);

        emailBodyField.set(email, new MimeMultipart());
        contentTypeField.set(email, "text/custom");

        email.buildMimeMessage();
        message = email.getMimeMessage();
    }

    @Test
    public void testGetHostName() throws EmailException{
        // test initial state
        assertNull(email.getHostName(), "hostname should not exist yet");

        // test setting a hostname
        String host = "mail1.example.com";
        email.setHostName(host);
        assertEquals(host, email.getHostName(), "hostname should match the set value");

        // test changing the hostname and setting a session
        String newHost = "mail2.example.net";
        email.setHostName(newHost);
        email.getMailSession();

        assertEquals(newHost, email.getHostName(), "hostname should update correctly");
    }

    @Test
    public void testGetMailSession() throws Exception {
        // test exception when hostname is missing
        assertThrows(EmailException.class, email::getMailSession,
            "should throw EmailException when hostname is not set");

        // set a valid hostname and initialize the session
        String host = "smtp.example.com";
        int port = 26;
        email.setHostName(host);
        email.setSmtpPort(port);
        Session session = email.getMailSession();

        assertNotNull(session, "mail session should not be null after setting hostname");
        assertEquals(host, session.getProperty("mail.smtp.host"),
            "mail session should contain the correct hostname");
        assertEquals("smtp", session.getProperty("mail.transport.protocol"),
            "default transport protocol should be SMTP");
        assertEquals(String.format("%d", port), session.getProperty("mail.smtp.port"),
            "custom port should be set correctly");

        // check that authentication is not enabled by default
        assertNull(session.getProperty("mail.smtp.auth"),
            "SMTP authentication should be null by default");

        // test enabling authentication
        email = new SimpleEmail(); // Reset email object to clear session
        email.setHostName(host);
        email.setAuthentication("user@example.com", "password");
        session = email.getMailSession(); // Recreate session

        assertNotNull(session.getProperty("mail.smtp.auth"),
            "SMTP authentication property should be set when authentication is enabled");
        assertEquals("true", session.getProperty("mail.smtp.auth"),
            "SMTP authentication should be enabled when set");

        // test enabling SSL
        email = new SimpleEmail(); // reset session
        email.setHostName(host);
        email.setSSLOnConnect(true);
        email.setSocketTimeout(30000);
        session = email.getMailSession();

        assertEquals("465", session.getProperty("mail.smtp.socketFactory.port"),
            "SSL port should be 465 when SSL is enabled");
        assertEquals("javax.net.ssl.SSLSocketFactory", session.getProperty("mail.smtp.socketFactory.class"),
            "SSL socket factory should be set correctly");
        assertEquals("false", session.getProperty("mail.smtp.socketFactory.fallback"),
            "SSL fallback should be disabled");
        assertEquals(30000, email.getSocketTimeout(),
            "custom socket timeout should be set correctly");

        // test enabling TLS
        email = new SimpleEmail(); // reset session
        email.setHostName(host);
        email.setStartTLSEnabled(true);
        session = email.getMailSession();

        assertEquals("true", session.getProperty("mail.smtp.starttls.enable"),
            "StartTLS should be enabled when set");

        // test enabling TLS required
        email = new SimpleEmail(); // reset session
        email.setHostName(host);
        email.setStartTLSRequired(true);
        session = email.getMailSession();

        assertEquals("true", session.getProperty("mail.smtp.starttls.required"),
            "StartTLS should be required when set");

        // test enabling SSL check server identity
        email = new SimpleEmail(); // reset session
        email.setHostName(host);
        email.setSSLOnConnect(true);
        email.setSSLCheckServerIdentity(true);
        session = email.getMailSession();

        assertEquals("true", session.getProperty(EmailConstants.MAIL_SMTP_SSL_CHECKSERVERIDENTITY),
            "SSL check server identity should be set to true");

        // test setting a bounce address
        email = new SimpleEmail(); // reset session
        email.setHostName(host);
        String bounceEmail = "bounce@example.com";
        email.setBounceAddress(bounceEmail);
        session = email.getMailSession();

        assertEquals(bounceEmail, session.getProperty("mail.smtp.from"),
            "mail session should contain the correct bounce address");

        // test setting a custom mail session and reusing it
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "override.example.net");
        Session customSession = Session.getInstance(props);

        email = new SimpleEmail(); // reset session
        email.setMailSession(customSession);
        assertSame(customSession, email.getMailSession(),
            "mail session should be the manually set custom session");
        assertEquals("override.example.net", email.getMailSession().getProperty("mail.smtp.host"),
            "custom session should retain its hostname");
    }

    @Test
    public void testGetSentDate() {
        // test default sent date should not be null and should be close to current time
        Date beforeCreation = new Date();
        Date sentDate = email.getSentDate();
        Date afterCreation = new Date();

        assertNotNull(sentDate, "sent date should not be null by default");
        assertTrue(sentDate.after(beforeCreation) || sentDate.equals(beforeCreation),
            "sent date should be after or equal to creation time");
        assertTrue(sentDate.before(afterCreation) || sentDate.equals(afterCreation),
            "sent date should be before or equal to after-creation time");

        // test setting a specific sent date
        Date customDate = new Date(1000000000L);
        email.setSentDate(customDate);
        assertEquals(customDate, email.getSentDate(), "sent date should match the explicitly set date");
    }

    @Test
    public void testGetSocketConnectionTimeout() {
        // test default timeout value
        assertEquals(60000, email.getSocketConnectionTimeout(),
            "default socket connection timeout should be 60000 milliseconds");

        // test setting a custom timeout value
        int customTimeout = 30000; // 30 seconds
        email.setSocketConnectionTimeout(customTimeout);
        assertEquals(customTimeout, email.getSocketConnectionTimeout(),
            "socket connection timeout should match the explicitly set value");
    }

    @Test
    public void testSetFrom() throws Exception {
        // test setting a valid from address
        String senderEmail = "sender@example.com";
        String senderName = "Sender Name";

        email.setFrom(senderEmail, senderName);

        assertEquals(senderEmail, email.getFromAddress().getAddress(),
            "from email should be set correctly");
        assertEquals(senderName, email.getFromAddress().getPersonal(),
            "from name should be set correctly");

        // test setting a valid from address without a name
        String senderEmailOnly = "only@example.com";
        email.setFrom(senderEmailOnly);

        assertEquals(senderEmailOnly, email.getFromAddress().getAddress(),
            "from email should be set correctly when no name is provided");
        assertNull(email.getFromAddress().getPersonal(),
            "from name should be null when no name is provided");

        // test setting an invalid from address
        assertThrows(EmailException.class, () -> email.setFrom("invalid-email"),
            "should throw EmailException for invalid email");
    }
}
