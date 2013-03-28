package de.thoca.allergieTagebuch.data;

public class TblT2T {
	public static final String COL_TAGEBUCH_ID = "TagebuchId";
	public static final String COL_TAG_ID = "TagId";
	public static final String COL_IS_MED_TAG = "IsMedTag";
	
	public static final String SQL_CREATE =
		"CREATE TABLE tblT2T (" +					
			"TagebuchId INTEGER NOT NULL, " +
			"TagId INTEGER NOT NULL, " +
			"IsMedTag BIT NOT NULL)";
	
	public static final String SQL_CREATE_INDEX_TO_TAGEBUCH = 
		"CREATE INDEX IxMapping ON tblT2T (" + COL_TAGEBUCH_ID + ")";
	
	public static final String SELECT_ALL_TAGS_FOR_DAY =
		"SELECT t." + TblTags.COL_ID + ", t." + TblTags.COL_TAG + ", t." + TblTags.COL_IS_MED_TAG + " " +  
			"FROM tblTags t " + 
			"INNER JOIN tblT2T m ON t." + TblTags.COL_ID + " = m." + COL_TAG_ID + " " +
		"WHERE m." + TblT2T.COL_TAGEBUCH_ID + " = ? ";
	
	public static final String SELECT_MED_TAGS_FOR_DAY =
		SELECT_ALL_TAGS_FOR_DAY + 
		" AND m." + TblTags.COL_IS_MED_TAG + " = 1 " +
		" ORDER BY t." + TblTags.COL_TAG;
	
	public static final String SELECT_EIGENE_TAGS_FOR_DAY =
		SELECT_ALL_TAGS_FOR_DAY + 
		" AND m." + TblTags.COL_IS_MED_TAG + " = 0 " +
		" ORDER BY t." + TblTags.COL_TAG;
}
