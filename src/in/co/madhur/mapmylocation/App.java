package in.co.madhur.mapmylocation;

import java.util.Collection;

import com.facebook.model.GraphUser;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import com.crittercism.app.Crittercism;

public class App extends Application
{

	public static final String TAG = "Hermes";
	public static final String LOG = "mapmylocation.log";

	public static final boolean DEBUG = false;
	public static final boolean LOCAL_LOGV = DEBUG;

	private Collection<GraphUser> selectedUsers;
 
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate()
	{
		super.onCreate();
		if(!Build.PRODUCT.equals("sdk_x86"))
			 Crittercism.initialize(getApplicationContext(), "526e3508e432f557fe000008");
		
	}
	
	public Collection<GraphUser> getSelectedUsers()
	{
		return selectedUsers;
	}

	public void setSelectedUsers(Collection<GraphUser> selectedUsers)
	{
		this.selectedUsers = selectedUsers;
	}
}
