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

import dnshostinginfo.DNSHostingConstant;
import dnshostinginfo.DnsHostingInfoDTO;
import dnshostinginfo.DnsHostingZoneRecordDTO;

public class RecordFileWriter {
	static Logger logger = Logger.getLogger(RecordFileWriter.class);
	String winDir = "";//"D:/root";	
	private  final String ZONE_FILE_CONSTANT_PORTION = "$TTL 86400\n"+
			"VAR_FQDN.    IN SOA 	VAR_DNS_1. VAR_DNS_2.  (\n"+
			"   VAR_SERIAL_NUMBER	; Serial\n"+
			"   1200	; Refresh \n"+
			"   3600	; Retry every hour\n"+
			"   604800	; Expire after a week\n"+
			"   86400 )	; Minimum ttl of 1 day\n"+
			" \t\t\t\tIN	NS	"+DNSHostingConstant.DNS_HOSTING_PRIMARY_DNS+". \n"+
			" \t\t\t\tIN	NS	"+DNSHostingConstant.DNS_HOSTING_SECONDARY_DNS+". \n";
	
	private  final String REVERSE_ZONE_FILE_CONSTANT_PORTION = "$TTL 86400\n"+
			"VAR_FQDN.    IN SOA 	VAR_DNS_1. VAR_DNS_2.  (\n"+
			"   VAR_SERIAL_NUMBER	; Serial\n"+
			"   1200	; Refresh \n"+
			"   3600	; Retry every hour\n"+
			"   604800	; Expire after a week\n"+
			"   86400 )	; Minimum ttl of 1 day\n"+
			" \t\t\t\tIN	NS	"+DNSHostingConstant.DNS_HOSTING_PRIMARY_DNS_REVERSE+". \n"+
			" \t\t\t\tIN	NS	"+DNSHostingConstant.DNS_HOSTING_SECONDARY_DNS_REVERSE+". \n";
	
	private  final String NAMED_FILE_CONTENT ="zone \"VAR_DOMAIN\" IN {\n"
			+ "  type master;\n"
			+ "  file \"VAR_ZONE_FILE_NAME\";\n"
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
			String header = ZONE_FILE_CONSTANT_PORTION.replace("VAR_FQDN", dto.getDomainName());
			header = header.replaceAll("VAR_DNS_1", dto.getPrimaryDNS());
			header = header.replaceAll("VAR_DNS_2", dto.getSecondaryDNS());
			//header = header.replaceAll("VAR_EMAIL", dto.getEmail());
			header = header.replaceAll("VAR_SERIAL_NUMBER", getSerialNumber(System.currentTimeMillis()));
			header +="$ORIGIN  "+dto.getDomainName()+".";
			writer.write(header);
			writer.write("\n");
			int maxPriority = 10;
			
			for (DnsHostingZoneRecordDTO zoneDTO : dto.getZoneRecordDTOMap().values()) {
				
				if(zoneDTO!=null) {
					StringBuffer sb = new StringBuffer();	
					
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
				createReverseDNSFile(mxData,aData);
			}
			
			
		}catch(Exception e) {
			logger.fatal(e.toString());
		}
		
	}
	
	public  void createReverseDNSFile(LinkedHashMap<String, String> mxData,LinkedHashMap<String, String> aData) {
		
		try {
			/*for(Entry<String, String> entry : mxData.entrySet()) {
				logger.debug("Key: "+entry.getKey()+", value: "+entry.getValue());
			}*/
			
			for(Entry<String, String> entry : aData.entrySet()) {
				//logger.debug("Key: "+entry.getKey()+", value: "+entry.getValue());
				String[] arr = entry.getValue().split(Pattern.quote("."));
				String reverse = arr[2]+"."+arr[1]+"."+arr[0];
				//logger.debug("reverse: "+reverse);
				String value = mxData.get(entry.getKey());;
				String content = arr[3]+" PTR "+value;
				String reverseFileName = "db."+reverse;
				String fqdn = reverse+".in-addr.arpa";
				String fileName = winDir+ZoneFileWriter.zoneFileLocation+"/"+reverseFileName;
				File file = new File(fileName);
				FileWriter fw = null;
				boolean writeReverseNamedFile = false;
				if(file.exists()) {
					removeDuplicate(value,fileName);
					fw = new FileWriter(file,true);					
					fw.write(content);
					fw.write("\n");
					
				}else {
					fw = new FileWriter(file,false);
					String header = REVERSE_ZONE_FILE_CONSTANT_PORTION.replace("VAR_FQDN", fqdn);
					header = header.replaceAll("VAR_DNS_1", DNSHostingConstant.DNS_HOSTING_PRIMARY_DNS_REVERSE);
					header = header.replaceAll("VAR_DNS_2", "root."+DNSHostingConstant.DNS_HOSTING_PRIMARY_DNS_REVERSE);					
					header = header.replaceAll("VAR_SERIAL_NUMBER", getSerialNumber(System.currentTimeMillis()));
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
					writeIntoNamedFile(fqdn,reverseFileName,new FileWriter(new File(winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName),true));
				}
			}
		}catch(Exception e) {
			logger.fatal(e.toString());
		}
		
	}
	
	public void removeDuplicate(String str,String fileName) {
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
	
	public  boolean processData(LinkedHashMap<Long, DnsHostingInfoDTO> data,String ids) {
		boolean status = false;
		for(DnsHostingInfoDTO dto:data.values() ) {
			
			try {
				
				dto.setDomainName(java.net.IDN.toASCII(dto.getDomainName()));
				String fileName = "db."+dto.getDomainName();
				createZoneFile(dto,new FileWriter(new File(winDir+ZoneFileWriter.zoneFileLocation+"/"+fileName)));
				if(dto.getIsFirstWrite()==1) {
					writeIntoNamedFile(dto.getDomainName(),fileName,new FileWriter(new File(winDir+ZoneFileWriter.namedFilePath+"/"+ZoneFileWriter.namedFileName),true));
				}
				status = true;
				
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
			
	
	

}
