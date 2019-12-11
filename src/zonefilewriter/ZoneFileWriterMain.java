package zonefilewriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import mail.MailService;
import shutdown.ShutDownListener;
import shutdown.ShutDownService;

public class ZoneFileWriterMain implements ShutDownListener{
	
	static Logger logger = Logger.getLogger(ZoneFileWriterMain.class);
	public static ZoneFileWriterMain obMain = null;
	public static   ZoneFileWriter obZoneFileWriter = null;
	public static MailService obMailService = null;
	
	public static void main(String[] args)	
	{
		PropertyConfigurator.configure("log4j.properties");
		obMain = new ZoneFileWriterMain();
		
		obZoneFileWriter = ZoneFileWriter.getInstance();
		obZoneFileWriter.start();
		
		
		
		obMailService = MailService.getInstance();
		obMailService.start();
				
		ShutDownService.getInstance().addShutDownListener(obMain);
		logger.debug("Zone File Writer started successfully.");
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		obZoneFileWriter.shutdown();
		obMailService.shutdown();
		logger.debug("Shut down server successfully");
		System.exit(0);
	}

}
