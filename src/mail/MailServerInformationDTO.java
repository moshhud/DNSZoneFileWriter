package mail;

public class MailServerInformationDTO {
	public String mailServertxt;
	public String additionToAddresstxt;
	public String fromAddresstxt;
	public String mailServerPorttxt;
	public boolean authFromServerChk;
	public String authEmailAddesstxt;
	public String authEmailPasstxt;
	public boolean tlsRequired;
	public boolean isActive;
	public String getMailServertxt() {
		return mailServertxt;
	}
	public void setMailServertxt(String mailServertxt) {
		this.mailServertxt = mailServertxt;
	}
	public String getAdditionToAddresstxt() {
		return additionToAddresstxt;
	}
	public void setAdditionToAddresstxt(String additionToAddresstxt) {
		this.additionToAddresstxt = additionToAddresstxt;
	}
	public String getFromAddresstxt() {
		return fromAddresstxt;
	}
	public void setFromAddresstxt(String fromAddresstxt) {
		this.fromAddresstxt = fromAddresstxt;
	}
	public String getMailServerPorttxt() {
		return mailServerPorttxt;
	}
	public void setMailServerPorttxt(String mailServerPorttxt) {
		this.mailServerPorttxt = mailServerPorttxt;
	}
	public boolean isAuthFromServerChk() {
		return authFromServerChk;
	}
	public void setAuthFromServerChk(boolean authFromServerChk) {
		this.authFromServerChk = authFromServerChk;
	}
	public String getAuthEmailAddesstxt() {
		return authEmailAddesstxt;
	}
	public void setAuthEmailAddesstxt(String authEmailAddesstxt) {
		this.authEmailAddesstxt = authEmailAddesstxt;
	}
	public String getAuthEmailPasstxt() {
		return authEmailPasstxt;
	}
	public void setAuthEmailPasstxt(String authEmailPasstxt) {
		this.authEmailPasstxt = authEmailPasstxt;
	}
	public boolean isTlsRequired() {
		return tlsRequired;
	}
	public void setTlsRequired(boolean tlsRequired) {
		this.tlsRequired = tlsRequired;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
	
}
