package common;

public class ApplicationConstants {
	
	public static class EMAIL_CONSTANT
	{
		public static final String FROM = "noreply@btcl.com.bd";
		public static final String TO ="moshhud@revesoft.com";
		public static final String CC ="moshhud@revesoft.com";
		public static final String SUBJECT ="DNS Hosting Service notification";		
		public static final String MSG_TYPE_EMAIL = "email";
		public static final String MSG_TYPE_SMS = "sms";
	}
	
	public static class CPANEL_ACCOUNT
	{
		public static final int CREATE_ACCOUNT = 1;
		public static final int SUSPEND_ACCOUNT = 2;
		public static final int UNSUSPEND_ACCOUNT = 3;
		public static final int PACKAGE_CHANGE = 4;
		public static final int RESELLER_SETUP = 5;
		public static final int ACL_SETUP = 6;
	}

	public static class CPANEL_PACKAGE
	{
		public static final int ADD = 1;
		public static final int EDIT = 2;
		public static final int DELETE = 3;
	}
	
	public static class CPANEL_WHM_PORT
	{
		public static final int WHM_ADMIN = 2087;
		public static final int WHM_RESELLER = 2083;
		public static final int WHM_ENDUSER_1 = 2095;
		public static final int WHM_ENDUSER_2 = 2096;
		
	}
}
