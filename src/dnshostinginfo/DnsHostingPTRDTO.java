package dnshostinginfo;

public class DnsHostingPTRDTO {

	long ID;	
	String clientName;
	String description;
	String ipAddress;
	String emailServerDomain;
	int zoneFileUpdateStatus;
	int zoneFileForSlave;
	String updateDescription;
	long creationTime;
	long lastModificationTime;
	int isDeleted;
	
	public int getZoneFileForSlave() {
		return zoneFileForSlave;
	}
	public void setZoneFileForSlave(int zoneFileForSlave) {
		this.zoneFileForSlave = zoneFileForSlave;
	}
	public long getID() {
		return ID;
	}
	public void setID(long iD) {
		ID = iD;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getEmailServerDomain() {
		return emailServerDomain;
	}
	public void setEmailServerDomain(String emailServerDomain) {
		this.emailServerDomain = emailServerDomain;
	}
	public int getZoneFileUpdateStatus() {
		return zoneFileUpdateStatus;
	}
	public void setZoneFileUpdateStatus(int zoneFileUpdateStatus) {
		this.zoneFileUpdateStatus = zoneFileUpdateStatus;
	}
	public String getUpdateDescription() {
		return updateDescription;
	}
	public void setUpdateDescription(String updateDescription) {
		this.updateDescription = updateDescription;
	}
	public long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	public long getLastModificationTime() {
		return lastModificationTime;
	}
	public void setLastModificationTime(long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}
	public int getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	

}
