package com.example.datatype;

import java.sql.Timestamp;

/**
 * 数据格式类
 * @author 114-FEI
 *
 */
public class SimDataType {
	private int id;
	private int sensorID;
	private float value;
	private Timestamp datetime;
	
	public Timestamp getDatetime() {
		return datetime;
	}
	public void setDatetime(Timestamp datetime) {
		this.datetime = datetime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSensorID() {
		return sensorID;
	}
	public void setSensorID(int sensorID) {
		this.sensorID = sensorID;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}

	
}
