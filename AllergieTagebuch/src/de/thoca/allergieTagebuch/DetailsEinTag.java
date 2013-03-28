package de.thoca.allergieTagebuch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.thoca.allergieTagebuch.data.Tag;
import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TagsHelper;
import de.thoca.allergieTagebuch.data.TblTagebuch;

public class DetailsEinTag extends Activity
{
	public static final String DATENSATZ_ID = "id";
	public static final int REQUEST_CODE_ALLERGIEBESCHWERDEN = 1;
	public static final int REQUEST_CODE_MEDIZIN_TAGS = 2;
	public static final int REQUEST_CODE_EIGENE_TAGS = 3;
	
	private long _id = -1;
	
	private TextView _lblTag;
	private TextView _lblDatum;
	private RelativeLayout _rlDatum;
	
	private TextView _lblAllergBeschwerden;
	private TextView _lblAllergBeschwerdenDaten;
	private ImageView _imgAllergie;
	private RelativeLayout _rlAllergieBeschwerden;
	
	private TextView _lblMedikamente;
	private TextView _lblMedikamenteDaten;
	private RelativeLayout _rlMedikamente;
	
	private TextView _lblEigeneStichworte;
	private TextView _lblEigeneStichworteDaten;
	private RelativeLayout _rlStichworte;
	
	private EditText _lblKommentarDaten;
	
	private Button _btOK;	
	
	private ArrayList<Tag> _medTags;
	private ArrayList<Tag> _eigeneTags;
	
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;
	
