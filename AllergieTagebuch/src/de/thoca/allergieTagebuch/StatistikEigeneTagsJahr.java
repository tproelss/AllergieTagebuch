package de.thoca.allergieTagebuch;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YLayoutStyle;

import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TagsHelper;
import de.thoca.allergieTagebuch.data.TblTagebuch;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class StatistikEigeneTagsJahr extends Activity implements OnTouchListener
{
	public static final String JAHR = "jahr";
	public static final String TAG_AUSWAHL = "tag_auswahl";

	private XYPlot statistikenPlot;
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;

	private int _jahr;	
	private ArrayList<Integer> _tagIds;
	
	private List<Integer> _werte;
	private List<Integer> _monate;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistiken);
		statistikenPlot = (XYPlot) findViewById(R.id.statistikenPlot);
		_tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);

		Bundle extras = getIntent().getExtras();
		_jahr = extras.getInt(JAHR);
		_tagIds = extras.getIntegerArrayList(TAG_AUSWAHL);
		
		ladeDaten();
		statistikAufbereiten();

		statistikenPlot.setOnTouchListener(this);
	}
	
	 @Override
	 protected void onDestroy() 
	 {
	  	super.onDestroy();	    	    
	    	
	  	if (_tagebuchDbOpenHelper != null)
	    		_tagebuchDbOpenHelper.close();
	 }

	private void ladeDaten()
	{
		SQLiteDatabase db = _tagebuchDbOpenHelper.getReadableDatabase();		

		StringBuilder tagIds = new StringBuilder();
		for (int i=0; i<_tagIds.size(); i++)
		{
			tagIds.append(_tagIds.get(i));
			if (i < _tagIds.size()-1)
				tagIds.append(",");
		}
		
		String query = String.format(TblTagebuch.SELECT_EIGENE_TAGS_COUNT_BY_MONAT_LIKE, tagIds.toString());
		
		String datumLike = String.format("%04d", _jahr) + "%";  // 2011%
		Cursor cursor = db.rawQuery(query,
				new String[] { datumLike });

		Map<String, Integer> alleWerte = new HashMap<String, Integer>();

		while (cursor.moveToNext())
		{
			String monat = cursor.getString(0); // 2011-09
			int stichwortCount = cursor.getInt(1);
			alleWerte.put(monat, stichwortCount);			
		}		

		cursor.close();
		db.close();

		// daten für aktuelles jahr beschaffen

		_monate = new ArrayList<Integer>();
		for (int i = 1; i <= 13; i++)
			_monate.add(i);

		_werte = new ArrayList<Integer>();

		for (int i = 1; i <= 12; i++)
		{
			String lookingFor = String.format("%04d-%02d", _jahr, i);
			if (alleWerte.containsKey(lookingFor))
				_werte.add(alleWerte.get(lookingFor));
			else
				_werte.add((int) 0);
		}
		_werte.add((int) 0); // dummy Monat "13"
	}

	@SuppressWarnings("serial")
	private void statistikAufbereiten()
	{
		Integer maxRangeValue = 0;
		for (Integer y: _werte)
		{
			if (y > maxRangeValue)
				maxRangeValue = y;
		}
		
		SimpleXYSeries series1 = new SimpleXYSeries(_monate, _werte, "");

		Paint lineFill = new Paint();
		lineFill.setAlpha(200);
		lineFill.setColor(Color.BLUE);

		StepFormatter stepFormatter = new StepFormatter(Color.rgb(0, 0, 0),
				Color.BLUE);
		stepFormatter.getLinePaint().setStrokeWidth(1);

		stepFormatter.getLinePaint().setAntiAlias(false);
		stepFormatter.setFillPaint(lineFill);

		statistikenPlot.addSeries(series1, stepFormatter);

		statistikenPlot.setTitle(String.format("%d", _jahr));

		statistikenPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
		statistikenPlot.setRangeUpperBoundary(maxRangeValue + 2,
				BoundaryMode.FIXED);
		statistikenPlot.setRangeValueFormat(new DecimalFormat("0"));
		statistikenPlot.setRangeLabel(buildRangeLabel());

		statistikenPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		statistikenPlot.setDomainLabel(getString(R.string.statistik_monat));
		statistikenPlot.position(statistikenPlot.getDomainLabelWidget(), 0, XLayoutStyle.RELATIVE_TO_CENTER, 15, YLayoutStyle.ABSOLUTE_FROM_BOTTOM);

		statistikenPlot.getLegendWidget().setVisible(false);
		statistikenPlot.disableAllMarkup();

		// create a custom formatter to draw our state names as range tick
		// labels:
		statistikenPlot.setDomainValueFormat(new Format()
		{
			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo,
					FieldPosition pos)
			{
				Number num = (Number) obj;
				switch (num.intValue())
				{
				case 1:
					toAppendTo.append(getString(R.string.statistik_monat_jan));
					break;
				case 2:
					toAppendTo.append(getString(R.string.statistik_monat_feb));
					break;
				case 3:
					toAppendTo.append(getString(R.string.statistik_monat_mrz));
					break;
				case 4:
					toAppendTo.append(getString(R.string.statistik_monat_apr));
					break;
				case 5:
					toAppendTo.append(getString(R.string.statistik_monat_mai));
					break;
				case 6:
					toAppendTo.append(getString(R.string.statistik_monat_jun));
					break;
				case 7:
					toAppendTo.append(getString(R.string.statistik_monat_jul));
					break;
				case 8:
					toAppendTo.append(getString(R.string.statistik_monat_aug));
					break;
				case 9:
					toAppendTo.append(getString(R.string.statistik_monat_sep));
					break;
				case 10:
					toAppendTo.append(getString(R.string.statistik_monat_okt));
					break;
				case 11:
					toAppendTo.append(getString(R.string.statistik_monat_nov));
					break;
				case 12:
					toAppendTo.append(getString(R.string.statistik_monat_dez));
					break;
				}

				return toAppendTo;
			}

			@Override
			public Object parseObject(String arg0, ParsePosition arg1)
			{
				return null;
			}
		});		
	}

	private String buildRangeLabel()
	{
		int maxLabelLength = 30;
		
		StringBuilder rangeLabel = new StringBuilder();
		for (int i=0; i<_tagIds.size(); i++)
		{
			String tagName = TagsHelper.GetEigenenTagNameForId(_tagIds.get(i));
			rangeLabel.append(tagName);
						
			if (i < _tagIds.size()-1)
				rangeLabel.append(", ");
		}	
		
		if (rangeLabel.length() > maxLabelLength)
		{
			rangeLabel.delete(maxLabelLength - 3, rangeLabel.length());
			rangeLabel.append("...");
		}
		
		return rangeLabel.toString();
	}
	
	private float xWhenDown;

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
			xWhenDown = event.getX();

		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
		{
			int neuesJahr = -1;

			float currentX = event.getX();
			if (currentX > xWhenDown && currentX - xWhenDown > 20)
				neuesJahr = _jahr - 1;
			else if (currentX < xWhenDown && xWhenDown - currentX > 20)
				neuesJahr = _jahr + 1;

			if (neuesJahr != -1)
			{
				// neue statistik
				Intent intent = new Intent(StatistikEigeneTagsJahr.this,
						StatistikEigeneTagsJahr.class);
				intent.putExtra(StatistikEigeneTagsJahr.JAHR, neuesJahr);
				intent.putExtra(StatistikEigeneTagsJahr.TAG_AUSWAHL, _tagIds);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		}

		return true;
	}
}
