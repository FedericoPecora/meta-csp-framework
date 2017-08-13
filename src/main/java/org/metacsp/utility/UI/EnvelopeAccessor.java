package org.metacsp.utility.UI;

import aurelienribon.tweenengine.TweenAccessor;

public class EnvelopeAccessor implements TweenAccessor<TweenableEnvelope> {

	@Override
	public int getValues(TweenableEnvelope target, int tweenType, float[] returnValues) {
		returnValues[0] = (float)target.getMinX();
		returnValues[1] = (float)target.getMinY();
		returnValues[2] = (float)target.getMaxX();
		returnValues[3] = (float)target.getMaxY();
		return 4;
	}

	@Override
	public void setValues(TweenableEnvelope target, int tweenType, float[] newValues) {
		target.setMinX(newValues[0]);
		target.setMinY(newValues[1]);
		target.setMaxX(newValues[2]);
		target.setMaxY(newValues[3]);
	}


}
