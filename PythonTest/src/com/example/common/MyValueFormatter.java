package com.example.common;

import java.text.DecimalFormat;

import com.github.mikephil.charting.utils.ValueFormatter;

public class MyValueFormatter implements ValueFormatter {
	
	private DecimalFormat mFormat;
	
	public MyValueFormatter() {
        mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
    }

	@Override
	public String getFormattedValue(float value) {
		// TODO Auto-generated method stub
		return mFormat.format(value);
	}

}
