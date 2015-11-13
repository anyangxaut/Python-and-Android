package com.example.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.datatype.SimDataType;

public class ParseSimDataType {
	
	public static List<SimDataType> JsonData2List(String jsondata) {
		List<SimDataType> resultList = new ArrayList<SimDataType>();
		
		try {
			JSONArray simlist = new JSONArray(jsondata);
			for (int i = 0; i < simlist.length(); i++) {
				JSONObject obj = (JSONObject)simlist.get(i);
				
				SimDataType sdt = new SimDataType();
				sdt.setId(obj.getInt("id"));
				sdt.setSensorID(obj.getInt("sensorID"));
				sdt.setValue((float)obj.getDouble("value"));
				sdt.setDatetime(Timestamp.valueOf(obj.getString("datetime")));
				resultList.add(sdt);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultList;
	}
}
