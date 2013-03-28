package de.thoca.allergieTagebuch.data;

import android.content.Context;
import android.database.sqlite.*;

public class TagebuchDbOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATENBANK_NAME = "tagebuch.db";
	private static final int DATENBANK_VERSION = 1;
	
	public TagebuchDbOpenHelper(Context context) {
		super(context, DATENBANK_NAME, null, DATENBANK_VERSION); 
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TblTagebuch.SQL_CREATE);
		db.execSQL(TblTagebuch.SQL_CREATE_INDEX_DATUM);
		db.execSQL(TblTags.SQL_CREATE);
		db.execSQL(TblT2T.SQL_CREATE);
		db.execSQL(TblT2T.SQL_CREATE_INDEX_TO_TAGEBUCH);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}	
}
