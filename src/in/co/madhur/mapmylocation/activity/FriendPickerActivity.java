

package in.co.madhur.mapmylocation.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.Session;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.R;
import in.co.madhur.mapmylocation.preferences.Preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.json.JSONException;

public class FriendPickerActivity extends ActionBarActivity
{
	private static final int PICK_FRIENDS_ACTIVITY = 1;
	// private Button pickFriendsButton;
	// private TextView resultsTextView;
//	private UiLifecycleHelper lifecycleHelper;
	boolean pickFriendsWhenSessionOpened;
	private ListView listView;
	HashMap<String, String> names;
	private Preferences appPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		listView = (ListView) findViewById(R.id.listview);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		appPreferences=new Preferences(this);
		// resultsTextView = (TextView) findViewById(R.id.resultsTextView);
		// pickFriendsButton = (Button) findViewById(R.id.pickFriendsButton);
		// pickFriendsButton.setOnClickListener(new View.OnClickListener()
		// {
		// public void onClick(View view)
		// {
		// onClickPickFriends();
		// }
		// });

		// lifecycleHelper = new UiLifecycleHelper(this,
		// new Session.StatusCallback()
		// {
		// @Override
		// public void call(Session session, SessionState state,
		// Exception exception)
		// {
		// onSessionStateChanged(session, state, exception);
		// }
		// });
		// lifecycleHelper.onCreate(savedInstanceState);

		ensureOpenSession();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		getMenuInflater().inflate(R.menu.fb_pickfriends, (android.view.Menu) menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_addfriend:

				onClickPickFriends();
				return true;

			case R.id.action_removefriend:
				deleteSelectedRows();
				return true;

			case R.id.action_done:
				if(saveFriends())
					finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);

		}

	}

	private boolean saveFriends()
	{
		int countItems=listView.getCount();
		if(countItems==0)
		{
			new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.dialog_zerofriends).setNeutralButton(android.R.string.ok, null).create().show();
			return false;
		}
		
		
		HashMapAdapter hashMapAdapter=(HashMapAdapter) listView.getAdapter();
		try
		{
			appPreferences.setCustomFriends(hashMapAdapter.items);
		}
		catch (JsonProcessingException e)
		{
			Log.e(App.TAG, e.getMessage());
			
			e.printStackTrace();
		}
		
		return true;
	}
	
	

	@Override
	protected void onStart()
	{
		super.onStart();

		// Update the display every time we are started.
		// displaySelectedFriends(RESULT_OK);
		
		ListAdapter adapter = listView.getAdapter();
		if(adapter==null)
		{
			displayStoredFriends();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Call the 'activateApp' method to log an app event for use in
		// analytics and advertising reporting. Do so in
		// the onResume methods of the primary Activities that an app may be
		// launched into.
		// AppEventsLogger.activateApp(this);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.v(App.TAG, "onActivityResult");
		
		switch (requestCode)
		{
			case PICK_FRIENDS_ACTIVITY:
				displaySelectedFriends(resultCode);
				break;
			default:
				Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
				break;
		}
	}

	private boolean ensureOpenSession()
	{
		Session fbSession = Session.openActiveSessionFromCache(this);

		if (fbSession == null)
			fbSession = Session.getActiveSession();

		if (fbSession != null && fbSession.isOpened())
			return true;

		return false;
	}

	// private void onSessionStateChanged(Session session, SessionState state,
	// Exception exception)
	// {
	// if (pickFriendsWhenSessionOpened && state.isOpened())
	// {
	// pickFriendsWhenSessionOpened = false;
	//
	// startPickFriendsActivity();
	// }
	// }

	private void displayStoredFriends()
	{
		Log.v(App.TAG, "displayStoredFriends");
		HashMap<String, String> friendsMap = null;
		
		try
		{
			friendsMap=appPreferences.getCustomFriends();
		}
		catch (JsonParseException e)
		{
			Log.e(App.TAG, e.getMessage());
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			Log.e(App.TAG, e.getMessage());
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			Log.e(App.TAG, e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			Log.e(App.TAG, e.getMessage());
			e.printStackTrace();
		}
		
		if(friendsMap!=null)
		{
			HashMapAdapter hashMapAdapter = new HashMapAdapter(this, friendsMap);

			listView.setAdapter(hashMapAdapter);
			
			hashMapAdapter.items=friendsMap;
			
			hashMapAdapter.notifyDataSetChanged();
			hashMapAdapter.notifyDataSetInvalidated();
			
		}
		
	}
	
	
	private void deleteSelectedRows()
	{
		Log.v(App.TAG, "deleteSelectedRows");

		SparseBooleanArray checked = listView.getCheckedItemPositions();
		HashMapAdapter hashMapAdapter=(HashMapAdapter) listView.getAdapter();
		//for (int i = 0; i < checked.size(); i++)
		for(int i=checked.size()-1; i>-1; i--)
		{
			if (checked.valueAt(i) == true)
			{
				String userName =  (String) listView.getItemAtPosition(checked.keyAt(i));
				hashMapAdapter.remove(userName);
			}
		}
		
		listView.clearChoices();
	}

	private void displaySelectedFriends(int resultCode)
	{
		Log.v(App.TAG, "displaySelectedFriends");
		
		App application = (App) getApplication();
		ListView listView = (ListView) findViewById(R.id.listview);
		Collection<GraphUser> selection = application.getSelectedUsers();
		if (selection != null && selection.size() > 0)
		{
			names = new HashMap<String, String>();
			// ArrayList<String> names = new ArrayList<String>();
			for (GraphUser user : selection)
			{
				// names.add(user.getName());
				names.put(user.getId(), user.getName());
			}
			// results = TextUtils.join(", ", names);

			// ArrayAdapter<String> stringAdapter=new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_multiple_choice, names);
			HashMapAdapter hashMapAdapter = new HashMapAdapter(this, names);

			listView.setAdapter(hashMapAdapter);

		}
		// else
		// {
		// results = "<No friends selected>";
		// }

		// resultsTextView.setText(results);
	}

	private void onClickPickFriends()
	{
		boolean fbConnected=appPreferences.isFBConnected();
		if(fbConnected)
		{
			startPickFriendsActivity();
		}
		else
		{
			
			Toast.makeText(this, getString(R.string.noti_fb_not_connected), Toast.LENGTH_SHORT).show();
		}
	}

	private void startPickFriendsActivity()
	{
		if (ensureOpenSession())
		{
			App application = (App) getApplication();
			application.setSelectedUsers(null);

			Intent intent = new Intent(this, PickFriendsActivity.class);
			// Note: The following line is optional, as multi-select behavior is
			// the default for
			// FriendPickerFragment. It is here to demonstrate how parameters
			// could be passed to the
			// friend picker if single-select functionality was desired, or if a
			// different user ID was
			// desired (for instance, to see friends of a friend).
			PickFriendsActivity.populateParameters(intent, null, true, true);
			startActivityForResult(intent, PICK_FRIENDS_ACTIVITY);
		}
		else
		{
			pickFriendsWhenSessionOpened = true;
		}
	}
}
