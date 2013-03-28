package de.thoca.allergieTagebuch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TblTagebuch;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class StatistikAuswahl extends Activity
{
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;
	
	private Spinner _statistikJahr;
	private Spinner _statistikArt; 
	private Spinner _statistikZeitraum;
	private TextView _txtMonat;
	private Spinner _statistikMonat;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.statistik_auswahl);

	    _tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);
	    
	    findViews();
	    
	    initSpinnerWerte();	    
	    initClickListener();
	}	
	
	@Override
    protected void onDestroy() 
    {
    	super.onDestroy();       
    	
    	if (_tagebuchDbOpenHelper != null)
    		_tagebuchDbOpenHelper.close();
    }

	private void initSpinnerWerte()
	{				
	    initStatistikArt();	    	    
	    initZeitraum();	    
	    initJahre();
	    initMonate();
	}

	private void initJahre()
	{
		ArrayList<String> jahre = new ArrayList<String>();
	    SQLiteDatabase db = _tagebuchDbOpenHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery(TblTagebuch.SELECT_ALL_JAHRE, null);
	    while (cursor.moveToNext())	    
	    	jahre.add(cursor.getString(0));	    	    
	    cursor.close();
	    db.close();
	    	    	   
	    if (jahre.size() == 0)
	    	jahre.add(String.format("%d", Calendar.getInstance().get(Calendar.YEAR)));
	    	    	   
	    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, jahre);
	    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    _statistikJahr.setAdapter(arrayAdapter);
	}

	private void initZeitraum()
	{
		ArrayAdapter statistikZeitraumAdapter = ArrayAdapter.createFromResource(
	            this, R.array.statistik_zeitraum, android.R.layout.simple_spinner_item);
	    statistikZeitraumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    _statistikZeitraum.setAdapter(statistikZeitraumAdapter);
	    
	    _statistikZeitraum.setOnItemSelectedListener( new OnItemSelectedListener(){

	    	 @Override
	    	 public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	        // position 0 = jahr, 1 = monat
	    		 if (position == 1)
	    		 {
	    			 _statistikMonat.setVisibility(View.VISIBLE);
	    			 _txtMonat.setVisibility(View.VISIBLE);
	    		 }
	    		 else
	    		 {
	    			 _statistikMonat.setVisibility(View.GONE);
	    			 _txtMonat.setVisibility(View.GONE);
	    		 }	    			 
	    	 }

	    	 @Override
	    	 public void onNothingSelected(AdapterView<?> parentView) {
	    	     // your code here
	    	 }	    	
	    });
	    
	}

	private void initStatistikArt()
	{
		ArrayAdapter statistikArtAdapter = ArrayAdapter.createFromResource(
	            this, R.array.statistik_arten, android.R.layout.simple_spinner_item);
	    statistikArtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    _statistikArt.setAdapter(statistikArtAdapter);
	}
	
	private void initMonate()
	{
		ArrayAdapter statistikMonateAdapter = ArrayAdapter.createFromResource(
	            this, R.array.statistik_monate, android.R.layout.simple_spinner_item);
	    statistikMonateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    _statistikMonat.setAdapter(statistikMonateAdapter);
	}

	private void findViews()
	{
		_statistikArt = (Spinner) findViewById(R.id.spinnerTyp);
		_statistikZeitraum = (Spinner) findViewById(R.id.spinnerZeitraum);
		_statistikJahr = (Spinner) findViewById(R.id.spinnerJahr);
		_statistikMonat = (Spinner) findViewById(R.id.spinnerMonat);
		_txtMonat = (TextView) findViewById(R.id.textViewMonat);
	}
		
	private void initClickListener()
	{
		Button btCalc = (Button)findViewById(R.id.btBerechneStatistik);
	    btCalc.setOnClickListener(new View.OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if (_statistikArt.getSelectedItemPosition() == 0) // Allerg. Beschwerden
				{
					 // position 0 = jahr, 1 = monat
					if (_statistikZeitraum.getSelectedItemPosition() == 0)
						runAllergieJahrStatistik();
					else 
						runAllergieMonatStatistik();
				}
				else  // Eigene Stichworte					
				{					
					runEigeneStichworteAuswahl();				
				}
			}		
		});
	}
	
	private void runAllergieJahrStatistik()
	{
		String jahr = (String)_statistikJahr.getSelectedItem();
		int jahrInt = Integer.parseInt(jahr);
		
		Intent intent = new Intent(StatistikAuswahl.this, StatistikAllergieJahr.class);
		intent.putExtra(StatistikAllergieJahr.JAHR, jahrInt);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}
	
	private void runAllergieMonatStatistik()
	{
		String jahr = (String)_statistikJahr.getSelectedItem();
		int jahrInt = Integer.parseInt(jahr);
		int monat = _statistikMonat.getSelectedItemPosition() + 1;
				
		Intent intent = new Intent(StatistikAuswahl.this, StatistikAllergieMonat.class);
		intent.putExtra(StatistikAllergieMonat.JAHR, jahrInt);
		intent.putExtra(StatistikAllergieMonat.MONAT, monat);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}
	
	private void runEigeneStichworteAuswahl()
	{
		String jahr = (String)_statistikJahr.getSelectedItem();
		int jahrInt = Integer.parseInt(jahr);
		
		Intent intent = new Intent(StatistikAuswahl.this, StatistikTagAuswahl.class);
		intent.putExtra(StatistikTagAuswahl.JAHR, jahrInt);
		
		 // position 0 = jahr, 1 = monat
		if (_statistikZeitraum.getSelectedItemPosition() == 0)
		{
			intent.putExtra(StatistikTagAuswahl.ZEITRAUM, StatistikTagAuswahl.ZEITRAUM_JAHR);	
		}
		else
		{
			intent.putExtra(StatistikTagAuswahl.ZEITRAUM, StatistikTagAuswahl.ZEITRAUM_MONAT);
			int monat = _statistikMonat.getSelectedItemPosition() + 1;
			intent.putExtra(StatistikTagAuswahl.MONAT, monat);
		}
		
		//intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);					
	}
}
