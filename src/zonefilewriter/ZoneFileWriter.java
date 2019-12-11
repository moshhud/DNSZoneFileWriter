package zonefilewriter;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import dnshostinginfo.DnsHostingInfoDAO;
import dnshostinginfo.DnsHostingInfoDTO;
import dnshostinginfo.DnsHostingPTRDTO;
import dnshostinginfo.DnsHostingZoneRecordDTO;
import util.ReturnObject;

public class ZoneFileWriter extends Thread{
	
	static Logger logger = Logger.getLogger(ZoneFileWriter.class);
	public static   ZoneFileWriter obZoneFileWriter;
	boolean running = false;
	public static final String DNS_HOSTING_TABLE_NAME = "at_dns";
	public static final String DNS_ZONE_RECORD_TABLE_NAME = "at_dns_zone_record";
	public static final String DNS_HOSTING_PTR_TABLE_NAME = "at_dns_additional_ptr";
	
	public static String zoneFileDIRDOTBD = "dotbd";
	public static String zoneFileDIRDOTBANGLA = "bangla";
	public static String zoneFileDIROTHER = "other";
	public static String reverseDIR = "reverse";
	public static String serverType ="dotBD";
	public static String zoneFileLocation = "/var/named";
	public static String reveresednamedFileName = "named.rev.conf";
	public static String namedFilePath = "/etc";
	public static String namedFileName = "named.conf";
	public static long interval = 60;
	public static String named_allowed_update_ip = "123.49.12.149;123.49.12.182;";
	public static String named_allowed_update_ip_rev = "123.49.12.3";
	LinkedHashMap<Long, DnsHostingInfoDTO> data = null;
	LinkedHashMap<Long, DnsHostingPTRDTO> ptrdata = null;
	public static String primaryDNS = "dns.bttb.net.bd";
	public static String secondaryDNS = "slave.bttb.net.bd";
	public static String tertiaryDNS = null;
	public static String parkingDNS1= "ns1.btclparked.com.bd";
	public static String parkingDNS2= "ns2.btclparked.com.bd";
	public static String parkingDNS3= null;
	public static String winDir = "D:/root";
	String ids = "";
	String ptrIDs = "";
	public static long emailCheck = 60;
	
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
			if(isWindows()) {
				winDir = "D:/root";
			}else {
				winDir = "";
			}
		}
		return obZoneFileWriter;
	}
	
	public static String getOsName(){
		  String OS = null;
	      if(OS == null) { OS = System.getProperty("os.name"); }
	      return OS;
	    }
	public static boolean isWindows(){
		      return getOsName().startsWith("Windows");
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
				
				
				try {
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
							if(!isWindows()) {
								fw.runUnixCommand("bash","-c","rndc reload");
								//fw.runUnixCommand("bash","-c","named restart");			
							}
																
						}
					}else {
						;//logger.debug("No Data found to write into zone file.");
					}
				}
				catch(Exception e){
			   	  	 logger.fatal("Error : "+e);	   	  	  
			   	}
				
				try {
					t1 = System.currentTimeMillis();
					//Processing external PTR records
					getPTRData();
					if(ptrdata!=null&&ptrdata.size()>0) {
						RecordFileWriter fw = new RecordFileWriter();
						boolean status = fw.processPTRData(ptrdata, ids);
						
						if(status) {
							//update status
							updatePTRStatus();								
							//reset data
							ptrdata=null;
							ptrIDs="";	
							t2 = System.currentTimeMillis();				
							logger.debug("Time to finish job(ms): "+(t2-t1));
							if(!isWindows()) {
								fw.runUnixCommand("bash","-c","rndc reload");
							}
							
						}
					}
				}
				catch(Exception e){
			   	  	 logger.fatal("Error : "+e);	   	  	  
			   	}
				
				Thread.sleep(interval);
            }
			
			
			
		}catch(Exception e){
	   	  	 logger.fatal("Error : "+e);	   	  	  
	   	}
	}
	
	public boolean updateStatus() {
		ReturnObject ro = new ReturnObject();
		boolean status = false;
		try {
			DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
			ro = dao.updateStatus(data,DNS_HOSTING_TABLE_NAME);
			if(ro.getIsSuccessful()) {
				status = true;
				logger.debug("DB Status Updated successfully...");
			}
		}catch(Exception e) {
			 logger.fatal("Error : "+e);
		  
		}
		return status;
	}
	
	public boolean updatePTRStatus() {
		ReturnObject ro = new ReturnObject();
		boolean status = false;
		try {
			DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
			ro = dao.updatePTRStatus(ptrdata,DNS_HOSTING_PTR_TABLE_NAME);
			if(ro.getIsSuccessful()) {
				status = true;
				logger.debug("DB Status Updated successfully...");
			}
		}catch(Exception e) {
			 logger.fatal("Error : "+e);
		  
		}
		return status;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReturnObject getPTRData() {
		ReturnObject ro = new ReturnObject();
		try {
			DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
			ro = dao.getIDList(DNS_HOSTING_PTR_TABLE_NAME,"dnsadPTRID"," and dnsadPTRZoneFileUpdateStatus>=1 limit 100");
			if(ro != null && ro.getIsSuccessful()) {
				ArrayList<Long> IDList = (ArrayList)ro.getData();
				if(IDList!=null&&IDList.size()>0) {
					ptrIDs = dao.getStringFromArrayList(IDList, false);
					ro = dao.getDNSHostingPTRInfoMap(DNS_HOSTING_PTR_TABLE_NAME, " and dnsadPTRID in("+ptrIDs+") ");
					if (ro != null && ro.getIsSuccessful() && ro.getData() instanceof LinkedHashMap) {
						ptrdata = (LinkedHashMap<Long, DnsHostingPTRDTO>)ro.getData();
						if (ptrdata != null && ptrdata.size() > 0) {
							ro = ReturnObject.clearInstance(ro);
							ro.setIsSuccessful(true);
							ro.setData(ptrdata);
						}
					}
				}
			}
			
		}
		catch (Exception ex)
	    {
			logger.fatal("Exception: "+ex.toString());
	    }
		
		return ro;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReturnObject getData() {
		
		ReturnObject ro = new ReturnObject();
		try {
			DnsHostingInfoDAO dao = new DnsHostingInfoDAO();
			ro = dao.getIDList(DNS_HOSTING_TABLE_NAME,"dnsID"," and dnsZoneFileUpdateStatus>=1 limit 100");
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
									
									ReturnObject ro = dao.getDNSZoneRecordMap(DNS_ZONE_RECORD_TABLE_NAME, " and dnszrDnsID in("+ids+") order by dnszrRecordType desc");
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
			/*else {
				logger.debug("No data found to write into zone file");
			}*/
			
			
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
		        if(properties.get("primaryDNS")!=null){
		        	primaryDNS =  (String) properties.get("primaryDNS");
		        }
		        if(properties.get("primaryDNS")!=null){
		        	primaryDNS =  (String) properties.get("primaryDNS");
		        }
		        if(properties.get("secondaryDNS")!=null){
		        	secondaryDNS =  (String) properties.get("secondaryDNS");
		        }
		        if(properties.get("tertiaryDNS")!=null){
		        	tertiaryDNS =  (String) properties.get("tertiaryDNS");
		        }
		        if(properties.get("parkingDNS1")!=null){
		        	parkingDNS1 =  (String) properties.get("parkingDNS1");
		        }
		        if(properties.get("parkingDNS2")!=null){
		        	parkingDNS2 =  (String) properties.get("parkingDNS2");
		        }
		        if(properties.get("parkingDNS3")!=null){
		        	parkingDNS3 =  (String) properties.get("parkingDNS3");
		        }
		        String strInterval = "";
		        if(properties.get("emailCheck")!=null){
		        	strInterval =  (String) properties.get("emailCheck");
		        	if(strInterval!=null&&strInterval.length()>0) {
		        		emailCheck = Long.parseLong(strInterval);
		        	}
		        	
		        	
		        }
		        emailCheck = emailCheck*1000;		        
		        
		        strInterval = "";
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
