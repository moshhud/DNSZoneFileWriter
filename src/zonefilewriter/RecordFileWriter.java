package zonefilewriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import common.ApplicationConstants;
import common.EmailValidator;
import common.SmsMailLogDAO;
import dnshostinginfo.DnsHostingInfoDTO;
import dnshostinginfo.DnsHostingPTRDTO;
import dnshostinginfo.DnsHostingZoneRecordDTO;


public class RecordFileWriter {
	static Logger logger = Logger.getLogger(RecordFileWriter.class);	
	String[] dotBDSlds = {"com", "co", "edu", "gov", "info", "net", "org", "ac", "mil" ,"ws", "tv"};
	
	private  final String ZONE_FILE_CONSTANT_PORTION = "$TTL 86400\n"+
			"VAR_FQDN.  900  IN SOA 	VAR_DNS_1. VAR_DNS_2.  (\n"+
			"   VAR_SERIAL_NUMBER	; Serial\n"+
			"   1200	; Refresh \n"+
			"   3600	; Retry\n"+
			"   604800	; Expire\n"+
			"   86400 )	; Minimum ttl\n"+
			" \t\t\t\tIN	NS	VAR_DEFAULT_DNS_1. \n"+
			" \t\t\t\tIN	NS	VAR_DEFAULT_DNS_2. \n";
	
	private  final String REVERSE_ZONE_FILE_CONSTANT_PORTION = "$TTL 86400\n"+
			"VAR_FQDN.  900  IN SOA 	VAR_DNS_1. VAR_DNS_2.  (\n"+
			"   VAR_SERIAL_NUMBER	; Serial\n"+
			"   1200	; Refresh \n"+
			"   3600	; Retry\n"+
			"   604800	; Expire\n"+
			"   86400 )	; Minimum ttl\n"+
			" \t\t\t\tIN	NS	VAR_DEFAULT_DNS_1. \n"+
			" \t\t\t\tIN	NS	VAR_DEFAULT_DNS_2. \n";
	
	private  final String NAMED_FILE_CONTENT ="zone \"VAR_DOMAIN\" IN {\n"
			+ "  type master;\n"
			+ "  file \""+ZoneFileWriter.zoneFileLocation+"/VAR_ZONE_FILE_NAME\";\n"
			+ "  allow-update {VAR_ALLOWED_UPDATE_IP;};\n"
			+ "};";
	
	public static String getSerialNumber(long currentTime){

		Calendar cal = new GregorianCalendar();
		int year = cal.get(cal.YEAR);		
		int month = cal.get(cal.MONTH)+1;
		String monthString = (month<10?"0"+month:""+month);
		int day = cal.get(cal.DAY_OF_MONTH);
		String dayString = (day<10?"0"+day:""+day);
		int hour = cal.get(cal.HOUR_OF_DAY);
		int min = cal.get(cal.MINUTE);
		
		int countOfHalfHour = hour*2+(min>=30?1:0);
		String countOfHalfHourString = ""+countOfHalfHour;
		while(countOfHalfHourString.length()<2){
			countOfHalfHourString="0"+countOfHalfHour;
		}
		return ""+year+monthString+dayString+countOfHalfHourString;
	}
	
	public void writeIntoNamedFile(String domain, String fileName,Writer writer) {
		try {
			
			String content = NAMED_FILE_CONTENT.replace("VAR_DOMAIN", domain);
			content = content.replaceAll("VAR_ZONE_FILE_NAME", fileName);
			content = content.replaceAll("VAR_ALLOWED_UPDATE_IP", ZoneFileWriter.named_allowed_update_ip);
			writer.write(content);
			writer.write("\n");
			writer.flush();
			writer.close();
		}
		catch(Exception e) {
			logger.fatal(e.toString());
			
		}
	}
	
