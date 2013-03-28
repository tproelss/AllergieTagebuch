package de.thoca.allergieTagebuch;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.thoca.allergieTagebuch.data.Tag;
import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TagsHelper;
import de.thoca.allergieTagebuch.data.TblT2T;

public abstract class TagAuswahlBase extends Activity
{
	public static final String DATENSATZ_ID = "id";
	public static final String TAGS_DIRTY = "tags_dirty";	
	
	private long _id = -1;
		
	private TableLayout _tableLayout;
	
	private int _newTagsStartIndex = 500;
	private int _existingTagsStartIndex = 510;	
	private ArrayList<Tag> _initialSelectedTagsList;	
		
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_auswahl);           
        
        _tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);
        
        Bundle extras = getIntent().getExtras();
        _id = extras.getLong(DATENSATZ_ID, -1);
        
        reloadUI();               
    }      
    
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();       
    	
    	if (_tagebuchDbOpenHelper != null)
    		_tagebuchDbOpenHelper.close();
    }
    
    private void reloadUI()
    {
    	initInitialTagsList();
        
        _tableLayout = (TableLayout)findViewById(R.id.tableLayoutTagAuswahl);
        _tableLayout.removeAllViewsInLayout();
        
        TagsHelper.Initialize(this);
        
        addBestehendeRows();
        addTheThreeNewRows();
        addOkButton();
    }
    
    private void addOkButton()
	{
    	TableRow row = new TableRow(this);        
        
    	Button btOk = new Button(this);
    	btOk.setText(R.string.ok);
    	
    	RelativeLayout relativLayout = new RelativeLayout(this);    	
    	
    	TableRow.LayoutParams lpLLayout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	lpLLayout.span = 3;
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
				setResultAndFinish();
			}
		});
		
	}

	private void initInitialTagsList()
    {    	    
    	_initialSelectedTagsList = GetTagsForDay(_id);    	    	   
    }
    protected abstract ArrayList<Tag> GetTagsForDay(long dayId);
    
    private int isInInitialTagsList(String s)
    {
    	for (Tag t:_initialSelectedTagsList)
    	{    		     		
    		if (s.equalsIgnoreCase(t.tag))
    			return t.id;
    	}
    	
    	return -1;
    }
    
    private void addBestehendeRows()
    {
    	int i=0;
        	    	
    	for (String tag : GetAllTags())
    	{
    		TableRow row = new TableRow(this);        
	        
	    	CheckBox cb = new CheckBox(this);
	        cb.setId(_existingTagsStartIndex + i*3);	        
	        cb.setChecked(isInInitialTagsList(tag) != -1);
	        
	        TextView tv = new TextView(this);
	        tv.setText(tag);
	        tv.setId(_existingTagsStartIndex + i*3 + 1);
	        
	        ImageView iv = new ImageView(this);
	        iv.setImageResource(R.drawable.loeschen);
	        iv.setId(_existingTagsStartIndex + i*3 + 2);	      
	        iv.setClickable(true);
	        iv.setOnClickListener( new View.OnClickListener()
			{				
				@Override
				public void onClick(View v)
				{
					deleteTagClicked(v);					
				}
			});
	        
	        row.addView(cb, 0);
	        row.addView(tv, 1);
	        row.addView(iv, 2);
	        row.setGravity(Gravity.CENTER);
	        
	        _tableLayout.addView(row, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
	        i++;
    	}    	
    }
    
    protected abstract String[] GetAllTags();
    
    private void deleteTagClicked(View v)
    {
    	AlertDialog ad = new AlertDialog.Builder(this).create();
    	ad.setCancelable(true);
    	
    	String tagName = 
			(String)((TextView)findViewById(v.getId() - 1))
			.getText();
    	
    	ad.setMessage(String.format(getString(R.string.confirmTagDelete), tagName));
    	ad.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ja), this.onDeleteSicherheitsabfrageClicked);
    	ad.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.nein), this.onDeleteSicherheitsabfrageClicked);
    	
    	_deleteRequestedByView = v;
    	ad.show();    	    
    }
    
    DialogInterface.OnClickListener onDeleteSicherheitsabfrageClicked = new DialogInterface.OnClickListener()
	{	
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			switch (which)
			{
				case DialogInterface.BUTTON_POSITIVE:
					String tagName = 
							(String)((TextView)findViewById(_deleteRequestedByView.getId() - 1))
							.getText();    	
			    	DeleteTag(tagName);			    				    	
					dialog.dismiss();
					reloadUI();
					break;
				default:					
					dialog.dismiss();
					break;
			}			
		}
	}; 
	private View _deleteRequestedByView = null;
	
	protected abstract void DeleteTag(String tagName);
    
    private void addTheThreeNewRows()
    {    	    
    	for (int i=0; i<3; i++)
    	{
	    	TableRow row = new TableRow(this);        
	        
	    	CheckBox cb = new CheckBox(this);
	        cb.setId(_newTagsStartIndex + i*2);
	        
	        EditText et = new EditText(this);	        	       
	        et.setId(_newTagsStartIndex + i*2 + 1);
	        et.setOnKeyListener(new View.OnKeyListener()
			{				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					if (keyCode != KeyEvent.KEYCODE_BACK) // eigentliche Bedingung: wenn text geändert wurde
					{
						CheckBox foundCb = (CheckBox)findViewById(v.getId()-1);
						EditText foundEt = (EditText)v;
						foundCb.setChecked(foundEt.getText().length() > 0);
					}
					return false;
				}
			});
	        
	        row.addView(cb, 0);
	        row.addView(et, 1);
        
	        _tableLayout.addView(row, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
    	}        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {            
        	setResultAndFinish();
        }
        return super.onKeyDown(keyCode, event);
    }
            	    
    private void setResultAndFinish()
    {       	
    	boolean dirty = false;
    	    	
    	// check existing tags    	
    	for (int i=0; i< GetAllTags().length; i++)
    	{
    		CheckBox cb = (CheckBox)findViewById(_existingTagsStartIndex + i*3);
    		TextView tv = (TextView)findViewById(_existingTagsStartIndex + i*3 + 1);
			String tagText = tv.getText().toString();
			int idFromInitialTagList = isInInitialTagsList(tagText);
			
    		if (cb.isChecked() && idFromInitialTagList == -1)
    		{
    			 // tag neu ausgewählt    
    			int tagId = GetTagIdForName(tagText);
    			assignTagToDay(tagId);
    			dirty = true;
    		}    		
    		
    		if (!cb.isChecked() && idFromInitialTagList != -1)
    		{
    			// tag abgewählt    			
    			SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
    			db.delete("tblT2T", 
    						TblT2T.COL_TAGEBUCH_ID + "=? " +
    						"AND " + TblT2T.COL_TAG_ID + "=?"
    						, new String[] { String.format("%d", _id), 
    										String.format("%d", idFromInitialTagList) });
    			db.close();
    			dirty = true;
    		}
    	}
    	
    	// check new tags
    	for (int i=0; i<3; i++)
    	{	    	     	       
	    	CheckBox cb = (CheckBox)findViewById(_newTagsStartIndex + i*2);
	        if (cb.isChecked())
	        {	        		        
	        	EditText et = (EditText)findViewById(_newTagsStartIndex + i*2 + 1);
	        	String tagText = et.getText().toString();
	        	if (tagText.trim().length() == 0)
	        		continue;
	        	
	        	if (newTagIsDuplicate(tagText))
	        	{
	        		Toast.makeText(this, String.format(getString(R.string.tag_doppelt_erfasst), tagText), 6000).show();
	        		continue;
	        	}
	        	
	        	int tagId = GetTagIdForName(tagText);
	        	if (tagId != -1)
	        	{
	        		// tag gab's schon -> einfach zuweisen
	        		assignTagToDay(tagId);
	        		dirty = true;
	        	}
	        	else
	        	{
	        		// neuen tag anlegen
	        		Tag newTag = AddNewTagToDB(tagText);
	        		assignTagToDay(newTag.id);
	        		dirty = true;
	        	}
	        }	        	       
    	}   
    	    	
    	// set result
    	final Intent intent = new Intent();
    	if (dirty = true)
    		intent.putExtra(TAGS_DIRTY, "1");
    	else
    		intent.putExtra(TAGS_DIRTY, "0");
    	setResult(Activity.RESULT_OK, intent);
    	finish();
    }
    
    private boolean newTagIsDuplicate(String tagText)
    {
    	tagText = tagText.trim();
    	
    	for (String existingTag : GetAllTags())
    	{
    		if (existingTag.equalsIgnoreCase(tagText))
    			return true;
    	}
    	
    	return false;
    }
    
    protected abstract int GetTagIdForName(String tagName);
    protected abstract Tag AddNewTagToDB(String tagName);
    
    private void assignTagToDay(int tagId)
    {
    	SQLiteDatabase db = _tagebuchDbOpenHelper.getWritableDatabase();
    	
    	ContentValues cv = new ContentValues();
		cv.put(TblT2T.COL_TAGEBUCH_ID, _id);
		cv.put(TblT2T.COL_TAG_ID, tagId);
		cv.put(TblT2T.COL_IS_MED_TAG, IsMedTag());
		
		
		db.insert("tblT2T", null, cv);
		db.close();
    }
    
    protected abstract int IsMedTag();
}
