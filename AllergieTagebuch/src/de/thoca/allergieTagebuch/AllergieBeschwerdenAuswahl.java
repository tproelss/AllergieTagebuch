package de.thoca.allergieTagebuch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TableRow;
import de.thoca.allergieTagebuch.data.TblTagebuch;

public class AllergieBeschwerdenAuswahl extends Activity
{
	public static final String ALLERGIE_BESCHWERDEN = "allergie_beschwerden";
	private int _datenAllergie;

	private TableRow _rowBeschwerdenUnbekannt;
	private TableRow _rowBeschwerdenKeine;
	private TableRow _rowBeschwerdenLeicht;
	private TableRow _rowBeschwerdenMittel;
	private TableRow _rowBeschwerdenSchwer;
	private TableRow _rowBeschwerdenSehrSchwer;
	
	private int _selectedBeschwerden;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allergie_beschwerden_auswahl);
        
        Bundle extras = getIntent().getExtras();
        _datenAllergie = extras.getInt(ALLERGIE_BESCHWERDEN, -1);               
        _selectedBeschwerden = _datenAllergie;
        
        findUiElements();
        updateUi();
        setClickListeners();
    }
    
    private void setClickListeners()
    {
    	_rowBeschwerdenUnbekannt.setOnClickListener( new View.OnClickListener() { public void onClick(View v) { _selectedBeschwerden = TblTagebuch.ALLERGIE_SCHWERE_UNDEFINIERT; updateUi(); setResultAndFinish(); }});
    	_rowBeschwerdenKeine.setOnClickListener( new View.OnClickListener() { public void onClick(View v) { _selectedBeschwerden = TblTagebuch.ALLERGIE_SCHWERE_KEINE; updateUi(); setResultAndFinish();}});
    	_rowBeschwerdenLeicht.setOnClickListener( new View.OnClickListener() { public void onClick(View v) { _selectedBeschwerden = TblTagebuch.ALLERGIE_SCHWERE_LEICHT; updateUi(); setResultAndFinish();}});
    	_rowBeschwerdenMittel.setOnClickListener( new View.OnClickListener() { public void onClick(View v) { _selectedBeschwerden = TblTagebuch.ALLERGIE_SCHWERE_MITTEL; updateUi(); setResultAndFinish();}});
    	_rowBeschwerdenSchwer.setOnClickListener( new View.OnClickListener() { public void onClick(View v) { _selectedBeschwerden = TblTagebuch.ALLERGIE_SCHWERE_SCHWER; updateUi(); setResultAndFinish();}});
    	_rowBeschwerdenSehrSchwer.setOnClickListener( new View.OnClickListener() { public void onClick(View v) { _selectedBeschwerden = TblTagebuch.ALLERGIE_SCHWERE_SEHR_SCHWER; updateUi(); setResultAndFinish();}});        
    }
    
    private void setResultAndFinish()
    {
    	final Intent intent = new Intent();
    	intent.putExtra(ALLERGIE_BESCHWERDEN, _selectedBeschwerden);
    	setResult(Activity.RESULT_OK, intent);
    	finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {        	
        	setResultAndFinish();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void findUiElements()
    {
    	_rowBeschwerdenUnbekannt = (TableRow)findViewById(R.id.rowBeschwerdenUnbekannt);
    	_rowBeschwerdenKeine = (TableRow)findViewById(R.id.rowBeschwerdenKeine);
    	_rowBeschwerdenLeicht = (TableRow)findViewById(R.id.rowBeschwerdenLeicht);
    	_rowBeschwerdenMittel = (TableRow)findViewById(R.id.rowBeschwerdenMittel);
    	_rowBeschwerdenSchwer = (TableRow)findViewById(R.id.rowBeschwerdenSchwer);
    	_rowBeschwerdenSehrSchwer = (TableRow)findViewById(R.id.rowBeschwerdenSehrSchwer);    	    
    }
    
    private void updateUi()
    {
    	int defaultColor = Color.BLACK;
    	_rowBeschwerdenUnbekannt.setBackgroundColor(defaultColor); 		
		_rowBeschwerdenKeine.setBackgroundColor(defaultColor); 		
		_rowBeschwerdenLeicht.setBackgroundColor(defaultColor); 		
		_rowBeschwerdenMittel.setBackgroundColor(defaultColor); 		
		_rowBeschwerdenSchwer.setBackgroundColor(defaultColor); 		
		_rowBeschwerdenSehrSchwer.setBackgroundColor(defaultColor);
    	
    	int selectedColor = //Color.WHITE;
    	Color.parseColor("#795102");
    	
    	switch (_selectedBeschwerden)
    	{
	    	case TblTagebuch.ALLERGIE_SCHWERE_UNDEFINIERT:
	    		_rowBeschwerdenUnbekannt.setBackgroundColor(selectedColor); 
	    		break;
	    	case TblTagebuch.ALLERGIE_SCHWERE_KEINE:
	    		_rowBeschwerdenKeine.setBackgroundColor(selectedColor); 
	    		break;
	    	case TblTagebuch.ALLERGIE_SCHWERE_LEICHT:
	    		_rowBeschwerdenLeicht.setBackgroundColor(selectedColor); 
	    		break;
	    	case TblTagebuch.ALLERGIE_SCHWERE_MITTEL:
	    		_rowBeschwerdenMittel.setBackgroundColor(selectedColor); 
	    		break;
	    	case TblTagebuch.ALLERGIE_SCHWERE_SCHWER:
	    		_rowBeschwerdenSchwer.setBackgroundColor(selectedColor); 
	    		break;
	    	case TblTagebuch.ALLERGIE_SCHWERE_SEHR_SCHWER:
	    		_rowBeschwerdenSehrSchwer.setBackgroundColor(selectedColor); 
	    		break;	    	    	
    	}
    }
	
}
