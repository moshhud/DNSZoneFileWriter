package zonefilewriter;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import dnshostinginfo.DnsHostingInfoDAO;
import dnshostinginfo.DnsHostingInfoDTO;
import dnshostinginfo.DnsHostingZoneRecordDTO;
import util.ReturnObject;

public class ZoneFileWriter extends Thread{
	
	static Logger logger = Logger.getLogger(ZoneFileWriter.class);
	public static   ZoneFileWriter obZoneFileWriter;
	boolean running = false;
	public static final String DNS_HOSTING_TABLE_NAME = "at_dns";
	public static final String DNS_ZONE_RECORD_TABLE_NAME = "at_dns_zone_record";
	
	public static String serverType ="dotBD";
	public static String zoneFileLocation = "/var/named";
	public static String reveresednamedFileName = "named.rev.conf";
	public static String namedFilePath = "/etc";
	public static String namedFileName = "named.conf";
	public static long interval = 60;
	public static String named_allowed_update_ip = "123.49.12.149";
	public static String named_allowed_update_ip_rev = "123.49.12.3";
	LinkedHashMap<Long, DnsHostingInfoDTO> data = null;
	String ids = "";
	
	public static  ZoneFileWriter getInstance(){
		if(obZoneFileWriter==null) {
			createInstance();
		}
		
		return obZoneFileWriter;
		
	}	
	public static synchronized ZoneFileWriter createInstance(){
		if(obZoneFileWriter==null) {
			obZoneFileWriter = new ZoneFileWriter();
			LoadConfiguration();
		}
		return obZoneFileWriter;
	}
	
	@Override
	public void run(){
		try {
			logger.debug("run...");
			running = true;
			logger.debug("Service started.");
			long t1=0L,t2=0L;
			
			while(running)
            {
				t1 = System.currentTimeMillis();
				getData();
				//logger.debug("Current ids: "+ids);
				if(data!=null&&data.size()>0) {
					logger.debug("Current ids: "+ids);
					RecordFileWriter fw = new RecordFileWriter();
					boolean status = fw.processData(data, ids);
					if(status) {
						updateStatus();
						data=null;
						ids="";						
						t2 = System.currentTimeMillis();				
						logger.debug("Time to finish job(ms): "+(t2-t1));
						fw.runUnixCommand("bash","-c","rndc reload");
					}
				}else {
					;//logger.debug("No Data found to write into zone file.");
				}				
				Thread.sleep(interval);
            }
			
			
			
		}catch(Exception e){
	   	  	 logger.fatal("Error : "+e);
	   	  	
	   	  	  
	   	}
	}
	
