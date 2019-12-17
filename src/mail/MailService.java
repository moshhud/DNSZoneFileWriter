package mail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;

import dnshostinginfo.DnsHostingInfoDAO;
import util.ReturnObject;
import zonefilewriter.ZoneFileWriter;

public class MailService extends Thread{
	public static MailService obMailService = null;
	static Logger logger = Logger.getLogger(MailService.class);
	boolean running = false;
	String ids = "";
	LinkedHashMap<Long,MailDTO> data = null;
	public static final String SMS_EMAIL_LOG_TABLE = "at_sms_and_mail_log";
	
	public static MailService getInstance(){
		if(obMailService==null) {
			createInstance();
		}
		return obMailService;
	}
	
	public static synchronized MailService createInstance() {
		if(obMailService==null) {
			obMailService = new MailService();
		}
		return obMailService;
	}
	
	@Override
	public void run(){
		running = true;
		logger.debug("Client Service started.");
		long t1=0L,t2=0L;
		
		while(running)
        {
			try {
				t1 = System.currentTimeMillis();
				ReturnObject ro = new ReturnObject();
				ro = getData();
				if(ro.getIsSuccessful()) {
					for(MailDTO dto : data.values()) {
						logger.debug("Sending email for ID: "+dto.getID());
						dto.setHtmlMail(true);
						MailSend mailSend = MailSend.getInstance();
						mailSend.sendMailWithContentAndSubject(dto);
						data = null;
						ids = "";
					}
				t2 = System.currentTimeMillis();				
				logger.debug("Time to finish Email service job(ms): "+(t2-t1));
				}
								
				Thread.sleep(ZoneFileWriter.emailCheck);
				
				
			}
			catch(Exception e){
		   	  	 logger.fatal("Error : "+e);		   	  	  
		   	}
        }
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReturnObject getData() {
		ReturnObject ro = new ReturnObject();
		DnsHostingInfoDAO dao = new DnsHostingInfoDAO();		
		try {
			ro = dao.getIDList(SMS_EMAIL_LOG_TABLE,"id"," and request_generated_from='dnshosting jar' and status='pending' ");
			if(ro != null && ro.getIsSuccessful()) {
				ArrayList<Long> IDList = (ArrayList)ro.getData();
				if(IDList!=null&&IDList.size()>0) {
					ids = dao.getStringFromArrayList(IDList, false);
					logger.debug(ids);
					ro = dao.getSMSAndEmailLogMap(SMS_EMAIL_LOG_TABLE, " and id in("+ids+") ");
					if (ro != null && ro.getIsSuccessful() && ro.getData() instanceof LinkedHashMap) {
						data = (LinkedHashMap<Long, MailDTO>)ro.getData();
						if (data != null && data.size() > 0) {
							ro = ReturnObject.clearInstance(ro);
							ro.setIsSuccessful(true);
							ro.setData(data);
						}
					}					
				}
			}
			else {
				ro.setIsSuccessful(false);
				//logger.debug("No data found");
			}
		}
		catch (Exception ex)
	    {
			logger.fatal("Exception: "+ex.toString());
	    }
		
		
		return ro;
	}
	
	public void shutdown()
	{		 	
	    running = false;	    
	 }

}
