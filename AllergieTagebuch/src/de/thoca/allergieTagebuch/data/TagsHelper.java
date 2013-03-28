package de.thoca.allergieTagebuch.data;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TagsHelper
{	
	private static TagebuchDbOpenHelper _tagebuchDbOpenHelper;
	private static ArrayList<Tag> _medTags = new ArrayList<Tag>();
	private static ArrayList<Tag> _eigeneTags = new ArrayList<Tag>();
	private static boolean _isInitialized = false;
	
	public static void Initialize(Context c)
	{
		if (_isInitialized)
			return;
		
		_tagebuchDbOpenHelper = new TagebuchDbOpenHelper(c);
		
		SQLiteDatabase db = _tagebuchDbOpenHelper.getReadableDatabase();
    	Cursor cursor = db.rawQuery(TblTags.SELECT_ALL_TAGS, null);    	
    	
    	while (cursor.moveToNext())
    	{
    		int id = cursor.getInt(0);
    		String tag = cursor.getString(1);
    		boolean isMedTag = cursor.getInt(2) == 1;
    		Tag t = new Tag(id, tag, isMedTag);
    		
    		if (t.isMedTag)
    			_medTags.add(t);
    		else 
    			_eigeneTags.add(t);    		    
    	}    	
    	cursor.close();
    	db.close();
    	
    	_isInitialized = true;
	}
	
	public static Tag GetMedTag(String tag)
	{
		String lookingFor = tag.trim();
		for (Tag t : _medTags)
		{
			if (t.tag.equalsIgnoreCase(lookingFor))
				return t;
		}		
		return null;
	}
	
	public static Tag GetEigenerTag(String tag)
	{
		String lookingFor = tag.trim();
		for (Tag t : _eigeneTags)
		{
			if (t.tag.equalsIgnoreCase(lookingFor))
				return t;
		}		
		return null;
	}
	
	public static Tag AddNewMedTag(String tagText)
	{
		Tag existingTag = GetMedTag(tagText);
		if (existingTag != null)
			return existingTag;
		
		SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
		db.execSQL(TblTags.INSERT_NEW_TAG, new Object[] { tagText, 1 });
				
		Cursor cursor = db.rawQuery(TblTags.GET_LATEST_ID, null);		
		cursor.moveToNext();
		int newId = cursor.getInt(0);
		cursor.close();
		db.close();
		
		Tag newTag = new Tag(newId, tagText, true);
		_medTags.add(newTag);
		
		return newTag;
	}
	
	public static Tag AddNewEigenenTag(String tagText)
	{
		Tag existingTag = GetEigenerTag(tagText);
		if (existingTag != null)
			return existingTag;
		
		SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
		db.execSQL(TblTags.INSERT_NEW_TAG, new Object[] { tagText, 0 });
				
		Cursor cursor = db.rawQuery(TblTags.GET_LATEST_ID, null);		
		cursor.moveToNext();
		int newId = cursor.getInt(0);
		cursor.close();
		db.close();
		
		Tag newTag = new Tag(newId, tagText, true);
		_eigeneTags.add(newTag);
		
		return newTag;
	}	
	
	public static String[] GetAllMedTags()
	{
		String[] result = new String[_medTags.size()];
		
		int i=0;
		for (Tag t : _medTags)
		{
			result[i] = t.tag;
			i++;
		}
		
		return result;
	}
	
	public static String[] GetAllEigeneTags()
	{
		String[] result = new String[_eigeneTags.size()];
		
		int i=0;
		for (Tag t : _eigeneTags)
		{
			result[i] = t.tag;
			i++;
		}
		
		return result;
	}	
	
	public static int CountMedTags()
	{
		return _medTags.size();
	}
	
	public static ArrayList<Tag> GetMedTagsForDay(long dayId)
	{
		ArrayList<Tag> result = new ArrayList<Tag>();
		
		Cursor cursor = _tagebuchDbOpenHelper.getReadableDatabase()
		.rawQuery(TblT2T.SELECT_MED_TAGS_FOR_DAY, new String[] { String.format("%d", dayId)});

		while (cursor.moveToNext())
		{
			result.add(new Tag( 
				cursor.getInt(0),  // id 
				cursor.getString(1), // tag
				(cursor.getInt(2) > 0)	// isMedTag
				));
		}
		cursor.close();
		
		return result;
	}
	
	public static ArrayList<Tag> GetEigeneTagsForDay(long dayId)
	{
		ArrayList<Tag> result = new ArrayList<Tag>();
		
		Cursor cursor = _tagebuchDbOpenHelper.getReadableDatabase()
		.rawQuery(TblT2T.SELECT_EIGENE_TAGS_FOR_DAY, new String[] { String.format("%d", dayId)});

		while (cursor.moveToNext())
		{
			result.add(new Tag( 
				cursor.getInt(0),  // id 
				cursor.getString(1), // tag
				(cursor.getInt(2) > 0)	// isMedTag
				));
		}
		cursor.close();
		
		return result;
	}	
	
	public static int GetMedTagIdForName(String name)
	{
		for (Tag t : _medTags)
		{
			if (t.tag.equalsIgnoreCase(name))
				return t.id;
		}		
		return -1;
	}
	
	public static int GetEigenenTagIdForName(String name)
	{
		for (Tag t : _eigeneTags)
		{
			if (t.tag.equalsIgnoreCase(name))
				return t.id;
		}		
		return -1;
	}
	
	public static String GetEigenenTagNameForId(int id)
	{
		for (Tag t : _eigeneTags)
		{
			if (t.id == id)
				return t.tag;
		}		
		return "";
	}
	
	public static void DeleteMedTag(String name)
	{		
		Tag medTag = GetMedTag(name);
		
		if (medTag == null)
			return;
		
		SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
		
		db.delete("tblT2T", 
				TblT2T.COL_TAG_ID + "=?", 
				new String[] { String.format("%d", medTag.id) });
		
		db.delete("tblTags", 
				TblTags.COL_ID + "=? " +
				"AND " + TblTags.COL_IS_MED_TAG + "=1", 
				new String[] { String.format("%d", medTag.id) });
		
		db.close();
		
		_medTags.remove(medTag);		
	}
	
	public static void DeleteEigenenTag(String name)
	{		
		Tag eigenerTag = GetEigenerTag(name);
		
		if (eigenerTag == null)
			return;
		
		SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
		
		db.delete("tblT2T", 
				TblT2T.COL_TAG_ID + "=?", 
				new String[] { String.format("%d", eigenerTag.id) });
		
		db.delete("tblTags", 
				TblTags.COL_ID + "=? " +
				"AND " + TblTags.COL_IS_MED_TAG + "=0", 
				new String[] { String.format("%d", eigenerTag.id) });
		
		db.close();
		
		_eigeneTags.remove(eigenerTag);		
	}	
}