	public  void createZoneFile(DnsHostingInfoDTO dto,Writer writer) {		
		LinkedHashMap<String, String> mxData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> aData = new LinkedHashMap<String, String>();
		
		try {
			String primaryDNS = dto.getPrimaryDNS().length()>3? dto.getPrimaryDNS():ZoneFileWriter.primaryDNS;
			String secondaryDNS = dto.getSecondaryDNS().length()>3? dto.getSecondaryDNS():ZoneFileWriter.secondaryDNS;			
			String header = ZONE_FILE_CONSTANT_PORTION.replace("VAR_FQDN", dto.getDomainName());
			header = header.replaceAll("VAR_DNS_1", dto.getPrimaryDNS());
			header = header.replaceAll("VAR_DNS_2", dto.getSecondaryDNS());
			//header = header.replaceAll("VAR_EMAIL", dto.getEmail());
			header = header.replaceAll("VAR_SERIAL_NUMBER", getSerialNumber(System.currentTimeMillis()));
			header = header.replaceAll("VAR_DEFAULT_DNS_1", primaryDNS);
			header = header.replaceAll("VAR_DEFAULT_DNS_2", secondaryDNS);
			if(ZoneFileWriter.tertiaryDNS!=null&& !ZoneFileWriter.tertiaryDNS.equals("")) {
				header += " \t\t\t\tIN	NS	VAR_DEFAULT_DNS_3. \n".replace("VAR_DEFAULT_DNS_3", ZoneFileWriter.tertiaryDNS);
			}
			header +="$ORIGIN  "+dto.getDomainName()+".";
			writer.write(header);
			writer.write("\n");
			int maxPriority = 10;
			
			if(dto.getZoneRecordDTOMap().values()!=null && dto.getZoneRecordDTOMap().values().size()>0) {
				for (DnsHostingZoneRecordDTO zoneDTO : dto.getZoneRecordDTOMap().values()) {
					
					if(zoneDTO!=null) {
						StringBuffer sb = new StringBuffer();	
						if(zoneDTO.getRecordType().equals("NS")) {
							continue;
						}
						if(zoneDTO.getRecordType().equals("MX")) {
							sb.append(zoneDTO.getRecordName()+"  ");
							sb.append(zoneDTO.getTtl()+" ");
							sb.append(zoneDTO.getRecordClass()+" ");
							sb.append(zoneDTO.getRecordType()+" ");
							sb.append(zoneDTO.getMxPriority()>0 ? zoneDTO.getMxPriority(): maxPriority +" ");
							sb.append(zoneDTO.getRecordValue()+" ");
							maxPriority += 10;
							String[] arr = zoneDTO.getRecordValue().split(Pattern.quote("."));
							mxData.put(arr[0], zoneDTO.getRecordValue());
							
						}else {
							sb.append(zoneDTO.getRecordName()+"  ");
							sb.append(zoneDTO.getTtl()+" ");
							sb.append(zoneDTO.getRecordClass()+" ");
							sb.append(zoneDTO.getRecordType()+" ");
							sb.append(zoneDTO.getRecordValue()+" ");					
						}
						
						if(zoneDTO.getRecordType().equals("A")) {
							if(mxData.containsKey(zoneDTO.getRecordName())) {
								aData.put(zoneDTO.getRecordName(), zoneDTO.getRecordValue());
							}
						}
						writer.write(sb.toString());
						writer.write("\n");
					}
				}
				
				writer.flush();
				writer.close();
				if(aData!=null&&aData.size()>0) {
					createReverseDNSFile(mxData,aData,primaryDNS,secondaryDNS);
				}
			}else {
				logger.debug("No zone Record found for "+dto.getDomainName());
			}
			
			
		}catch(Exception e) {
			logger.fatal(e.toString());
		}
		
	}
	
