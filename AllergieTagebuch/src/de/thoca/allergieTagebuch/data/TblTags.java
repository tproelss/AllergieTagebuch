package de.thoca.allergieTagebuch.data;

public class TblTags {
	
	// Tabellenspalten
	public static final String COL_ID = "_id";	
	public static final String COL_TAG = "Tag";
	public static final String COL_IS_MED_TAG = "IsMedTag";
	
	public static final String SQL_CREATE =
		"CREATE TABLE tblTags (" +
			COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +			
			COL_TAG + " TEXT NOT NULL, " + 
			COL_IS_MED_TAG + " BIT NOT NULL)";
	
	public static final String SELECT_ALL_TAGS =
		"SELECT " + 
			COL_ID + ", " +
			COL_TAG + ", " +
			COL_IS_MED_TAG + " " +
		"FROM tblTags " + 
		"ORDER BY " + COL_TAG;
	
	public static final String INSERT_NEW_TAG =
		"INSERT INTO tblTags " + 
		"(" + COL_TAG + ", " +
		COL_IS_MED_TAG + ") " +			
		"VALUES (?,?);";
	
	public static final String GET_LATEST_ID =		
		"SELECT MAX(" + COL_ID + ") FROM tblTags;";
}
