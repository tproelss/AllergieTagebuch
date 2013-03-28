package de.thoca.allergieTagebuch;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TblTagebuch;

public class ChronologischeUebersicht extends ListActivity 
{			
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;
	private Cursor _uebersichtCursor;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	Eula.show(this);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronologische_uebersicht);
        
        _tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);
       
        btNeuerEintragInitialize();
        listClickInitialize();
        listeAnzeigen();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu_main, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Intent intent;
    	switch (item.getItemId())
    	{
	    	case R.id.menu_statistiken:
	    		intent = new Intent(ChronologischeUebersicht.this, StatistikAuswahl.class);
	    		startActivity(intent);				
	    		return true;
	    	case R.id.menu_notizen:
	    		intent = new Intent(ChronologischeUebersicht.this, NotizenBrowser.class);
	    		startActivity(intent);				
	    		return true;
	    	case R.id.menu_about:
	    		intent = new Intent(ChronologischeUebersicht.this, About.class);
	    		startActivity(intent);
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }    	    	
    }
    
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
    	if (_uebersichtCursor != null && !_uebersichtCursor.isClosed())
    		_uebersichtCursor.close();
    	
    	if (_tagebuchDbOpenHelper != null)
    		_tagebuchDbOpenHelper.close();
    }
    
    private void btNeuerEintragInitialize() 
    {
    	final Button btNeuerEintrag = (Button)findViewById(R.id.btNeuerEintrag);
    	btNeuerEintrag.setOnClickListener(
    			new View.OnClickListener() 
    			{					
					@Override
					public void onClick(View v) 
					{
						Intent intent = new Intent(ChronologischeUebersicht.this, DetailsEinTag.class);
						intent.putExtra(DetailsEinTag.DATENSATZ_ID, (long)-1);
						startActivityForResult(intent, 0);
					}
				});
    }
    
    private void listClickInitialize()
    {
    	ListView listView = getListView();
    	listView.setTextFilterEnabled(false);
    	listView.setOnItemClickListener(
			new OnItemClickListener() 
	    	{
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					Intent intent = new Intent(ChronologischeUebersicht.this, DetailsEinTag.class);
					intent.putExtra(DetailsEinTag.DATENSATZ_ID, id);
					startActivityForResult(intent, 0);													
				}			
			});
    }
    
    private void listeAnzeigen() 
    {    	
    	_uebersichtCursor = 
    		_tagebuchDbOpenHelper.getReadableDatabase().
    		rawQuery(TblTagebuch.SELECT_UEBERSICHT, null);
    	startManagingCursor(_uebersichtCursor);
    	
    	int[] viewIds = new int[] 
    	{
			R.id.lblTag, R.id.lblDatum, R.id.imgAllergie, 
			R.id.imgMedikamente, R.id.imgNotitz, 
			R.id.lblAllergie
		};
    	
    	String[] tableColumns = new String[] 
        {
    			TblTagebuch.COL_WOCHENTAG, TblTagebuch.COL_DATUM, TblTagebuch.COL_ALLERGIE, 
    	        TblTagebuch.COL_CALC_MED_VORHANDEN, TblTagebuch.COL_CALC_KOMMENTAR_VORHANDEN, 
    	        TblTagebuch.COL_ID /*fake für allergieLbl*/
        };
    	
    	SimpleCursorAdapter tblTagebuchAdapter = 
			new SimpleCursorAdapter(
					this, R.layout.uebersicht_row,
					_uebersichtCursor,
					tableColumns,
					viewIds);
    	
    	tblTagebuchAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() 
    	{    		
			@Override
            public boolean setViewValue(View view, Cursor theCursor, int column) 
            {										
				if (column == 0) // eigentlich _id, aber als fake fur allergie lbl 
				{  
					TextView allergieLbl = (TextView)view;
					int val = theCursor.getInt(3);					
					switch (val) 
					{
						case TblTagebuch.ALLERGIE_SCHWERE_UNDEFINIERT: 
							allergieLbl.setText(getResources().getString(R.string.allergie_keine_angabe)); break;
						case TblTagebuch.ALLERGIE_SCHWERE_KEINE: 
							allergieLbl.setText(getResources().getString(R.string.allergie_keine)); break;
						case TblTagebuch.ALLERGIE_SCHWERE_LEICHT: 
							allergieLbl.setText(getResources().getString(R.string.allergie_leicht)); break;
						case TblTagebuch.ALLERGIE_SCHWERE_MITTEL: 
							allergieLbl.setText(getResources().getString(R.string.allergie_mittel)); break;
						case TblTagebuch.ALLERGIE_SCHWERE_SCHWER: 
							allergieLbl.setText(getResources().getString(R.string.allergie_schwer)); break;
						case TblTagebuch.ALLERGIE_SCHWERE_SEHR_SCHWER: 
							allergieLbl.setText(getResources().getString(R.string.allergie_sehr_schwer)); break;
					}											
				}								
				else if (column == 1) // tag 
				{ 
					TextView txt = (TextView)view;						
					txt.setText(Wochentag.getTextForNumber(getApplicationContext(), theCursor.getInt(1)));											
				} 
				else if (column == 2) // datum 
				{ 
					TextView txt = (TextView)view;
					txt.setText(theCursor.getString(2));
				} 
				else if (column == 3) // Allergie Image
				{  
					ImageView img = (ImageView)view;
					int val = theCursor.getInt(3);					
					switch (val) 
					{
						case TblTagebuch.ALLERGIE_SCHWERE_UNDEFINIERT: 
							img.setVisibility(View.INVISIBLE); break;
						case TblTagebuch.ALLERGIE_SCHWERE_KEINE: 
							img.setImageResource(R.drawable.allergie_0_sehr_gut); break;							
						case TblTagebuch.ALLERGIE_SCHWERE_LEICHT: 
							img.setImageResource(R.drawable.allergie_1_gut); break;														
						case TblTagebuch.ALLERGIE_SCHWERE_MITTEL: 
							img.setImageResource(R.drawable.allergie_2_mittel); break;
						case TblTagebuch.ALLERGIE_SCHWERE_SCHWER: 
							img.setImageResource(R.drawable.allergie_3_schlecht); break;
						case TblTagebuch.ALLERGIE_SCHWERE_SEHR_SCHWER: 
							img.setImageResource(R.drawable.allergie_4_sehr_schlecht); break;
					}
					
				} 
				else if (column == 4) // Medikamente Tags
				{  
					ImageView img = (ImageView)view;
					int val = theCursor.getInt(4);
					if (val > 0)
						img.setVisibility(View.VISIBLE);
					else
						img.setVisibility(View.INVISIBLE);
				} 
				else if (column == 5) // Kommentar
				{  
					ImageView img = (ImageView)view;
					int val = theCursor.getInt(5);
					if (val > 0)
						img.setVisibility(View.VISIBLE);
					else
						img.setVisibility(View.INVISIBLE);
				}				
            
                return true;
            }
		});
    	
    	setListAdapter(tblTagebuchAdapter);
    }
}