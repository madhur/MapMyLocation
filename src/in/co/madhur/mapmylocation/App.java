package in.co.madhur.mapmylocation;

import java.util.Collection;

import com.facebook.model.GraphUser;

import android.app.Application;

public class App extends Application
{
	
	public static final String TAG="MapMyLocation";
	public static final String LOG="mapmylocation.log";
	
	public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final boolean LOCAL_LOGV = DEBUG;
    
    private Collection<GraphUser> selectedUsers;

    public Collection<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(Collection<GraphUser> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }
}
