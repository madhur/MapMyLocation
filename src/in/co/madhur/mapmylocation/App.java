package in.co.madhur.mapmylocation;

import java.util.Collection;

import org.acra.annotation.ReportsCrashes;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import com.facebook.model.GraphUser;

import android.app.Application;

@ReportsCrashes(formKey = "", // This is required for backward compatibility but
								// not used

formUri = "http://www.bugsense.com/api/acra?api_key=89160b74" ,  mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class App extends Application
{

	public static final String TAG = "Hermes";
	public static final String LOG = "mapmylocation.log";

	public static final boolean DEBUG = BuildConfig.DEBUG;
	public static final boolean LOCAL_LOGV = DEBUG;

	private Collection<GraphUser> selectedUsers;
 
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		
	//	ACRA.init(this);
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