	private int _datenAllergie;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_ein_tag);   
        _tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);
        
        Bundle extras = getIntent().getExtras();
        _id = extras.getLong(DATENSATZ_ID, -1);
        
        if (_id == -1) // Neuanlage für aktuellen Tag
        {
        	final Calendar c = new GregorianCalendar();     	
        	_id = CreateOrLoadEntryForDay(c.get(Calendar.YEAR), 
        			c.get(Calendar.MONTH)+1, 
        			c.get(Calendar.DAY_OF_MONTH));
        }
        
        findUiElements();        
        updateUiForCurrentIdentifier();               
        initializeClickListeners();                  
    }
    
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	    	
    	if (_tagebuchDbOpenHelper != null)
    		_tagebuchDbOpenHelper.close();
    }
    
    private long CreateOrLoadEntryForDay(int year, int month, int day)
    {    	
    	String currentDate = String.format("%04d-%02d-%02d", 
        		year, month, day );
        	
    	// gibt's den schon?
    	SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
    	Cursor cursor = db.rawQuery(TblTagebuch.SELECT_ID_BY_DATUM, new String[] { currentDate });
    	
    	long foundId = -1;
    	if (cursor.moveToNext())    	
    		foundId = cursor.getLong(0);
    	cursor.close();
    	
    	if (foundId != -1)
    		return foundId; // hamma scho
    	
    	Calendar c = new GregorianCalendar(year, month-1, day);
        int currentWeekday = c.get(Calendar.DAY_OF_WEEK);
    	
    	ContentValues cv = new ContentValues();
    	cv.put(TblTagebuch.COL_DATUM, currentDate);
    	cv.put(TblTagebuch.COL_WOCHENTAG, currentWeekday);    	
    	long newId = db.insert("TblTagebuch", null, cv);
    	
    	db.close();
    	return newId;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {    	
    	switch (requestCode)
    	{
    		case REQUEST_CODE_ALLERGIEBESCHWERDEN:
    			_datenAllergie = data.getExtras().getInt(AllergieBeschwerdenAuswahl.ALLERGIE_BESCHWERDEN);    	
    	    	saveAllergie();    	
    	    	updateUiAllergie();
    	    	break;
    		case REQUEST_CODE_MEDIZIN_TAGS:
    			String medTagsDirty = data.getExtras().getString(MedikamenteAuswahl.TAGS_DIRTY);
    			if (medTagsDirty.equals("1"))
    			{
    				// reload medikamente daten
    				_medTags = TagsHelper.GetMedTagsForDay(_id);
    				_lblMedikamenteDaten.setText(buildTagsString(_medTags));
    			}    			    			
    			break;
    		case REQUEST_CODE_EIGENE_TAGS:
    			String eigeneTagsDirty = data.getExtras().getString(EigeneTagsAuswahl.TAGS_DIRTY);
    			if (eigeneTagsDirty.equals("1"))
    			{
    				// reload eigene Tags daten
    				_eigeneTags = TagsHelper.GetEigeneTagsForDay(_id);
    				_lblEigeneStichworteDaten.setText(buildTagsString(_eigeneTags));
    			}    			    			
    			break;
    	}    	
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {            
            saveKommentar();
        }
        return super.onKeyDown(keyCode, event);
    }

    
    private void saveKommentar()
    {
    	if (_id == -1)
    		return;
    	    	
    	SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
    	
    	SQLiteStatement stmtUpdate = 
    		db.compileStatement(TblTagebuch.UPDATE_KOMMENTAR);
    	stmtUpdate.bindString(1, _lblKommentarDaten.getText().toString());
    	stmtUpdate.bindLong(2, _id);
    	stmtUpdate.execute();
    	stmtUpdate.close();    	
    	db.close();
    }
    
    private void saveAllergie()
    {
    	if (_id == -1)  
    		return;
    	
    	SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
    	
    	SQLiteStatement stmtUpdate = 
    		db.compileStatement(TblTagebuch.UPDATE_ALLERGIE);
    	stmtUpdate.bindLong(1, _datenAllergie);
    	stmtUpdate.bindLong(2, _id);
    	stmtUpdate.execute();
    	stmtUpdate.close();    	
    	db.close();
    }
    
    private void initializeClickListeners()
	{
    	_lblAllergBeschwerden.setOnClickListener(this.onAllergieBeschwerdenClick);
    	_lblAllergBeschwerdenDaten.setOnClickListener(this.onAllergieBeschwerdenClick);
    	_imgAllergie.setOnClickListener(this.onAllergieBeschwerdenClick);    	
    	_rlAllergieBeschwerden.setOnClickListener(this.onAllergieBeschwerdenClick);
    	
    	_lblMedikamente.setOnClickListener(this.onMedikamenteClick);
    	_lblMedikamenteDaten.setOnClickListener(this.onMedikamenteClick);
    	_rlMedikamente.setOnClickListener(this.onMedikamenteClick);
    	
    	_lblEigeneStichworte.setOnClickListener(this.onEigeneStichworteClick);
    	_lblEigeneStichworteDaten.setOnClickListener(this.onEigeneStichworteClick);
    	_rlStichworte.setOnClickListener(this.onEigeneStichworteClick);
    	
    	_lblDatum.setOnClickListener(this.onDatumClick);
    	_rlDatum.setOnClickListener(this.onDatumClick);
    	_lblTag.setOnClickListener(this.onDatumClick);
    	
    	_btOK.setOnClickListener(this.onBtOKClick);    	    
	}
    
    private View.OnClickListener onBtOKClick = new View.OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			saveKommentar();
			finish();
		}	
	};
    
    private View.OnClickListener onDatumClick = new View.OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			String[] dateParts =_lblDatum.getText().toString().split("-");
			int year = Integer.parseInt(dateParts[0]);
			int month = Integer.parseInt(dateParts[1]);
			int day = Integer.parseInt(dateParts[2]);					
			
			DatePickerDialog datePickerDialog = 
				new DatePickerDialog(DetailsEinTag.this, 
						OnDateChanged, 
						year,
						month-1,
						day);			
			datePickerDialog.show();
						
		}	
	};
	
	private DatePickerDialog.OnDateSetListener OnDateChanged = 
		new DatePickerDialog.OnDateSetListener()
	{
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{
			     	
        	_id = CreateOrLoadEntryForDay(year, 
        			monthOfYear+1, 
        			dayOfMonth);
	        	        	              
	        updateUiForCurrentIdentifier();               	        
		}
	};

    
    private View.OnClickListener onAllergieBeschwerdenClick = new View.OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(DetailsEinTag.this, AllergieBeschwerdenAuswahl.class);
			intent.putExtra(AllergieBeschwerdenAuswahl.ALLERGIE_BESCHWERDEN, _datenAllergie);
			startActivityForResult(intent, REQUEST_CODE_ALLERGIEBESCHWERDEN);
		}	
	};
	
	private View.OnClickListener onMedikamenteClick = new View.OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(DetailsEinTag.this, MedikamenteAuswahl.class);
			intent.putExtra(MedikamenteAuswahl.DATENSATZ_ID, _id);			
			startActivityForResult(intent, REQUEST_CODE_MEDIZIN_TAGS);			
		}	
	};
        
	private View.OnClickListener onEigeneStichworteClick = new View.OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(DetailsEinTag.this, EigeneTagsAuswahl.class);
			intent.putExtra(EigeneTagsAuswahl.DATENSATZ_ID, _id);			
			startActivityForResult(intent, REQUEST_CODE_EIGENE_TAGS);			
		}	
	};
	
    private void findUiElements()
    {
    	_lblTag = (TextView)findViewById(R.id.lblTag);
		_lblDatum = (TextView)findViewById(R.id.lblDatum);
		_rlDatum = (RelativeLayout)findViewById(R.id.rlDatum);
		
		_lblAllergBeschwerden = (TextView)findViewById(R.id.lblAllergBeschwerden);
		_lblAllergBeschwerdenDaten = (TextView)findViewById(R.id.lblAllergBeschwerdenDaten);
		_imgAllergie = (ImageView)findViewById(R.id.imgAllergie);
		
		_lblMedikamente = (TextView)findViewById(R.id.lblMedikamente);
		_lblMedikamenteDaten = (TextView)findViewById(R.id.lblMedikamenteDaten);
		
		_lblEigeneStichworte = (TextView)findViewById(R.id.lblEigeneStichworte);
		_lblEigeneStichworteDaten = (TextView)findViewById(R.id.lblEigeneStichworteDaten);
				
		_lblKommentarDaten = (EditText)findViewById(R.id.lblKommentarDaten);
		
		_btOK = (Button)findViewById(R.id.btDetailsOK);		
		
		_rlAllergieBeschwerden = (RelativeLayout)findViewById(R.id.rlAllergieBeschwerden);
		_rlMedikamente = (RelativeLayout)findViewById(R.id.rlMedikamente);
		_rlStichworte = (RelativeLayout)findViewById(R.id.rlStichworte);
    }
	       
	private void updateUiForCurrentIdentifier()
	{				
		// vorhandenen Datensatz bearbeiten					
				
		Cursor cursor =_tagebuchDbOpenHelper.getReadableDatabase()
		.rawQuery(TblTagebuch.SELECT_DETAILS_EIN_TAG, new String[] { String.format("%d", _id)});
			
		cursor.moveToNext();
		
		_lblTag.setText(Wochentag.getTextForNumber(getApplicationContext(), cursor.getInt(1)));
		
		_lblDatum.setText(cursor.getString(2));			
		_datenAllergie = cursor.getInt(3);			
		//_lblEigeneStichworteDaten.setText(cursor.getString(5));
		_lblKommentarDaten.setText(cursor.getString(4));		
		cursor.close();
		
		TagsHelper.Initialize(this);
		_medTags = TagsHelper.GetMedTagsForDay(_id);
		_lblMedikamenteDaten.setText(buildTagsString(_medTags));
		
		_eigeneTags = TagsHelper.GetEigeneTagsForDay(_id);
		_lblEigeneStichworteDaten.setText(buildTagsString(_eigeneTags));
		
		updateUiAllergie();			
	}
	
	private String buildTagsString(ArrayList<Tag> tags)
	{
		boolean firstTag = true;
		StringBuilder sb = new StringBuilder();
		for (Tag t : tags)
		{
			if (!firstTag)
				sb.append(", ");			
			firstTag = false;
			
			sb.append(t.tag);
		}	
		
		return sb.toString();
	}
	
	private void updateUiAllergie()
    {
    	switch (_datenAllergie) 
		{
			case TblTagebuch.ALLERGIE_SCHWERE_UNDEFINIERT: 
				_lblAllergBeschwerdenDaten.setText(getResources().getString(R.string.allergie_keine_angabe));
				_imgAllergie.setVisibility(View.INVISIBLE);
				break;
			case TblTagebuch.ALLERGIE_SCHWERE_KEINE: 
				_lblAllergBeschwerdenDaten.setText(getResources().getString(R.string.allergie_keine));				
				_imgAllergie.setImageResource(R.drawable.allergie_0_sehr_gut);
				_imgAllergie.setVisibility(View.VISIBLE);
				break;
			case TblTagebuch.ALLERGIE_SCHWERE_LEICHT: 
				_lblAllergBeschwerdenDaten.setText(getResources().getString(R.string.allergie_leicht));
				_imgAllergie.setImageResource(R.drawable.allergie_1_gut);
				_imgAllergie.setVisibility(View.VISIBLE);
				break;
			case TblTagebuch.ALLERGIE_SCHWERE_MITTEL: 
				_lblAllergBeschwerdenDaten.setText(getResources().getString(R.string.allergie_mittel));
				_imgAllergie.setImageResource(R.drawable.allergie_2_mittel);
				_imgAllergie.setVisibility(View.VISIBLE);
				break;
			case TblTagebuch.ALLERGIE_SCHWERE_SCHWER: 
				_lblAllergBeschwerdenDaten.setText(getResources().getString(R.string.allergie_schwer));
				_imgAllergie.setImageResource(R.drawable.allergie_3_schlecht);
				_imgAllergie.setVisibility(View.VISIBLE);
				break;
			case TblTagebuch.ALLERGIE_SCHWERE_SEHR_SCHWER: 
				_lblAllergBeschwerdenDaten.setText(getResources().getString(R.string.allergie_sehr_schwer));
				_imgAllergie.setImageResource(R.drawable.allergie_4_sehr_schlecht);
				_imgAllergie.setVisibility(View.VISIBLE);
				break;
		}
    }
		
}
