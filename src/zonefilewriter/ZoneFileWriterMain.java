package zonefilewriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import mail.MailService;
import namedfile.NamedFileBackup;
import shutdown.ShutDownListener;
import shutdown.ShutDownService;

public class ZoneFileWriterMain implements ShutDownListener{
	
	static Logger logger = Logger.getLogger(ZoneFileWriterMain.class);
	public static ZoneFileWriterMain obMain = null;
	public static   ZoneFileWriter obZoneFileWriter = null;
	public static MailService obMailService = null;
	public static NamedFileBackup obNamedFileBackup = null;
	public static BindServiceCheckThread obBindServiceCheckThread = null;
	public static   ZoneFileWriter2 obZoneFileWriter2 = null;
	
	public static void main(String[] args)	
	{
		PropertyConfigurator.configure("log4j.properties");
		obMain = new ZoneFileWriterMain();
		
		obZoneFileWriter = ZoneFileWriter.getInstance();
		obZoneFileWriter.start();
		
		obMailService = MailService.getInstance();
		obMailService.start();
		
		obNamedFileBackup = NamedFileBackup.getInstance();
		obNamedFileBackup.start();
		
		obBindServiceCheckThread = BindServiceCheckThread.getInstance();
		obBindServiceCheckThread.start();
		
		/*obZoneFileWriter2 = ZoneFileWriter2.getInstance();
		obZoneFileWriter2.start();*/
				
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
