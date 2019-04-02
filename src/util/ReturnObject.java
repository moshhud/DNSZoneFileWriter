package util;


public class ReturnObject {

	private boolean isSuccessful;
	private Object data;
	private Exception exception;

	public ReturnObject() {
		isSuccessful = false;
		
	}

	public ReturnObject(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;

	}

	public boolean getIsSuccessful() {
		return isSuccessful;
	}

	public void setIsSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}

	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception e) {
		this.exception = e;
	}	

	public void clear() {
		isSuccessful = false;		
		data = null;
		exception = null;
	}

	public static synchronized ReturnObject clearInstance(ReturnObject ro) {
		if (ro == null) {
			ro = new ReturnObject();
		} else {
			ro.clear();
		}
		return ro;
	}

}
