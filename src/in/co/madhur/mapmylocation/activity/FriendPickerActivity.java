/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.co.madhur.mapmylocation.activity;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import com.facebook.AppEventsLogger;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.Session;

import in.co.madhur.mapmylocation.App;
import in.co.madhur.mapmylocation.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class FriendPickerActivity extends ActionBarActivity
{
	private static final int PICK_FRIENDS_ACTIVITY = 1;
	// private Button pickFriendsButton;
	// private TextView resultsTextView;
	private UiLifecycleHelper lifecycleHelper;
	boolean pickFriendsWhenSessionOpened;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		listView = (ListView) findViewById(R.id.listview);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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
		// TODO Auto-generated method stub

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

				return true;

			case R.id.action_done:

				return true;

			default:
				return super.onOptionsItemSelected(item);

		}

	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// Update the display every time we are started.
		displaySelectedFriends(RESULT_OK);
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

	private void deleteSelectedRows()
	{

		SparseBooleanArray checked = listView.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++)
		{
			if (checked.valueAt(i) == true)
			{
				Tag tag = (Tag) listView.getItemAtPosition(checked.keyAt(i));
			}
		}
	}

	private void displaySelectedFriends(int resultCode)
	{
		String results = "";
		App application = (App) getApplication();
		ListView listView = (ListView) findViewById(R.id.listview);
		Collection<GraphUser> selection = application.getSelectedUsers();
		if (selection != null && selection.size() > 0)
		{
			HashMap<String, String> names = new HashMap<String, String>();
			// ArrayList<String> names = new ArrayList<String>();
			for (GraphUser user : selection)
			{
				// names.add(user.getName());
				names.put(user.getId(), user.getName());
				Log.v(App.TAG, user.getId());
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
		startPickFriendsActivity();
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