	public void updateStatus() {
		ReturnObject ro = new ReturnObject();
		try {
			DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
			ro = dao.updateStatus(ids,DNS_HOSTING_TABLE_NAME);
			if(ro.getIsSuccessful()) {
				logger.debug("DB Status Updated successfully...");
			}
		}catch(Exception e) {
			 logger.fatal("Error : "+e);
		  
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReturnObject getData() {
		
		ReturnObject ro = new ReturnObject();
		try {
			DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
			ro = dao.getIDList(DNS_HOSTING_TABLE_NAME,"dnsID"," and dnsZoneFileUpdateStatus=1");
			if(ro != null && ro.getIsSuccessful()) {
				
				ArrayList<Long> IDList = (ArrayList)ro.getData();
				
				if(IDList!=null&&IDList.size()>0) {
					ids = dao.getStringFromArrayList(IDList, false);
					
					ro = dao.getDNSHostingInfoMap(DNS_HOSTING_TABLE_NAME, " and dnsID in("+ids+") ");
					if (ro != null && ro.getIsSuccessful() && ro.getData() instanceof LinkedHashMap) {
						data = (LinkedHashMap<Long, DnsHostingInfoDTO>)ro.getData();
						if (data != null && data.size() > 0) {
							
							Thread t1 = new Thread(new Runnable() {
								@Override
								public void run() {
									DnsHostingInfoDTO dnsDTO = null;
									LinkedHashMap<Long, DnsHostingZoneRecordDTO> zoneData = null;
									long key;
									
									ReturnObject ro = dao.getDNSZoneRecordMap(DNS_ZONE_RECORD_TABLE_NAME, " and dnszrDnsID in("+ids+")");
									if (ro != null && ro.getIsSuccessful() && ro.getData() instanceof LinkedHashMap) {
										zoneData = (LinkedHashMap<Long, DnsHostingZoneRecordDTO>) ro.getData();
										if (zoneData != null && zoneData.size() > 0) {
											for (DnsHostingZoneRecordDTO zoneDTO : zoneData.values()) {
												key = zoneDTO.getDnsHosingInfoID();
												
												if (data.containsKey(key)) {
													dnsDTO = data.get(key);
													if (dnsDTO != null) {
														if(dnsDTO.getZoneRecordDTOMap()==null) {
															
															dnsDTO.setZoneRecordDTOMap(new LinkedHashMap<Long, DnsHostingZoneRecordDTO>());
														}
														
														dnsDTO.getZoneRecordDTOMap().put(zoneDTO.getID(), zoneDTO);
													}
												}
											}
										}
									}
								}
							});
							t1.start();
							
							while(true) {
								if (t1 != null && t1.isAlive()) {
									t1.join(10);
								}else {
									break;
								}
								
							}
							
						}
						
					}
				}
				
				
			}
			else {
				logger.debug("No data found to write into zone file");
			}
			
			
		}catch (Exception ex)
	    {
			logger.fatal("Exception: "+ex.toString());
	    }
		
		ro = ReturnObject.clearInstance(ro);
		ro.setIsSuccessful(true);
		ro.setData(data);
		return ro;
	}
	
	public static void LoadConfiguration(){
		FileInputStream fileInputStream = null;
		String strConfigFileName = "properties.cfg";
		try
	    {
			Properties properties = new Properties();
		    File configFile = new File(strConfigFileName);
		    if (configFile.exists())
		      {
		    	fileInputStream = new FileInputStream(strConfigFileName);
		        properties.load(fileInputStream);	
		        
		        if(properties.get("namedFilePath")!=null){
		        	namedFilePath = (String) properties.get("namedFilePath");
		        }		        
		        if(properties.get("namedFileName")!=null){
		        	namedFileName =  (String) properties.get("namedFileName");
		        }
		        if(properties.get("reveresednamedFileName")!=null){
		        	reveresednamedFileName =  (String) properties.get("reveresednamedFileName");
		        }
		        if(properties.get("zoneFileLocation")!=null){
		        	zoneFileLocation =  (String) properties.get("zoneFileLocation");
		        }
		        if(properties.get("serverType")!=null){
		        	serverType =  (String) properties.get("serverType");
		        }
		        
		        if(properties.get("named_allowed_update_ip")!=null){
		        	named_allowed_update_ip =  (String) properties.get("named_allowed_update_ip");
		        }
		        
		        
		        String strInterval = "";
		        if(properties.get("interval")!=null){
		        	strInterval =  (String) properties.get("interval");
		        	if(strInterval!=null&&strInterval.length()>0) {
		        		interval = Long.parseLong(strInterval);
		        	}
		        }
		        
		        interval = interval*1000;
		       
		        
		        logger.debug("namedFileName: "+namedFileName+", Interval: "+interval);
		        
		        fileInputStream.close();

		      }
	    }
		catch (Exception ex)
	    {
	      logger.fatal("Error while loading configuration file :" + ex.toString(), ex);
	   
	      System.exit(0);
	    }
	    finally
	    {
	      if (fileInputStream != null)
	      {
	        try
	        {
	        	fileInputStream.close();
	        }
	        catch (Exception ex)
	        {
	        	logger.fatal(ex.toString());
	        }
	      }
	    }
		
		
	}
	
	public void shutdown()
	{
		logger.debug("Server shutdown");			
	    running = false;	    
	 }
	
			
			

}
