package in.co.madhur.mapmylocation.activity;

import in.co.madhur.mapmylocation.App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class HashMapAdapter extends BaseAdapter
{
	public HashMap<String, String> items;
	private String[] mValues;
	private Context context;

	public HashMapAdapter(Context context, HashMap<String, String> items)
	{
		
		this.items = items;
		this.mValues = items.values().toArray(new String[items.size()]);
		this.context = context;

	}

	@Override
	public int getCount()
	{
		return items.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mValues[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public void remove(String userNameRemoved)
	{
		
		String userIdremoved = null;
		for (Map.Entry<String, String> e : items.entrySet())
		{
			String userId = e.getKey();
			String userName = e.getValue();

			if (userNameRemoved.equals(userName))
				userIdremoved = userId;
		}

		if (userIdremoved != null)
		{
			String val=items.remove(userIdremoved);
			Log.v(App.TAG, val);
			this.mValues = items.values().toArray(new String[items.size()]);
			
			this.notifyDataSetChanged();
			this.notifyDataSetInvalidated();
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		
		
		String userName = (String) getItem(position);

		View view = GetInflater(context).inflate(android.R.layout.simple_list_item_multiple_choice, null);

			CheckedTextView textView = (CheckedTextView) view.findViewById(android.R.id.text1);

			textView.setText(userName);

		return view;
	}

	private static LayoutInflater GetInflater(Context context)
	{

		return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

}
