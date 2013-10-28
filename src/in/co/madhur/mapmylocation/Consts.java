package in.co.madhur.mapmylocation;


public final class Consts {
	
	public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	public static final String ACCESS_EXPIRES = "ACCESS_EXPIRES";
	public static final String USER_NAME = "USER_NAME";
			
	
	
	public static String SENT="SMS_SENT";
	public static String DELIVERED="SMS_DELIVERED";
	public static String GOOGLE_MAPS_URL="http://maps.google.com/?q=%s,%s";
	public static String FB_FRIENDS_FIRE="CUSTOM";
	
	public static String FB_POST_ACTION="FB_POST_ACTION";
	
	public static String DONATE_URL="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=G22C78FDUM2VY";
	
	public static enum FBPrivacies
	{
		EVERYONE("EVERYONE"),
		FRIENDS_OF_FRIENDS("FRIENDS_OF_FRIENDS"),
		FRIENDS("FRIENDS"),
		SELF("SELF"),
		CUSTOM("CUSTOM");
		
		FBPrivacies(String key)
		{
			this.Key=key;
		}
		
		private final String Key;
		
		@Override
		public String toString()
		{
			return Key;
		}
		
	}
	
	
}
