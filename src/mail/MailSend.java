package mail;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*; 
import org.apache.log4j.*;
import common.SmsMailLogDAO;
import dnshostinginfo.DnsHostingInfoDAO;

public class MailSend extends Thread {
	static Logger logger = Logger.getLogger(MailSend.class);
	public String toList;
	public String subject;
	public String messageText;
	public static MailServerInformationDTO mailServerInformationDTO = null;
	public static LinkedBlockingQueue<MailDTO> mailQueue;
	boolean running = false;
	public static MailSend mailSend;
	public static String contextPath;
	public static String blockIpEmailContent;
	public long lastMailConfigReadTime=0;
	public static MailSend getInstance() throws Exception {
		if (mailSend == null) {
			createInstance();
		}
		return mailSend;
	}

	private MailSend() throws Exception {
		super("Mail Sender");
		setPriority(Thread.MIN_PRIORITY);
		mailQueue = new LinkedBlockingQueue<MailDTO>();
		
		try
		{
			readMailConfigFromDB();
		}
		catch(Exception ex)
		{
			logger.debug("Exception in reading mail config");
			return;
		}
		
		this.setDaemon(true);

		start();
	}

	public void readMailConfigFromDB() throws Exception {
		DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
		mailServerInformationDTO = dao.getEmailServerInfoDTO();
		if(!mailServerInformationDTO.isActive())
		{
			logger.debug("Mail Service is not Active !!!");
			throw new Exception("Mail Service is not Active !!!");
		}
		logger.debug("mailServerInformationDTO " +  mailServerInformationDTO);
		lastMailConfigReadTime=System.currentTimeMillis();
	}

	

	private static synchronized MailSend createInstance() throws Exception {
		if (mailSend == null) {
			mailSend = new MailSend();
		}
		return mailSend;
	}
		
	public void sendMailWithContentAndSubject(MailDTO mailDTO) {
		logger.debug("sendMailWithContentAndSubject with running " + running);
		if(!running)return;
		mailQueue.offer(mailDTO);
	}
	
	public void run() {
		running = true;
		logger.debug("Mail server run is called");
		while (running) {
			try {				
				MailDTO mailDTO = mailQueue.take();												
				sendMailToParticipients(mailDTO);
			} catch (Exception e) {
				logger.debug("Exception", e);
			}
		}

	}

	public void sendMailToParticipients(MailDTO mailDTO) {

		String toList = mailDTO.toList;
		String msgText = mailDTO.msgText;
		String ccList = mailDTO.ccList;
		String attachment = mailDTO.attachmentPath;
		
		try {
			StringTokenizer stk = new StringTokenizer(toList, ",;");
			String to[] = new String[stk.countTokens()];
			for (int i = 0; i < to.length; i++) {
				to[i] = stk.nextToken().trim();
			}
			
			logger.debug("ccList " + ccList);
			String cc[] = null;
			if(ccList != null && ccList.length() > 0)
			{
				stk = new StringTokenizer(ccList, ",;");
				cc = new String[stk.countTokens()];
				if(cc != null && cc.length > 0)
				{
					for (int i = 0; i < cc.length; i++) {
						cc[i] = stk.nextToken().trim();
					}
				}
			}
			
			Properties props = new Properties();
			Authenticator authenticator = null;
			
			authenticator = new Authenticator(mailServerInformationDTO.getAuthEmailAddesstxt(),mailServerInformationDTO.getAuthEmailPasstxt());
			
			props.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
			props.setProperty("mail.smtp.auth", "" + mailServerInformationDTO.isAuthFromServerChk());
			props.setProperty("mail.smtp.host", mailServerInformationDTO.getMailServertxt());
			props.setProperty("mail.smtp.port", mailServerInformationDTO.getMailServerPorttxt());
			props.setProperty("mail.smtp.starttls.enable", ""+mailServerInformationDTO.isTlsRequired() ); 			
			props.setProperty("mail.transport.protocol", "smtp");
							
			Session session = Session.getInstance(props, authenticator);		
			session.setDebug(false);
			
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailServerInformationDTO.getFromAddresstxt()));
			msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
				
			InternetAddress[] address = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				logger.debug("to["+i+"] " + to[i]);
	
