package org.metacsp.examples.meta;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

public class TestTrajectoryEnvelopeLoadingFromFile {
	
	public static void main(String[] args) {		
		ConstraintNetwork con = ConstraintNetwork.loadConstraintNetwork("savedConstraintNetworks/example.cn");
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("Trajectory Envelopes for " + con.getVariables().length/4 + " drill targets");
		tea.setTrajectoryEnvelopes(con);
	}
	
}
