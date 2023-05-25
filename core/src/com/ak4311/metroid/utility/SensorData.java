package com.ak4311.metroid.utility;

public class SensorData {
	public enum Type { WALL, HEAD, FOOT };
	
	public Type type = Type.WALL;
	public boolean inContact = false;
	
	public SensorData() {
		
	}
	
	public SensorData(Type type) {
		this.type = type;
	}
}