				address[i] = new InternetAddress(to[i]);
				logger.debug("address["+ i + "] " + address[i]);
			}
			
			InternetAddress[] addressCC = null;			
	
			msg.setRecipients(Message.RecipientType.TO, address);
			if(cc != null && cc.length > 0)
			{
				addressCC = new InternetAddress[cc.length];
				for (int i = 0; i < cc.length; i++) {
					logger.debug("cc["+i+"] " + cc[i]); 
					addressCC[i] = new InternetAddress(cc[i]);
					logger.debug("addressCC["+ i + "] " + addressCC[i]);
				} 
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			msg.setSubject(mailDTO.mailSubject);
			msg.setSentDate(new Date());
	
			if (attachment != null) {
				BodyPart messageBodyPart = new MimeBodyPart();
				if(mailDTO.isHtmlMail){messageBodyPart.setContent(msgText,"text/html");}
				else messageBodyPart.setText(msgText);
	
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
	
				addAttachments(attachment, multipart, "abc.csv");
				msg.setContent(multipart);
			}
			else
			{
				if(mailDTO.isHtmlMail) {
					
					msg.setContent(msgText,"text/html; charset=UTF-8");
				}
				else{
					
					msg.setContent(msgText, "text/plain; charset=UTF-8");
					logger.debug("msg: " + msg.getContent());
				}
			}
			try {			 
				Transport.send(msg); 
				
				String mailBody = new String(msgText.getBytes(),"UTF-8");
				SmsMailLogDAO log = new SmsMailLogDAO("email", toList, mailServerInformationDTO.fromAddresstxt, mailDTO.mailSubject,mailBody, ccList);
				log.updateStatus(mailDTO.getID(), "sent");
				
				
			} 
			catch (Exception ex) {
				//logger.debug(ex.getMessage());
				logger.debug(ex.toString());
			}			

	   } 
	   catch (Exception mex) {
			logger.fatal("Exception handling in SendMail.java", mex);
			mex.printStackTrace();
			Exception ex = mex;
			do {
				if (ex instanceof SendFailedException) {
					SendFailedException sfex = (SendFailedException) ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (invalid != null) {
						logger.fatal("    ** Invalid Addresses");
						if (invalid != null) {
							for (int i = 0; i < invalid.length; i++)
								logger.fatal(" " + invalid[i]);
						}
					}
					Address[] validUnsent = sfex.getValidUnsentAddresses();
					if (validUnsent != null) {
						logger.fatal("    ** ValidUnsent Addresses");
						if (validUnsent != null) {
							for (int i = 0; i < validUnsent.length; i++)
								logger.fatal("         " + validUnsent[i]);
						}
					}
					Address[] validSent = sfex.getValidSentAddresses();
					if (validSent != null) {
						logger.fatal("    ** ValidSent Addresses");
						if (validSent != null) {
							for (int i = 0; i < validSent.length; i++)
								logger.fatal("         " + validSent[i]);
						}
					}
				}

				if (ex instanceof MessagingException)
					ex = ((MessagingException) ex).getNextException();
				else
					ex = null;
			} while (ex != null);
		}
		finally
		{
			try
			{
				if(attachment != null)
				{
					File file = new File(attachment);
					file.delete();
				}
			}
			catch(Exception ex)
			{
				logger.fatal("Exception deleting mail attachment", ex);
			}
		}	

		toList = msgText = null;
		logger.debug("DONE");
	}
	

	protected void addAttachments(String attachment, Multipart multipart, String AttachmentName)
			throws MessagingException, AddressException {
		MimeBodyPart attachmentBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(attachment);
		attachmentBodyPart.setDataHandler(new DataHandler(source));
		attachmentBodyPart.setFileName(AttachmentName);
		multipart.addBodyPart(attachmentBodyPart);
	}
	
	public static void main(String args[]) throws Exception
	{
		
		String billViewLink = "http://localhost:8080/BTCL_Automation/common/bill/billView.jsp?id=";
		
    	MailDTO mailDTO = new MailDTO();
		mailDTO.isHtmlMail = true;
		mailDTO.mailSubject = "BTCL Bill Generation  Notification";
		mailDTO.msgText = "Dear Customer,<br> Your have a bill waiting to be paid. To view or download the bill " +
						"Log in to BTCL system and click the following link : "+billViewLink+1;
		mailDTO.toList = "palash@revesoft.com";
		MailSend.getInstance().sendMailWithContentAndSubject(mailDTO);
	}
}
