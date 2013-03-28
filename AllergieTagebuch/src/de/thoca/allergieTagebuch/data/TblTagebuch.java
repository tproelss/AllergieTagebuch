package de.thoca.allergieTagebuch.data;

public class TblTagebuch {
	// Tabellenspalten
	public static final String COL_ID = "_id";
	public static final String COL_DATUM = "Datum";
	public static final String COL_WOCHENTAG = "Wochentag";
	public static final String COL_ALLERGIE = "Allergie";	
	public static final String COL_KOMMENTAR = "Kommentar";
	
	// berechnete Spalten
	public static final String COL_CALC_MED_VORHANDEN = "MedVorhanden";	
	public static final String COL_CALC_KOMMENTAR_VORHANDEN = "KommentarVorhanden";
	public static final String COL_CALC_MONAT = "Monat";
	
	// Konstanten für Inhalte
	public static final int ALLERGIE_SCHWERE_UNDEFINIERT = -1;
	public static final int ALLERGIE_SCHWERE_KEINE = 0;
	public static final int ALLERGIE_SCHWERE_LEICHT = 1;
	public static final int ALLERGIE_SCHWERE_MITTEL = 2;
	public static final int ALLERGIE_SCHWERE_SCHWER = 3;
	public static final int ALLERGIE_SCHWERE_SEHR_SCHWER = 4;
	
	// TODO: UNIQUE CONSTRAINT AUF tblTagebuch.Datum
	
	// SQL Queries
	public static final String SQL_CREATE =
		"CREATE TABLE tblTagebuch ( " +
			COL_ID 				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COL_DATUM 			+ " TEXT NOT NULL, " +
			COL_WOCHENTAG 		+ " INTEGER NOT NULL, " + 
			COL_ALLERGIE 		+ " INTEGER, " +			
			COL_KOMMENTAR 		+ " TEXT)";
	public static final String SQL_CREATE_INDEX_DATUM = 
		"CREATE INDEX IxDatum ON tblTagebuch ("+COL_DATUM+")";
	 
	public static final String SELECT_ALL_JAHRE =
		"SELECT DISTINCT substr(" + COL_DATUM + ",1,4) AS JAHR " +
		"FROM tblTagebuch " +
		"ORDER BY JAHR DESC";		
	
	public static final String SELECT_ALLERGIE_JAHR =
		"SELECT substr(" + COL_DATUM + ", 1, 7) AS " + COL_CALC_MONAT + ", AVG(" + COL_ALLERGIE + ") " +
			"FROM tblTagebuch " +	
			"GROUP BY " + COL_CALC_MONAT + " " +
			"ORDER BY " + COL_CALC_MONAT;
	
	public static final String SELECT_ALLERGIE_BY_DATUM_LIKE =
			"SELECT " + COL_DATUM + "," + COL_ALLERGIE + " " +
				"FROM tblTagebuch " +	
				"WHERE " + COL_DATUM + " LIKE ? " + 				
				"ORDER BY " + COL_DATUM;
	
	public static final String SELECT_EIGENE_TAGS_COUNT_BY_DATUM_LIKE =
			"SELECT " + COL_DATUM + ", COUNT(1) " + 
			"FROM tblTagebuch t INNER JOIN tblT2T m on t._id = m.TagebuchId " +
			"WHERE t." + COL_DATUM + " LIKE ? " +
			"AND m." + TblT2T.COL_TAG_ID + " IN (%s) " +
			"GROUP BY " + COL_DATUM;	
	
	public static final String SELECT_EIGENE_TAGS_COUNT_BY_MONAT_LIKE =
			"SELECT substr(" + COL_DATUM + ", 1, 7) AS " + COL_CALC_MONAT + ", COUNT(1) " + 
			"FROM tblTagebuch t INNER JOIN tblT2T m on t._id = m.TagebuchId " +
			"WHERE t." + COL_DATUM + " LIKE ? " +
			"AND m." + TblT2T.COL_TAG_ID + " IN (%s) " +
			"GROUP BY " + COL_CALC_MONAT;
	
	public static final String SELECT_UEBERSICHT =
		"SELECT DISTINCT " +
			"t." + COL_ID + ", " +
			"t." + COL_WOCHENTAG + ", " +
			"t." + COL_DATUM + ", " +
			"t." + COL_ALLERGIE + ", " + 
			"IFNULL(m." + TblT2T.COL_TAGEBUCH_ID + ", 0) AS " + COL_CALC_MED_VORHANDEN + ", " + 
			"IFNULL(LENGTH(t." + COL_KOMMENTAR + "), 0) AS " + COL_CALC_KOMMENTAR_VORHANDEN + " " +		
		"FROM tblTagebuch t " + 
			"LEFT JOIN tblT2T m " + 
			"ON t." + COL_ID + " = m." + TblT2T.COL_TAGEBUCH_ID + " " +
			"AND m." + TblT2T.COL_IS_MED_TAG + "=1 " +		
		"ORDER BY t." + COL_DATUM + " DESC";			
	
	public static final String SELECT_NOTIZEN =
			"SELECT " +
				"t." + COL_ID + ", " +
				"t." + COL_WOCHENTAG + ", " +
				"t." + COL_DATUM + ", " +								 
				"t." + COL_KOMMENTAR + " " +		
			"FROM tblTagebuch t " + 
			"WHERE IFNULL(LENGTH(t." + COL_KOMMENTAR + "), 0) > 0 "	+		
			"ORDER BY t." + COL_DATUM + " DESC";

	public static final String SELECT_DETAILS_EIN_TAG =
		"SELECT " + 
			COL_ID + ", " + 
			COL_WOCHENTAG + ", " +
			COL_DATUM + ", " +
			COL_ALLERGIE + ", " +			
			COL_KOMMENTAR + " " +
		"FROM tblTagebuch " + 
		"WHERE " + COL_ID + "=?";
	
	public static final String UPDATE_ALLERGIE =
		"UPDATE tblTagebuch " +
			"SET " + COL_ALLERGIE + "=? " +
			"WHERE " + COL_ID + "=?";
	
	public static final String UPDATE_KOMMENTAR =
		"UPDATE tblTagebuch " +
			"SET " + COL_KOMMENTAR + "=? " +
			"WHERE " + COL_ID + "=?";

	public static final String SELECT_ID_BY_DATUM =
		"SELECT " + COL_ID + " " +
		"FROM TblTagebuch " +
		"WHERE " + COL_DATUM + " = ?";
}