	public  void createParkedZoneFile(DnsHostingInfoDTO dto,Writer writer) {	
		try {
			String header = ZONE_FILE_CONSTANT_PORTION.replace("VAR_FQDN", dto.getDomainName());
			header = header.replaceAll("VAR_DNS_1", "ns1.btclparked.com.bd");
			header = header.replaceAll("VAR_DNS_2", "ns2.btclparked.com.bd");
			header = header.replaceAll("VAR_SERIAL_NUMBER", getSerialNumber(System.currentTimeMillis()));
			header = header.replaceAll("VAR_DEFAULT_DNS_1", ZoneFileWriter.parkingDNS1);
			header = header.replaceAll("VAR_DEFAULT_DNS_2", ZoneFileWriter.parkingDNS2);
			if(ZoneFileWriter.parkingDNS3!=null&& !ZoneFileWriter.parkingDNS3.equals("")) {
				header += " \t\t\t\tIN	NS	VAR_DEFAULT_DNS_3. \n".replace("VAR_DEFAULT_DNS_3", ZoneFileWriter.parkingDNS3);
			}
			
			header +="$ORIGIN  "+dto.getDomainName()+".";
			writer.write(header);
			writer.write("\n");
			
			StringBuffer sb = new StringBuffer();	
			
			sb.append("www ");
			sb.append("900  ");
			sb.append("IN ");
			sb.append("A ");
			sb.append("123.49.12.133 ");			
			writer.write(sb.toString());
			writer.write("\n");
			
			sb = new StringBuffer();	
			sb.append(dto.getDomainName()+".  ");
			sb.append("900  ");
			sb.append("IN ");
			sb.append("A ");
			sb.append("123.49.12.133 ");			
			writer.write(sb.toString());
			writer.write("\n");
			
			writer.flush();
			writer.close();			
			
		}catch(Exception e) {
			logger.fatal(e.toString());
		}
		
	}
	
	public void deleteContent(String filePath, String matchString) {
	   try {
		    //logger.debug("Deleting if any duplicate for : "+matchString+" in named.conf");
		    BufferedReader file = new BufferedReader(new FileReader(filePath));
	        String line;
	        StringBuffer inputBuffer = new StringBuffer();
	        int count=1;
	        boolean ignore = false;
	        
	        while ((line = file.readLine()) != null) {
	        	if(line.contains(matchString)){	
	        		  ignore=true; 
	        	} 
	        	if(ignore && count<=5){
	        			count++;
	        			continue;
	        	}
	        	if(count>5){
	        		ignore = false;
	        		count=1;
	        	}
	        	
	            inputBuffer.append(line);
	            inputBuffer.append("\n");
	        }
	        String inputStr = inputBuffer.toString();	
	        file.close();	       
	        FileOutputStream fileOut = new FileOutputStream(filePath);
	        fileOut.write(inputStr.getBytes());
	        fileOut.flush();
	        fileOut.close();
	   }catch(Exception e) {
			logger.fatal(e.toString());
		}
	}
	
	public void deleteZoneFileEntry(DnsHostingInfoDTO dto, String filePath) {
		try {
			String namedFile = ZoneFileWriter.winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName;
			
			deleteContent(namedFile,dto.getDomainName());
	        deleteFile(filePath);
	        	        
	        if(dto.getZoneRecordDTOMap().values()!=null && dto.getZoneRecordDTOMap().values().size()>0) {
	        	LinkedHashMap<String, String> mxData = new LinkedHashMap<String, String>();
				LinkedHashMap<String, String> aData = new LinkedHashMap<String, String>();
				
	        	for (DnsHostingZoneRecordDTO zoneDTO : dto.getZoneRecordDTOMap().values()) {
	        		if(zoneDTO!=null) {
	        			if(zoneDTO.getRecordType().equals("MX")) {							
							String[] arr = zoneDTO.getRecordValue().split(Pattern.quote("."));
							mxData.put(arr[0], zoneDTO.getRecordValue());							
						}
	        			if(zoneDTO.getRecordType().equals("A")) {
							if(mxData.containsKey(zoneDTO.getRecordName())) {
								aData.put(zoneDTO.getRecordName(), zoneDTO.getRecordValue());
							}
						}
	        		}
	        	}
	        	
	        	if(aData!=null&&aData.size()>0) {
	        		deleteReverseDNSInfo(mxData,aData,namedFile);
				}
	        }
	        
		}
		catch(Exception e) {
			logger.fatal(e.toString());
		}
	}
	
