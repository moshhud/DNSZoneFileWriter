package dnshostinginfo;

import org.apache.log4j.Logger;

import com.mysql.jdbc.PreparedStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import databasemanager.DatabaseManager;
import util.ReturnObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;



public class DnsHostingInfoDAO {
	
	Logger logger = Logger.getLogger(DnsHostingInfoDAO.class);
	ReturnObject ro = new ReturnObject();
	
	@SuppressWarnings({ "null" })
	public ReturnObject getIDList(String tableName,String colName,String condition) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = null;		
		List<Long> IDList = null;	
		try {
			connection = DatabaseManager.getInstance().getConnection();
			IDList = new ArrayList<Long>();
			if(condition==null&&condition.isEmpty()) {
				condition="";
			}
			
			sql = "select "+colName+" from "+tableName+" where 1=1 "+condition;
			//logger.debug(sql);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				IDList.add(rs.getLong(colName));
			}
			
			rs.close();
			stmt.close();
			
			if(IDList.size()>0) {
				ro.setData(IDList);
				ro.setIsSuccessful(true);
			}
			
		}catch(Exception ex){
			logger.debug("fatal",ex);
		}finally{
			try{
				DatabaseManager.getInstance().freeConnection(connection);
				
			}catch(Exception exx){}
		}
		return ro;
	}
	
	public ReturnObject updateStatus(String ids,String tableName) {
		Connection connection = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		try {
			sql = "UPDATE "+tableName +" set dnsZoneFileUpdateStatus=?,dnsIsFirstWrite=? where dnsID in("+ids+")";
			connection = DatabaseManager.getInstance().getConnection();
			pstmt = (PreparedStatement) connection.prepareStatement(sql);
			int i=1;
			pstmt.setInt(i++, 0);
			pstmt.setInt(i++, 0);
			//pstmt.setString(i++, ids);
			
			if (pstmt.executeUpdate() > 0) {
				ro.clear();
				ro.setIsSuccessful(true);
			}
			
		}catch (Exception e) {
			logger.fatal("Error : "+e);
		}finally {			
			try{
				DatabaseManager.getInstance().freeConnection(connection);
				
			}catch(Exception exx){}
		}
		
		return ro;
	}
	
	
	@SuppressWarnings("null")
	public ReturnObject getDNSHostingInfoMap(String tableName,String condition) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = null;		
		
		LinkedHashMap<Long, DnsHostingInfoDTO> data = null;
		DnsHostingInfoDTO dto = null;
	
		try {
			connection = DatabaseManager.getInstance().getConnection();			 
			if(condition==null&&condition.isEmpty()) {
				condition="";
			}
			
			sql = "select dnsID,dnsClientID,dnsFQDN,dnsPrimaryDNS"
					+ ",dnsSecondaryDNS,dnsEmail,dnsIsPrivileged"
					+ ",dnsStatus,dnsZoneFileUpdateStatus,dnsIsFirstWrite,dnsTLDType"
					+ "  from "+tableName+" where 1=1 "+condition;
			//logger.debug(sql);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			data = new LinkedHashMap<Long, DnsHostingInfoDTO>();			
			while(rs.next())
			{
				dto = new DnsHostingInfoDTO();
				dto.setID(rs.getLong("dnsID"));
				dto.setClientID(rs.getLong("dnsClientID"));
				dto.setDomainName(rs.getString("dnsFQDN"));
				dto.setPrimaryDNS(rs.getString("dnsPrimaryDNS"));
				dto.setSecondaryDNS(rs.getString("dnsSecondaryDNS"));
				dto.setEmail(rs.getString("dnsEmail"));
				dto.setIsPrivileged(rs.getInt("dnsIsPrivileged"));
				dto.setStatus(rs.getInt("dnsStatus"));
				dto.setZoneFileUpdateStatus(rs.getInt("dnsZoneFileUpdateStatus"));
				dto.setIsFirstWrite(rs.getInt("dnsIsFirstWrite"));
				dto.setTldType(rs.getInt("dnsTLDType"));
				data.put(rs.getLong("dnsID"), dto);
			}
			
			rs.close();
			stmt.close();
			
			if(data != null && data.size() > 0) {
				ro.setData(data);
				ro.setIsSuccessful(true);
			}
			
		}catch(Exception ex){
			logger.debug("fatal",ex);
		}finally{
			try{
				DatabaseManager.getInstance().freeConnection(connection);
				
			}catch(Exception exx){}
		}
		return ro;
	}
	
	@SuppressWarnings("null")
	public ReturnObject getDNSZoneRecordMap(String tableName,String condition) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = null;		
		
		LinkedHashMap<Long, DnsHostingZoneRecordDTO> data = null;
		DnsHostingZoneRecordDTO dto = null;
	
		try {
			connection = DatabaseManager.getInstance().getConnection();			 
			if(condition==null&&condition.isEmpty()) {
				condition="";
			}
			
			sql = "select dnszrID,dnszrClientID,dnszrDnsID,dnszrRecordName"
					+ ",dnszrRecordClass,dnszrRecordValue,dnszrMXPriority"
					+ ",dnszrRecordTtl,dnszrRecordType"
					+ "  from "+tableName+" where 1=1 "+condition;
			//logger.debug(sql);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			
			data = new LinkedHashMap<Long, DnsHostingZoneRecordDTO>();			
			while(rs.next())
			{
				dto = new DnsHostingZoneRecordDTO();
				dto.setID(rs.getLong("dnszrID"));				
				dto.setClientID(rs.getLong("dnszrClientID"));
				dto.setDnsHosingInfoID(rs.getLong("dnszrDnsID"));
				dto.setRecordName(rs.getString("dnszrRecordName"));
				dto.setRecordClass(rs.getString("dnszrRecordClass"));
				dto.setRecordValue(rs.getString("dnszrRecordValue"));
				dto.setMxPriority(rs.getInt("dnszrMXPriority"));
				dto.setTtl(rs.getInt("dnszrRecordTtl"));
				dto.setRecordType(rs.getString("dnszrRecordType"));
				data.put(rs.getLong("dnszrID"), dto);
			}
			
			rs.close();
			stmt.close();
			
			if(data != null && data.size() > 0) {
				ro.setData(data);
				ro.setIsSuccessful(true);
			}
			
		}catch(Exception ex){
			logger.debug("fatal",ex);
		}finally{
			try{
				DatabaseManager.getInstance().freeConnection(connection);
				
			}catch(Exception exx){}
		}
		return ro;
	}
	
	
	
	public String getStringFromArrayList(
			@SuppressWarnings("rawtypes") ArrayList vals,
			boolean useInvertedComma) {
		String data = null;
		try {
			if (vals != null && vals.size() > 0) {
				data = "";
				for (Object val : vals) {
					if (useInvertedComma) {
						data += "'" + val + "',";
					} else {
						data += val + ",";
					}
				}
				if (data != null && data.endsWith(",")) {
					data = data.substring(0, data.length() - 1);
				}
			}
		} catch (RuntimeException e) {
			logger.fatal("RuntimeException", e);
		}
		return data;
	}
	
	  
	
	

}
