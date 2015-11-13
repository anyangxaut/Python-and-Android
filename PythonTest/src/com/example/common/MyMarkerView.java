package com.example.common;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Utils;
import com.example.pythontest.R;

/**
 * Custom implementation of the MarkerView.
 * 
 * @author Philipp Jahoda
 */
public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    /**
     * m_Flag 标记不同的数据类型
     * 0 表示心血数据 
     * 1 表示体温数据
     * 2 表示卡路里数据
     */
    private int m_Flag=3;
    //private ArrayList<String> m_xValues;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
    }
    
    /**
     * 重载MyMarkerView构造函数
     * @param context 
     * @param layoutResource
     * @param flag  0:心血数据        1:体温数据       2:卡路里数据
     */
    public MyMarkerView(Context context, int layoutResource, int flag){
    	super(context, layoutResource);
    	m_Flag = flag;
    	//m_xValues = new ArrayList<String>();
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, int dataSetIndex) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
        } else {

            //tvContent.setText("" + Utils.formatNumber(e.getVal(), 0, true));
            //tvContent.setText("" + e.getVal());
            String markerVal = "";
            float value = e.getVal();
            
            switch(m_Flag){
            	case 0:
            		switch (dataSetIndex) {
					case 0:
						markerVal = "心率：|" + (int)(value) + "次/分";
						break;
					case 1:
						markerVal = "收缩压：|" + (int)(value) + "mmHg";
						break;
					case 2:
						markerVal = "舒张压：|" + (int)(value) + "mmHg";
						break;

					default:
						break;
					}
            		break;
            	case 1:
            		markerVal = "体温：|" + value + "℃";
            		break;
            	case 2:
            		markerVal = "卡路里：|" + value + "J";
            		break;
            	default:
            		break;
            }
            tvContent.setText(markerVal.replace("|", "\n"));
        }
    }

    @Override
    public int getXOffset() {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset() {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }
}