	public void deleteReverseDNSInfo(LinkedHashMap<String, String> mxData,LinkedHashMap<String, String> aData, String namedFile) {
		try {
			for(Entry<String, String> entry : aData.entrySet()) {
				String[] arr = entry.getValue().split(Pattern.quote("."));
				String reverse = arr[2]+"."+arr[1]+"."+arr[0];		
				String forward = arr[0]+"."+arr[1]+"."+arr[2];				
				String reverseFileName = "rev."+forward;//"db."+reverse;
				String fqdn = reverse+".in-addr.arpa";
				logger.debug("Reverse: "+reverseFileName);
				String fileName = ZoneFileWriter.winDir+ZoneFileWriter.zoneFileLocation+"/"+ZoneFileWriter.reverseDIR+"/"+reverseFileName;
				File file = new File(fileName);
				if(file.exists()) {
					deleteFile(fileName);					
				}
				deleteContent(namedFile,fqdn);
				
			}
		}catch(Exception e){
    		logger.fatal(e.toString());
    	}
	}
	
    public void deleteFile(String f){    	
    	try{
    		File file = new File(f);
    		if(file.delete()){
    			logger.debug(file.getName() + " is deleted!");
    		}else{
    			logger.debug("Delete operation is failed for file: "+file.getName());
    		}    
    		

    	}catch(Exception e){
    		logger.fatal(e.toString());
    	}
    }
	
	public  void createReverseDNSFile(LinkedHashMap<String, String> mxData,LinkedHashMap<String, String> aData,String primaryDNS, String secondaryDNS) {
		
		try {
						
			for(Entry<String, String> entry : aData.entrySet()) {
				//logger.debug("Key: "+entry.getKey()+", value: "+entry.getValue());
				String[] arr = entry.getValue().split(Pattern.quote("."));
				String reverse = arr[2]+"."+arr[1]+"."+arr[0];
				String forward = arr[0]+"."+arr[1]+"."+arr[2];
				logger.debug("reverse: "+reverse);
				String value = mxData.get(entry.getKey());;
				String content = arr[3]+" PTR "+value;
				String reverseFileName = "rev."+forward;//reverse;//"db."+reverse;
				String fqdn = reverse+".in-addr.arpa";
				String fileName = ZoneFileWriter.winDir+ZoneFileWriter.zoneFileLocation+"/"+ZoneFileWriter.reverseDIR+"/"+reverseFileName;
				File file = new File(fileName);
				FileWriter fw = null;
				boolean writeReverseNamedFile = false;
				if(file.exists()) {
					removeEntry(value,fileName);
					fw = new FileWriter(file,true);					
					fw.write(content);
					fw.write("\n");
					
				}else {
					fw = new FileWriter(file,false);
					String header = REVERSE_ZONE_FILE_CONSTANT_PORTION.replace("VAR_FQDN", fqdn);
					header = header.replaceAll("VAR_DNS_1", primaryDNS);
					header = header.replaceAll("VAR_DNS_2", secondaryDNS);					
					header = header.replaceAll("VAR_SERIAL_NUMBER", getSerialNumber(System.currentTimeMillis()));
					header = header.replaceAll("VAR_DEFAULT_DNS_1", primaryDNS);
					header = header.replaceAll("VAR_DEFAULT_DNS_2", secondaryDNS);
					header +="$ORIGIN  "+fqdn+".";
					fw.write(header);
					fw.write("\n");
					fw.write(content);
					fw.write("\n");
					writeReverseNamedFile = true;
				}
				fw.flush();
				fw.close();
				
				if(writeReverseNamedFile) {
					writeIntoNamedFile(fqdn,ZoneFileWriter.reverseDIR+"/"+reverseFileName,new FileWriter(new File(ZoneFileWriter.winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName),true));
				}
			}
		}catch(Exception e) {
			logger.fatal(e.toString());
		}
		
	}
	
	public void removeEntry(String str,String fileName) {
		try {	       
	        BufferedReader file = new BufferedReader(new FileReader(fileName));
	        String line;
	        StringBuffer inputBuffer = new StringBuffer();
	
	        while ((line = file.readLine()) != null) {
	        	if(line.contains(str)) continue;
	            inputBuffer.append(line);
	            inputBuffer.append('\n');
	        }
	        String inputStr = inputBuffer.toString();	
	        file.close();
	        
	        FileOutputStream fileOut = new FileOutputStream(fileName);
	        fileOut.write(inputStr.getBytes());
	        fileOut.close();
	
	    } catch (Exception e) {
	        logger.fatal("Error: "+e.toString());
	    }
	}
	
