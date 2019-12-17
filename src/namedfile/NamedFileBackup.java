package namedfile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import util.DateAndTime;
import zonefilewriter.ZoneFileWriter;

public class NamedFileBackup extends Thread{
	public static NamedFileBackup obNamedFileBackup = null;
	static Logger logger = Logger.getLogger(NamedFileBackup.class.getName());
	public static long daily = 8640000;
	public static long weekly = 604800000;
	
	boolean running = false;
	
	public static NamedFileBackup getInstance(){
		if(obNamedFileBackup==null) {
			createInstance();
		}
		return obNamedFileBackup;
	}
	
	public static synchronized NamedFileBackup createInstance() {
		if(obNamedFileBackup==null) {
			obNamedFileBackup  = new NamedFileBackup();
		}
		return obNamedFileBackup;
	}
	
	@Override
	public void run(){
		running = true;
		logger.debug("Named file backup Service started.");
		long temp=0L,currentTime=0L;
		String namedFile = ZoneFileWriter.winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName;
		String backupFileName = "";
		long interval = 1000L;
		if(ZoneFileWriter.namedBackupTime.equals("daily")) {
			interval = daily;					
		}else if(ZoneFileWriter.namedBackupTime.equals("weekly")) {
			interval = weekly;
		}
		logger.debug("Interval: "+interval+", named file: "+namedFile);
		
		while(running){
			try {
								
				currentTime = System.currentTimeMillis();
				
				if((currentTime-temp)>=interval) {
					String today = new DateAndTime().getDateFromLongUnderScore(currentTime);
					temp=currentTime;
					backupFileName = ZoneFileWriter.winDir+ZoneFileWriter.namedFilePath+"/"+"namedBackup/"+ZoneFileWriter.namedFileName +"_"+today;
					logger.debug("Taking backup with name "+backupFileName+" at: "+currentTime);
					File source = new File(namedFile);
					File dest = new File(backupFileName);
					if(dest.exists()) {
						dest.delete();
					}
					Path pathToFile = Paths.get(backupFileName);
					Files.createDirectories(pathToFile.getParent());
					Files.copy(source.toPath(), dest.toPath());
				}				
				
				Thread.sleep(3000);
				
			}
			catch(Exception e){
		   	  	 logger.fatal("Error : "+e);		   	  	  
		   	}
        }
		
	}

}
