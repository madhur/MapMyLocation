package in.co.madhur.mapmylocation.exceptions;

import in.co.madhur.mapmylocation.R;



public class EmptyFriendsException extends Exception implements LocalizableException
{

	@Override
	public int errorResourceId()
	{
		// TODO Auto-generated method stub
		return R.string.noti_zerofriends;
	}

}