	public  void createReverseDNSFile(DnsHostingPTRDTO dto, String primaryDNS, String secondaryDNS) {
		try {
			
			primaryDNS= primaryDNS.length()>3? primaryDNS:ZoneFileWriter.primaryDNS;
			secondaryDNS = secondaryDNS.length()>3? secondaryDNS:ZoneFileWriter.secondaryDNS;
			
			String[] arr = dto.getIpAddress().split(Pattern.quote("."));
			String reverse = arr[2]+"."+arr[1]+"."+arr[0];
			String forward = arr[0]+"."+arr[1]+"."+arr[2];
			logger.debug("reverse: "+reverse);
			String value =dto.getEmailServerDomain();
			String content = arr[3]+" PTR "+value;
			String reverseFileName = "rev."+forward;
			String fqdn = reverse+".in-addr.arpa";
			String fileName = ZoneFileWriter.winDir+ZoneFileWriter.zoneFileLocation+"/"+ZoneFileWriter.reverseDIR+"/"+reverseFileName;
			File file = new File(fileName);
			FileWriter fw = null;
			boolean writeReverseNamedFile = false;
			
			if(file.exists()) {
				//remove duplicate
				removeEntry(value,fileName);
				fw = new FileWriter(file,true);					
				fw.write(content);
				fw.write("\n");				
			}
			else {
				fw = new FileWriter(file,false);
				String header = REVERSE_ZONE_FILE_CONSTANT_PORTION.replace("VAR_FQDN", fqdn);
				header = header.replaceAll("VAR_DNS_1", primaryDNS);
				header = header.replaceAll("VAR_DNS_2", secondaryDNS);					
				header = header.replaceAll("VAR_SERIAL_NUMBER", getSerialNumber(System.currentTimeMillis()));
				header = header.replaceAll("VAR_DEFAULT_DNS_1", primaryDNS);
				header = header.replaceAll("VAR_DEFAULT_DNS_2", secondaryDNS);
				header +="$ORIGIN  "+fqdn+".";
				fw.write(header);
				fw.write("\n");
				fw.write(content);
				fw.write("\n");
				writeReverseNamedFile = true;
			}
			fw.flush();
			fw.close();
			
			if(writeReverseNamedFile) {
				writeIntoNamedFile(fqdn,ZoneFileWriter.reverseDIR+"/"+reverseFileName,new FileWriter(new File(ZoneFileWriter.winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName),true));
			}
			
		}
		catch(Exception e) {
			logger.fatal(e.toString());
		}
	}
	
	public  boolean processPTRData(LinkedHashMap<Long, DnsHostingPTRDTO> data,String ids) {
		boolean status = false;
		for(DnsHostingPTRDTO dto:data.values() ) {
			try {
				if(!dto.getEmailServerDomain().endsWith(".")) {
					dto.setEmailServerDomain(dto.getEmailServerDomain()+".");
				}
				//1=add,2=edit,3=delete
				if(dto.getZoneFileUpdateStatus()==1 || dto.getZoneFileUpdateStatus()==2) {
					createReverseDNSFile(dto,"","");
				}
				else if(dto.getZoneFileUpdateStatus()==3) {
					String[] arr = dto.getIpAddress().split(Pattern.quote("."));
					String reverse = arr[2]+"."+arr[1]+"."+arr[0];
					String forward = arr[0]+"."+arr[1]+"."+arr[2];
					logger.debug("reverse: "+reverse);
					String value =dto.getEmailServerDomain();					
					String reverseFileName = "rev."+forward;					
					String fileName = ZoneFileWriter.winDir+ZoneFileWriter.zoneFileLocation+"/"+ZoneFileWriter.reverseDIR+"/"+reverseFileName;
					 
					removeEntry(value,fileName);
				}
				
				status = true;
				Thread.sleep(100);
			}
			catch(Exception e) {
				logger.fatal(e.toString());
			}
		}
		
		return status;
	}
	
