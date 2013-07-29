/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.onLineMonitoring;

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
