package org.metacsp.examples.meta;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

public class TestTrajectoryEnvelopeLoadingFromFile {
	
	public static void main(String[] args) {		
		ConstraintNetwork con = ConstraintNetwork.loadConstraintNetwork("savedConstraintNetworks/example.cn");
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("Trajectory Envelopes loaded from file");
		tea.addTrajectoryEnvelopes(con);
	}
	
}