	public  boolean processData(LinkedHashMap<Long, DnsHostingInfoDTO> data,String ids) {
		boolean status = false;
		for(DnsHostingInfoDTO dto:data.values() ) {
			
			try {
				
				dto.setDomainName(java.net.IDN.toASCII(dto.getDomainName()));
				String domainName = dto.getDomainName();
				String fileName = domainName;//"db."+dto.getDomainName();				
				String fileDIR = "";
				
				if(domainName.endsWith(".bd")) {
					for(String sld: dotBDSlds){
						sld = sld+".bd";
						if(domainName.contains(sld)) {
							fileDIR = sld;
							break;
						}else {
							fileDIR = "bd";							
						}
						
					}					
				}else if(domainName.endsWith(".বাংলা")){
					fileDIR = ZoneFileWriter.zoneFileDIRDOTBANGLA;
				}else {
					fileDIR = ZoneFileWriter.zoneFileDIROTHER;
				}
				
				String filePath = ZoneFileWriter.winDir+ZoneFileWriter.zoneFileLocation+"/"+fileDIR+"/"+fileName;
                if(dto.getZoneFileUpdateStatus()==1) {
                	
    				if(dto.getIsFirstWrite()==1) {
    					File file = new File(filePath);
    					if(file.exists()) {
    						logger.debug("Zone File already exists...");
    					}
    					else {
    						String namedFile = ZoneFileWriter.winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName;
        					//deleteContent(namedFile,domainName);
        					//Thread.sleep(200);
        					writeIntoNamedFile(domainName,fileDIR+"/"+fileName,new FileWriter(new File(namedFile),true));
        					Thread.sleep(100);
        					if(dto.getEmail()!=null&&dto.getEmail().length()>0) {
        						EmailValidator ob = new EmailValidator();
        						if(ob.validateEmail(dto.getEmail())) {
        							sendEMailToClient(dto);
        							Thread.sleep(100);
        						}else {
        							logger.debug("Email notificaiton not sent due to Invalid Email: "+dto.getEmail());
        						}
        						
        					}
    					}
    				}
    				
    				createZoneFile(dto,new FileWriter(new File(filePath)));
                	Thread.sleep(100);
    				
				}else if(dto.getZoneFileUpdateStatus()==2){
					createParkedZoneFile(dto,new FileWriter(new File(filePath)));
				}
				else if(dto.getZoneFileUpdateStatus()==3){
					deleteZoneFileEntry(dto,filePath);
				}
				
				
				status = true;
				Thread.sleep(100);
				
			}catch(Exception e) {
				logger.fatal(e.toString());
			}
			
		}		
		logger.debug("Zone File written successfully.");
		
		return status;
	}
	
	
	public void runUnixCommand(String... command){
		
		try{
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String termialOutputLine;
			while ((termialOutputLine = stdInput.readLine()) != null) {
				logger.debug(termialOutputLine);
			}
		}catch(Exception ex){
			logger.fatal("fatal",ex);
		}
	}
	
	public void sendEMailToClient(DnsHostingInfoDTO dto) {
		try {
			
			StringBuilder sb = new StringBuilder();
			sb.append("Dear Customer,<br>");
			sb.append("Congratulations!!!<br>");
			sb.append("Your DNS Hosting service activated successfully for the domain: "+dto.getDomainName());
			sb.append("<br><br>");
			sb.append("Regards,<br>");
			sb.append("DNS hosting Automation Service.");
			
			String msgText = sb.toString();
			String mailBody = new String(msgText.getBytes(),"UTF-8");
			SmsMailLogDAO log = new SmsMailLogDAO(
					ApplicationConstants.EMAIL_CONSTANT.MSG_TYPE_EMAIL,
					dto.getEmail(), 
					ApplicationConstants.EMAIL_CONSTANT.FROM, 
					ApplicationConstants.EMAIL_CONSTANT.SUBJECT,
					mailBody, 
					"");
			log.run();
		}
		catch(Exception e) {
			 logger.fatal("Error : "+e);
		  
		}
		
	}
			
	
	

}
