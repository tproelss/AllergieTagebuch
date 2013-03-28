package de.thoca.allergieTagebuch;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
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

public class StatistikEigeneTagsMonat extends Activity implements OnTouchListener
{
	public static final String JAHR = "jahr";
	public static final String MONAT = "monat";
	public static final String TAG_AUSWAHL = "tag_auswahl";
	
	private XYPlot statistikenPlot;
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;

	private int _jahr;
	private int _monat;
	private ArrayList<Integer> _tagIds;
	
	private List<Integer> _werte;
	private List<Integer> _tage;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistiken);
		statistikenPlot = (XYPlot) findViewById(R.id.statistikenPlot);
		_tagebuchDbOpenHelper = new TagebuchDbOpenHelper(this);

		Bundle extras = getIntent().getExtras();
		_jahr = extras.getInt(JAHR);
		_monat = extras.getInt(MONAT);
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
		// alle Werte des Monats aus db laden

		SQLiteDatabase db = _tagebuchDbOpenHelper.getReadableDatabase();		

		StringBuilder tagIds = new StringBuilder();
		for (int i=0; i<_tagIds.size(); i++)
		{
			tagIds.append(_tagIds.get(i));
			if (i < _tagIds.size()-1)
				tagIds.append(",");
		}
		
		String query = String.format(TblTagebuch.SELECT_EIGENE_TAGS_COUNT_BY_DATUM_LIKE, tagIds.toString());
		
		String datumLike = String.format("%04d-%02d", _jahr, _monat) + "%";
		Cursor cursor = db.rawQuery(query,
				new String[] { datumLike });

		Map<String, Integer> alleWerte = new HashMap<String, Integer>();

		while (cursor.moveToNext())
		{
			String tag = cursor.getString(0); // 2011-09-01
			int stichwortCount = cursor.getInt(1);
			alleWerte.put(tag, stichwortCount);
		}

		cursor.close();
		db.close();

		
		// daten für aktuellen monat beschaffen

		_tage = new ArrayList<Integer>();
		_werte = new ArrayList<Integer>();

		Calendar cal = GregorianCalendar.getInstance();
		cal.set(_jahr, _monat - 1, 1);
		int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

		for (int i = 1; i <= daysInMonth; i++)
		{
			String currentDate = String.format("%04d-%02d-%02d", _jahr, _monat,
					i);

			_tage.add(i);

			if (alleWerte.containsKey(currentDate))
				_werte.add(alleWerte.get(currentDate));
			else
				_werte.add(0);
		}
		
		_tage.add(daysInMonth + 1); // dummy-Eintrag, damit die letzte Säule sichtbar ist
		_werte.add(0);
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
		
		SimpleXYSeries series1 = new SimpleXYSeries(_tage, _werte, "");

		Paint lineFill = new Paint();
		lineFill.setAlpha(200);
		lineFill.setColor(Color.BLUE);

		StepFormatter stepFormatter = new StepFormatter(Color.rgb(0, 0, 0),
				Color.BLUE);
		stepFormatter.getLinePaint().setStrokeWidth(1);

		stepFormatter.getLinePaint().setAntiAlias(false);
		stepFormatter.setFillPaint(lineFill);

		/*statistikenPlot.addSeries(
				series1,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(0, 200, 200), Color.rgb(0,
						200, 0), Color.rgb(0, 0, 200)));*/

		statistikenPlot.addSeries(series1, stepFormatter);

		String[] monateRes = getResources().getStringArray(R.array.statistik_monate);
		
		statistikenPlot.setTitle(String.format("%s %d", monateRes[_monat-1], _jahr));

		statistikenPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
		statistikenPlot.setRangeUpperBoundary(maxRangeValue + 1, BoundaryMode.FIXED);
		statistikenPlot.setRangeValueFormat(new DecimalFormat("0"));
		statistikenPlot
				.setRangeLabel(buildRangeLabel());

		statistikenPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);		
		statistikenPlot.setDomainValueFormat(new DecimalFormat("0"));
		statistikenPlot.setDomainLabel(getString(R.string.statistik_tag));
		statistikenPlot.position(statistikenPlot.getDomainLabelWidget(), 0, XLayoutStyle.RELATIVE_TO_CENTER, 15, YLayoutStyle.ABSOLUTE_FROM_BOTTOM);

		statistikenPlot.getLegendWidget().setVisible(false);
		statistikenPlot.disableAllMarkup();

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
						toAppendTo.append("1");
						break;				
					case 5:
						toAppendTo.append("5");
						break;
					case 10:
						toAppendTo.append("10");
						break;
					case 15:
						toAppendTo.append("15");
						break;
					case 20:
						toAppendTo.append("20");
						break;
					case 25:
						toAppendTo.append("25");
						break;
					case 30:
						toAppendTo.append("30");
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

	//@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
			xWhenDown = event.getX();

		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
		{
			float currentX = event.getX();
			if (currentX > xWhenDown && currentX - xWhenDown > 20)
				runActivityPreviousMonth();
			else if (currentX < xWhenDown && xWhenDown - currentX > 20)
				runActivityNextMonth();			
		}

		return true;
	}
	
	private void runActivityNextMonth()
	{							
		int neuesMonat = _monat + 1;
		int neuesJahr =  _jahr;
		
		if (neuesMonat == 13)
		{
			neuesMonat = 1;
			neuesJahr++;
		}
		
		neueStatistik(neuesJahr, neuesMonat);
	}
	
	private void runActivityPreviousMonth()
	{
		int neuesMonat = _monat - 1;
		int neuesJahr =  _jahr;
		
		if (neuesMonat == 0)
		{
			neuesMonat = 12;
			neuesJahr--;
		}
		
		neueStatistik(neuesJahr, neuesMonat);		
	}
	
	private void neueStatistik(int jahr, int monat)
	{
		Intent intent = new Intent(StatistikEigeneTagsMonat.this, StatistikEigeneTagsMonat.class);
		intent.putExtra(StatistikEigeneTagsMonat.JAHR, jahr);
		intent.putExtra(StatistikEigeneTagsMonat.MONAT, monat);
		intent.putExtra(StatistikEigeneTagsMonat.TAG_AUSWAHL, _tagIds);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);			
	}

}
