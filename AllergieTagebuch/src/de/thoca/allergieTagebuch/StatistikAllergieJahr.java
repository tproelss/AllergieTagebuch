package de.thoca.allergieTagebuch;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YLayoutStyle;

import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import de.thoca.allergieTagebuch.data.TblTagebuch;

public class StatistikAllergieJahr extends Activity implements OnTouchListener
{
	public static final String JAHR = "jahr";

	private XYPlot statistikenPlot;
	private TagebuchDbOpenHelper _tagebuchDbOpenHelper;

	private int _jahr;
	private double _maxRangeValue = -1;
	private List<Double> _werte;
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
		Cursor cursor = db.rawQuery(TblTagebuch.SELECT_ALLERGIE_JAHR, null);

		Map<String, Double> alleMonate = new HashMap<String, Double>();

		while (cursor.moveToNext())
		{
			String monat = cursor.getString(0); // 2011-09
			double allergie = cursor.getDouble(1);
			alleMonate.put(monat, allergie);
			if (allergie > _maxRangeValue)
				_maxRangeValue = allergie;
		}

		if (_maxRangeValue < 1.5)
			_maxRangeValue = 1.5;

		cursor.close();
		db.close();

		// daten für aktuelles jahr beschaffen

		_monate = new ArrayList<Integer>();
		for (int i = 1; i <= 13; i++)
			_monate.add(i);

		_werte = new ArrayList<Double>();

		for (int i = 1; i <= 12; i++)
		{
			String lookingFor = String.format("%04d-%02d", _jahr, i);
			if (alleMonate.containsKey(lookingFor))
				_werte.add(alleMonate.get(lookingFor));
			else
				_werte.add((double) 0);
		}
		_werte.add((double) 0); // dummy Monat "13"
	}

	@SuppressWarnings("serial")
	private void statistikAufbereiten()
	{
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
		statistikenPlot.setRangeUpperBoundary(_maxRangeValue * 1.1,
				BoundaryMode.FIXED);
		statistikenPlot.setRangeValueFormat(new DecimalFormat("0"));
		statistikenPlot
				.setRangeLabel(getString(R.string.statistik_beschwerden));

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

		statistikenPlot.setRangeValueFormat(new Format()
		{
			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo,
					FieldPosition pos)
			{
				Number num = (Number) obj;
				switch (num.intValue())
				{
				case 0:
					toAppendTo
							.append(getString(R.string.statistik_allerg_keine));
					break;
				case 1:
					toAppendTo
							.append(getString(R.string.statistik_allerg_leicht));
					break;
				case 2:
					toAppendTo
							.append(getString(R.string.statistik_allerg_mittel));
					break;
				case 3:
					toAppendTo
							.append(getString(R.string.statistik_allerg_schwehr));
					break;
				case 4:
					toAppendTo
							.append(getString(R.string.statistik_allerg_sehr_schwehr));
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
				Intent intent = new Intent(StatistikAllergieJahr.this,
						StatistikAllergieJahr.class);
				intent.putExtra(StatistikAllergieJahr.JAHR, neuesJahr);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		}

		return true;
	}

}
