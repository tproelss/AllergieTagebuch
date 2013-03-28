package de.thoca.allergieTagebuch;

import java.util.ArrayList;

import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TagsHelper;
import de.thoca.allergieTagebuch.data.TblT2T;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

public class StatistikTagAuswahl extends Activity
{
	public static final String JAHR = "Jahr";
	public static final String MONAT = "Monat";
	public static final String ZEITRAUM = "ZEITRAUM";
	public static final String ZEITRAUM_MONAT = "Zeitraum_Monat";
	public static final String ZEITRAUM_JAHR = "Zeitraum_Jahr";
	
	private TableLayout _tableLayout;
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;
	
	private int _existingTagsStartIndex = 510;
	
	private int _jahr;
	private int _monat;
	private String _zeitraum;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistik_tag_auswahl);           
        
        _tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);
        
        Bundle extras = getIntent().getExtras();
        _jahr = extras.getInt(JAHR);
        _zeitraum = extras.getString(ZEITRAUM);
        if (_zeitraum.equals(ZEITRAUM_MONAT))
        	_monat = extras.getInt(MONAT);
        
        addTagsWithCheckbox();       
        addOkButton();
    }
    
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();       
    	
    	if (_tagebuchDbOpenHelper != null)
    		_tagebuchDbOpenHelper.close();
    }
	
    private void addTagsWithCheckbox()
    {
    	_tableLayout = (TableLayout)findViewById(R.id.tableLayoutTagAuswahl);
    	TagsHelper.Initialize(this);
    	    	
    	int i=0;
    	
    	for (String tag : TagsHelper.GetAllEigeneTags())
    	{
    		TableRow row = new TableRow(this);        
	        
	    	CheckBox cb = new CheckBox(this);
	        cb.setId(_existingTagsStartIndex + i*2);	        
	        cb.setChecked(false);
	        
	        TextView tv = new TextView(this);
	        tv.setText(tag);
	        tv.setId(_existingTagsStartIndex + i*2 + 1);
	        	        	        
	        row.addView(cb, 0);
	        row.addView(tv, 1);
	        
	        row.setGravity(Gravity.CENTER);
	        
	        _tableLayout.addView(row, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
	        i++;
    	}    	
    }
    
    private void addOkButton()
	{
    	TableRow row = new TableRow(this);        
        
    	Button btOk = new Button(this);
    	btOk.setText(R.string.ok);
    	
    	RelativeLayout relativLayout = new RelativeLayout(this);    	
    	
    	TableRow.LayoutParams lpLLayout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	lpLLayout.span = 2;
    	relativLayout.setLayoutParams(lpLLayout);    	    
    	relativLayout.addView( btOk );
    	
    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)btOk.getLayoutParams();
    	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    	
    	btOk.setLayoutParams(params); //causes layout update
    	
        row.addView(relativLayout, 0);           
        _tableLayout.addView(row, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );

        btOk.setClickable(true);
        btOk.setBackgroundResource(R.drawable.button);
        btOk.setHeight((int) (getResources().getDisplayMetrics().density * 35));
        
        btOk.setOnClickListener(new View.OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				runStatistik();
			}
		});	
	}
    
    private void runStatistik()
    {
    	// welche Stichworte wurden gewählt?
    	
    	ArrayList<Integer> selectedTagIds = new ArrayList<Integer>();
    	
    	for (int i=0; i< TagsHelper.GetAllEigeneTags().length; i++)
    	{
    		CheckBox cb = (CheckBox)findViewById(_existingTagsStartIndex + i*2);
    		TextView tv = (TextView)findViewById(_existingTagsStartIndex + i*2 + 1);
			String tagText = tv.getText().toString();
									
    		if (cb.isChecked())    		
    			selectedTagIds.add(TagsHelper.GetEigenenTagIdForName(tagText));    			    		    		    		    	
    	}
    	    	
    	if (_zeitraum.equals(ZEITRAUM_MONAT))
    	{
    		Intent intent = new Intent(StatistikTagAuswahl.this, StatistikEigeneTagsMonat.class);
    		intent.putExtra(StatistikEigeneTagsMonat.JAHR, _jahr);
    		intent.putExtra(StatistikEigeneTagsMonat.MONAT, _monat);
    		intent.putExtra(StatistikEigeneTagsMonat.TAG_AUSWAHL, selectedTagIds);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    		startActivity(intent);
    	}
    	else    		
    	{
    		Intent intent = new Intent(StatistikTagAuswahl.this, StatistikEigeneTagsJahr.class);
    		intent.putExtra(StatistikEigeneTagsJahr.JAHR, _jahr);    		
    		intent.putExtra(StatistikEigeneTagsJahr.TAG_AUSWAHL, selectedTagIds);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    		startActivity(intent);		
    	}        
    }    
}
