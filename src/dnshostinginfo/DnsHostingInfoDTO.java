package dnshostinginfo;

import java.util.LinkedHashMap;

public class DnsHostingInfoDTO{
	
	long ID;
	long clientID;
	String domainName;
	String primaryDNS;
	String secondaryDNS;
	String tertiaryDNS;
	String email;
	long packageID;
	long expiryDate;
	long activationDate;
	long lastModificationTime;
	int isBlocked;
	int isPrivileged;
	int status;
	long domainID;
	int zoneFileUpdateStatus;
	LinkedHashMap<Long, DnsHostingZoneRecordDTO> zoneRecordDTOMap;	
	int isFirstWrite;	
	long nextPackageID;
	int tldType;
	int zoneFileForSlave;
			
	public int getZoneFileForSlave() {
		return zoneFileForSlave;
	}
	public void setZoneFileForSlave(int zoneFileForSlave) {
		this.zoneFileForSlave = zoneFileForSlave;
	}
	public String getTertiaryDNS() {
		return tertiaryDNS;
	}
	public void setTertiaryDNS(String tertiaryDNS) {
		this.tertiaryDNS = tertiaryDNS;
	}
	public long getID() {
		return ID;
	}
	public void setID(long iD) {
		ID = iD;
	}
	public long getClientID() {
		return clientID;
	}
	public void setClientID(long clientID) {
		this.clientID = clientID;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getPrimaryDNS() {
		return primaryDNS;
	}
	public void setPrimaryDNS(String primaryDNS) {
		this.primaryDNS = primaryDNS;
	}
	public String getSecondaryDNS() {
		return secondaryDNS;
	}
	public void setSecondaryDNS(String secondaryDNS) {
		this.secondaryDNS = secondaryDNS;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public long getPackageID() {
		return packageID;
	}
	public void setPackageID(long packageID) {
		this.packageID = packageID;
	}
	public long getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(long expiryDate) {
		this.expiryDate = expiryDate;
	}
	public long getActivationDate() {
		return activationDate;
	}
	public void setActivationDate(long activationDate) {
		this.activationDate = activationDate;
	}
	
	public long getLastModificationTime() {
		return lastModificationTime;
	}
	public void setLastModificationTime(long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}
	public int getIsBlocked() {
		return isBlocked;
	}
	public void setIsBlocked(int isBlocked) {
		this.isBlocked = isBlocked;
	}
	public int getIsPrivileged() {
		return isPrivileged;
	}
	public void setIsPrivileged(int isPrivileged) {
		this.isPrivileged = isPrivileged;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public long getDomainID() {
		return domainID;
	}
	public void setDomainID(long domainID) {
		this.domainID = domainID;
	}
	public int getZoneFileUpdateStatus() {
		return zoneFileUpdateStatus;
	}
	public void setZoneFileUpdateStatus(int zoneFileUpdateStatus) {
		this.zoneFileUpdateStatus = zoneFileUpdateStatus;
	}
	public LinkedHashMap<Long, DnsHostingZoneRecordDTO> getZoneRecordDTOMap() {
		return zoneRecordDTOMap;
	}
	public void setZoneRecordDTOMap(LinkedHashMap<Long, DnsHostingZoneRecordDTO> zoneRecordDTOMap) {
		this.zoneRecordDTOMap = zoneRecordDTOMap;
	}
	public int getIsFirstWrite() {
		return isFirstWrite;
	}
	public void setIsFirstWrite(int isFirstWrite) {
		this.isFirstWrite = isFirstWrite;
	}
	public long getNextPackageID() {
		return nextPackageID;
	}
	public void setNextPackageID(long nextPackageID) {
		this.nextPackageID = nextPackageID;
	}
	public int getTldType() {
		return tldType;
	}
	public void setTldType(int tldType) {
		this.tldType = tldType;
	}	
	
	
	
	

}
