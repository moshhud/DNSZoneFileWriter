package zonefilewriter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import common.ApplicationConstants;
import common.SmsMailLogDAO;

public class BindServiceCheckThread extends Thread{

	boolean running = false;
	static Logger logger = Logger.getLogger(BindServiceCheckThread.class);
	public static BindServiceCheckThread obBindServiceCheckThread = null;
	
	public static BindServiceCheckThread getInstance(){
		if(obBindServiceCheckThread==null) {
			createInstance();
		}
		return obBindServiceCheckThread;
	}
	
	public static synchronized BindServiceCheckThread createInstance() {
		if(obBindServiceCheckThread==null) {
			obBindServiceCheckThread = new BindServiceCheckThread();
		}
		return obBindServiceCheckThread;
	}
	
	@Override
	public void run(){
		running = true;
		logger.debug("BindServiceCheckThread started.");
		
		while(running) {
			try {
				
				boolean status = checkWithDig("dig @localhost btcl.gov.bd");
				if(!status) {
					logger.debug("Bind service is not running. Sending email notification...");
					sendEMailToAdmin(ZoneFileWriter.bindNotificationTo);
				}
				
				
				Thread.sleep(10*60000);
			}
			catch(Exception e){
		   	  	 logger.fatal("Error : "+e);	   	  	  
		   	}
			
			
		}
		
	}
	
	public boolean checkWithDig(String command){
		boolean runningStatus = true;
		try{
			logger.debug("Checking for: "+command);
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String termialOutputLine;
			StringBuffer sb = new StringBuffer();
			
			while ((termialOutputLine = stdInput.readLine()) != null) {			
				sb.append(termialOutputLine);
				sb.append("\n");
			}
			
			logger.debug(sb.toString());
			
			if(sb.toString().contains("connection timed out; no servers could be reached")){
				logger.debug("Service is not running. Please check bind service immediately.");
				runningStatus = false;
			}
						
		}
		catch(Exception e){
			logger.fatal(e.toString());
		}
		
		return runningStatus;
	}
	
	public void sendEMailToAdmin(String to) {
		try {
			
			StringBuilder sb = new StringBuilder();
			sb.append("Dear Concern,<br>");
			sb.append("Bind Service is not running in the server: "+ZoneFileWriter.serverIP+"<br>");
			sb.append("Please check the server immediately.<br>");
			sb.append("<br><br>");
			sb.append("Regards,<br>");
			sb.append("DNS hosting Automation Service.");
			
			String subject = "Bind Service is not running in the server: "+ZoneFileWriter.serverIP;
			
			String msgText = sb.toString();
			String mailBody = new String(msgText.getBytes(),"UTF-8");
			SmsMailLogDAO log = new SmsMailLogDAO(
					ApplicationConstants.EMAIL_CONSTANT.MSG_TYPE_EMAIL,
					to, 
					ApplicationConstants.EMAIL_CONSTANT.FROM, 
					subject,
					mailBody, 
					"");
			log.run();
		}
		catch(Exception e) {
			 logger.fatal("Error : "+e);
		  
		}
		
	}
	
	public void shutdown(){
		logger.debug("BindServiceCheckThread shutdown successfully");			
	    running = false;	    
	}
}
