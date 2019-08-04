package common;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import connection.DatabaseConnection;

public class SmsMailLogDAO extends Thread{
	
	Logger logger = Logger.getLogger(SmsMailLogDAO.class);

	private String type;
	private String sentTo;
	private String sentFrom;
	private String sentBody;
	private String sentSubject;
	private String sentCC;
	
	public SmsMailLogDAO(String type,String sentTo,String sentFrom,String subject,String sentBody, String sentCC) {
		this.type = type;
		this.sentTo = sentTo;
		this.sentFrom = sentFrom;
		this.sentBody = sentBody;
		this.sentCC = sentCC;
		this.sentSubject = subject;
		if(!this.type.equalsIgnoreCase("email") && !this.type.equalsIgnoreCase("sms")) {
			this.type = "email";
		}
		if(StringUtils.isBlank(this.sentTo)) {
			this.sentTo ="";
		}
	}
	
	public void run() {
		PreparedStatement pstmt = null;
		DatabaseConnection db = null;
		try {
			db = new DatabaseConnection();
			db.dbOpen();
			db.dbTransationStart();
			String sql = "INSERT INTO at_sms_and_mail_log (sent_type,sent_from,sent_to,sent_cc,sent_subject,sent_body,request_generated_from) VALUES(?,?,?,?,?,?,?)";
			pstmt = db.getNewPrepareStatement(sql);
			int i=1;			
			pstmt.setString(i++, this.type);
			pstmt.setString(i++, this.sentFrom);
			pstmt.setString(i++, this.sentTo);
			pstmt.setString(i++, this.sentCC);
			pstmt.setString(i++, this.sentSubject);
			pstmt.setString(i++, this.sentBody);
			pstmt.setString(i++, "webhosting jar");
			if(pstmt.executeUpdate() > 0 == false) {
				logger.fatal("Failed to insert data in sms mail log table");
			}
			db.dbTransationEnd();
		}catch(SQLException e) {
			logger.fatal("SmsMailLogDAO->SQLException",e);
		} catch (Exception e) {
			logger.fatal("SmsMailLogDAO->Exception",e);
		}finally {
			try {
				pstmt.close();
				db.dbClose();
			}catch(SQLException e) {
				logger.fatal("SmsMailLogDAO->SQLException",e);
			}
		}
	}
	
	public void updateStatus(Long id,String status) {
		PreparedStatement pstmt = null;
		DatabaseConnection db = null;
		try {
			db = new DatabaseConnection();
			db.dbOpen();
			db.dbTransationStart();
			String sql = "UPDATE at_sms_and_mail_log set status=? WHERE id=?";
			pstmt = db.getNewPrepareStatement(sql);
			int i=1;			
			pstmt.setString(i++, status);
			pstmt.setLong(i++, id);
			
			if(pstmt.executeUpdate() > 0 == false) {
				logger.fatal("Failed to update data in sms mail log table: ID: "+id);
			}
			db.dbTransationEnd();
		}catch(SQLException e) {
			logger.fatal("SmsMailLogDAO->SQLException",e);
		} catch (Exception e) {
			logger.fatal("SmsMailLogDAO->Exception",e);
		}finally {
			try {
				pstmt.close();
				db.dbClose();
			}catch(SQLException e) {
				logger.fatal("SmsMailLogDAO->SQLException",e);
			}
		}
	}
}
