package de.thoca.allergieTagebuch;

import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TblTagebuch;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class NotizenBrowser extends ListActivity
{
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;	
	private Cursor _uebersichtCursor;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notizen_browser);        
        _tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);
                
        listeAnzeigen();
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
                   
    private void listeAnzeigen() 
    {    	
    	_uebersichtCursor = 
    		_tagebuchDbOpenHelper.getReadableDatabase().
    		rawQuery(TblTagebuch.SELECT_NOTIZEN, null);
    	startManagingCursor(_uebersichtCursor);
    	
    	int[] viewIds = new int[] 
    	{
			R.id.lblTag, R.id.lblDatum, R.id.lblNotiz
		};
    	
    	String[] tableColumns = new String[] 
        {
    			TblTagebuch.COL_WOCHENTAG, TblTagebuch.COL_DATUM, TblTagebuch.COL_KOMMENTAR
        };
    	
    	SimpleCursorAdapter tblTagebuchAdapter = 
			new SimpleCursorAdapter(
					this, R.layout.notizen_row,
					_uebersichtCursor,
					tableColumns,
					viewIds);
    	
    	tblTagebuchAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() 
    	{    		
			@Override
            public boolean setViewValue(View view, Cursor theCursor, int column) 
            {										
				if (column == 1) // tag 
				{ 
					TextView txt = (TextView)view;						
					txt.setText(Wochentag.getTextForNumber(getApplicationContext(), theCursor.getInt(1)));											
				} 
				else if (column == 2) // datum 
				{ 
					TextView txt = (TextView)view;
					txt.setText(theCursor.getString(2));
				} 
				else if (column == 3) // kommentar 
				{  
					TextView kommentarLbl = (TextView)view;
					String val = theCursor.getString(3);
					kommentarLbl.setText(val);																	
				}		
            
                return true;
            }
		});
    	
    	setListAdapter(tblTagebuchAdapter);
    }
}
