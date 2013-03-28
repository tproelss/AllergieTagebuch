package de.thoca.allergieTagebuch;

import android.content.Context;

public class Wochentag
{
	public static String getTextForNumber(Context c, int number)
	{		
		String retval = "";
		
		switch (number)
		{
			case 1: 
				retval = c.getString(R.string.tag_so); break;
			case 2: 
				retval = c.getString(R.string.tag_mo); break;
			case 3: 
				retval = c.getString(R.string.tag_di); break;
			case 4: 
				retval = c.getString(R.string.tag_mi); break;
			case 5: 
				retval = c.getString(R.string.tag_do); break;
			case 6: 
				retval = c.getString(R.string.tag_fr); break;
			case 7: 
				retval = c.getString(R.string.tag_sa); break;			
		}		
		
		return retval;
	}

}
