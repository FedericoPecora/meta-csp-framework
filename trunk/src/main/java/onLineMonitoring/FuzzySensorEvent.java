package onLineMonitoring;

import java.util.Arrays;


public class FuzzySensorEvent {
	
	private long time;
	private PhysicalSensor sensor;
	private double[] possibilities;
	
	public FuzzySensorEvent(PhysicalSensor sensor, double[] possibilities, long time) {
		this.setTime(time);
		this.setPossibilities(possibilities);
		this.setSensor(sensor);
	}
 
	public long getTime() {
		return time;
	}

	private void setTime(long time) {
		this.time = time;
	}

	public PhysicalSensor getSensor() {
		return sensor;
	}

	private void setSensor(PhysicalSensor sensor) {
		this.sensor = sensor;
	}

	public double[] getPossibilities() {
		return possibilities;
	}

	private void setPossibilities(double[] possibilities) {
		this.possibilities = possibilities;
	}
	
	public String toString() {
		return "[" + sensor.getName() + "] " + Arrays.toString(sensor.getStates()) + " = " + Arrays.toString(possibilities) + " @ " + time;
	}

}
