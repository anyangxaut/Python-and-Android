package com.example.pythontest;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.common.MyMarkerView;
import com.example.common.MyValueFormatter;
import com.example.datatype.SimDataType;
import com.example.socketclient.SocketClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;

public class TemperatureActivity extends ActionBarActivity implements OnChartValueSelectedListener{

	private LineChart mChart;
    private ArrayList<String> m_xValues;
    private ArrayList<Entry> m_yValues;
	private SocketClient m_client = null;
	private ProgressDialog progressDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temperature);
		
		// Chart==============================================================>>
        mChart = (LineChart)findViewById(R.id.chart_temperature);
        mChart.setOnChartValueSelectedListener(this);
        
        mChart.setDrawGridBackground(false);
        mChart.setDescription("");

        // mChart.setStartAtZero(true);

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        
        MyMarkerView mv = new MyMarkerView(this, R.layout.customer_marker_view, 1);

        // set the marker to the chart
        mChart.setMarkerView(mv);
        
        mChart.getAxisRight().setEnabled(false);
        
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new MyValueFormatter());
        //leftAxis.setAxisMaxValue(1000);
        leftAxis.setStartAtZero(false);
        leftAxis.setAxisMinValue(0);

        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_CENTER);
		
        progressDialog = ProgressDialog.show(this, "Please wait", "loading...");
        new Thread(getDataRun).start();
        //onSetChartData();
		// <<===============================================================Chart
        
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.temperature, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub
		
	}
	
	Runnable getDataRun = new Runnable() {
		
		@Override
		public void run() {
			getTemperatureData();
			onSetChartData();
			progressDialog.dismiss();
		}
	};
	
	
	private void getTemperatureData() {
		if(m_xValues != null) {
			m_xValues.clear();
		} else {
			m_xValues = new ArrayList<String>();
		}
		
		if(m_yValues != null) {
			m_yValues.clear();
		} else {
			m_yValues = new ArrayList<Entry>();
		}
		List<SimDataType> temperLists = new ArrayList<SimDataType>();
		
		if(m_client == null)
			m_client = new SocketClient();

		try {
			temperLists = m_client.GetTemperature();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!temperLists.isEmpty()) {
			int i = 0;
			for(SimDataType temper : temperLists) {
				m_xValues.add(timeStamp2String(temper.getDatetime()));	
				Log.d("DateTime---->", timeStamp2String(temper.getDatetime()));
				m_yValues.add(new Entry(temper.getValue(), i++));
				Log.d("Value---->", String.valueOf(temper.getValue()));
			}
		}
		return;
	}
	
	
	private int[] mColors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0], 
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };
	
	public void onSetChartData() {
		//getTemperatureData();
		
		//mChart.resetTracking();
		ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
		
		LineDataSet d = new LineDataSet(m_yValues, "体温");
		d.setValueFormatter(new MyValueFormatter());
        d.setLineWidth(2f);
        d.setCircleSize(3f);

        int color = mColors[0];
        d.setColor(color);
        d.setCircleColor(color);
        dataSets.add(d);
 
        // make the first DataSet dashed
        dataSets.get(0).enableDashedLine(10, 10, 0);
        dataSets.get(0).setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSets.get(0).setCircleColors(ColorTemplate.VORDIPLOM_COLORS);

        //LineData data = new LineData(xVals, dataSets);
        LineData data = new LineData(m_xValues, dataSets);
        //data.setValueFormatter(new MyValueFormatter());
        
        if(mChart == null) {
        	Log.e("mChart is null--------------->", "NULL");
        	return;
        }
        mChart.setData(data);
        mChart.postInvalidate();
		
	}

	@SuppressLint("SimpleDateFormat")
	private String timeStamp2String(Timestamp timestamp) {
		String date = "";
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			date = df.format(timestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return date;
	}
	
}
