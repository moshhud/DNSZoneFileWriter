package connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import databasemanager.DatabaseManager;

public class DatabaseConnection {

	private Logger logger = Logger.getLogger(getClass());

	private Connection cn = null;
	private ArrayList<Statement> stmtList = new ArrayList<Statement>();
	private ArrayList<PreparedStatement> psList = new ArrayList<PreparedStatement>();
	private List<String> tableNameList = new ArrayList<String>();
	private List<Long> lastModificationTimeList = new ArrayList<Long>();
	private Map<String,Long> mapOfNextSequenceIDToTableName = new HashMap<>();
	
	
	public void addSequencerSql(String tableName,long lastModificationTime){
		tableNameList.add(tableName);
		lastModificationTimeList.add(lastModificationTime);
	}
	
	public Savepoint getSavePoint() throws Exception{
		return cn.setSavepoint();
	}
	
	public void rollBackSavePoint(Savepoint savepoint) throws Exception{
		cn.rollback(savepoint);
	}
	
	private void runSequencerSQLs() throws Exception{
		String sql = "update vbSequencer set table_LastModificationTime = ? where table_name = ?";
		PreparedStatement ps = cn.prepareStatement(sql);
		for(int i=0;i<tableNameList.size();i++){
			ps.setObject(2, tableNameList.get(i));
			ps.setObject(1, lastModificationTimeList.get(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}
	
	public void printConnection(){
		logger.debug(cn);
	}
	public void dbOpen() throws Exception {
		cn = DatabaseManager.getInstance().getConnection();
	}
	public Statement getNewStatement() throws SQLException
	{
		Statement stmt = cn.createStatement();
		stmtList.add(stmt);
		return stmt; 
	}
	public PreparedStatement getNewPrepareStatement(String sql) throws Exception{
		PreparedStatement ps = cn.prepareStatement(sql);
		psList.add(ps);
		return ps;
	}
	public void closeStatements()
	{
		try{
			for(int i = 0; i < stmtList.size(); i++)
			{
				Statement stmt = stmtList.get(i);
				if(stmt != null && !stmt.isClosed())stmt.close();
			}
		}
		catch(Exception ex){}
		try{
			for(int i = 0; i < psList.size(); i++)
			{
				PreparedStatement ps = psList.get(i);
				if(ps != null && !ps.isClosed())ps.close();
			}
		}
		catch(Exception ex){}
	}
	
	public void dbClose(){
		closeStatements();
		if (cn != null) {
			try{
				DatabaseManager.getInstance().freeConnection(cn);
			}catch(Exception ex){}

		}
	}
	@SuppressWarnings("unused")
	private void deallocateMemory(){
		stmtList.clear();
		psList.clear();
		mapOfNextSequenceIDToTableName.clear();
	}
	public boolean hasStartedTransaction() throws Exception{
		return cn.getAutoCommit() == false;
	}


	public void dbTransationStart() throws Exception { 
		if(!hasStartedTransaction()){
			cn.setAutoCommit(false);
		}
	}

	public void dbTransationRollBack() throws Exception {
		cn.rollback();
		cn.setAutoCommit(true);
	}


	public void dbTransationEnd() throws Exception { 		
		if(hasStartedTransaction()){
			runSequencerSQLs();
			cn.commit();
			cn.setAutoCommit(true);
		}
	}

	public long getNextID(String tableName) throws Exception {
		long id = DatabaseManager.getInstance().getNextSequenceId(tableName);
		if(mapOfNextSequenceIDToTableName.containsKey(tableName)){
			id = mapOfNextSequenceIDToTableName.get(tableName);
			mapOfNextSequenceIDToTableName.remove(tableName);
		}else{
			id = DatabaseManager.getInstance().getNextSequenceId(tableName);
		}
		return id;

	}
	
	public long getNextIDWithoutIncrementing(String tableName) throws Exception{
		if(!mapOfNextSequenceIDToTableName.containsKey(tableName)){
			long nextID = DatabaseManager.getInstance().getNextSequenceId(tableName);
			mapOfNextSequenceIDToTableName.put(tableName, nextID);
		}
		return getNextID(tableName);
	}
}
