package dnshostinginfo;


public class DnsHostingZoneRecordDTO{

	long ID;
	long dnsHosingInfoID;
	String recordType;
	String recordName;
	String recordClass;
	String recordValue;
	String recordLastValue;
	int mxPriority;
	int isReadOnly;
	int ttl;
	long lastModificationTime;
	long clientID;
	
	
	
	public long getClientID() {
		return clientID;
	}
	public void setClientID(long clientID) {
		this.clientID = clientID;
	}
	public long getID() {
		return ID;
	}
	public void setID(long iD) {
		ID = iD;
	}
	public long getDnsHosingInfoID() {
		return dnsHosingInfoID;
	}
	public void setDnsHosingInfoID(long dnsHosingInfoID) {
		this.dnsHosingInfoID = dnsHosingInfoID;
	}
	public String getRecordType() {
		return recordType;
	}
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}
	public String getRecordName() {
		return recordName;
	}
	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}
	public String getRecordClass() {
		return recordClass;
	}
	public void setRecordClass(String recordClass) {
		this.recordClass = recordClass;
	}
	public String getRecordValue() {
		return recordValue;
	}
	public void setRecordValue(String recordValue) {
		this.recordValue = recordValue;
	}
	public String getRecordLastValue() {
		return recordLastValue;
	}
	public void setRecordLastValue(String recordLastValue) {
		this.recordLastValue = recordLastValue;
	}
	public int getMxPriority() {
		return mxPriority;
	}
	public void setMxPriority(int mxPriority) {
		this.mxPriority = mxPriority;
	}
	public int getIsReadOnly() {
		return isReadOnly;
	}
	public void setIsReadOnly(int isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public long getLastModificationTime() {
		return lastModificationTime;
	}
	public void setLastModificationTime(long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}
	

}
